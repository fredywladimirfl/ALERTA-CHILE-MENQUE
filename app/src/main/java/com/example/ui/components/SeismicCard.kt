package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EarthquakeItem
import com.example.data.model.TsunamiThreat
import com.example.ui.theme.AlertEmergencyRed
import com.example.ui.theme.AlertWarningOrange

@Composable
fun SeismicCard(
  earthquake: EarthquakeItem,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  val magColor = when {
    earthquake.magnitude >= 6.5 -> AlertEmergencyRed
    earthquake.magnitude >= 5.5 -> Color(0xFFEA580C)
    earthquake.magnitude >= 4.5 -> AlertWarningOrange
    earthquake.magnitude >= 3.5 -> Color(0xFF0284C7)
    else -> Color(0xFF059669)
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, Color(0xFF26374F), RoundedCornerShape(16.dp))
      .testTag("seismic_card_${earthquake.id}"),
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
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Magnitude Circular Badge
        Box(
          modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(magColor)
            .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = String.format("%.1f", earthquake.magnitude),
              style = MaterialTheme.typography.titleLarge,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp
            )
            Text(
              text = "Mw",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.85f),
              fontSize = 9.sp
            )
          }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = earthquake.place,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(2.dp))

          Row(
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = null,
              tint = Color(0xFF94A3B8),
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "Región ${earthquake.chileRegion}",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF94A3B8)
            )
            Text(
              text = " • ${earthquake.formattedTime}",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF38BDF8),
              fontWeight = FontWeight.Medium
            )
            if (earthquake.distanceKmFromUser != null) {
              Text(
                text = " • A ${earthquake.distanceKmFromUser.toInt()} km (GPS)",
                style = MaterialTheme.typography.bodySmall,
                color = if (earthquake.distanceKmFromUser < 100.0) Color(0xFFFBBF24) else Color(0xFF94A3B8),
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Technical metrics row: Depth, Mercalli, Tsunami status
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0xFF0F172A))
          .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "PROFUNDIDAD",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF64748B),
            fontSize = 9.sp
          )
          Text(
            text = "${earthquake.depthKm} km",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.Bold
          )
        }

        Column {
          Text(
            text = "INTENSIDAD MERCALLI",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF64748B),
            fontSize = 9.sp
          )
          Text(
            text = earthquake.estimatedIntensityMercalli,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFFBBF24),
            fontWeight = FontWeight.Bold
          )
        }

        // Map button
        OutlinedButton(
          onClick = {
            val geoUri = Uri.parse("geo:${earthquake.latitude},${earthquake.longitude}?q=${earthquake.latitude},${earthquake.longitude}(${Uri.encode(earthquake.place)})")
            val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
            context.startActivity(mapIntent)
          },
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color(0xFF38BDF8)
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("map_button_${earthquake.id}")
        ) {
          Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Mapa", fontSize = 12.sp)
        }
      }

      // Tsunami Threat Chip if evaluated
      Spacer(modifier = Modifier.height(8.dp))
      val isTsunamiWarning = earthquake.tsunamiThreat != TsunamiThreat.NO_AMENAZA
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(6.dp))
          .background(if (isTsunamiWarning) AlertEmergencyRed.copy(alpha = 0.2f) else Color(0xFF064E3B).copy(alpha = 0.4f))
          .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Waves,
          contentDescription = null,
          tint = if (isTsunamiWarning) AlertEmergencyRed else Color(0xFF34D399),
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "SHOA: ${earthquake.tsunamiThreat.description}",
          style = MaterialTheme.typography.labelSmall,
          color = if (isTsunamiWarning) Color(0xFFFCA5A5) else Color(0xFFA7F3D0),
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}
