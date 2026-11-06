package com.example.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ChileEmergencyData
import com.example.data.model.AlertCategory
import com.example.data.model.AlertItem
import com.example.data.model.AlertSeverity
import com.example.data.model.EarthquakeItem
import com.example.data.model.SosContact
import com.example.data.model.SurvivalCategory
import com.example.data.model.SurvivalItem
import com.example.data.model.TsunamiThreat
import com.example.data.model.WeatherInfo
import com.example.data.repository.EmergencyAlertRepository
import com.example.data.repository.SosContactsRepository
import com.example.data.repository.SurvivalKitRepository
import com.example.data.repository.WeatherRepository
import com.example.util.AudioSirenManager
import com.example.util.BatteryEmergencyManager
import com.example.util.BatteryEmergencyProfile
import com.example.util.DeviceBatteryState
import com.example.util.FlashlightMode
import com.example.util.SirenSoundMode
import com.example.util.TorchManager
import com.example.util.VibrationHelper
import com.example.data.local.EmergencyDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class EmergencyTab(val title: String) {
  ALERTAS("Alertas"),
  MAPA("Mapa"),
  TIEMPO_UV("Tiempo & UV"),
  SISMOS("Sismos"),
  HERRAMIENTAS_SOS("Kit SOS"),
  PREVENCION("Prevención")
}

data class EmergencyUiState(
  val currentTab: EmergencyTab = EmergencyTab.ALERTAS,
  val officialAlerts: List<AlertItem> = emptyList(),
  val earthquakes: List<EarthquakeItem> = emptyList(),
  val weatherInfo: WeatherInfo? = null,
  val isLoading: Boolean = false,
  val isRefreshing: Boolean = false,
  val isLoadingWeather: Boolean = false,
  val errorMessage: String? = null,
  val selectedRegion: String = ChileEmergencyData.DEFAULT_REGION, // Centered in Biobío
  val searchQuery: String = "",
  val activeSaeAlert: AlertItem? = null,
  val isSirenPlaying: Boolean = false,
  val activeSirenMode: SirenSoundMode = SirenSoundMode.SAE_ALARM,
  val isTorchActive: Boolean = false,
  val activeTorchMode: FlashlightMode = FlashlightMode.STEADY,
  // GPS telemetry in real-time
  val userLatitude: Double = ChileEmergencyData.DEFAULT_BIOBIO_LAT,
  val userLongitude: Double = ChileEmergencyData.DEFAULT_BIOBIO_LON,
  val detectedUserRegion: String = "Biobío",
  val detectedUserCommune: String = "Concepción / Biobío",
  val isGpsActive: Boolean = false,
  val gpsAccuracyMeters: Float? = null,
  val lastGpsUpdateFormatted: String? = null,
  val onlyNearbyGps: Boolean = false,
  val minMagnitudeFilter: Double = 0.0,
  // Room local caching state
  val isOfflineMode: Boolean = false,
  val cachedAlertsCount: Int = 0,
  val lastCacheSyncFormatted: String? = null,
  // Battery state and adaptive power management
  val batteryState: DeviceBatteryState = DeviceBatteryState(),
  // Theme state: dark mode for battery saving / night emergencies, light mode for daytime visibility
  val isDarkTheme: Boolean = true
)

class MainEmergencyViewModel(application: Application) : AndroidViewModel(application) {

  private val prefs = application.getSharedPreferences("alerta_chile_prefs", Context.MODE_PRIVATE)
  private val database = EmergencyDatabase.getInstance(application)
  private val repository = EmergencyAlertRepository(
    alertDao = database.alertDao(),
    earthquakeDao = database.earthquakeDao()
  )
  private val survivalRepo = SurvivalKitRepository(application)
  private val sosRepo = SosContactsRepository(application)
  private val weatherRepo = WeatherRepository()
  private val sirenManager = AudioSirenManager()
  private val torchManager = TorchManager(application)
  private val vibrationHelper = VibrationHelper(application)
  private val batteryManager = BatteryEmergencyManager(application)

  private val locationManager = application.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

  private val _uiState = MutableStateFlow(EmergencyUiState())
  val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()

  val survivalItems: StateFlow<List<SurvivalItem>> = survivalRepo.items
  val sosContacts: StateFlow<List<SosContact>> = sosRepo.contacts

  private val locationListener = object : LocationListener {
    override fun onLocationChanged(location: Location) {
      updateRealtimeGpsLocation(location)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
  }

  init {
    val savedDark = prefs.getBoolean("pref_dark_theme", true)
    _uiState.value = _uiState.value.copy(isDarkTheme = savedDark)
    observeBatteryState()
    observeRoomAlerts()
    loadData()
    startRealTimeGpsTracking()
  }

  private fun observeBatteryState() {
    viewModelScope.launch {
      var lastProfile: BatteryEmergencyProfile? = null
      batteryManager.batteryState.collectLatest { bState ->
        val effective = bState.effectiveProfile
        _uiState.value = _uiState.value.copy(batteryState = bState)

        // Automatically switch to dark mode if entering critical battery mode to save screen energy
        if (effective == BatteryEmergencyProfile.AHORRO_CRITICO && !_uiState.value.isDarkTheme) {
          setTheme(true)
        }

        // When the effective power profile changes, re-adjust the GPS update frequency
        if (lastProfile != effective) {
          lastProfile = effective
          startRealTimeGpsTracking()
        }
      }
    }
  }

  private fun observeRoomAlerts() {
    viewModelScope.launch {
      val lat = _uiState.value.userLatitude
      val lon = _uiState.value.userLongitude
      repository.observeAlerts(lat, lon).collectLatest { alerts ->
        if (alerts.isNotEmpty()) {
          val sdf = SimpleDateFormat("HH:mm:ss", Locale("es", "CL"))
          _uiState.value = _uiState.value.copy(
            officialAlerts = alerts,
            cachedAlertsCount = alerts.size,
            lastCacheSyncFormatted = sdf.format(Date())
          )
        }
      }
    }
  }

  fun setTab(tab: EmergencyTab) {
    _uiState.value = _uiState.value.copy(currentTab = tab)
  }

  fun setRegion(region: String) {
    _uiState.value = _uiState.value.copy(selectedRegion = region)
    loadWeatherForCurrentSelection()
  }

  fun setSearchQuery(query: String) {
    _uiState.value = _uiState.value.copy(searchQuery = query)
  }

  fun setMinMagnitudeFilter(mag: Double) {
    _uiState.value = _uiState.value.copy(minMagnitudeFilter = mag)
  }

  fun toggleNearbyGpsFilter() {
    _uiState.value = _uiState.value.copy(onlyNearbyGps = !_uiState.value.onlyNearbyGps)
  }

  fun toggleTheme() {
    val newMode = !_uiState.value.isDarkTheme
    prefs.edit().putBoolean("pref_dark_theme", newMode).apply()
    _uiState.value = _uiState.value.copy(isDarkTheme = newMode)
  }

  fun setTheme(isDark: Boolean) {
    prefs.edit().putBoolean("pref_dark_theme", isDark).apply()
    _uiState.value = _uiState.value.copy(isDarkTheme = isDark)
  }

  fun toggleOfflineMode() {
    val newMode = !_uiState.value.isOfflineMode
    _uiState.value = _uiState.value.copy(isOfflineMode = newMode)
    loadData()
  }

  fun resetAlertsCache() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isRefreshing = true)
      repository.resetAlertsCache()
      loadData()
    }
  }

  fun refresh() {
    _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)
    loadData()
  }

  private fun loadData() {
    viewModelScope.launch {
      val lat = _uiState.value.userLatitude
      val lon = _uiState.value.userLongitude
      val isOffline = _uiState.value.isOfflineMode

      val alerts = repository.getOfficialAlerts(lat, lon)
      val eqResult = repository.fetchEarthquakes(lat, lon, forceOfflineMode = isOffline)
      val eqList = eqResult.getOrDefault(emptyList())

      val sdf = SimpleDateFormat("HH:mm:ss", Locale("es", "CL"))
      _uiState.value = _uiState.value.copy(
        officialAlerts = alerts,
        earthquakes = eqList,
        cachedAlertsCount = alerts.size,
        lastCacheSyncFormatted = sdf.format(Date()),
        isLoading = false,
        isRefreshing = false
      )
      if (!isOffline) {
        loadWeatherForCurrentSelection()
      } else {
        _uiState.value = _uiState.value.copy(isLoadingWeather = false)
      }
    }
  }

  fun loadWeatherForCurrentSelection() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoadingWeather = true)
      val region = _uiState.value.selectedRegion
      val userLat = _uiState.value.userLatitude
      val userLon = _uiState.value.userLongitude

      val result = weatherRepo.fetchWeatherForRegion(
        regionName = region,
        userLat = userLat,
        userLon = userLon
      )
      result.onSuccess { info ->
        _uiState.value = _uiState.value.copy(
          weatherInfo = info,
          isLoadingWeather = false
        )
      }.onFailure {
        _uiState.value = _uiState.value.copy(isLoadingWeather = false)
      }
    }
  }

  @SuppressLint("MissingPermission")
  fun startRealTimeGpsTracking() {
    try {
      try { locationManager?.removeUpdates(locationListener) } catch (_: Exception) {}

      val providers = locationManager?.getProviders(true) ?: emptyList()
      var bestLocation: Location? = null

      for (provider in providers) {
        val l = try { locationManager?.getLastKnownLocation(provider) } catch (_: SecurityException) { null }
        if (l != null && (bestLocation == null || l.accuracy < bestLocation.accuracy)) {
          bestLocation = l
        }
      }

      bestLocation?.let { updateRealtimeGpsLocation(it) }

      val profile = _uiState.value.batteryState.effectiveProfile
      val interval = profile.gpsIntervalMillis
      val minDistance = profile.gpsMinDistanceMeters

      // Register live location listener with battery-adaptive interval
      if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
        locationManager.requestLocationUpdates(
          LocationManager.GPS_PROVIDER,
          interval,
          minDistance,
          locationListener
        )
      }
      if (locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true) {
        locationManager.requestLocationUpdates(
          LocationManager.NETWORK_PROVIDER,
          interval,
          minDistance,
          locationListener
        )
      }
    } catch (_: Exception) {}
  }

  @SuppressLint("MissingPermission")
  fun manualRefreshGpsLocation() {
    try {
      val providers = locationManager?.getProviders(true) ?: emptyList()
      var bestLocation: Location? = null
      for (provider in providers) {
        val l = try { locationManager?.getLastKnownLocation(provider) } catch (_: SecurityException) { null }
        if (l != null && (bestLocation == null || l.accuracy < bestLocation.accuracy)) {
          bestLocation = l
        }
      }
      bestLocation?.let { updateRealtimeGpsLocation(it) }
    } catch (_: Exception) {}
  }

  fun setAutoBatteryAdaptive(enabled: Boolean) {
    batteryManager.setAutoAdaptive(enabled)
  }

  fun setManualBatteryProfile(profile: BatteryEmergencyProfile?) {
    batteryManager.setManualProfile(profile)
  }

  fun setSimulatedBattery(levelPercent: Int, isCharging: Boolean = false) {
    batteryManager.setSimulatedBattery(levelPercent, isCharging)
  }

  private fun updateRealtimeGpsLocation(loc: Location) {
    val lat = loc.latitude
    val lon = loc.longitude
    val region = ChileEmergencyData.detectRegionFromCoordinates(lat, lon)
    val commune = if (region == "Biobío") {
      ChileEmergencyData.detectBiobioCommune(lat, lon)
    } else {
      "$region (Sector Local)"
    }

    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(loc.time))

    // Recompute distances for existing earthquakes and alerts
    val updatedEqs = repository.applyDistancesToEarthquakes(_uiState.value.earthquakes, lat, lon)
    val updatedAlerts = _uiState.value.officialAlerts.map { alert ->
      if (alert.latitude != null && alert.longitude != null) {
        val dist = ChileEmergencyData.calculateDistanceKm(lat, lon, alert.latitude, alert.longitude)
        alert.copy(distanceKmFromUser = dist)
      } else {
        alert
      }
    }

    _uiState.value = _uiState.value.copy(
      userLatitude = lat,
      userLongitude = lon,
      detectedUserRegion = region,
      detectedUserCommune = commune,
      isGpsActive = true,
      gpsAccuracyMeters = loc.accuracy,
      lastGpsUpdateFormatted = formattedTime,
      earthquakes = updatedEqs,
      officialAlerts = updatedAlerts
    )

    // Automatically refresh weather for new GPS coordinate
    loadWeatherForCurrentSelection()
  }

  fun triggerSaeSimulation(
    title: String = "ALERTA SAE: EVACUACIÓN INMEDIATA POR INCENDIO FORESTAL EN EL BIOBÍO",
    region: String = "Biobío"
  ) {
    val saeAlert = AlertItem(
      id = "sae_sim_${System.currentTimeMillis()}",
      title = title,
      description = "SENAPRED solicita evacuar de inmediato hacia zonas seguras en la Comuna de Santa Juana / San Pedro de la Paz (Región del Biobío). Actúe con calma y a pie. Lleve su kit de emergencia.",
      category = AlertCategory.SAE_BROADCAST,
      severity = AlertSeverity.ROJA,
      region = region,
      commune = "Santa Juana / San Pedro de la Paz",
      timestampFormatted = "Alerta Activa",
      timestampMillis = System.currentTimeMillis(),
      source = "SISTEMA DE ALERTA DE EMERGENCIA (SAE) - SENAPRED BIOBÍO",
      latitude = -37.1731,
      longitude = -72.9419,
      distanceKmFromUser = 0.0,
      instructions = listOf(
        "EVACÚE DE INMEDIATO HACIA PUNTOS DE ENCUENTRO SEGUROS.",
        "Cubra nariz y boca con un paño húmedo ante presencia de humo denso.",
        "Despeje la calzada para vehículos de Bomberos, CONAF y SAMU.",
        "Lleve a sus mascotas con correa o en transportadora.",
        "Permanezca en zona segura hasta la cancelación oficial por SENAPRED."
      ),
      isSaeBroadcast = true
    )

    viewModelScope.launch {
      repository.saveAlertToCache(saeAlert)
    }

    _uiState.value = _uiState.value.copy(activeSaeAlert = saeAlert)
    vibrationHelper.vibrateSaeAlarm()
    playSiren(SirenSoundMode.SAE_ALARM)
  }

  fun dismissSaeAlert() {
    _uiState.value = _uiState.value.copy(activeSaeAlert = null)
    stopSiren()
    vibrationHelper.cancel()
  }

  fun toggleSiren(mode: SirenSoundMode) {
    if (_uiState.value.isSirenPlaying && _uiState.value.activeSirenMode == mode) {
      stopSiren()
    } else {
      playSiren(mode)
    }
  }

  private fun playSiren(mode: SirenSoundMode) {
    _uiState.value = _uiState.value.copy(activeSirenMode = mode)
    sirenManager.startSound(viewModelScope, mode) { isPlaying ->
      _uiState.value = _uiState.value.copy(isSirenPlaying = isPlaying)
    }
    if (mode == SirenSoundMode.SAE_ALARM) {
      vibrationHelper.vibrateSaeAlarm()
    } else {
      vibrationHelper.vibrateSos()
    }
  }

  fun stopSiren() {
    sirenManager.stopSound { isPlaying ->
      _uiState.value = _uiState.value.copy(isSirenPlaying = isPlaying)
    }
    vibrationHelper.cancel()
  }

  fun toggleTorch(mode: FlashlightMode) {
    if (_uiState.value.isTorchActive && _uiState.value.activeTorchMode == mode) {
      torchManager.setFlashlight(viewModelScope, false) {
        _uiState.value = _uiState.value.copy(isTorchActive = it)
      }
    } else {
      _uiState.value = _uiState.value.copy(activeTorchMode = mode)
      torchManager.setFlashlight(viewModelScope, true, mode) {
        _uiState.value = _uiState.value.copy(isTorchActive = it)
      }
    }
  }

  fun toggleSurvivalItem(id: String) {
    survivalRepo.toggleItem(id)
  }

  fun addCustomSurvivalItem(title: String, category: SurvivalCategory) {
    survivalRepo.addNewItem(title, category)
  }

  fun resetSurvivalKit() {
    survivalRepo.resetAll()
  }

  fun addSosContact(name: String, phone: String, relationship: String) {
    sosRepo.addContact(name, phone, relationship)
  }

  fun updateSosContact(contact: SosContact) {
    sosRepo.updateContact(contact)
  }

  fun deleteSosContact(id: String) {
    sosRepo.deleteContact(id)
  }

  fun toggleSosContact(id: String) {
    sosRepo.toggleContact(id)
  }

  fun resetSosContacts() {
    sosRepo.resetDefaults()
  }

  fun getSosMessageText(): String {
    val lat = _uiState.value.userLatitude
    val lon = _uiState.value.userLongitude
    val region = _uiState.value.detectedUserRegion
    val commune = _uiState.value.detectedUserCommune
    val accuracy = _uiState.value.gpsAccuracyMeters?.toInt() ?: 10
    val mapsUrl = "https://maps.google.com/?q=${String.format(Locale.US, "%.5f", lat)},${String.format(Locale.US, "%.5f", lon)}"
    return "¡EMERGENCIA SOS CHILE! Necesito auxilio o rescate urgente.\n" +
      "Ubicación GPS: $commune, Región de $region\n" +
      "Coordenadas: Lat ${String.format(Locale.US, "%.5f", lat)}, Lon ${String.format(Locale.US, "%.5f", lon)} (±${accuracy}m)\n" +
      "Ver mapa en vivo: $mapsUrl\n" +
      "(Enviado desde App Alerta Chile - Biobío)"
  }

  fun sendSosSmsToContacts(context: Context): SosSendResult {
    val message = getSosMessageText()
    val activeContacts = sosRepo.contacts.value.filter { it.isEnabled && it.phone.isNotBlank() }
    val phones = activeContacts.map { it.phone }

    if (activeContacts.isEmpty()) {
      return SosSendResult(
        successCount = 0,
        failureCount = 0,
        sentDirectly = false,
        recipientPhones = emptyList(),
        messageText = message,
        feedbackMessage = "No tienes contactos de emergencia activos seleccionados."
      )
    }

    val hasSmsPermission = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.SEND_SMS
    ) == PackageManager.PERMISSION_GRANTED

    if (hasSmsPermission) {
      val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(SmsManager::class.java)
      } else {
        @Suppress("DEPRECATION")
        SmsManager.getDefault()
      }

      var success = 0
      var fail = 0

      for (contact in activeContacts) {
        try {
          val parts = smsManager.divideMessage(message)
          if (parts.size > 1) {
            smsManager.sendMultipartTextMessage(contact.phone, null, parts, null, null)
          } else {
            smsManager.sendTextMessage(contact.phone, null, message, null, null)
          }
          success++
        } catch (_: Exception) {
          fail++
        }
      }

      vibrationHelper.vibrateSos()

      return SosSendResult(
        successCount = success,
        failureCount = fail,
        sentDirectly = true,
        recipientPhones = phones,
        messageText = message,
        feedbackMessage = "Coordenadas GPS enviadas por SMS a $success contacto(s) de emergencia."
      )
    } else {
      return SosSendResult(
        successCount = 0,
        failureCount = 0,
        sentDirectly = false,
        recipientPhones = phones,
        messageText = message,
        feedbackMessage = "Permiso SMS pendiente. Abriendo app de mensajes con tus coordenadas..."
      )
    }
  }

  fun getSafeStatusMessageText(): String {
    val region = _uiState.value.detectedUserRegion
    val commune = _uiState.value.detectedUserCommune
    return "¡ESTOY A SALVO! Te informo que me encuentro bien y en zona segura tras la emergencia en $commune, Región de $region.\n" +
      "(Enviado desde App Alerta Chile)"
  }

  override fun onCleared() {
    super.onCleared()
    try {
      locationManager?.removeUpdates(locationListener)
    } catch (_: Exception) {}
    batteryManager.release()
    sirenManager.stopSound()
    torchManager.release()
    vibrationHelper.cancel()
  }
}

data class SosSendResult(
  val successCount: Int,
  val failureCount: Int,
  val sentDirectly: Boolean,
  val recipientPhones: List<String>,
  val messageText: String,
  val feedbackMessage: String
)
