package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.EarthquakeItem
import com.example.data.model.TsunamiThreat

/**
 * Room Entity representing seismic events cached locally on device.
 */
@Entity(tableName = "cached_earthquakes")
data class CachedEarthquakeEntity(
  @PrimaryKey
  val id: String,
  val place: String,
  val magnitude: Double,
  val depthKm: Double,
  val timeMillis: Long,
  val formattedTime: String,
  val latitude: Double,
  val longitude: Double,
  val chileRegion: String,
  val tsunamiThreatName: String,
  val estimatedIntensityMercalli: String,
  val source: String,
  val cachedAtMillis: Long = System.currentTimeMillis()
) {
  fun toDomain(): EarthquakeItem {
    val tsunami = runCatching { TsunamiThreat.valueOf(tsunamiThreatName) }
      .getOrDefault(TsunamiThreat.NO_AMENAZA)

    return EarthquakeItem(
      id = id,
      place = place,
      magnitude = magnitude,
      depthKm = depthKm,
      timeMillis = timeMillis,
      formattedTime = formattedTime,
      latitude = latitude,
      longitude = longitude,
      chileRegion = chileRegion,
      tsunamiThreat = tsunami,
      estimatedIntensityMercalli = estimatedIntensityMercalli,
      source = source
    )
  }

  companion object {
    fun fromDomain(item: EarthquakeItem): CachedEarthquakeEntity {
      return CachedEarthquakeEntity(
        id = item.id,
        place = item.place,
        magnitude = item.magnitude,
        depthKm = item.depthKm,
        timeMillis = item.timeMillis,
        formattedTime = item.formattedTime,
        latitude = item.latitude,
        longitude = item.longitude,
        chileRegion = item.chileRegion,
        tsunamiThreatName = item.tsunamiThreat.name,
        estimatedIntensityMercalli = item.estimatedIntensityMercalli,
        source = item.source,
        cachedAtMillis = System.currentTimeMillis()
      )
    }
  }
}
