package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WindPower
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.data.model.UvLevel
import com.example.data.model.WeatherInfo
import com.example.ui.theme.AlertEmergencyRed
import com.example.ui.theme.AlertWarningOrange

@Composable
fun WeatherUvBannerCard(
  weatherInfo: WeatherInfo?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (weatherInfo == null) return

  val uvColor = Color(weatherInfo.uvLevel.colorHex)

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .border(1.dp, Color(0xFF26374F), RoundedCornerShape(18.dp))
      .clickable { onClick() }
      .testTag("weather_uv_banner_card"),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF141F30)),
    shape = RoundedCornerShape(18.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.horizontalGradient(
            colors = listOf(
              Color(0xFF141F30),
              Color(0xFF1A2A42)
            )
          )
        )
        .padding(16.dp)
    ) {
      // Top row: Location and Condition badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.WbSunny,
            contentDescription = null,
            tint = Color(0xFFFBBF24),
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${weatherInfo.cityName} • Región ${weatherInfo.regionName}",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.SemiBold
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "Detalles",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF38BDF8)
          )
          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF38BDF8),
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Main content: Temp & UV Index side by side
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Temperature
        Column {
          Row(verticalAlignment = Alignment.Bottom) {
            Text(
              text = "${weatherInfo.temperature.toInt()}°",
              style = MaterialTheme.typography.displayMedium,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 38.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.padding(bottom = 6.dp)) {
              Text(
                text = weatherInfo.conditionDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFCBD5E1),
                fontWeight = FontWeight.Medium
              )
              Text(
                text = "ST ${weatherInfo.apparentTemperature.toInt()}° • Min ${weatherInfo.tempMin.toInt()}° / Max ${weatherInfo.tempMax.toInt()}°",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
              )
            }
          }
        }

        // UV Index Badge
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(uvColor.copy(alpha = 0.2f))
            .border(1.5.dp, uvColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "RADIACIÓN UV",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFFCBD5E1),
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = String.format("%.1f", weatherInfo.uvIndex),
              style = MaterialTheme.typography.titleLarge,
              color = uvColor,
              fontWeight = FontWeight.Bold,
              fontSize = 22.sp
            )
            Text(
              text = weatherInfo.uvLevel.displayName.uppercase(),
              style = MaterialTheme.typography.labelSmall,
              color = uvColor,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp
            )
          }
        }
      }

      // Wildfire 30-30-30 Alert Banner if triggered
      if (weatherInfo.isFireRisk30_30_30) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AlertEmergencyRed.copy(alpha = 0.25f))
            .border(1.dp, AlertEmergencyRed, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.LocalFireDepartment,
            contentDescription = null,
            tint = AlertEmergencyRed,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "ALERTA CONAF 30-30-30: Alto riesgo crítico de propagación de incendios.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFFCA5A5),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}
