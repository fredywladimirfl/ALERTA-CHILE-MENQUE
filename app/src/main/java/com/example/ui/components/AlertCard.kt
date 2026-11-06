package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flood
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Volcano
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertCategory
import com.example.data.model.AlertItem
import com.example.data.model.AlertSeverity
import com.example.ui.theme.AlertEmergencyRed
import com.example.ui.theme.AlertInfoBlue
import com.example.ui.theme.AlertWarningOrange

@Composable
fun AlertCard(
  alert: AlertItem,
  modifier: Modifier = Modifier
) {
  var isExpanded by remember { mutableStateOf(false) }

  val severityColor = when (alert.severity) {
    AlertSeverity.ROJA -> AlertEmergencyRed
    AlertSeverity.AMARILLA -> AlertWarningOrange
    AlertSeverity.TEMPRANA_PREVENTIVA -> AlertInfoBlue
    AlertSeverity.INFORMATIVA -> Color(0xFF64748B)
  }

  val categoryIcon: ImageVector = when (alert.category) {
    AlertCategory.INCENDIO_FORESTAL -> Icons.Default.LocalFireDepartment
    AlertCategory.METEOROLOGICA -> Icons.Default.Thunderstorm
    AlertCategory.TSUNAMI -> Icons.Default.Waves
    AlertCategory.SISMO -> Icons.Default.Warning
    AlertCategory.VOLCANICA -> Icons.Default.Volcano
    AlertCategory.ALUVION -> Icons.Default.Flood
    AlertCategory.SAE_BROADCAST -> Icons.Default.Warning
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, severityColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
      .clickable { isExpanded = !isExpanded }
      .testTag("alert_card_${alert.id}"),
    colors = CardDefaults.cardColors(
      containerColor = Color(0xFF141F30)
    ),
    shape = RoundedCornerShape(16.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      // Top header: Severity Badge, Region, and Time
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(severityColor)
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = alert.severity.displayName.uppercase(),
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF243447))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "Región ${alert.region}",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFFE2E8F0),
              fontWeight = FontWeight.Medium
            )
          }

          if (alert.distanceKmFromUser != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (alert.distanceKmFromUser < 30.0) Color(0xFFDC2626).copy(alpha = 0.25f) else Color(0xFF0369A1).copy(alpha = 0.25f))
                .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
              Text(
                text = if (alert.distanceKmFromUser < 5.0) "📍 En tu sector" else "📍 A ${alert.distanceKmFromUser.toInt()} km",
                style = MaterialTheme.typography.labelSmall,
                color = if (alert.distanceKmFromUser < 30.0) Color(0xFFFCA5A5) else Color(0xFF7DD3FC),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
              )
            }
          }
        }

        Text(
          text = alert.timestampFormatted,
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFF94A3B8)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Title with category icon
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .background(severityColor.copy(alpha = 0.2f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = categoryIcon,
            contentDescription = null,
            tint = severityColor,
            modifier = Modifier.size(22.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = alert.title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "Fuente oficial: ${alert.source}",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF38BDF8)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = alert.description,
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFFCBD5E1)
      )

      // Instructions expandable section
      if (alert.instructions.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isExpanded) "Ocultar recomendaciones" else "¿Qué hacer? Ver recomendaciones (${alert.instructions.size})",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFFFBBF24),
            fontWeight = FontWeight.SemiBold
          )
          Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = Color(0xFFFBBF24)
          )
        }

        AnimatedVisibility(visible = isExpanded) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 8.dp)
              .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
              .padding(12.dp)
          ) {
            alert.instructions.forEachIndexed { i, step ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 3.dp),
                verticalAlignment = Alignment.Top
              ) {
                Text(
                  text = "•",
                  color = Color(0xFFFBBF24),
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.width(14.dp)
                )
                Text(
                  text = step,
                  style = MaterialTheme.typography.bodySmall,
                  color = Color(0xFFE2E8F0)
                )
              }
            }
          }
        }
      }
    }
  }
}
