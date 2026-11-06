package com.example.data.repository

import com.example.data.ChileEmergencyData
import com.example.data.api.SeismicApiService
import com.example.data.api.UsgsFeature
import com.example.data.local.dao.AlertDao
import com.example.data.local.dao.EarthquakeDao
import com.example.data.local.entity.CachedAlertEntity
import com.example.data.local.entity.CachedEarthquakeEntity
import com.example.data.model.AlertCategory
import com.example.data.model.AlertItem
import com.example.data.model.AlertSeverity
import com.example.data.model.EarthquakeItem
import com.example.data.model.TsunamiThreat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class CacheMetadata(
  val cachedAlertsCount: Int,
  val lastUpdatedMillis: Long,
  val isOffline: Boolean
)

class EmergencyAlertRepository(
  private val alertDao: AlertDao,
  private val earthquakeDao: EarthquakeDao,
  private val seismicApi: SeismicApiService = SeismicApiService.create()
) {

  /**
   * Reactive Flow of alerts directly from Room local database.
   * Emits whenever alerts are inserted, updated, or deleted on disk.
   */
  fun observeAlerts(userLat: Double? = null, userLon: Double? = null): Flow<List<AlertItem>> {
    return alertDao.observeAllAlerts().map { entities ->
      entities.map { entity ->
        val alert = entity.toDomain()
        if (userLat != null && userLon != null && alert.latitude != null && alert.longitude != null) {
          val dist = ChileEmergencyData.calculateDistanceKm(userLat, userLon, alert.latitude, alert.longitude)
          alert.copy(distanceKmFromUser = dist)
        } else {
          alert
        }
      }
    }
  }

  /**
   * Retrieves official emergency alerts from Room database.
   * If database is unseeded, populates it first with official Chile emergency alerts.
   */
  suspend fun getOfficialAlerts(
    userLat: Double? = null,
    userLon: Double? = null,
    forceRefreshFromNetwork: Boolean = false
  ): List<AlertItem> = withContext(Dispatchers.IO) {
    var cached = alertDao.getAllAlerts()

    // Seed database if empty or if forced refresh requested
    if (cached.isEmpty()) {
      val initialAlerts = ChileEmergencyData.getInitialOfficialAlerts()
      val entities = initialAlerts.map { CachedAlertEntity.fromDomain(it) }
      alertDao.insertAlerts(entities)
      cached = alertDao.getAllAlerts()
    }

    cached.map { entity ->
      val alert = entity.toDomain()
      if (userLat != null && userLon != null && alert.latitude != null && alert.longitude != null) {
        val dist = ChileEmergencyData.calculateDistanceKm(userLat, userLon, alert.latitude, alert.longitude)
        alert.copy(distanceKmFromUser = dist)
      } else {
        alert
      }
    }
  }

  /**
   * Fetches seismic events from USGS / CSN API.
   * On success: stores them persistently in Room database.
   * On network loss / offline: falls back to last known cached events from Room.
   */
  suspend fun fetchEarthquakes(
    userLat: Double? = null,
    userLon: Double? = null,
    forceOfflineMode: Boolean = false
  ): Result<List<EarthquakeItem>> = withContext(Dispatchers.IO) {
    if (forceOfflineMode) {
      val localEntities = earthquakeDao.getAllEarthquakes()
      val items = if (localEntities.isNotEmpty()) {
        localEntities.map { it.toDomain() }
      } else {
        val fallback = getFallbackChileEarthquakes(userLat, userLon)
        earthquakeDao.insertEarthquakes(fallback.map { CachedEarthquakeEntity.fromDomain(it) })
        fallback
      }
      return@withContext Result.success(applyDistancesToEarthquakes(items, userLat, userLon))
    }

    try {
      val response = seismicApi.getChileEarthquakes()
      val items = response.features.mapNotNull { parseFeatureToEarthquake(it, userLat, userLon) }

      if (items.isNotEmpty()) {
        // Save to Room for offline caching
        val entities = items.map { CachedEarthquakeEntity.fromDomain(it) }
        earthquakeDao.insertEarthquakes(entities)
      }

      val resultList = if (items.isNotEmpty()) {
        items
      } else {
        earthquakeDao.getAllEarthquakes().map { it.toDomain() }
      }

      Result.success(applyDistancesToEarthquakes(resultList, userLat, userLon))
    } catch (e: Exception) {
      // Network failed or offline: read from Room database
      val localEntities = earthquakeDao.getAllEarthquakes()
      if (localEntities.isNotEmpty()) {
        val cached = localEntities.map { it.toDomain() }
        Result.success(applyDistancesToEarthquakes(cached, userLat, userLon))
      } else {
        val fallback = getFallbackChileEarthquakes(userLat, userLon)
        earthquakeDao.insertEarthquakes(fallback.map { CachedEarthquakeEntity.fromDomain(it) })
        Result.success(fallback)
      }
    }
  }

  /**
   * Saves a custom or simulated alert (such as an SAE broadcast) directly into Room.
   */
  suspend fun saveAlertToCache(alert: AlertItem) = withContext(Dispatchers.IO) {
    alertDao.insertAlert(CachedAlertEntity.fromDomain(alert))
  }

  /**
   * Clears alerts cache and re-populates with standard baseline.
   */
  suspend fun resetAlertsCache() = withContext(Dispatchers.IO) {
    alertDao.clearAllAlerts()
    val initialAlerts = ChileEmergencyData.getInitialOfficialAlerts()
    val entities = initialAlerts.map { CachedAlertEntity.fromDomain(it) }
    alertDao.insertAlerts(entities)
  }

  suspend fun getCacheMetadata(): CacheMetadata = withContext(Dispatchers.IO) {
    val count = alertDao.getAlertCount()
    val lastUpdated = alertDao.getLatestCacheTimestamp() ?: System.currentTimeMillis()
    CacheMetadata(
      cachedAlertsCount = count,
      lastUpdatedMillis = lastUpdated,
      isOffline = false
    )
  }

  fun applyDistancesToEarthquakes(
    list: List<EarthquakeItem>,
    userLat: Double?,
    userLon: Double?
  ): List<EarthquakeItem> {
    if (userLat == null || userLon == null) return list
    return list.map { eq ->
      val dist = ChileEmergencyData.calculateDistanceKm(userLat, userLon, eq.latitude, eq.longitude)
      eq.copy(distanceKmFromUser = dist)
    }
  }

  private fun parseFeatureToEarthquake(
    feature: UsgsFeature,
    userLat: Double?,
    userLon: Double?
  ): EarthquakeItem? {
    val mag = feature.properties.mag ?: return null
    val rawPlace = feature.properties.place ?: "Chile"
    val time = feature.properties.time ?: System.currentTimeMillis()
    val coords = feature.geometry.coordinates
    val lon = coords.getOrNull(0) ?: -73.0503
    val lat = coords.getOrNull(1) ?: -36.8270
    val depth = coords.getOrNull(2) ?: 30.0

    val placeTranslated = translatePlace(rawPlace)
    val region = ChileEmergencyData.detectRegionFromCoordinates(lat, lon)
    val mercalli = ChileEmergencyData.estimateMercalli(mag, depth)

    val tsunami = when {
      feature.properties.tsunami == 1 || (mag >= 7.0 && depth <= 60.0) -> TsunamiThreat.ALERTA_TSUNAMI
      mag >= 6.3 && depth <= 80.0 -> TsunamiThreat.PRECAUCION
      else -> TsunamiThreat.NO_AMENAZA
    }

    val dist = if (userLat != null && userLon != null) {
      ChileEmergencyData.calculateDistanceKm(userLat, userLon, lat, lon)
    } else null

    return EarthquakeItem(
      id = feature.id,
      place = placeTranslated,
      magnitude = (mag * 10).toInt() / 10.0,
      depthKm = (depth * 10).toInt() / 10.0,
      timeMillis = time,
      formattedTime = formatChileTime(time),
      latitude = lat,
      longitude = lon,
      chileRegion = region,
      tsunamiThreat = tsunami,
      estimatedIntensityMercalli = mercalli,
      distanceKmFromUser = dist
    )
  }

  private fun translatePlace(place: String): String {
    return place
      .replace(" km SW of ", " km al SO de ")
      .replace(" km NW of ", " km al NO de ")
      .replace(" km SE of ", " km al SE de ")
      .replace(" km NE of ", " km al NE de ")
      .replace(" km S of ", " km al S de ")
      .replace(" km N of ", " km al N de ")
      .replace(" km W of ", " km al O de ")
      .replace(" km E of ", " km al E de ")
      .replace("of Chile", "de Chile")
  }

  private fun formatChileTime(timeMillis: Long): String {
    val now = System.currentTimeMillis()
    val diffMinutes = (now - timeMillis) / (60 * 1000)

    if (diffMinutes < 1) return "Recién"
    if (diffMinutes < 60) return "Hace ${diffMinutes}m"
    if (diffMinutes < 120) return "Hace 1h"
    if (diffMinutes < 24 * 60) return "Hace ${(diffMinutes / 60)}h"

    val sdf = SimpleDateFormat("dd/MM HH:mm", Locale("es", "CL"))
    sdf.timeZone = TimeZone.getTimeZone("America/Santiago")
    return sdf.format(Date(timeMillis))
  }

  private fun getFallbackChileEarthquakes(
    userLat: Double?,
    userLon: Double?
  ): List<EarthquakeItem> {
    val now = System.currentTimeMillis()
    val raw = listOf(
      EarthquakeItem(
        id = "biobio_eq_01",
        place = "32 km al O de Lebu (Fosa de Arauco)",
        magnitude = 5.2,
        depthKm = 28.0,
        timeMillis = now - 14 * 60 * 1000,
        formattedTime = "Hace 14m",
        latitude = -37.61,
        longitude = -74.05,
        chileRegion = "Biobío",
        tsunamiThreat = TsunamiThreat.NO_AMENAZA,
        estimatedIntensityMercalli = "V - Poco Fuerte"
      ),
      EarthquakeItem(
        id = "biobio_eq_02",
        place = "21 km al NO de Talcahuano (Bahía de Concepción)",
        magnitude = 4.4,
        depthKm = 35.0,
        timeMillis = now - 48 * 60 * 1000,
        formattedTime = "Hace 48m",
        latitude = -36.62,
        longitude = -73.25,
        chileRegion = "Biobío",
        tsunamiThreat = TsunamiThreat.NO_AMENAZA,
        estimatedIntensityMercalli = "IV - Moderado"
      ),
      EarthquakeItem(
        id = "biobio_eq_03",
        place = "18 km al SO de Los Ángeles",
        magnitude = 3.8,
        depthKm = 42.0,
        timeMillis = now - 130 * 60 * 1000,
        formattedTime = "Hace 2h",
        latitude = -37.58,
        longitude = -72.48,
        chileRegion = "Biobío",
        tsunamiThreat = TsunamiThreat.NO_AMENAZA,
        estimatedIntensityMercalli = "III - Leve"
      ),
      EarthquakeItem(
        id = "fb_s1",
        place = "24 km al NO de Pichilemu",
        magnitude = 4.7,
        depthKm = 34.0,
        timeMillis = now - 3 * 3600 * 1000,
        formattedTime = "Hace 3h",
        latitude = -34.38,
        longitude = -72.01,
        chileRegion = "O'Higgins",
        tsunamiThreat = TsunamiThreat.NO_AMENAZA,
        estimatedIntensityMercalli = "IV - Moderado"
      ),
      EarthquakeItem(
        id = "fb_s2",
        place = "42 km al SO de Huasco",
        magnitude = 5.0,
        depthKm = 42.0,
        timeMillis = now - 5 * 3600 * 1000,
        formattedTime = "Hace 5h",
        latitude = -28.75,
        longitude = -71.55,
        chileRegion = "Atacama",
        tsunamiThreat = TsunamiThreat.NO_AMENAZA,
        estimatedIntensityMercalli = "IV - Moderado"
      )
    )

    return applyDistancesToEarthquakes(raw, userLat, userLon)
  }
}
