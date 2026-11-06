package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class BatteryEmergencyProfile(
  val id: String,
  val displayName: String,
  val shortLabel: String,
  val description: String,
  val gpsIntervalMillis: Long,
  val gpsMinDistanceMeters: Float,
  val minEarthquakeMagnitudeOnMap: Double,
  val isFullMapDetail: Boolean
) {
  NORMAL(
    id = "normal",
    displayName = "Modo Rendimiento Normal",
    shortLabel = "Normal (GPS 4s)",
    description = "GPS en vivo cada 4s. Muestra todos los sismos y alertas completas en el mapa.",
    gpsIntervalMillis = 4000L,
    gpsMinDistanceMeters = 5f,
    minEarthquakeMagnitudeOnMap = 0.0,
    isFullMapDetail = true
  ),
  AHORRO_EQUILIBRADO(
    id = "balanced",
    displayName = "Ahorro Inteligente",
    shortLabel = "Ahorro (GPS 30s)",
    description = "GPS cada 30s (25m). Filtra microsismos (< M 3.5) y simplifica perómetros.",
    gpsIntervalMillis = 30000L,
    gpsMinDistanceMeters = 25f,
    minEarthquakeMagnitudeOnMap = 3.5,
    isFullMapDetail = false
  ),
  AHORRO_CRITICO(
    id = "critical",
    displayName = "Ahorro Crítico Táctico",
    shortLabel = "Crítico (GPS 2m)",
    description = "GPS cada 2 min (100m). Solo alertas rojas/SAE y sismos mayores (≥ M 5.0). Prolonga la batería hasta 4x.",
    gpsIntervalMillis = 120000L,
    gpsMinDistanceMeters = 100f,
    minEarthquakeMagnitudeOnMap = 5.0,
    isFullMapDetail = false
  )
}

data class DeviceBatteryState(
  val levelPercent: Int = 100,
  val isCharging: Boolean = false,
  val isPowerSaveMode: Boolean = false,
  val temperatureCelsius: Float = 25.0f,
  val isAutoAdaptiveEnabled: Boolean = true,
  val manualProfileOverride: BatteryEmergencyProfile? = null
) {
  val effectiveProfile: BatteryEmergencyProfile
    get() {
      if (manualProfileOverride != null) return manualProfileOverride
      if (!isAutoAdaptiveEnabled) return BatteryEmergencyProfile.NORMAL
      if (isCharging) return BatteryEmergencyProfile.NORMAL
      return when {
        levelPercent <= 15 || isPowerSaveMode -> BatteryEmergencyProfile.AHORRO_CRITICO
        levelPercent <= 35 -> BatteryEmergencyProfile.AHORRO_EQUILIBRADO
        else -> BatteryEmergencyProfile.NORMAL
      }
    }

  val isLowBattery: Boolean
    get() = levelPercent <= 35 && !isCharging

  val isCriticalBattery: Boolean
    get() = levelPercent <= 15 && !isCharging
}

class BatteryEmergencyManager(private val context: Context) {

  private val _batteryState = MutableStateFlow(DeviceBatteryState())
  val batteryState: StateFlow<DeviceBatteryState> = _batteryState.asStateFlow()

  private val receiver = object : BroadcastReceiver() {
    override fun onReceive(c: Context?, intent: Intent?) {
      if (intent != null) {
        updateFromIntent(intent)
      }
    }
  }

  private var isReceiverRegistered = false

  init {
    readInitialState()
    registerReceiver()
  }

  private fun readInitialState() {
    try {
      val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
      val batteryStatus: Intent? = context.registerReceiver(null, ifilter)
      if (batteryStatus != null) {
        updateFromIntent(batteryStatus)
      } else {
        // Fallback default
        _batteryState.value = _batteryState.value.copy(levelPercent = 100)
      }
    } catch (_: Exception) {}
  }

  private fun updateFromIntent(intent: Intent) {
    try {
      val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
      val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
      val pct = if (level >= 0 && scale > 0) {
        ((level.toFloat() / scale.toFloat()) * 100).toInt().coerceIn(0, 100)
      } else {
        _batteryState.value.levelPercent
      }

      val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
      val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
        status == BatteryManager.BATTERY_STATUS_FULL

      val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 250)
      val tempC = tempTenths / 10f

      val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
      val isPowerSave = powerManager?.isPowerSaveMode ?: false

      _batteryState.value = _batteryState.value.copy(
        levelPercent = pct,
        isCharging = isCharging,
        isPowerSaveMode = isPowerSave,
        temperatureCelsius = tempC
      )
    } catch (_: Exception) {}
  }

  private fun registerReceiver() {
    try {
      val filter = IntentFilter().apply {
        addAction(Intent.ACTION_BATTERY_CHANGED)
        addAction(Intent.ACTION_POWER_CONNECTED)
        addAction(Intent.ACTION_POWER_DISCONNECTED)
        addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
      }
      context.registerReceiver(receiver, filter)
      isReceiverRegistered = true
    } catch (_: Exception) {}
  }

  fun setAutoAdaptive(enabled: Boolean) {
    _batteryState.value = _batteryState.value.copy(isAutoAdaptiveEnabled = enabled)
  }

  fun setManualProfile(profile: BatteryEmergencyProfile?) {
    _batteryState.value = _batteryState.value.copy(manualProfileOverride = profile)
  }

  fun setSimulatedBattery(levelPercent: Int, isCharging: Boolean = false) {
    _batteryState.value = _batteryState.value.copy(
      levelPercent = levelPercent.coerceIn(0, 100),
      isCharging = isCharging
    )
  }

  fun release() {
    if (isReceiverRegistered) {
      try {
        context.unregisterReceiver(receiver)
        isReceiverRegistered = false
      } catch (_: Exception) {}
    }
  }
}
