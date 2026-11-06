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
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EarthquakeItem
import com.example.data.model.TsunamiThreat
import com.example.ui.EmergencyUiState
import com.example.ui.components.SeismicCard
import com.example.ui.theme.AlertEmergencyRed
import com.example.ui.theme.AlertWarningOrange

@Composable
fun SeismicMonitorScreen(
  uiState: EmergencyUiState,
  onMinMagSelected: (Double) -> Unit,
  onToggleNearbyGps: () -> Unit,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier
) {
  val filteredEqs = uiState.earthquakes.filter { eq ->
    val matchesMag = eq.magnitude >= uiState.minMagnitudeFilter
    val matchesNearby = !uiState.onlyNearbyGps || (eq.distanceKmFromUser != null && eq.distanceKmFromUser <= 150.0)
    val matchesRegion = uiState.onlyNearbyGps || uiState.selectedRegion == "Todas las Regiones" || eq.chileRegion.contains(uiState.selectedRegion, ignoreCase = true)
    matchesMag && matchesNearby && matchesRegion
  }.sortedBy { if (uiState.onlyNearbyGps) (it.distanceKmFromUser ?: 9999.0) else -it.timeMillis.toDouble() }

  val maxMagEq = uiState.earthquakes.maxByOrNull { it.magnitude }
  val hasTsunamiAlert = uiState.earthquakes.any { it.tsunamiThreat != TsunamiThreat.NO_AMENAZA }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("seismic_monitor_list"),
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
                .background(Color(0xFF38BDF8), CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "RED SÍSMICA NACIONAL",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF38BDF8),
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
          }
          Text(
            text = "Monitoreo Sísmico de Chile",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
        }

        IconButton(
          onClick = onRefresh,
          modifier = Modifier.testTag("refresh_seismic_btn")
        ) {
          if (uiState.isRefreshing) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              color = Color(0xFF38BDF8),
              strokeWidth = 2.dp
            )
          } else {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Actualizar sismos",
              tint = Color(0xFF38BDF8)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
    }

    // Telemetry Stat Cards Row
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Total sismos
        Card(
          modifier = Modifier.weight(1f),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF141F30)),
          shape = RoundedCornerShape(14.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "SISMOS (24H)",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF94A3B8),
              fontSize = 9.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "${uiState.earthquakes.size}",
              style = MaterialTheme.typography.headlineSmall,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "En territorio chileno",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF64748B),
              fontSize = 10.sp
            )
          }
        }

        // Mayor Magnitud
        Card(
          modifier = Modifier.weight(1f),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF141F30)),
          shape = RoundedCornerShape(14.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "MAYOR SISMO",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF94A3B8),
              fontSize = 9.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (maxMagEq != null) "M ${maxMagEq.magnitude}" else "--",
              style = MaterialTheme.typography.headlineSmall,
              color = if ((maxMagEq?.magnitude ?: 0.0) >= 5.0) AlertWarningOrange else Color(0xFF38BDF8),
              fontWeight = FontWeight.Bold
            )
            Text(
              text = maxMagEq?.chileRegion ?: "Sin eventos",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF64748B),
              fontSize = 10.sp,
              maxLines = 1
            )
          }
        }

        // Tsunami status
        Card(
          modifier = Modifier.weight(1.2f),
          colors = CardDefaults.cardColors(
            containerColor = if (hasTsunamiAlert) AlertEmergencyRed.copy(alpha = 0.3f) else Color(0xFF0F261F)
          ),
          shape = RoundedCornerShape(14.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Waves,
                contentDescription = null,
                tint = if (hasTsunamiAlert) AlertEmergencyRed else Color(0xFF10B981),
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "ESTADO SHOA",
                style = MaterialTheme.typography.labelSmall,
                color = if (hasTsunamiAlert) AlertEmergencyRed else Color(0xFF10B981),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (hasTsunamiAlert) "ALERTA TSUNAMI" else "SIN AMENAZA",
              style = MaterialTheme.typography.titleSmall,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Costas de Chile",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF94A3B8),
              fontSize = 10.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
    }

    // Magnitude Filter Chips
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Filtrar por Magnitud mínima:",
          style = MaterialTheme.typography.titleSmall,
          color = Color(0xFFE2E8F0),
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.testTag("mag_filter_row")
        ) {
          item {
            FilterChip(
              selected = uiState.onlyNearbyGps,
              onClick = onToggleNearbyGps,
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.NearMe,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
              },
              label = {
                Text(
                  text = "Cercanos a mí (< 150 km)",
                  fontSize = 12.sp,
                  fontWeight = if (uiState.onlyNearbyGps) FontWeight.Bold else FontWeight.Normal
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFF0369A1),
                selectedLabelColor = Color.White,
                selectedLeadingIconColor = Color.White,
                containerColor = Color(0xFF141F30),
                labelColor = Color(0xFF38BDF8)
              ),
              border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = uiState.onlyNearbyGps,
                borderColor = if (uiState.onlyNearbyGps) Color(0xFF38BDF8) else Color(0xFF0284C7)
              )
            )
          }

          val filters = listOf(
            0.0 to "Todos",
            3.0 to "M ≥ 3.0",
            4.0 to "M ≥ 4.0",
            5.0 to "M ≥ 5.0"
          )
          items(filters) { (mag, label) ->
            val isSelected = uiState.minMagnitudeFilter == mag
            FilterChip(
              selected = isSelected,
              onClick = { onMinMagSelected(mag) },
              label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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
      }

      Spacer(modifier = Modifier.height(16.dp))
    }

    // List of Earthquakes
    if (filteredEqs.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF141F30)),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = Color(0xFF38BDF8),
              modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No hay sismos registrados con este filtro",
              style = MaterialTheme.typography.titleMedium,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Intente reducir la magnitud mínima o cambiar la región seleccionada.",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF94A3B8)
            )
          }
        }
      }
    } else {
      items(filteredEqs, key = { it.id }) { eq ->
        Box(modifier = Modifier.padding(vertical = 5.dp)) {
          SeismicCard(earthquake = eq)
        }
      }
    }
  }
}
