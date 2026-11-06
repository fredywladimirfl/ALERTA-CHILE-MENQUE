package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WifiOff
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ChileEmergencyData
import com.example.ui.EmergencyUiState
import com.example.ui.components.AlertCard
import com.example.ui.components.WeatherUvBannerCard
import com.example.ui.theme.ChileRedDark
import com.example.ui.theme.ChileRedLight

@Composable
fun AlertsFeedScreen(
  uiState: EmergencyUiState,
  onRegionSelected: (String) -> Unit,
  onToggleNearbyGps: () -> Unit,
  onRefresh: () -> Unit,
  onTriggerSaeSimulation: () -> Unit,
  onNavigateToWeather: () -> Unit,
  onNavigateToMap: () -> Unit,
  onNavigateToSos: () -> Unit = {},
  onToggleOfflineMode: () -> Unit = {},
  onResetCache: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "gps_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "gps_scale"
  )

  val filteredAlerts = uiState.officialAlerts.filter { alert ->
    val matchesNearby = !uiState.onlyNearbyGps || (alert.distanceKmFromUser != null && alert.distanceKmFromUser <= 150.0)
    val matchesRegion = uiState.onlyNearbyGps || uiState.selectedRegion == "Todas las Regiones" || alert.region.contains(uiState.selectedRegion, ignoreCase = true)
    val matchesSearch = uiState.searchQuery.isBlank() ||
      alert.title.contains(uiState.searchQuery, ignoreCase = true) ||
      alert.description.contains(uiState.searchQuery, ignoreCase = true)
    matchesNearby && matchesRegion && matchesSearch
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("alerts_feed_list"),
    contentPadding = PaddingValues(bottom = 96.dp)
  ) {
    // Top Hero Banner with visual asset
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(190.dp)
      ) {
        Image(
          painter = painterResource(id = R.drawable.chile_emergency_hero_1793876721245),
          contentDescription = "Chile Alerta Emergencias Banner",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color.Transparent,
                  Color(0xCC0C131F),
                  Color(0xFF0C131F)
                )
              )
            )
        )

        Column(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(16.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .background(Color(0xFF10B981), CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "GPS ACTIVO • REGIÓN DEL BIOBÍO",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF34D399),
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "Centro de Alertas de Emergencia",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
          )

          Text(
            text = "Monitoreo SENAPRED, SHOA y CSN en tu posición GPS",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFCBD5E1)
          )
        }
      }
    }

    // REAL-TIME GPS TELEMETRY CARD (Centered on Biobío / Current GPS)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .clip(RoundedCornerShape(16.dp))
          .border(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
          .testTag("realtime_gps_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E33))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Blinking GPS icon
          Box(
            modifier = Modifier
              .size(40.dp)
              .background(Color(0xFF0369A1).copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Box(
              modifier = Modifier
                .size(14.dp)
                .scale(pulseScale)
                .background(Color(0xFF38BDF8), CircleShape)
            )
            Icon(
              imageVector = Icons.Default.GpsFixed,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Tu Ubicación GPS (Tiempo Real)",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF38BDF8),
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(Color(0xFF10B981).copy(alpha = 0.2f))
                  .padding(horizontal = 5.dp, vertical = 1.dp)
              ) {
                Text(
                  text = "EN VIVO",
                  color = Color(0xFF34D399),
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
              text = "${uiState.detectedUserCommune}, ${uiState.detectedUserRegion}",
              style = MaterialTheme.typography.titleMedium,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )

            Text(
              text = "Coord: ${String.format("%.4f", uiState.userLatitude)}, ${String.format("%.4f", uiState.userLongitude)} • Precisión: ±${uiState.gpsAccuracyMeters?.toInt() ?: 8}m",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF94A3B8),
              fontSize = 11.sp
            )
          }

          Spacer(modifier = Modifier.width(6.dp))

          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFDC2626).copy(alpha = 0.25f))
                .clickable { onNavigateToSos() }
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .testTag("feed_sos_quick_btn")
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Campaign,
                  contentDescription = null,
                  tint = Color(0xFFEF4444),
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "SOS",
                  color = Color(0xFFEF4444),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0284C7).copy(alpha = 0.2f))
                .clickable { onNavigateToMap() }
                .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Map,
                  contentDescription = null,
                  tint = Color(0xFF38BDF8),
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "MAPA",
                  color = Color(0xFF38BDF8),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    }

    // Room Database Local Cache & Offline Resilience Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 5.dp)
          .clip(RoundedCornerShape(14.dp))
          .border(
            width = 1.dp,
            color = if (uiState.isOfflineMode) Color(0xFFF59E0B) else Color(0xFF10B981).copy(alpha = 0.4f),
            shape = RoundedCornerShape(14.dp)
          )
          .testTag("room_cache_card"),
        colors = CardDefaults.cardColors(
          containerColor = if (uiState.isOfflineMode) Color(0xFF291C08) else Color(0xFF0F231D)
        )
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .background(
                    if (uiState.isOfflineMode) Color(0xFFF59E0B).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.2f),
                    CircleShape
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (uiState.isOfflineMode) Icons.Default.WifiOff else Icons.Default.Storage,
                  contentDescription = null,
                  tint = if (uiState.isOfflineMode) Color(0xFFFBBF24) else Color(0xFF34D399),
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = if (uiState.isOfflineMode) "MODO SIN CONEXIÓN (OFFLINE)" else "CACHÉ LOCAL ROOM ACTIVO",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (uiState.isOfflineMode) Color(0xFFFBBF24) else Color(0xFF34D399),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(
                        if (uiState.isOfflineMode) Color(0xFFF59E0B).copy(alpha = 0.25f) else Color(0xFF10B981).copy(alpha = 0.25f)
                      )
                      .padding(horizontal = 5.dp, vertical = 1.dp)
                  ) {
                    Text(
                      text = "${uiState.cachedAlertsCount} ALERTAS",
                      color = if (uiState.isOfflineMode) Color(0xFFFDE68A) else Color(0xFF6EE7B7),
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
                Text(
                  text = if (uiState.isOfflineMode)
                    "Visualizando datos desde base de datos SQLite local"
                  else
                    "Último respaldo en disco: ${uiState.lastCacheSyncFormatted ?: "Automático"}",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color(0xFF94A3B8),
                  fontSize = 11.sp
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(
                  if (uiState.isOfflineMode) Color(0xFFF59E0B).copy(alpha = 0.25f) else Color(0xFF334155).copy(alpha = 0.6f)
                )
                .clickable { onToggleOfflineMode() }
                .padding(vertical = 7.dp, horizontal = 10.dp)
                .testTag("toggle_offline_mode_btn"),
              contentAlignment = Alignment.Center
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (uiState.isOfflineMode) Icons.Default.CloudDone else Icons.Default.CloudOff,
                  contentDescription = null,
                  tint = if (uiState.isOfflineMode) Color(0xFFFBBF24) else Color(0xFFCBD5E1),
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (uiState.isOfflineMode) "Restaurar Red Online" else "Simular Corte de Red",
                  color = if (uiState.isOfflineMode) Color(0xFFFBBF24) else Color(0xFFE2E8F0),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF334155).copy(alpha = 0.6f))
                .clickable { onResetCache() }
                .padding(vertical = 7.dp, horizontal = 10.dp)
                .testTag("reset_room_cache_btn"),
              contentAlignment = Alignment.Center
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Refresh,
                  contentDescription = "Restablecer Base de Datos",
                  tint = Color(0xFF94A3B8),
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Restablecer",
                  color = Color(0xFFCBD5E1),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }
      }
    }

    // Weather and UV Banner
    item {
      Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        WeatherUvBannerCard(
          weatherInfo = uiState.weatherInfo,
          onClick = onNavigateToWeather
        )
      }
    }

    // SAE Alert Simulator Action Card (Centered on Biobío)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .clip(RoundedCornerShape(16.dp))
          .border(2.dp, ChileRedLight, RoundedCornerShape(16.dp))
          .clickable { onTriggerSaeSimulation() }
          .testTag("sae_simulator_card"),
        colors = CardDefaults.cardColors(
          containerColor = ChileRedDark.copy(alpha = 0.45f)
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .background(ChileRedLight, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CellTower,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Simulador Alerta SAE Biobío",
              style = MaterialTheme.typography.titleMedium,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Probar recepción sonora de emergencia para la Región del Biobío",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFFFFD1D1)
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(ChileRedLight)
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Text(
              text = "PROBAR",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // Regional Filter Chips and GPS Filter
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Filtrar por Ubicación / Región:",
            style = MaterialTheme.typography.titleSmall,
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.SemiBold
          )

          IconButton(
            onClick = onRefresh,
            modifier = Modifier
              .size(32.dp)
              .testTag("refresh_alerts_btn")
          ) {
            if (uiState.isRefreshing) {
              CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = Color(0xFF38BDF8),
                strokeWidth = 2.dp
              )
            } else {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Actualizar alertas",
                tint = Color(0xFF38BDF8)
              )
            }
          }
        }

        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.testTag("region_chip_row")
        ) {
          // Dedicated Nearby GPS Chip
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
                  text = "Cercanos a mi GPS (< 150 km)",
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

          items(ChileEmergencyData.CHILE_REGIONS) { region ->
            val isSelected = !uiState.onlyNearbyGps && uiState.selectedRegion == region
            FilterChip(
              selected = isSelected,
              onClick = {
                if (uiState.onlyNearbyGps) onToggleNearbyGps()
                onRegionSelected(region)
              },
              label = {
                Text(
                  text = if (region == "Biobío") "⭐ Biobío (Principal)" else region,
                  fontSize = 12.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              },
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
    }

    // Section title & count with active filter indicator
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = if (uiState.onlyNearbyGps) "Alertas Cercanas a tu GPS (${filteredAlerts.size})"
                   else "Alertas en ${uiState.selectedRegion} (${filteredAlerts.size})",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Calculadas en tiempo real según tu posición",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8),
            fontSize = 11.sp
          )
        }
      }
    }

    // List of alerts
    if (filteredAlerts.isEmpty()) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = Color(0xFF10B981),
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "Sin alertas activas en esta área",
              style = MaterialTheme.typography.titleMedium,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (uiState.onlyNearbyGps) "No hay alertas a menos de 150 km de tu ubicación GPS."
                     else "No hay avisos de emergencia vigentes para ${uiState.selectedRegion}.",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF94A3B8)
            )
          }
        }
      }
    } else {
      items(filteredAlerts, key = { it.id }) { alert ->
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
          AlertCard(alert = alert)
        }
      }
    }
  }
}
