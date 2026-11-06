package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class UvLevel(val displayName: String, val rangeText: String, val colorHex: Long) {
  BAJO("Bajo", "0 - 2", 0xFF10B981),
  MODERADO("Moderado", "3 - 5", 0xFFEAB308),
  ALTO("Alto", "6 - 7", 0xFFF97316),
  MUY_ALTO("Muy Alto", "8 - 10", 0xFFDC2626),
  EXTREMO("Extremo", "11+", 0xFF9333EA)
}

data class WeatherInfo(
  val temperature: Double,
  val apparentTemperature: Double,
  val uvIndex: Double,
  val maxUvToday: Double,
  val uvLevel: UvLevel,
  val humidity: Double,
  val windSpeedKmH: Double,
  val weatherCode: Int,
  val conditionDescription: String,
  val cityName: String,
  val regionName: String,
  val tempMax: Double,
  val tempMin: Double,
  val isFireRisk30_30_30: Boolean, // Regla 30-30-30 de CONAF para incendios forestales
  val isExtremeHeat: Boolean, // Alerta de calor extremo
  val uvRecommendations: List<String>,
  val hourlyForecast: List<HourlyWeatherPoint> = emptyList(),
  val dailyForecast: List<DailyWeatherPoint> = emptyList()
)

data class HourlyWeatherPoint(
  val timeFormatted: String,
  val temp: Double,
  val uv: Double
)

data class DailyWeatherPoint(
  val dateFormatted: String,
  val tempMax: Double,
  val tempMin: Double,
  val uvMax: Double
)

// DTOs for Open-Meteo
@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
  @Json(name = "current") val current: OpenMeteoCurrent?,
  @Json(name = "daily") val daily: OpenMeteoDaily?,
  @Json(name = "hourly") val hourly: OpenMeteoHourly?
)

@JsonClass(generateAdapter = true)
data class OpenMeteoCurrent(
  @Json(name = "temperature_2m") val temperature: Double?,
  @Json(name = "apparent_temperature") val apparentTemperature: Double?,
  @Json(name = "relative_humidity_2m") val relativeHumidity: Double?,
  @Json(name = "wind_speed_10m") val windSpeed: Double?,
  @Json(name = "weather_code") val weatherCode: Int?,
  @Json(name = "uv_index") val uvIndex: Double?
)

@JsonClass(generateAdapter = true)
data class OpenMeteoDaily(
  @Json(name = "time") val time: List<String>?,
  @Json(name = "temperature_2m_max") val tempMax: List<Double>?,
  @Json(name = "temperature_2m_min") val tempMin: List<Double>?,
  @Json(name = "uv_index_max") val uvMax: List<Double>?
)

@JsonClass(generateAdapter = true)
data class OpenMeteoHourly(
  @Json(name = "time") val time: List<String>?,
  @Json(name = "temperature_2m") val temp: List<Double>?,
  @Json(name = "uv_index") val uv: List<Double>?
)
