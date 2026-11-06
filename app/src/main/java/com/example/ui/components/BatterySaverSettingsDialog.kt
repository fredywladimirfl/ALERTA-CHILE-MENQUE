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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ChileBlue
import com.example.util.BatteryEmergencyProfile
import com.example.util.DeviceBatteryState

@Composable
fun BatterySaverSettingsDialog(
  batteryState: DeviceBatteryState,
  onSetAutoAdaptive: (Boolean) -> Unit,
  onSetManualProfile: (BatteryEmergencyProfile?) -> Unit,
  onSetSimulatedBattery: (Int, Boolean) -> Unit,
  onManualGpsRefresh: () -> Unit,
  onDismiss: () -> Unit
) {
  val level = batteryState.levelPercent
  val effective = batteryState.effectiveProfile
  val isCharging = batteryState.isCharging

  val levelColor = when {
    isCharging -> Color(0xFF10B981)
    level <= 15 -> Color(0xFFEF4444)
    level <= 35 -> Color(0xFFF59E0B)
    else -> Color(0xFF10B981)
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = ChileBlue),
        modifier = Modifier.testTag("close_battery_dialog_btn")
      ) {
        Text("Aceptar")
      }
    },
    dismissButton = {
      TextButton(
        onClick = {
          onSetManualProfile(null)
          onSetAutoAdaptive(true)
        },
        modifier = Modifier.testTag("reset_battery_auto_btn")
      ) {
        Text("Restablecer Automático", color = Color(0xFF94A3B8))
      }
    },
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .background(levelColor.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = when {
                isCharging -> Icons.Default.BatteryChargingFull
                level <= 15 -> Icons.Default.BatteryAlert
                level <= 35 -> Icons.Default.BatterySaver
                else -> Icons.Default.BatteryFull
              },
              contentDescription = null,
              tint = levelColor,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Gestión de Batería de Emergencia",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
            Text(
              text = "Ajuste dinámico de GPS y detalle de mapa",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF94A3B8),
              fontSize = 11.sp
            )
          }
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        // Battery Telemetry Meter Card
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, levelColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .testTag("battery_telemetry_card"),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "$level%",
                  fontSize = 26.sp,
                  fontWeight = FontWeight.Black,
                  color = levelColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (isCharging) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(Color(0xFF10B981).copy(alpha = 0.2f))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = "CARGANDO ⚡",
                      color = Color(0xFF34D399),
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "Temp: ${String.format("%.1f", batteryState.temperatureCelsius)}°C",
                  fontSize = 11.sp,
                  color = Color(0xFFCBD5E1)
                )
                Text(
                  text = if (batteryState.isPowerSaveMode) "Ahorro SO: ACTIVO" else "Ahorro SO: Inactivo",
                  fontSize = 10.sp,
                  color = if (batteryState.isPowerSaveMode) Color(0xFFFBBF24) else Color(0xFF64748B)
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
              progress = { (level / 100f).coerceIn(0f, 1f) },
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
              color = levelColor,
              trackColor = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Perfil activo: ${effective.displayName}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = levelColor
              )
              Text(
                text = "GPS: ${effective.gpsIntervalMillis / 1000}s",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Automatic Adaptation Toggle
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF141F30)),
          shape = RoundedCornerShape(12.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Ajuste Automático Inteligente",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "Ajusta la frecuencia del GPS y el detalle visual del mapa según el nivel real de batería.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
              checked = batteryState.isAutoAdaptiveEnabled && batteryState.manualProfileOverride == null,
              onCheckedChange = { checked ->
                if (checked) {
                  onSetManualProfile(null)
                  onSetAutoAdaptive(true)
                } else {
                  onSetAutoAdaptive(false)
                }
              },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ChileBlue
              ),
              modifier = Modifier.testTag("toggle_auto_battery_saver")
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Seleccionar Perfil de Consumo:",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = Color(0xFFE2E8F0)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Profiles list
        BatteryEmergencyProfile.values().forEach { profile ->
          val isSelected = effective == profile
          val profileColor = when (profile) {
            BatteryEmergencyProfile.NORMAL -> Color(0xFF10B981)
            BatteryEmergencyProfile.AHORRO_EQUILIBRADO -> Color(0xFFF59E0B)
            BatteryEmergencyProfile.AHORRO_CRITICO -> Color(0xFFEF4444)
          }

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
              .clip(RoundedCornerShape(10.dp))
              .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) profileColor else Color(0xFF1E293B),
                shape = RoundedCornerShape(10.dp)
              )
              .clickable { onSetManualProfile(profile) }
              .testTag("btn_profile_${profile.id}"),
            colors = CardDefaults.cardColors(
              containerColor = if (isSelected) profileColor.copy(alpha = 0.12f) else Color(0xFF111827)
            )
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = isSelected,
                onClick = { onSetManualProfile(profile) },
                colors = RadioButtonDefaults.colors(selectedColor = profileColor)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = profile.displayName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(profileColor.copy(alpha = 0.2f))
                      .padding(horizontal = 5.dp, vertical = 1.dp)
                  ) {
                    Text(
                      text = profile.shortLabel,
                      fontSize = 9.sp,
                      color = profileColor,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = profile.description,
                  fontSize = 11.sp,
                  color = Color(0xFF94A3B8)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Simulation Bar for Testing & Demonstration
        Text(
          text = "Simular Nivel de Batería (Prueba Rápida):",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFFEF4444).copy(alpha = 0.2f))
              .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
              .clickable { onSetSimulatedBattery(12, false) }
              .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            Text("12% Crítico", color = Color(0xFFFCA5A5), fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
              .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
              .clickable { onSetSimulatedBattery(28, false) }
              .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            Text("28% Medio", color = Color(0xFFFDE68A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF10B981).copy(alpha = 0.2f))
              .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
              .clickable { onSetSimulatedBattery(90, false) }
              .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            Text("90% Lleno", color = Color(0xFF6EE7B7), fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(ChileBlue.copy(alpha = 0.25f))
              .border(1.dp, ChileBlue, RoundedCornerShape(8.dp))
              .clickable { onSetSimulatedBattery(100, true) }
              .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            Text("⚡ Cargar", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Manual GPS on-demand refresh
        OutlinedButton(
          onClick = onManualGpsRefresh,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("dialog_manual_gps_refresh_btn"),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
          shape = RoundedCornerShape(8.dp)
        ) {
          Icon(Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Solicitar Ping GPS Inmediato Ahora", fontSize = 12.sp)
        }
      }
    }
  )
}
