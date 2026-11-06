package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.EmergencyTab
import com.example.ui.MainEmergencyViewModel
import com.example.ui.components.SaeAlertModal
import com.example.ui.screens.AlertsFeedScreen
import com.example.ui.screens.BiobioEmergencyMapScreen
import com.example.ui.screens.EmergencyToolsScreen
import com.example.ui.screens.ProtocolsScreen
import com.example.ui.screens.SeismicMonitorScreen
import com.example.ui.screens.SurvivalKitScreen
import com.example.ui.screens.WeatherScreen
import com.example.ui.theme.AlertEmergencyRed
import com.example.ui.theme.ChileBlue
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: MainEmergencyViewModel = viewModel()
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()
      MyApplicationTheme(darkTheme = uiState.isDarkTheme) {
        MainEmergencyApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun MainEmergencyApp(
  viewModel: MainEmergencyViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val survivalItems by viewModel.survivalItems.collectAsStateWithLifecycle()
  val sosContacts by viewModel.sosContacts.collectAsStateWithLifecycle()
  var preventionSubTab by remember { mutableIntStateOf(0) } // 0: Mochila, 1: Guías

  // Permission launcher for runtime location & notification permissions
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    ) {
      viewModel.startRealTimeGpsTracking()
    }
  }

  LaunchedEffect(Unit) {
    val perms = mutableListOf(
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.ACCESS_COARSE_LOCATION
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      perms.add(Manifest.permission.POST_NOTIFICATIONS)
    }
    permissionLauncher.launch(perms.toTypedArray())
  }

  // Handle back press to return to main Alertas feed
  BackHandler(enabled = uiState.currentTab != EmergencyTab.ALERTAS) {
    viewModel.setTab(EmergencyTab.ALERTAS)
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .windowInsetsPadding(WindowInsets.statusBars),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .background(if (uiState.isDarkTheme) Color(0xFF10B981) else Color(0xFF0039A6), CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "ALERTA CHILE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Biobío • Emergencias en Tiempo Real",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Battery Status Indicator (Click jumps to Map/Battery control)
            val bLevel = uiState.batteryState.levelPercent
            val bEff = uiState.batteryState.effectiveProfile
            val isCritical = bEff == com.example.util.BatteryEmergencyProfile.AHORRO_CRITICO
            val isBalanced = bEff == com.example.util.BatteryEmergencyProfile.AHORRO_EQUILIBRADO
            val bColor = when {
              uiState.batteryState.isCharging -> Color(0xFF10B981)
              isCritical -> Color(0xFFEF4444)
              isBalanced -> Color(0xFFF59E0B)
              else -> if (uiState.isDarkTheme) Color(0xFF6EE7B7) else Color(0xFF047857)
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(bColor.copy(alpha = 0.15f))
                .clickable { viewModel.setTab(EmergencyTab.MAPA) }
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .testTag("main_top_battery_indicator")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(
                  imageVector = when {
                    uiState.batteryState.isCharging -> Icons.Default.BatteryChargingFull
                    isCritical -> Icons.Default.BatteryAlert
                    isBalanced -> Icons.Default.BatterySaver
                    else -> Icons.Default.BatteryFull
                  },
                  contentDescription = "Batería $bLevel%",
                  tint = bColor,
                  modifier = Modifier.size(15.dp)
                )
                Text(
                  text = "$bLevel%",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = bColor
                )
              }
            }

            // Top Theme Switcher Pill Button (Light / Dark Mode)
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                  if (uiState.isDarkTheme) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                )
                .clickable { viewModel.toggleTheme() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("theme_switcher_top_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
              ) {
                Icon(
                  imageVector = if (uiState.isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                  contentDescription = if (uiState.isDarkTheme) "Cambiar a Modo Claro" else "Cambiar a Modo Oscuro",
                  tint = if (uiState.isDarkTheme) Color(0xFFFBBF24) else Color(0xFFD97706),
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = if (uiState.isDarkTheme) "Noche" else "Día",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (uiState.isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF0F172A)
                )
              }
            }
          }
        }
      }
    },
    bottomBar = {
      NavigationBar(
        modifier = Modifier
          .windowInsetsPadding(WindowInsets.navigationBars)
          .testTag("main_navigation_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
      ) {
        val navItems = listOf(
          Triple(EmergencyTab.ALERTAS, "Alertas", Icons.Default.NotificationsActive),
          Triple(EmergencyTab.MAPA, "Mapa Biobío", Icons.Default.Map),
          Triple(EmergencyTab.TIEMPO_UV, "Tiempo", Icons.Default.WbSunny),
          Triple(EmergencyTab.SISMOS, "Sismos", Icons.Default.Waves),
          Triple(EmergencyTab.HERRAMIENTAS_SOS, "Kit SOS", Icons.Default.Campaign)
        )

        navItems.forEach { (tab, label, icon) ->
          val isSelected = uiState.currentTab == tab
          NavigationBarItem(
            selected = isSelected,
            onClick = { viewModel.setTab(tab) },
            icon = {
              Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp)
              )
            },
            label = {
              Text(
                text = label,
                fontSize = 10.sp,
                maxLines = 1
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Color.White,
              selectedTextColor = MaterialTheme.colorScheme.onSurface,
              indicatorColor = if (tab == EmergencyTab.HERRAMIENTAS_SOS) AlertEmergencyRed else ChileBlue,
              unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
              unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (uiState.currentTab) {
        EmergencyTab.ALERTAS -> {
          AlertsFeedScreen(
            uiState = uiState,
            onRegionSelected = { viewModel.setRegion(it) },
            onToggleNearbyGps = { viewModel.toggleNearbyGpsFilter() },
            onRefresh = { viewModel.refresh() },
            onTriggerSaeSimulation = { viewModel.triggerSaeSimulation() },
            onNavigateToWeather = { viewModel.setTab(EmergencyTab.TIEMPO_UV) },
            onNavigateToMap = { viewModel.setTab(EmergencyTab.MAPA) },
            onNavigateToSos = { viewModel.setTab(EmergencyTab.HERRAMIENTAS_SOS) },
            onToggleOfflineMode = { viewModel.toggleOfflineMode() },
            onResetCache = { viewModel.resetAlertsCache() }
          )
        }
        EmergencyTab.MAPA -> {
          BiobioEmergencyMapScreen(
            uiState = uiState,
            onTriggerSaeSimulation = { viewModel.triggerSaeSimulation() }
          )
        }
        EmergencyTab.TIEMPO_UV -> {
          WeatherScreen(
            uiState = uiState,
            onRegionSelected = { viewModel.setRegion(it) },
            onRefresh = { viewModel.loadWeatherForCurrentSelection() }
          )
        }
        EmergencyTab.SISMOS -> {
          SeismicMonitorScreen(
            uiState = uiState,
            onMinMagSelected = { viewModel.setMinMagnitudeFilter(it) },
            onToggleNearbyGps = { viewModel.toggleNearbyGpsFilter() },
            onRefresh = { viewModel.refresh() }
          )
        }
        EmergencyTab.HERRAMIENTAS_SOS -> {
          EmergencyToolsScreen(
            uiState = uiState,
            sosContacts = sosContacts,
            onToggleSiren = { viewModel.toggleSiren(it) },
            onStopSiren = { viewModel.stopSiren() },
            onToggleTorch = { viewModel.toggleTorch(it) },
            sosMessageText = viewModel.getSosMessageText(),
            safeStatusMessageText = viewModel.getSafeStatusMessageText(),
            onToggleTheme = { viewModel.toggleTheme() },
            onSendSosSms = { viewModel.sendSosSmsToContacts(it) },
            onAddSosContact = { name, phone, rel -> viewModel.addSosContact(name, phone, rel) },
            onToggleSosContact = { viewModel.toggleSosContact(it) },
            onDeleteSosContact = { viewModel.deleteSosContact(it) },
            onResetSosContacts = { viewModel.resetSosContacts() }
          )
        }
        EmergencyTab.PREVENCION -> {
          Column(modifier = Modifier.fillMaxSize()) {
            // Prevention Subtab Selector (Mochila 72h vs Guías de Actuación)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF141F30))
                .padding(4.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (preventionSubTab == 0) ChileBlue else Color.Transparent)
                  .clickable { preventionSubTab = 0 }
                  .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Backpack,
                    contentDescription = null,
                    tint = if (preventionSubTab == 0) Color.White else Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                  )
                  Text(
                    text = " Mochila 72h",
                    color = if (preventionSubTab == 0) Color.White else Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                  )
                }
              }

              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (preventionSubTab == 1) ChileBlue else Color.Transparent)
                  .clickable { preventionSubTab = 1 }
                  .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = if (preventionSubTab == 1) Color.White else Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                  )
                  Text(
                    text = " Guías SENAPRED",
                    color = if (preventionSubTab == 1) Color.White else Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                  )
                }
              }
            }

            if (preventionSubTab == 0) {
              SurvivalKitScreen(
                items = survivalItems,
                onToggleItem = { viewModel.toggleSurvivalItem(it) },
                onAddItem = { title, cat -> viewModel.addCustomSurvivalItem(title, cat) },
                onReset = { viewModel.resetSurvivalKit() }
              )
            } else {
              ProtocolsScreen()
            }
          }
        }
      }

      // Display SAE Alert Modal when triggered
      uiState.activeSaeAlert?.let { saeAlert ->
        SaeAlertModal(
          alert = saeAlert,
          isSirenPlaying = uiState.isSirenPlaying,
          onSilenceSiren = { viewModel.stopSiren() },
          onDismiss = { viewModel.dismissSaeAlert() }
        )
      }
    }
  }
}
