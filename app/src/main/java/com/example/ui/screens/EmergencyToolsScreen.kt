package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChileEmergencyData
import com.example.data.model.EmergencyContact
import com.example.data.model.SosContact
import com.example.ui.EmergencyUiState
import com.example.ui.SosSendResult
import com.example.ui.components.SosEmergencySection
import com.example.ui.theme.AlertEmergencyRed
import com.example.ui.theme.AlertSafeGreen
import com.example.ui.theme.AlertWarningOrange
import com.example.ui.theme.ChileRedDark
import com.example.ui.theme.ChileRedLight
import com.example.util.FlashlightMode
import com.example.util.SirenSoundMode

@Composable
fun EmergencyToolsScreen(
  uiState: EmergencyUiState,
  sosContacts: List<SosContact>,
  onToggleSiren: (SirenSoundMode) -> Unit,
  onStopSiren: () -> Unit,
  onToggleTorch: (FlashlightMode) -> Unit,
  sosMessageText: String,
  safeStatusMessageText: String,
  onSendSosSms: (Context) -> SosSendResult,
  onAddSosContact: (String, String, String) -> Unit,
  onToggleSosContact: (String) -> Unit,
  onDeleteSosContact: (String) -> Unit,
  onResetSosContacts: () -> Unit,
  onToggleTheme: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  val infiniteTransition = rememberInfiniteTransition(label = "siren_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.08f,
    animationSpec = infiniteRepeatable(
      animation = tween(450, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "siren_pulse_scale"
  )

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("emergency_tools_list"),
    contentPadding = PaddingValues(16.dp, bottom = 96.dp)
  ) {
    // Header
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(10.dp)
            .background(AlertEmergencyRed, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "HERRAMIENTAS SOS Y AUXILIO",
          style = MaterialTheme.typography.labelSmall,
          color = AlertEmergencyRed,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
      }
      Text(
        text = "Kit de Emergencia Inmediata",
        style = MaterialTheme.typography.headlineMedium,
        color = Color.White,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Sirena de pánico, linterna de rescate, reporte de ubicación y teléfonos de Chile",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF94A3B8)
      )

      Spacer(modifier = Modifier.height(16.dp))
    }

    // 1. BOTÓN SOS DE EMERGENCIA CON TRANSMISIÓN GPS POR SMS A CONTACTOS
    item {
      SosEmergencySection(
        uiState = uiState,
        sosContacts = sosContacts,
        onSendSosSms = onSendSosSms,
        onAddContact = onAddSosContact,
        onToggleContact = onToggleSosContact,
        onDeleteContact = onDeleteSosContact,
        onResetContacts = onResetSosContacts,
        sosMessageText = sosMessageText
      )

      Spacer(modifier = Modifier.height(16.dp))
    }

    // 2. SIRENA Y ALARMA SONORA DE SUPERVIVENCIA
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .border(
            2.dp,
            if (uiState.isSirenPlaying) AlertEmergencyRed else Color(0xFF26374F),
            RoundedCornerShape(18.dp)
          )
          .testTag("siren_tool_card"),
        colors = CardDefaults.cardColors(
          containerColor = if (uiState.isSirenPlaying) ChileRedDark.copy(alpha = 0.5f) else Color(0xFF141F30)
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .then(if (uiState.isSirenPlaying) Modifier.scale(pulseScale) else Modifier)
                  .background(if (uiState.isSirenPlaying) AlertEmergencyRed else Color(0xFF243447), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (uiState.isSirenPlaying) Icons.Default.Campaign else Icons.Default.VolumeUp,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(24.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Sirena Sonora / Pánico",
                  style = MaterialTheme.typography.titleMedium,
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = if (uiState.isSirenPlaying) "¡EMITIENDO ALARMA SONORA!" else "Señal acústica para ser ubicado por rescatistas",
                  style = MaterialTheme.typography.bodySmall,
                  color = if (uiState.isSirenPlaying) Color(0xFFFCA5A5) else Color(0xFF94A3B8)
                )
              }
            }

            if (uiState.isSirenPlaying) {
              Button(
                onClick = onStopSiren,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("stop_siren_btn")
              ) {
                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("DETENER", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // 3 Audio Mode Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // SAE Alarm
            val isSaeActive = uiState.isSirenPlaying && uiState.activeSirenMode == SirenSoundMode.SAE_ALARM
            Button(
              onClick = { onToggleSiren(SirenSoundMode.SAE_ALARM) },
              modifier = Modifier
                .weight(1f)
                .testTag("siren_sae_btn"),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isSaeActive) AlertEmergencyRed else Color(0xFF1E293B)
              ),
              shape = RoundedCornerShape(10.dp)
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = "Alarma SAE",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = Color.White
                )
                Text(
                  text = "Evacuación",
                  fontSize = 10.sp,
                  color = Color(0xFFCBD5E1)
                )
              }
            }

            // Silbato 2900Hz
            val isWhistleActive = uiState.isSirenPlaying && uiState.activeSirenMode == SirenSoundMode.SURVIVAL_WHISTLE
            Button(
              onClick = { onToggleSiren(SirenSoundMode.SURVIVAL_WHISTLE) },
              modifier = Modifier
                .weight(1f)
                .testTag("siren_whistle_btn"),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isWhistleActive) AlertWarningOrange else Color(0xFF1E293B)
              ),
              shape = RoundedCornerShape(10.dp)
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = "Silbato",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = Color.White
                )
                Text(
                  text = "2.9 kHz Agudo",
                  fontSize = 10.sp,
                  color = Color(0xFFCBD5E1)
                )
              }
            }

            // Morse SOS
            val isMorseActive = uiState.isSirenPlaying && uiState.activeSirenMode == SirenSoundMode.SOS_MORSE
            Button(
              onClick = { onToggleSiren(SirenSoundMode.SOS_MORSE) },
              modifier = Modifier
                .weight(1f)
                .testTag("siren_morse_btn"),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isMorseActive) Color(0xFF0284C7) else Color(0xFF1E293B)
              ),
              shape = RoundedCornerShape(10.dp)
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = "Morse S-O-S",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = Color.White
                )
                Text(
                  text = "... --- ...",
                  fontSize = 10.sp,
                  color = Color(0xFFCBD5E1)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
    }

    // 2. LINTERNA Y ESTROBOSCOPIO SOS
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .border(
            2.dp,
            if (uiState.isTorchActive) Color(0xFFEAB308) else Color(0xFF26374F),
            RoundedCornerShape(18.dp)
          )
          .testTag("torch_tool_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141F30))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .background(if (uiState.isTorchActive) Color(0xFFEAB308) else Color(0xFF243447), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (uiState.isTorchActive) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                contentDescription = null,
                tint = if (uiState.isTorchActive) Color.Black else Color.White,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Linterna de Rescate",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = if (uiState.isTorchActive) "Linterna Encendida (${uiState.activeTorchMode.name})" else "Iluminación de emergencia y destellos SOS",
                style = MaterialTheme.typography.bodySmall,
                color = if (uiState.isTorchActive) Color(0xFFFDE047) else Color(0xFF94A3B8)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            val isSteady = uiState.isTorchActive && uiState.activeTorchMode == FlashlightMode.STEADY
            Button(
              onClick = { onToggleTorch(FlashlightMode.STEADY) },
              modifier = Modifier
                .weight(1f)
                .testTag("torch_steady_btn"),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isSteady) Color(0xFFEAB308) else Color(0xFF1E293B),
                contentColor = if (isSteady) Color.Black else Color.White
              ),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Fija", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            val isStrobe = uiState.isTorchActive && uiState.activeTorchMode == FlashlightMode.STROBE
            Button(
              onClick = { onToggleTorch(FlashlightMode.STROBE) },
              modifier = Modifier
                .weight(1f)
                .testTag("torch_strobe_btn"),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isStrobe) Color(0xFFF97316) else Color(0xFF1E293B),
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Estrobo", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            val isSos = uiState.isTorchActive && uiState.activeTorchMode == FlashlightMode.SOS
            Button(
              onClick = { onToggleTorch(FlashlightMode.SOS) },
              modifier = Modifier
                .weight(1f)
                .testTag("torch_sos_btn"),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isSos) AlertEmergencyRed else Color(0xFF1E293B),
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("SOS Morse", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
    }

    // 3. TEMA DE PANTALLA: AHORRO DE BATERÍA & MODO NOCTURNO
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .border(
            1.dp,
            if (uiState.isDarkTheme) Color(0xFFFBBF24).copy(alpha = 0.5f) else Color(0xFF38BDF8).copy(alpha = 0.5f),
            RoundedCornerShape(18.dp)
          )
          .testTag("theme_battery_tool_card"),
        colors = CardDefaults.cardColors(
          containerColor = if (uiState.isDarkTheme) Color(0xFF1E1E2E) else Color(0xFFE2E8F0)
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              modifier = Modifier.weight(1f),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .background(
                    if (uiState.isDarkTheme) Color(0xFFFBBF24).copy(alpha = 0.2f) else Color(0xFF0284C7).copy(alpha = 0.2f),
                    CircleShape
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (uiState.isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                  contentDescription = null,
                  tint = if (uiState.isDarkTheme) Color(0xFFFBBF24) else Color(0xFF0284C7),
                  modifier = Modifier.size(24.dp)
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column {
                Text(
                  text = "Tema & Ahorro de Energía",
                  style = MaterialTheme.typography.titleMedium,
                  color = if (uiState.isDarkTheme) Color.White else Color(0xFF0F172A),
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = if (uiState.isDarkTheme)
                    "Modo Oscuro (Ahorro de batería en pantallas OLED)"
                  else
                    "Modo Claro (Máxima visibilidad bajo luz solar)",
                  style = MaterialTheme.typography.bodySmall,
                  color = if (uiState.isDarkTheme) Color(0xFF94A3B8) else Color(0xFF475569),
                  fontSize = 11.sp
                )
              }
            }

            Switch(
              checked = uiState.isDarkTheme,
              onCheckedChange = { onToggleTheme() },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFFFBBF24),
                checkedTrackColor = Color(0xFF78350F),
                uncheckedThumbColor = Color(0xFF0284C7),
                uncheckedTrackColor = Color(0xFFBAE6FD)
              ),
              modifier = Modifier.testTag("theme_switch_tools")
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "El modo oscuro reduce notablemente el gasto de batería durante cortes de luz prolongados y evita el deslumbramiento nocturno. El modo claro optimiza el contraste si debes evacuar a plena luz del día.",
            style = MaterialTheme.typography.bodySmall,
            color = if (uiState.isDarkTheme) Color(0xFFCBD5E1) else Color(0xFF334155),
            fontSize = 11.sp
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { if (!uiState.isDarkTheme) onToggleTheme() },
              modifier = Modifier
                .weight(1f)
                .testTag("btn_select_dark_theme"),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (uiState.isDarkTheme) Color(0xFF312E81) else Color(0xFF64748B),
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFFBBF24))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Oscuro (Noche)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = { if (uiState.isDarkTheme) onToggleTheme() },
              modifier = Modifier
                .weight(1f)
                .testTag("btn_select_light_theme"),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (!uiState.isDarkTheme) Color(0xFF0284C7) else Color(0xFF334155),
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFFDE68A))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Claro (Día)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
    }

    // 4. REPORTE DE ESTADO Y UBICACIÓN GPS
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .border(1.dp, Color(0xFF26374F), RoundedCornerShape(18.dp))
          .testTag("location_status_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141F30))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF0369A1), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Reporte de Estado a Familiares",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "GPS en tiempo real: ${uiState.detectedUserCommune}, ${uiState.detectedUserRegion}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF38BDF8),
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "Coord: ${String.format("%.4f", uiState.userLatitude)}, ${String.format("%.4f", uiState.userLongitude)} (Biobío)",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // "Estoy a Salvo" button
            Button(
              onClick = {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                  putExtra(Intent.EXTRA_TEXT, safeStatusMessageText)
                  type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Enviar reporte: Estoy a Salvo"))
              },
              modifier = Modifier
                .weight(1f)
                .testTag("im_safe_button"),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("¡Estoy a Salvo!", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            // "Necesito Auxilio" button
            Button(
              onClick = {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                  putExtra(Intent.EXTRA_TEXT, sosMessageText)
                  type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Compartir SOS de Emergencia"))
              },
              modifier = Modifier
                .weight(1f)
                .testTag("share_sos_button"),
              colors = ButtonDefaults.buttonColors(containerColor = AlertEmergencyRed),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Pedir Auxilio", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }

    // 4. TELÉFONOS DE EMERGENCIA DE CHILE
    item {
      Text(
        text = "Teléfonos Oficiales de Emergencia (Chile)",
        style = MaterialTheme.typography.titleLarge,
        color = Color.White,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Toque para marcar inmediatamente sin costo",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF94A3B8)
      )
      Spacer(modifier = Modifier.height(10.dp))
    }

    items(ChileEmergencyData.OFFICIAL_CONTACTS, key = { it.number }) { contact ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
          .clip(RoundedCornerShape(14.dp))
          .clickable {
            val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.number}"))
            context.startActivity(callIntent)
          }
          .testTag("dial_contact_${contact.number}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141F30))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Number badge
          Box(
            modifier = Modifier
              .size(52.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(Color(contact.badgeColorHex)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = contact.number,
              style = MaterialTheme.typography.titleLarge,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = contact.name,
              style = MaterialTheme.typography.titleMedium,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = contact.organization,
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF38BDF8)
            )
            Text(
              text = contact.description,
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF94A3B8),
              maxLines = 2
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(Color(0xFF0F261F)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Call,
              contentDescription = "Llamar a ${contact.name}",
              tint = Color(0xFF10B981),
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}
