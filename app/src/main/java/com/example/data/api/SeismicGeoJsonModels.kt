package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UsgsFeatureCollection(
  @Json(name = "features") val features: List<UsgsFeature> = emptyList()
)

@JsonClass(generateAdapter = true)
data class UsgsFeature(
  @Json(name = "id") val id: String,
  @Json(name = "properties") val properties: UsgsProperties,
  @Json(name = "geometry") val geometry: UsgsGeometry
)

@JsonClass(generateAdapter = true)
data class UsgsProperties(
  @Json(name = "mag") val mag: Double?,
  @Json(name = "place") val place: String?,
  @Json(name = "time") val time: Long?,
  @Json(name = "title") val title: String?,
  @Json(name = "tsunami") val tsunami: Int?
)

@JsonClass(generateAdapter = true)
data class UsgsGeometry(
  @Json(name = "coordinates") val coordinates: List<Double> = emptyList()
)
