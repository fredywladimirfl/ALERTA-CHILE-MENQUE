package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChileEmergencyData
import com.example.data.model.AlertCategory
import com.example.data.model.AlertItem
import com.example.data.model.AlertSeverity
import com.example.data.model.EarthquakeItem
import com.example.ui.EmergencyUiState
import com.example.ui.components.BatterySaverSettingsDialog
import com.example.ui.theme.AlertEmergencyRed
import com.example.ui.theme.AlertWarningOrange
import com.example.ui.theme.ChileBlue
import com.example.ui.theme.ChileRedLight
import com.example.util.BatteryEmergencyProfile
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

data class MapAlertFilters(
  val showSeismic: Boolean = true,
  val showWildfires: Boolean = true,
  val showWeatherWarnings: Boolean = true,
  val showSaeAlerts: Boolean = true,
  val showCoastalThreats: Boolean = true,
  val onlyBiobioRegion: Boolean = false
) {
  val activeCount: Int
    get() = (if (showSeismic) 1 else 0) +
      (if (showWildfires) 1 else 0) +
      (if (showWeatherWarnings) 1 else 0) +
      (if (showSaeAlerts) 1 else 0) +
      (if (showCoastalThreats) 1 else 0)

  val allActive: Boolean
    get() = showSeismic && showWildfires && showWeatherWarnings && showSaeAlerts && showCoastalThreats

  val noneActive: Boolean
    get() = !showSeismic && !showWildfires && !showWeatherWarnings && !showSaeAlerts && !showCoastalThreats
}

sealed class SelectedMapEntity {
  data class Alert(val alert: AlertItem) : SelectedMapEntity()
  data class Earthquake(val eq: EarthquakeItem) : SelectedMapEntity()
}

@Composable
fun BiobioEmergencyMapScreen(
  uiState: EmergencyUiState,
  onTriggerSaeSimulation: () -> Unit,
  onSetAutoBatteryAdaptive: (Boolean) -> Unit = {},
  onSetManualBatteryProfile: (BatteryEmergencyProfile?) -> Unit = {},
  onSetSimulatedBattery: (Int, Boolean) -> Unit = { _, _ -> },
  onManualRefreshGps: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  val batteryState = uiState.batteryState
  val effectiveProfile = batteryState.effectiveProfile
  val isCriticalBattery = effectiveProfile == BatteryEmergencyProfile.AHORRO_CRITICO
  val isBalancedBattery = effectiveProfile == BatteryEmergencyProfile.AHORRO_EQUILIBRADO

  var showBatteryDialog by remember { mutableStateOf(false) }

  // Center of Biobío Region: Concepción
  val biobioCenter = remember { LatLng(ChileEmergencyData.DEFAULT_BIOBIO_LAT, ChileEmergencyData.DEFAULT_BIOBIO_LON) }
  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(biobioCenter, 9.5f)
  }

  var currentMapType by remember { mutableStateOf(MapType.NORMAL) }
  var isFilterPanelExpanded by remember { mutableStateOf(false) }
  var alertFilters by remember { mutableStateOf(MapAlertFilters()) }
  var selectedEntity by remember { mutableStateOf<SelectedMapEntity?>(null) }

  val mapUiSettings = remember {
    MapUiSettings(
      zoomControlsEnabled = false,
      myLocationButtonEnabled = false,
      mapToolbarEnabled = false,
      compassEnabled = true
    )
  }

  val mapProperties = remember(currentMapType) {
    MapProperties(
      mapType = currentMapType,
      isMyLocationEnabled = false
    )
  }

  // Count emergencies per category for badges
  val seismicCount = remember(uiState.earthquakes, alertFilters.onlyBiobioRegion) {
    if (alertFilters.onlyBiobioRegion) {
      uiState.earthquakes.count { it.chileRegion.contains("Biobío", ignoreCase = true) }
    } else {
      uiState.earthquakes.size
    }
  }

  val wildfireCount = remember(uiState.officialAlerts, alertFilters.onlyBiobioRegion) {
    uiState.officialAlerts.count {
      it.category == AlertCategory.INCENDIO_FORESTAL &&
        (!alertFilters.onlyBiobioRegion || it.region.contains("Biobío", ignoreCase = true))
    }
  }

  val weatherCount = remember(uiState.officialAlerts, alertFilters.onlyBiobioRegion) {
    uiState.officialAlerts.count {
      (it.category == AlertCategory.METEOROLOGICA || it.category == AlertCategory.ALUVION || it.category == AlertCategory.VOLCANICA) &&
        (!alertFilters.onlyBiobioRegion || it.region.contains("Biobío", ignoreCase = true))
    }
  }

  val saeCount = remember(uiState.officialAlerts, alertFilters.onlyBiobioRegion) {
    uiState.officialAlerts.count {
      (it.isSaeBroadcast || it.severity == AlertSeverity.ROJA) &&
        (!alertFilters.onlyBiobioRegion || it.region.contains("Biobío", ignoreCase = true))
    }
  }

  val coastalCount = remember(uiState.officialAlerts, alertFilters.onlyBiobioRegion) {
    uiState.officialAlerts.count {
      it.category == AlertCategory.TSUNAMI &&
        (!alertFilters.onlyBiobioRegion || it.region.contains("Biobío", ignoreCase = true))
    }
  }

  // Reactive filtering of official alerts with battery-aware detail shedding
  val filteredAlerts = remember(uiState.officialAlerts, alertFilters, effectiveProfile) {
    uiState.officialAlerts.filter { alert ->
      if (alert.latitude == null || alert.longitude == null) return@filter false

      if (alertFilters.onlyBiobioRegion && !alert.region.contains("Biobío", ignoreCase = true)) {
        return@filter false
      }

      // During critical battery emergency, prioritize only extreme threats (SAE / Red / Wildfire / Tsunami)
      if (isCriticalBattery) {
        val isEssential = alert.isSaeBroadcast ||
          alert.severity == AlertSeverity.ROJA ||
          alert.category == AlertCategory.INCENDIO_FORESTAL ||
          alert.category == AlertCategory.TSUNAMI
        if (!isEssential) return@filter false
      }

      val matchesSae = alertFilters.showSaeAlerts && (alert.isSaeBroadcast || alert.severity == AlertSeverity.ROJA)
      val matchesWildfire = alertFilters.showWildfires && alert.category == AlertCategory.INCENDIO_FORESTAL
      val matchesWeather = alertFilters.showWeatherWarnings && (
        alert.category == AlertCategory.METEOROLOGICA ||
          alert.category == AlertCategory.ALUVION ||
          alert.category == AlertCategory.VOLCANICA
        )
      val matchesCoastal = alertFilters.showCoastalThreats && alert.category == AlertCategory.TSUNAMI

      matchesSae || matchesWildfire || matchesWeather || matchesCoastal
    }
  }

  // Reactive filtering of earthquakes with dynamic magnitude threshold based on battery profile
  val filteredEarthquakes = remember(uiState.earthquakes, alertFilters, effectiveProfile) {
    if (!alertFilters.showSeismic) {
      emptyList()
    } else {
      uiState.earthquakes.filter { eq ->
        val matchesRegion = !alertFilters.onlyBiobioRegion || eq.chileRegion.contains("Biobío", ignoreCase = true)
        val matchesMag = eq.magnitude >= effectiveProfile.minEarthquakeMagnitudeOnMap
        matchesRegion && matchesMag
      }
    }
  }

  Box(modifier = modifier.fillMaxSize().testTag("biobio_map_container")) {
    // 1. GOOGLE MAP COMPOSABLE
    GoogleMap(
      modifier = Modifier.fillMaxSize(),
      cameraPositionState = cameraPositionState,
      properties = mapProperties,
      uiSettings = mapUiSettings,
      onMapClick = {
        selectedEntity = null
        if (isFilterPanelExpanded) isFilterPanelExpanded = false
      }
    ) {
      // User GPS Location Marker & Accuracy Circle
      val userGps = LatLng(uiState.userLatitude, uiState.userLongitude)
      Marker(
        state = MarkerState(position = userGps),
        title = "Tu Ubicación GPS: ${uiState.detectedUserCommune}",
        snippet = "Región del Biobío",
        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
        onClick = {
          selectedEntity = null
          false
        }
      )
      Circle(
        center = userGps,
        radius = (uiState.gpsAccuracyMeters ?: 40f).toDouble().coerceAtLeast(30.0),
        fillColor = Color(0x330284C7),
        strokeColor = Color(0xFF0284C7),
        strokeWidth = 3f
      )

      // Alert Markers & Hazard Perimeters
      filteredAlerts.forEach { alert ->
        val pos = LatLng(alert.latitude!!, alert.longitude!!)
        val isSaeOrRoja = alert.severity == AlertSeverity.ROJA || alert.isSaeBroadcast

        val hue = when {
          isSaeOrRoja -> BitmapDescriptorFactory.HUE_RED
          alert.severity == AlertSeverity.AMARILLA -> BitmapDescriptorFactory.HUE_ORANGE
          alert.category == AlertCategory.INCENDIO_FORESTAL -> BitmapDescriptorFactory.HUE_ORANGE
          alert.category == AlertCategory.TSUNAMI -> BitmapDescriptorFactory.HUE_CYAN
          else -> BitmapDescriptorFactory.HUE_YELLOW
        }

        Marker(
          state = MarkerState(position = pos),
          title = alert.title,
          snippet = "${alert.severity.displayName} • ${alert.commune ?: alert.region}",
          icon = BitmapDescriptorFactory.defaultMarker(hue),
          onClick = {
            selectedEntity = SelectedMapEntity.Alert(alert)
            coroutineScope.launch {
              cameraPositionState.animate(CameraUpdateFactory.newLatLng(pos), 500)
            }
            true
          }
        )

        // Circle perimeter for red alert or wildfire
        if (alert.category == AlertCategory.INCENDIO_FORESTAL || isSaeOrRoja) {
          Circle(
            center = pos,
            radius = if (isSaeOrRoja) 6000.0 else 4000.0,
            fillColor = if (isSaeOrRoja) Color(0x33DC2626) else Color(0x28F97316),
            strokeColor = if (isSaeOrRoja) Color(0xFFDC2626) else Color(0xFFF97316),
            strokeWidth = 3f
          )
        }
      }

      // Seismic Markers
      filteredEarthquakes.forEach { eq ->
        val eqPos = LatLng(eq.latitude, eq.longitude)
        val magHue = when {
          eq.magnitude >= 6.0 -> BitmapDescriptorFactory.HUE_ROSE
          eq.magnitude >= 5.0 -> BitmapDescriptorFactory.HUE_ORANGE
          eq.magnitude >= 4.0 -> BitmapDescriptorFactory.HUE_YELLOW
          else -> BitmapDescriptorFactory.HUE_CYAN
        }

        Marker(
          state = MarkerState(position = eqPos),
          title = "M ${eq.magnitude} - ${eq.place}",
          snippet = "Profundidad: ${eq.depthKm} km • ${eq.formattedTime}",
          icon = BitmapDescriptorFactory.defaultMarker(magHue),
          onClick = {
            selectedEntity = SelectedMapEntity.Earthquake(eq)
            coroutineScope.launch {
              cameraPositionState.animate(CameraUpdateFactory.newLatLng(eqPos), 500)
            }
            true
          }
        )

        // Circle wave indicator for earthquake epicenter
        Circle(
          center = eqPos,
          radius = (eq.magnitude * 2500.0).coerceAtLeast(3000.0),
          fillColor = Color(0x22F43F5E),
          strokeColor = Color(0x88F43F5E),
          strokeWidth = 2f
        )
      }
    }

    // 2. TOP UI FILTER PANEL & CONTROL BAR (Situated above the map)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.TopCenter)
        .padding(top = 10.dp)
        .testTag("top_filter_panel_container")
    ) {
      // Header Card with Layer Toggle & Filter Panel Opener
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp)
          .clip(RoundedCornerShape(16.dp))
          .border(1.dp, Color(0xFF26374F), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xF20C131F))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .background(ChileBlue, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Map,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Mapa de Emergencias Biobío",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
              Text(
                text = "${filteredAlerts.size + filteredEarthquakes.size} alertas visibles en mapa",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF38BDF8),
                fontSize = 11.sp
              )
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            // Battery Status & Adaptive Power HUD Chip
            val batLevel = batteryState.levelPercent
            val batColor = when {
              batteryState.isCharging -> Color(0xFF10B981)
              isCriticalBattery -> Color(0xFFEF4444)
              isBalancedBattery -> Color(0xFFF59E0B)
              else -> Color(0xFF10B981)
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(batColor.copy(alpha = 0.15f))
                .border(1.dp, batColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .clickable { showBatteryDialog = true }
                .padding(horizontal = 7.dp, vertical = 6.dp)
                .testTag("battery_hud_chip")
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = when {
                    batteryState.isCharging -> Icons.Default.BatteryChargingFull
                    isCriticalBattery -> Icons.Default.BatteryAlert
                    isBalancedBattery -> Icons.Default.BatterySaver
                    else -> Icons.Default.BatteryFull
                  },
                  contentDescription = "Estado de Batería",
                  tint = batColor,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "$batLevel%",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = batColor
                )
              }
            }

            // Layer switch button (Normal / Satelite / Terreno)
            OutlinedButton(
              onClick = {
                currentMapType = when (currentMapType) {
                  MapType.NORMAL -> MapType.SATELLITE
                  MapType.SATELLITE -> MapType.TERRAIN
                  else -> MapType.NORMAL
                }
              },
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE2E8F0)),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              modifier = Modifier.testTag("map_layer_toggle_btn")
            ) {
              Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(15.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = when (currentMapType) {
                  MapType.NORMAL -> "Satélite"
                  MapType.SATELLITE -> "Terreno"
                  else -> "Mapa"
                },
                fontSize = 11.sp
              )
            }

            // Main Filter Panel Toggle Button
            Button(
              onClick = { isFilterPanelExpanded = !isFilterPanelExpanded },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isFilterPanelExpanded) ChileBlue else Color(0xFF1E293B)
              ),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
              modifier = Modifier.testTag("toggle_filter_panel_btn")
            ) {
              Icon(
                imageVector = if (isFilterPanelExpanded) Icons.Default.ExpandLess else Icons.Default.Tune,
                contentDescription = "Filtros de Mapa",
                modifier = Modifier.size(16.dp),
                tint = if (isFilterPanelExpanded) Color.White else Color(0xFF38BDF8)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isFilterPanelExpanded) "Cerrar" else "Filtros (${alertFilters.activeCount}/5)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFilterPanelExpanded) Color.White else Color(0xFFE2E8F0)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Quick Horizontal Filter Chips Row (1-Tap Toggle directly above the map)
      LazyRow(
        contentPadding = PaddingValues(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.testTag("map_quick_filter_chips_row")
      ) {
        // Quick "Todos" toggle chip
        item {
          val allOn = alertFilters.allActive
          FilterChip(
            selected = allOn,
            onClick = {
              alertFilters = if (allOn) {
                alertFilters.copy(
                  showSeismic = false,
                  showWildfires = false,
                  showWeatherWarnings = false,
                  showSaeAlerts = false,
                  showCoastalThreats = false
                )
              } else {
                alertFilters.copy(
                  showSeismic = true,
                  showWildfires = true,
                  showWeatherWarnings = true,
                  showSaeAlerts = true,
                  showCoastalThreats = true
                )
              }
            },
            leadingIcon = {
              Icon(
                imageVector = if (allOn) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
              )
            },
            label = { Text("Todas", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = ChileBlue,
              selectedLabelColor = Color.White,
              selectedLeadingIconColor = Color.White,
              containerColor = Color(0xF00F172A),
              labelColor = Color(0xFFCBD5E1)
            ),
            modifier = Modifier.testTag("quick_filter_chip_all")
          )
        }

        // Seismic Activity Toggle Chip
        item {
          FilterChip(
            selected = alertFilters.showSeismic,
            onClick = { alertFilters = alertFilters.copy(showSeismic = !alertFilters.showSeismic) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Waves,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
              )
            },
            label = {
              Text(
                text = "Sismos ($seismicCount)",
                fontSize = 11.sp,
                fontWeight = if (alertFilters.showSeismic) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = Color(0xFFE11D48),
              selectedLabelColor = Color.White,
              selectedLeadingIconColor = Color.White,
              containerColor = Color(0xF00F172A),
              labelColor = Color(0xFFCBD5E1)
            ),
            modifier = Modifier.testTag("quick_filter_chip_seismic")
          )
        }

        // Wildfires Toggle Chip
        item {
          FilterChip(
            selected = alertFilters.showWildfires,
            onClick = { alertFilters = alertFilters.copy(showWildfires = !alertFilters.showWildfires) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
              )
            },
            label = {
              Text(
                text = "Incendios ($wildfireCount)",
                fontSize = 11.sp,
                fontWeight = if (alertFilters.showWildfires) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = Color(0xFFEA580C),
              selectedLabelColor = Color.White,
              selectedLeadingIconColor = Color.White,
              containerColor = Color(0xF00F172A),
              labelColor = Color(0xFFCBD5E1)
            ),
            modifier = Modifier.testTag("quick_filter_chip_wildfires")
          )
        }

        // Weather Warnings Toggle Chip
        item {
          FilterChip(
            selected = alertFilters.showWeatherWarnings,
            onClick = { alertFilters = alertFilters.copy(showWeatherWarnings = !alertFilters.showWeatherWarnings) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.CloudQueue,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
              )
            },
            label = {
              Text(
                text = "Tiempo ($weatherCount)",
                fontSize = 11.sp,
                fontWeight = if (alertFilters.showWeatherWarnings) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = Color(0xFF0284C7),
              selectedLabelColor = Color.White,
              selectedLeadingIconColor = Color.White,
              containerColor = Color(0xF00F172A),
              labelColor = Color(0xFFCBD5E1)
            ),
            modifier = Modifier.testTag("quick_filter_chip_weather")
          )
        }

        // SAE Evacuation Toggle Chip
        item {
          FilterChip(
            selected = alertFilters.showSaeAlerts,
            onClick = { alertFilters = alertFilters.copy(showSaeAlerts = !alertFilters.showSaeAlerts) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
              )
            },
            label = {
              Text(
                text = "SAE ($saeCount)",
                fontSize = 11.sp,
                fontWeight = if (alertFilters.showSaeAlerts) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = AlertEmergencyRed,
              selectedLabelColor = Color.White,
              selectedLeadingIconColor = Color.White,
              containerColor = Color(0xF00F172A),
              labelColor = Color(0xFFCBD5E1)
            ),
            modifier = Modifier.testTag("quick_filter_chip_sae")
          )
        }

        // Coastal / Tsunami Toggle Chip
        item {
          FilterChip(
            selected = alertFilters.showCoastalThreats,
            onClick = { alertFilters = alertFilters.copy(showCoastalThreats = !alertFilters.showCoastalThreats) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
              )
            },
            label = {
              Text(
                text = "Costa ($coastalCount)",
                fontSize = 11.sp,
                fontWeight = if (alertFilters.showCoastalThreats) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = Color(0xFF0D9488),
              selectedLabelColor = Color.White,
              selectedLeadingIconColor = Color.White,
              containerColor = Color(0xF00F172A),
              labelColor = Color(0xFFCBD5E1)
            ),
            modifier = Modifier.testTag("quick_filter_chip_coastal")
          )
        }
      }

      // Battery Emergency Adaptation Banner (Pulsing HUD notification)
      AnimatedVisibility(visible = isCriticalBattery || isBalancedBattery) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(
              width = 1.dp,
              color = if (isCriticalBattery) Color(0xFFEF4444) else Color(0xFFF59E0B),
              shape = RoundedCornerShape(10.dp)
            )
            .clickable { showBatteryDialog = true }
            .testTag("battery_adaptation_banner"),
          colors = CardDefaults.cardColors(
            containerColor = if (isCriticalBattery) Color(0xF2450A0A) else Color(0xF2451A03)
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Icon(
                imageVector = if (batteryState.isCharging) Icons.Default.BatteryChargingFull else if (isCriticalBattery) Icons.Default.BatteryAlert else Icons.Default.BatterySaver,
                contentDescription = null,
                tint = if (isCriticalBattery) Color(0xFFFCA5A5) else Color(0xFFFDE68A),
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isCriticalBattery) {
                  "⚡ Modo Crítico (${batteryState.levelPercent}%): GPS cada 2m • Sismos ≥ M5.0"
                } else {
                  "🔋 Ahorro Inteligente (${batteryState.levelPercent}%): GPS cada 30s • Filtro M3.5+"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCriticalBattery) Color(0xFFFECACA) else Color(0xFFFEF3C7)
              )
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isCriticalBattery) Color(0xFFDC2626) else Color(0xFFD97706))
                .clickable { onManualRefreshGps() }
                .padding(horizontal = 7.dp, vertical = 3.dp)
                .testTag("manual_gps_ping_chip")
            ) {
              Text(
                text = "GPS Ahora",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }
      }

      // 3. EXPANDABLE DETAILED FILTER PANEL (Smooth Material 3 overlay)
      AnimatedVisibility(
        visible = isFilterPanelExpanded,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
      ) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .testTag("detailed_filter_panel_card"),
          colors = CardDefaults.cardColors(containerColor = Color(0xF80A101D))
        ) {
          Column(
            modifier = Modifier
              .padding(14.dp)
              .verticalScroll(rememberScrollState())
          ) {
            // Panel Header with Action Shortcuts
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .background(Color(0xFF0284C7).copy(alpha = 0.25f), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.FilterAlt,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(16.dp)
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Filtros de Emergencias",
                  style = MaterialTheme.typography.titleSmall,
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
              }

              Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                  onClick = {
                    alertFilters = alertFilters.copy(
                      showSeismic = true,
                      showWildfires = true,
                      showWeatherWarnings = true,
                      showSaeAlerts = true,
                      showCoastalThreats = true
                    )
                  },
                  contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text("Activar Todo", fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                }

                TextButton(
                  onClick = {
                    alertFilters = alertFilters.copy(
                      showSeismic = false,
                      showWildfires = false,
                      showWeatherWarnings = false,
                      showSaeAlerts = false,
                      showCoastalThreats = false
                    )
                  },
                  contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text("Limpiar", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }

                IconButton(
                  onClick = { isFilterPanelExpanded = false },
                  modifier = Modifier.size(28.dp)
                ) {
                  Icon(Icons.Default.Close, contentDescription = "Cerrar Panel", tint = Color(0xFF94A3B8))
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Switch 1: Actividad Sísmica
            FilterSwitchRow(
              icon = Icons.Default.Waves,
              iconBgColor = Color(0xFFE11D48),
              title = "Actividad Sísmica (Sismos CSN / USGS)",
              subtitle = "Epicentros, magnitud Mw y profundidad en tiempo real ($seismicCount activos)",
              checked = alertFilters.showSeismic,
              onCheckedChange = { alertFilters = alertFilters.copy(showSeismic = it) },
              testTag = "filter_switch_seismic"
            )

            // Switch 2: Incendios Forestales
            FilterSwitchRow(
              icon = Icons.Default.LocalFireDepartment,
              iconBgColor = Color(0xFFEA580C),
              title = "Incendios Forestales (CONAF)",
              subtitle = "Perímetros y zonas de riesgo de propagación activa ($wildfireCount activos)",
              checked = alertFilters.showWildfires,
              onCheckedChange = { alertFilters = alertFilters.copy(showWildfires = it) },
              testTag = "filter_switch_wildfires"
            )

            // Switch 3: Avisos Meteorológicos
            FilterSwitchRow(
              icon = Icons.Default.CloudQueue,
              iconBgColor = Color(0xFF0284C7),
              title = "Avisos y Alertas Meteorológicas (DMC)",
              subtitle = "Vientos extremos, temporales y crecida de ríos ($weatherCount activos)",
              checked = alertFilters.showWeatherWarnings,
              onCheckedChange = { alertFilters = alertFilters.copy(showWeatherWarnings = it) },
              testTag = "filter_switch_weather"
            )

            // Switch 4: Alertas SAE / Evacuación
            FilterSwitchRow(
              icon = Icons.Default.Campaign,
              iconBgColor = AlertEmergencyRed,
              title = "Alertas SAE / Alerta Roja (SENAPRED)",
              subtitle = "Evacuación inmediata y mensajes del Estado ($saeCount activos)",
              checked = alertFilters.showSaeAlerts,
              onCheckedChange = { alertFilters = alertFilters.copy(showSaeAlerts = it) },
              testTag = "filter_switch_sae"
            )

            // Switch 5: Borde Costero / Tsunami
            FilterSwitchRow(
              icon = Icons.Default.Security,
              iconBgColor = Color(0xFF0D9488),
              title = "Borde Costero y Tsunami (SHOA)",
              subtitle = "Estados de precaución y vigilancia costera ($coastalCount activos)",
              checked = alertFilters.showCoastalThreats,
              onCheckedChange = { alertFilters = alertFilters.copy(showCoastalThreats = it) },
              testTag = "filter_switch_coastal"
            )

            HorizontalDivider(
              modifier = Modifier.padding(vertical = 8.dp),
              color = Color(0xFF1E293B)
            )

            // Geographical Switch: Solo Biobío
            FilterSwitchRow(
              icon = Icons.Default.NearMe,
              iconBgColor = Color(0xFF10B981),
              title = "Solo Región del Biobío",
              subtitle = "Excluir alertas de otras regiones del país (Concepción, Arauco, Biobío)",
              checked = alertFilters.onlyBiobioRegion,
              onCheckedChange = { alertFilters = alertFilters.copy(onlyBiobioRegion = it) },
              testTag = "filter_switch_biobio_only"
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Mostrando ${filteredAlerts.size + filteredEarthquakes.size} incidentes en el mapa",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
              )

              Button(
                onClick = { isFilterPanelExpanded = false },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Text("Listo", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }

    // 4. EMPTY STATE NOTICE (If user turned off all filters)
    if (alertFilters.noneActive) {
      Card(
        modifier = Modifier
          .align(Alignment.Center)
          .padding(24.dp)
          .clip(RoundedCornerShape(16.dp))
          .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(16.dp))
          .testTag("all_filters_disabled_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xF20F172A))
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(
            imageVector = Icons.Default.VisibilityOff,
            contentDescription = null,
            tint = Color(0xFFFBBF24),
            modifier = Modifier.size(36.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Todos los filtros están desactivados",
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Activa al menos una categoría en el panel superior para visualizar sismos, incendios o alertas en el mapa.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(vertical = 6.dp)
          )
          Button(
            onClick = {
              alertFilters = alertFilters.copy(
                showSeismic = true,
                showWildfires = true,
                showWeatherWarnings = true,
                showSaeAlerts = true,
                showCoastalThreats = true
              )
            },
            colors = ButtonDefaults.buttonColors(containerColor = ChileBlue),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("enable_all_filters_btn")
          ) {
            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Activar Todos los Filtros", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // 5. RIGHT FLOATING CONTROLS: "Centrar en Biobío" & "Mi Ubicación GPS"
    Column(
      modifier = Modifier
        .align(Alignment.CenterEnd)
        .padding(end = 14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Center Biobío
      FloatingActionButton(
        onClick = {
          coroutineScope.launch {
            cameraPositionState.animate(
              CameraUpdateFactory.newLatLngZoom(biobioCenter, 9.5f),
              700
            )
          }
        },
        containerColor = Color(0xF00F172A),
        contentColor = Color(0xFF38BDF8),
        shape = CircleShape,
        modifier = Modifier
          .size(46.dp)
          .testTag("center_biobio_fab")
      ) {
        Icon(Icons.Default.Navigation, contentDescription = "Centrar en Región del Biobío", modifier = Modifier.size(22.dp))
      }

      // Center My GPS
      FloatingActionButton(
        onClick = {
          val myGps = LatLng(uiState.userLatitude, uiState.userLongitude)
          coroutineScope.launch {
            cameraPositionState.animate(
              CameraUpdateFactory.newLatLngZoom(myGps, 13f),
              700
            )
          }
        },
        containerColor = Color(0xF00369A1),
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier
          .size(46.dp)
          .testTag("my_gps_location_fab")
      ) {
        Icon(Icons.Default.MyLocation, contentDescription = "Ir a mi ubicación GPS", modifier = Modifier.size(22.dp))
      }

      // Manual GPS on-demand refresh FAB (Especially in low-power throttled mode)
      FloatingActionButton(
        onClick = onManualRefreshGps,
        containerColor = if (isCriticalBattery) Color(0xFFDC2626) else Color(0xF00D9488),
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier
          .size(46.dp)
          .testTag("manual_gps_refresh_btn")
      ) {
        Icon(Icons.Default.GpsFixed, contentDescription = "Actualizar posición GPS", modifier = Modifier.size(20.dp))
      }
    }

    // 6. BOTTOM SELECTED ENTITY DETAIL CARD (Appears when any marker is selected)
    AnimatedVisibility(
      visible = selectedEntity != null,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      enter = slideInVertically { it },
      exit = slideOutVertically { it }
    ) {
      selectedEntity?.let { entity ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(20.dp))
            .testTag("map_marker_detail_card"),
          colors = CardDefaults.cardColors(containerColor = Color(0xF50C131F))
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            when (entity) {
              is SelectedMapEntity.Alert -> {
                val alert = entity.alert
                val isRedOrSae = alert.severity == AlertSeverity.ROJA || alert.isSaeBroadcast

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isRedOrSae) AlertEmergencyRed else AlertWarningOrange)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                      Text(
                        text = alert.severity.displayName.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                      )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = alert.commune ?: alert.region,
                      style = MaterialTheme.typography.labelSmall,
                      color = Color(0xFFE2E8F0)
                    )
                  }

                  IconButton(
                    onClick = { selectedEntity = null },
                    modifier = Modifier.size(28.dp)
                  ) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color(0xFF94A3B8))
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                  text = alert.title,
                  style = MaterialTheme.typography.titleMedium,
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )

                Text(
                  text = alert.description,
                  style = MaterialTheme.typography.bodySmall,
                  color = Color(0xFFCBD5E1),
                  maxLines = 2
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = if (alert.distanceKmFromUser != null)
                      "📍 A ${alert.distanceKmFromUser.toInt()} km de tu GPS"
                    else "📍 Región del Biobío",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFFBBF24),
                    fontWeight = FontWeight.Bold
                  )

                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isRedOrSae) {
                      Button(
                        onClick = onTriggerSaeSimulation,
                        colors = ButtonDefaults.buttonColors(containerColor = AlertEmergencyRed),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                      ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Alarma SAE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                      }
                    }

                    OutlinedButton(
                      onClick = {
                        val geoUri = Uri.parse("geo:${alert.latitude},${alert.longitude}?q=${alert.latitude},${alert.longitude}(${Uri.encode(alert.title)})")
                        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
                        context.startActivity(mapIntent)
                      },
                      colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                      shape = RoundedCornerShape(8.dp),
                      contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                      Text("Ruta / GPS", fontSize = 11.sp)
                    }
                  }
                }
              }

              is SelectedMapEntity.Earthquake -> {
                val eq = entity.eq
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE11D48))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                      Text(
                        text = "SISMO Mw ${eq.magnitude}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                      )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = eq.formattedTime,
                      style = MaterialTheme.typography.bodySmall,
                      color = Color(0xFF38BDF8)
                    )
                  }

                  IconButton(
                    onClick = { selectedEntity = null },
                    modifier = Modifier.size(28.dp)
                  ) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color(0xFF94A3B8))
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                  text = eq.place,
                  style = MaterialTheme.typography.titleMedium,
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )

                Text(
                  text = "Profundidad: ${eq.depthKm} km • Intensidad: ${eq.estimatedIntensityMercalli} • SHOA: ${eq.tsunamiThreat.description}",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color(0xFFCBD5E1)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = if (eq.distanceKmFromUser != null)
                      "📍 A ${eq.distanceKmFromUser.toInt()} km de tu GPS"
                    else "📍 Epicentro",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFFBBF24),
                    fontWeight = FontWeight.Bold
                  )

                  OutlinedButton(
                    onClick = {
                      val geoUri = Uri.parse("geo:${eq.latitude},${eq.longitude}?q=${eq.latitude},${eq.longitude}(${Uri.encode(eq.place)})")
                      val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
                      context.startActivity(mapIntent)
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                  ) {
                    Text("Abrir en Maps", fontSize = 11.sp)
                  }
                }
              }
            }
          }
        }
      }
    }

    // 7. BATTERY SAVER EMERGENCY SETTINGS DIALOG
    if (showBatteryDialog) {
      BatterySaverSettingsDialog(
        batteryState = batteryState,
        onSetAutoAdaptive = onSetAutoBatteryAdaptive,
        onSetManualProfile = onSetManualBatteryProfile,
        onSetSimulatedBattery = onSetSimulatedBattery,
        onManualGpsRefresh = onManualRefreshGps,
        onDismiss = { showBatteryDialog = false }
      )
    }
  }
}

@Composable
private fun FilterSwitchRow(
  icon: ImageVector,
  iconBgColor: Color,
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  testTag: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable { onCheckedChange(!checked) }
      .padding(vertical = 6.dp, horizontal = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(34.dp)
          .background(iconBgColor.copy(alpha = if (checked) 0.9f else 0.3f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (checked) Color.White else Color(0xFF94A3B8),
          modifier = Modifier.size(18.dp)
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyMedium,
          color = if (checked) Color.White else Color(0xFF94A3B8),
          fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal,
          fontSize = 13.sp
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFF64748B),
          fontSize = 11.sp,
          maxLines = 1
        )
      }
    }

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      modifier = Modifier.testTag(testTag),
      colors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = iconBgColor,
        uncheckedThumbColor = Color(0xFF94A3B8),
        uncheckedTrackColor = Color(0xFF1E293B)
      )
    )
  }
}
