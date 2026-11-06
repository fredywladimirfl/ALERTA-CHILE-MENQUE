package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AlertCategory
import com.example.data.model.AlertItem
import com.example.data.model.AlertSeverity
import com.example.data.model.TsunamiThreat

/**
 * Room Entity representing emergency alerts cached locally on device
 * to ensure availability during blackouts or network connectivity loss.
 */
@Entity(tableName = "cached_alerts")
data class CachedAlertEntity(
  @PrimaryKey
  val id: String,
  val title: String,
  val description: String,
  val categoryName: String,
  val severityName: String,
  val region: String,
  val commune: String?,
  val timestampFormatted: String,
  val timestampMillis: Long,
  val source: String,
  val magnitude: Double?,
  val depthKm: Double?,
  val tsunamiThreatName: String,
  val instructionsDelimited: String,
  val isActive: Boolean,
  val isSaeBroadcast: Boolean,
  val latitude: Double?,
  val longitude: Double?,
  val cachedAtMillis: Long = System.currentTimeMillis()
) {
  fun toDomain(): AlertItem {
    val category = runCatching { AlertCategory.valueOf(categoryName) }
      .getOrDefault(AlertCategory.METEOROLOGICA)
    val severity = runCatching { AlertSeverity.valueOf(severityName) }
      .getOrDefault(AlertSeverity.AMARILLA)
    val tsunami = runCatching { TsunamiThreat.valueOf(tsunamiThreatName) }
      .getOrDefault(TsunamiThreat.NO_APLICA)
    val instructionsList = if (instructionsDelimited.isBlank()) {
      emptyList()
    } else {
      instructionsDelimited.split("|||")
    }

    return AlertItem(
      id = id,
      title = title,
      description = description,
      category = category,
      severity = severity,
      region = region,
      commune = commune,
      timestampFormatted = timestampFormatted,
      timestampMillis = timestampMillis,
      source = source,
      magnitude = magnitude,
      depthKm = depthKm,
      tsunamiThreat = tsunami,
      instructions = instructionsList,
      isActive = isActive,
      isSaeBroadcast = isSaeBroadcast,
      latitude = latitude,
      longitude = longitude
    )
  }

  companion object {
    fun fromDomain(item: AlertItem): CachedAlertEntity {
      return CachedAlertEntity(
        id = item.id,
        title = item.title,
        description = item.description,
        categoryName = item.category.name,
        severityName = item.severity.name,
        region = item.region,
        commune = item.commune,
        timestampFormatted = item.timestampFormatted,
        timestampMillis = item.timestampMillis,
        source = item.source,
        magnitude = item.magnitude,
        depthKm = item.depthKm,
        tsunamiThreatName = item.tsunamiThreat.name,
        instructionsDelimited = item.instructions.joinToString("|||"),
        isActive = item.isActive,
        isSaeBroadcast = item.isSaeBroadcast,
        latitude = item.latitude,
        longitude = item.longitude,
        cachedAtMillis = System.currentTimeMillis()
      )
    }
  }
}
