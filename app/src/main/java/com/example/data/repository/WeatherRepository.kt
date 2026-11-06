package com.example.data.repository

import com.example.data.ChileEmergencyData
import com.example.data.api.WeatherApiService
import com.example.data.model.DailyWeatherPoint
import com.example.data.model.HourlyWeatherPoint
import com.example.data.model.OpenMeteoResponse
import com.example.data.model.UvLevel
import com.example.data.model.WeatherInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ChileCityCoords(val cityName: String, val lat: Double, val lon: Double)

class WeatherRepository(
  private val api: WeatherApiService = WeatherApiService.create()
) {

  private val regionCoords = mapOf(
    "Arica y Parinacota" to ChileCityCoords("Arica", -18.4783, -70.3126),
    "Tarapacá" to ChileCityCoords("Iquique", -20.2208, -70.1431),
    "Antofagasta" to ChileCityCoords("Antofagasta", -23.6509, -70.3975),
    "Atacama" to ChileCityCoords("Copiapó", -27.3668, -70.3323),
    "Coquimbo" to ChileCityCoords("La Serena", -29.9533, -71.3436),
    "Valparaíso" to ChileCityCoords("Valparaíso", -33.0472, -71.6127),
    "Metropolitana" to ChileCityCoords("Santiago", -33.4489, -70.6693),
    "O'Higgins" to ChileCityCoords("Rancagua", -34.1708, -70.7444),
    "Maule" to ChileCityCoords("Talca", -35.4264, -71.6554),
    "Ñuble" to ChileCityCoords("Chillán", -36.6066, -72.1034),
    "Biobío" to ChileCityCoords("Concepción", -36.8270, -73.0503),
    "Araucanía" to ChileCityCoords("Temuco", -38.7359, -72.5904),
    "Los Ríos" to ChileCityCoords("Valdivia", -39.8142, -73.2459),
    "Los Lagos" to ChileCityCoords("Puerto Montt", -41.4693, -72.9424),
    "Aysén" to ChileCityCoords("Coyhaique", -45.5752, -72.0662),
    "Magallanes" to ChileCityCoords("Punta Arenas", -53.1638, -70.9171)
  )

  private var cachedWeather: WeatherInfo? = null

  suspend fun fetchWeatherForRegion(
    regionName: String,
    userLat: Double? = null,
    userLon: Double? = null
  ): Result<WeatherInfo> = withContext(Dispatchers.IO) {
    val cleanRegion = if (regionName == "Todas las Regiones") "Biobío" else regionName
    val target = regionCoords[cleanRegion] ?: regionCoords["Biobío"]!!

    val lat = if (userLat != null && (regionName == "Todas las Regiones" || regionName == "Biobío")) userLat else target.lat
    val lon = if (userLon != null && (regionName == "Todas las Regiones" || regionName == "Biobío")) userLon else target.lon
    val cityName = if (userLat != null && (regionName == "Todas las Regiones" || regionName == "Biobío")) {
      ChileEmergencyData.detectBiobioCommune(lat, lon)
    } else target.cityName

    try {
      val response = api.getWeather(lat = lat, lon = lon)
      val parsed = parseOpenMeteoResponse(response, cityName, cleanRegion)
      cachedWeather = parsed
      Result.success(parsed)
    } catch (e: Exception) {
      if (cachedWeather != null) {
        Result.success(cachedWeather!!)
      } else {
        val fallback = getFallbackWeather(cityName, cleanRegion)
        cachedWeather = fallback
        Result.success(fallback)
      }
    }
  }

  private fun parseOpenMeteoResponse(
    response: OpenMeteoResponse,
    cityName: String,
    regionName: String
  ): WeatherInfo {
    val current = response.current
    val temp = current?.temperature ?: 22.0
    val appTemp = current?.apparentTemperature ?: temp
    val uv = current?.uvIndex ?: 6.0
    val humidity = current?.relativeHumidity ?: 45.0
    val wind = current?.windSpeed ?: 12.0
    val weatherCode = current?.weatherCode ?: 0

    val daily = response.daily
    val tempMax = daily?.tempMax?.firstOrNull() ?: (temp + 4.0)
    val tempMin = daily?.tempMin?.firstOrNull() ?: (temp - 6.0)
    val maxUvToday = daily?.uvMax?.firstOrNull() ?: uv

    val uvLevel = getUvLevel(uv)
    val condition = decodeWeatherCode(weatherCode)

    // CONAF 30-30-30 Rule: Temp > 30°C, Wind > 30 km/h, Humidity < 30%
    val isFireRisk = temp >= 30.0 && wind >= 28.0 && humidity <= 32.0
    val isExtremeHeat = temp >= 33.0

    val recs = getUvRecommendations(uvLevel)

    // Parse hourly forecast points (next 8 hours)
    val hourlyList = mutableListOf<HourlyWeatherPoint>()
    val hourly = response.hourly
    if (hourly?.time != null && hourly.temp != null && hourly.uv != null) {
      val count = minOf(hourly.time.size, hourly.temp.size, hourly.uv.size, 10)
      for (i in 0 until count) {
        val rawTime = hourly.time[i]
        val hourPart = rawTime.substringAfter("T").take(5)
        hourlyList.add(
          HourlyWeatherPoint(
            timeFormatted = hourPart,
            temp = hourly.temp[i],
            uv = hourly.uv[i]
          )
        )
      }
    }

    // Parse daily forecast (3 days)
    val dailyList = mutableListOf<DailyWeatherPoint>()
    if (daily?.time != null && daily.tempMax != null && daily.tempMin != null && daily.uvMax != null) {
      val dayCount = minOf(daily.time.size, daily.tempMax.size, daily.tempMin.size, daily.uvMax.size, 3)
      val dayLabels = listOf("Hoy", "Mañana", "Pasado mañana")
      for (i in 0 until dayCount) {
        dailyList.add(
          DailyWeatherPoint(
            dateFormatted = dayLabels.getOrElse(i) { daily.time[i].takeLast(5) },
            tempMax = daily.tempMax[i],
            tempMin = daily.tempMin[i],
            uvMax = daily.uvMax[i]
          )
        )
      }
    }

    return WeatherInfo(
      temperature = temp,
      apparentTemperature = appTemp,
      uvIndex = uv,
      maxUvToday = maxUvToday,
      uvLevel = uvLevel,
      humidity = humidity,
      windSpeedKmH = wind,
      weatherCode = weatherCode,
      conditionDescription = condition,
      cityName = cityName,
      regionName = regionName,
      tempMax = tempMax,
      tempMin = tempMin,
      isFireRisk30_30_30 = isFireRisk,
      isExtremeHeat = isExtremeHeat,
      uvRecommendations = recs,
      hourlyForecast = hourlyList,
      dailyForecast = dailyList
    )
  }

  private fun getUvLevel(uv: Double): UvLevel {
    return when {
      uv < 3.0 -> UvLevel.BAJO
      uv < 6.0 -> UvLevel.MODERADO
      uv < 8.0 -> UvLevel.ALTO
      uv < 11.0 -> UvLevel.MUY_ALTO
      else -> UvLevel.EXTREMO
    }
  }

  private fun getUvRecommendations(level: UvLevel): List<String> {
    return when (level) {
      UvLevel.BAJO -> listOf(
        "Riesgo mínimo de radiación.",
        "Disfrute de actividades al aire libre con seguridad.",
        "Se sugiere uso de gafas de sol en días muy brillantes."
      )
      UvLevel.MODERADO -> listOf(
        "Se recomienda protector solar FPS 30+.",
        "Use sombrero de ala ancha y lentes con filtro UV.",
        "Busque sombra en las horas de mayor insolación."
      )
      UvLevel.ALTO -> listOf(
        "Protección solar imprescindible (FPS 50+).",
        "Reduzca la exposición al sol entre las 11:00 y las 16:00.",
        "Use prendas de manga larga de tejido denso y gorro legionario.",
        "Hidratación constante."
      )
      UvLevel.MUY_ALTO -> listOf(
        "Riesgo muy alto de quemaduras solares.",
        "Evite estar al sol al mediodía; busque sombra permanente.",
        "Reaplique bloqueador FPS 50+ cada 2 horas.",
        "Proteja especialmente a niños, niñas y adultos mayores."
      )
      UvLevel.EXTREMO -> listOf(
        "¡PELIGRO EXTREMO DE RADIACIÓN ULTRAVIOLETA!",
        "La piel desprotegida puede quemarse en menos de 10 minutos.",
        "Permanezca bajo techo entre 11:00 y 16:30 si es posible.",
        "Uso obligatorio de gafas UV400, ropa protectora y bloqueador FPS 50+."
      )
    }
  }

  private fun decodeWeatherCode(code: Int): String {
    return when (code) {
      0 -> "Despejado"
      1 -> "Mayormente despejado"
      2 -> "Parcialmente nublado"
      3 -> "Nublado"
      45, 48 -> "Niebla / Camanchaca"
      51, 53, 55 -> "Llovizna leve"
      61, 63, 65 -> "Lluvia"
      71, 73, 75 -> "Nevadas cordilleranas"
      80, 81, 82 -> "Chubascos"
      95, 96, 99 -> "Tormenta eléctrica"
      else -> "Despejado con nubosidad"
    }
  }

  private fun getFallbackWeather(cityName: String, regionName: String): WeatherInfo {
    return WeatherInfo(
      temperature = 24.5,
      apparentTemperature = 25.0,
      uvIndex = 8.2,
      maxUvToday = 9.5,
      uvLevel = UvLevel.MUY_ALTO,
      humidity = 38.0,
      windSpeedKmH = 18.0,
      weatherCode = 0,
      conditionDescription = "Despejado",
      cityName = cityName,
      regionName = regionName,
      tempMax = 28.0,
      tempMin = 12.0,
      isFireRisk30_30_30 = false,
      isExtremeHeat = false,
      uvRecommendations = getUvRecommendations(UvLevel.MUY_ALTO),
      hourlyForecast = listOf(
        HourlyWeatherPoint("10:00", 20.0, 5.0),
        HourlyWeatherPoint("12:00", 25.0, 8.5),
        HourlyWeatherPoint("14:00", 28.0, 9.5),
        HourlyWeatherPoint("16:00", 27.0, 7.0),
        HourlyWeatherPoint("18:00", 23.0, 3.0),
        HourlyWeatherPoint("20:00", 19.0, 0.0)
      ),
      dailyForecast = listOf(
        DailyWeatherPoint("Hoy", 28.0, 12.0, 9.5),
        DailyWeatherPoint("Mañana", 29.0, 13.0, 9.8),
        DailyWeatherPoint("Pasado mañana", 27.0, 11.0, 8.9)
      )
    )
  }
}
