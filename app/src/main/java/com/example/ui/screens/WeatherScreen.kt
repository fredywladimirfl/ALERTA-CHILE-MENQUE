package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChileEmergencyData
import com.example.data.model.UvLevel
import com.example.data.model.WeatherInfo
import com.example.ui.EmergencyUiState
import com.example.ui.theme.AlertEmergencyRed
import com.example.ui.theme.AlertWarningOrange

@Composable
fun WeatherScreen(
  uiState: EmergencyUiState,
  onRegionSelected: (String) -> Unit,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier
) {
  val weather = uiState.weatherInfo

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("weather_screen_list"),
    contentPadding = PaddingValues(16.dp, bottom = 96.dp)
  ) {
    // Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .background(Color(0xFFFBBF24), CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "METEOROLOGÍA & RADIACIÓN UV",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFFFBBF24),
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
          }
          Text(
            text = "Tiempo y Radiación UV",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
        }

        IconButton(
          onClick = onRefresh,
          modifier = Modifier.testTag("refresh_weather_btn")
        ) {
          if (uiState.isLoadingWeather) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              color = Color(0xFF38BDF8),
              strokeWidth = 2.dp
            )
          } else {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Actualizar clima",
              tint = Color(0xFF38BDF8)
            )
          }
        }
      }

      Text(
        text = "Temperatura en vivo, índice UV y factores de riesgo meteorológico para Chile",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF94A3B8)
      )

      Spacer(modifier = Modifier.height(14.dp))
    }

    // Regional Selector Chips
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.testTag("weather_region_chips")
      ) {
        items(ChileEmergencyData.CHILE_REGIONS) { region ->
          val isSelected = uiState.selectedRegion == region
          FilterChip(
            selected = isSelected,
            onClick = { onRegionSelected(region) },
            label = { Text(region, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = Color(0xFF0039A6),
              selectedLabelColor = Color.White,
              containerColor = Color(0xFF141F30),
              labelColor = Color(0xFF94A3B8)
            ),
            border = FilterChipDefaults.filterChipBorder(
              enabled = true,
              selected = isSelected,
              borderColor = if (isSelected) Color(0xFF3B82F6) else Color(0xFF26374F)
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
    }

    if (weather == null) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF141F30)),
          shape = RoundedCornerShape(16.dp)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(40.dp),
            contentAlignment = Alignment.Center
          ) {
            CircularProgressIndicator(color = Color(0xFF38BDF8))
          }
        }
      }
    } else {
      val uvColor = Color(weather.uvLevel.colorHex)

      // 1. HERO TEMPERATURE & CONDITION CARD
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, Color(0xFF26374F), RoundedCornerShape(22.dp)),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF141F30))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                Brush.verticalGradient(
                  colors = listOf(
                    Color(0xFF1A2A42),
                    Color(0xFF141F30)
                  )
                )
              )
              .padding(20.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = weather.cityName,
                  style = MaterialTheme.typography.titleLarge,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 22.sp
                )
                Text(
                  text = "Región ${weather.regionName}",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color(0xFF94A3B8)
                )
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0xFF243447))
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(
                  text = weather.conditionDescription,
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFFFDE047),
                  fontWeight = FontWeight.Bold
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Large Temperature Display
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(verticalAlignment = Alignment.Top) {
                Text(
                  text = "${weather.temperature.toInt()}",
                  style = MaterialTheme.typography.displayLarge,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 64.sp,
                  lineHeight = 64.sp
                )
                Text(
                  text = "°C",
                  style = MaterialTheme.typography.headlineMedium,
                  color = Color(0xFF38BDF8),
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(top = 8.dp)
                )
              }

              // Min / Max Pill
              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "Sensación térmica: ${weather.apparentTemperature.toInt()}°C",
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color(0xFFE2E8F0),
                  fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                  Text(
                    text = "↓ ${weather.tempMin.toInt()}°",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.Bold
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "↑ ${weather.tempMax.toInt()}°",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF87171),
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = Color(0xFF26374F))
            Spacer(modifier = Modifier.height(14.dp))

            // Metrics row: Humidity & Wind
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceAround
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.WaterDrop,
                  contentDescription = null,
                  tint = Color(0xFF38BDF8),
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                  Text("HUMEDAD", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B), fontSize = 9.sp)
                  Text("${weather.humidity.toInt()}%", style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
                }
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Air,
                  contentDescription = null,
                  tint = Color(0xFF94A3B8),
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                  Text("VIENTO", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B), fontSize = 9.sp)
                  Text("${weather.windSpeedKmH.toInt()} km/h", style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
      }

      // 2. MEDIDOR Y ANÁLISIS DE RADIACIÓN UV
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(2.dp, uvColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .testTag("uv_meter_card"),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF141F30))
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(42.dp)
                    .background(uvColor.copy(alpha = 0.2f), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = uvColor,
                    modifier = Modifier.size(24.dp)
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = "Índice de Radiación UV",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "Medición oficial para Chile",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                  )
                }
              }

              // Level pill
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(uvColor)
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(
                  text = weather.uvLevel.displayName.uppercase(),
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // UV Gauge Values Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Bottom
            ) {
              Column {
                Text(
                  text = "ÍNDICE ACTUAL",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFF94A3B8),
                  fontSize = 10.sp
                )
                Text(
                  text = String.format("%.1f", weather.uvIndex),
                  style = MaterialTheme.typography.displayMedium,
                  color = uvColor,
                  fontWeight = FontWeight.Bold,
                  fontSize = 36.sp
                )
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "MÁXIMO ESPERADO HOY",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFF94A3B8),
                  fontSize = 10.sp
                )
                Text(
                  text = "Pico: ${String.format("%.1f", weather.maxUvToday)}",
                  style = MaterialTheme.typography.titleMedium,
                  color = Color(0xFFFDE047),
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Horario crítico: 11:30 a 16:00 hrs",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color(0xFF94A3B8),
                  fontSize = 11.sp
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Segmented UV Spectrum Bar (0 to 12+)
            Column(modifier = Modifier.fillMaxWidth()) {
              val uvFraction = (weather.uvIndex.toFloat() / 12f).coerceIn(0f, 1f)
              LinearProgressIndicator(
                progress = { uvFraction },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(10.dp)
                  .clip(RoundedCornerShape(5.dp)),
                color = uvColor,
                trackColor = Color(0xFF243447)
              )

              Spacer(modifier = Modifier.height(6.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Bajo (0-2)", fontSize = 9.sp, color = Color(0xFF10B981))
                Text("Mod (3-5)", fontSize = 9.sp, color = Color(0xFFEAB308))
                Text("Alto (6-7)", fontSize = 9.sp, color = Color(0xFFF97316))
                Text("Muy Alto (8-10)", fontSize = 9.sp, color = Color(0xFFDC2626))
                Text("Extremo (11+)", fontSize = 9.sp, color = Color(0xFF9333EA), fontWeight = FontWeight.Bold)
              }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFF26374F))
            Spacer(modifier = Modifier.height(12.dp))

            // UV Protective Recommendations
            Text(
              text = "RECOMENDACIONES DE SALUD (MINSAL / DMC):",
              style = MaterialTheme.typography.labelMedium,
              color = Color(0xFFFBBF24),
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            weather.uvRecommendations.forEach { rec ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 3.dp),
                verticalAlignment = Alignment.Top
              ) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = uvColor,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = rec,
                  style = MaterialTheme.typography.bodySmall,
                  color = Color(0xFFE2E8F0)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
      }

      // 3. REGLA 30-30-30 CONAF (Incendios Forestales)
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
              1.dp,
              if (weather.isFireRisk30_30_30) AlertEmergencyRed else Color(0xFF26374F),
              RoundedCornerShape(18.dp)
            ),
          colors = CardDefaults.cardColors(
            containerColor = if (weather.isFireRisk30_30_30) AlertEmergencyRed.copy(alpha = 0.2f) else Color(0xFF141F30)
          )
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = if (weather.isFireRisk30_30_30) AlertEmergencyRed else AlertWarningOrange,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Factor de Riesgo de Incendio (CONAF)",
                  style = MaterialTheme.typography.titleMedium,
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Criterio técnico Regla 30-30-30",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color(0xFF94A3B8)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              val isTempTrigger = weather.temperature >= 30.0
              val isWindTrigger = weather.windSpeedKmH >= 30.0
              val isHumidityTrigger = weather.humidity <= 30.0

              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Temp > 30°C", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8), fontSize = 10.sp)
                Text("${weather.temperature.toInt()}°C", color = if (isTempTrigger) AlertEmergencyRed else Color(0xFF10B981), fontWeight = FontWeight.Bold)
              }

              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Viento > 30 km/h", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8), fontSize = 10.sp)
                Text("${weather.windSpeedKmH.toInt()} km/h", color = if (isWindTrigger) AlertEmergencyRed else Color(0xFF10B981), fontWeight = FontWeight.Bold)
              }

              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Humedad < 30%", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8), fontSize = 10.sp)
                Text("${weather.humidity.toInt()}%", color = if (isHumidityTrigger) AlertEmergencyRed else Color(0xFF10B981), fontWeight = FontWeight.Bold)
              }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = if (weather.isFireRisk30_30_30)
                "¡ALERTA MÁXIMA DE PROPAGACIÓN! Las condiciones actuales facilitan el inicio y avance descontrolado de fuego forestal."
              else
                "Condiciones meteorológicas actuales no cumplen simultáneamente los 3 factores críticos 30-30-30.",
              style = MaterialTheme.typography.bodySmall,
              color = if (weather.isFireRisk30_30_30) Color(0xFFFCA5A5) else Color(0xFF94A3B8)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
      }

      // 4. PRONÓSTICO POR HORAS (TEMPERATURA Y UV)
      if (weather.hourlyForecast.isNotEmpty()) {
        item {
          Text(
            text = "Evolución por Horas (Temperatura y UV)",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(10.dp))

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.testTag("hourly_forecast_row")
          ) {
            items(weather.hourlyForecast) { point ->
              Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141F30)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .width(84.dp)
                  .border(1.dp, Color(0xFF26374F), RoundedCornerShape(12.dp))
              ) {
                Column(
                  modifier = Modifier.padding(10.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = point.timeFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8)
                  )
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = "${point.temp.toInt()}°",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(6.dp))
                      .background(Color(0xFF243447))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = "UV ${point.uv.toInt()}",
                      style = MaterialTheme.typography.labelSmall,
                      color = Color(0xFFFDE047),
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))
        }
      }

      // 5. PRONÓSTICO 3 DÍAS
      if (weather.dailyForecast.isNotEmpty()) {
        item {
          Text(
            text = "Pronóstico para los Próximos Días",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(10.dp))
        }

        items(weather.dailyForecast) { day ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141F30)),
            shape = RoundedCornerShape(14.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = day.dateFormatted,
                  style = MaterialTheme.typography.titleMedium,
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Índice UV Máximo: ${day.uvMax.toInt()}",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color(0xFFFBBF24)
                )
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "${day.tempMin.toInt()}°C",
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color(0xFF38BDF8),
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = " / ",
                  color = Color(0xFF64748B)
                )
                Text(
                  text = "${day.tempMax.toInt()}°C",
                  style = MaterialTheme.typography.titleMedium,
                  color = Color(0xFFF87171),
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    }
  }
}
