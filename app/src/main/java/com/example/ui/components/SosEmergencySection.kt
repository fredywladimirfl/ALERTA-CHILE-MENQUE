package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SosContact
import com.example.ui.EmergencyUiState
import com.example.ui.SosSendResult
import com.example.ui.theme.AlertEmergencyRed
import com.example.ui.theme.AlertSafeGreen
import com.example.ui.theme.ChileRedDark
import com.example.ui.theme.ChileRedLight

@Composable
fun SosEmergencySection(
  uiState: EmergencyUiState,
  sosContacts: List<SosContact>,
  onSendSosSms: (Context) -> SosSendResult,
  onAddContact: (String, String, String) -> Unit,
  onToggleContact: (String) -> Unit,
  onDeleteContact: (String) -> Unit,
  onResetContacts: () -> Unit,
  sosMessageText: String,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showContactsDialog by remember { mutableStateOf(false) }
  var showAddDialog by remember { mutableStateOf(false) }
  var showConfirmSendDialog by remember { mutableStateOf(false) }
  var lastSendStatus by remember { mutableStateOf<String?>(null) }

  val enabledContacts = sosContacts.filter { it.isEnabled && it.phone.isNotBlank() }

  // Pulsing animation for the SOS button
  val infiniteTransition = rememberInfiniteTransition(label = "sos_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(650, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "sos_scale"
  )

  // SMS Permission launcher
  val smsPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    val result = onSendSosSms(context)
    if (result.sentDirectly) {
      lastSendStatus = result.feedbackMessage
      Toast.makeText(context, result.feedbackMessage, Toast.LENGTH_LONG).show()
    } else {
      // Fallback: Open native SMS app with pre-filled numbers & message
      launchNativeSmsApp(context, enabledContacts.map { it.phone }, result.messageText)
    }
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .border(2.dp, ChileRedLight, RoundedCornerShape(20.dp))
      .testTag("sos_emergency_main_card"),
    colors = CardDefaults.cardColors(
      containerColor = if (uiState.isDarkTheme) Color(0xFF1F0D11) else Color(0xFFFFF1F2)
    )
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      // Header tag
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .background(AlertEmergencyRed, CircleShape)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "ALERTA SOS • TRANSMISIÓN DE COORDENADAS",
            style = MaterialTheme.typography.labelSmall,
            color = AlertEmergencyRed,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
        }

        // Manage contacts button
        TextButton(
          onClick = { showContactsDialog = true },
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
          modifier = Modifier.testTag("manage_sos_contacts_btn")
        ) {
          Icon(Icons.Default.ContactPhone, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF38BDF8))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Contactos (${enabledContacts.size})", fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Real-time GPS location chip
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(if (uiState.isDarkTheme) Color(0xFF0F172A) else Color(0xFFE2E8F0))
          .padding(horizontal = 12.dp, vertical = 8.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.GpsFixed,
            contentDescription = null,
            tint = Color(0xFF38BDF8),
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "GPS en vivo: ${uiState.detectedUserCommune}, ${uiState.detectedUserRegion}",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = if (uiState.isDarkTheme) Color.White else Color(0xFF0F172A)
            )
            Text(
              text = "Lat: ${String.format("%.4f", uiState.userLatitude)}, Lon: ${String.format("%.4f", uiState.userLongitude)} • Precisión: ±${uiState.gpsAccuracyMeters?.toInt() ?: 12}m",
              style = MaterialTheme.typography.bodySmall,
              color = if (uiState.isDarkTheme) Color(0xFF94A3B8) else Color(0xFF475569),
              fontSize = 11.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // BIG PROMINENT SOS BUTTON
      Button(
        onClick = { showConfirmSendDialog = true },
        modifier = Modifier
          .fillMaxWidth()
          .height(64.dp)
          .scale(pulseScale)
          .testTag("sos_emergency_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = AlertEmergencyRed,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = Color.White
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column(horizontalAlignment = Alignment.Start) {
            Text(
              text = "ENVIAR SOS CON GPS POR SMS",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Black,
              letterSpacing = 0.5.sp
            )
            Text(
              text = "Notifica a ${enabledContacts.size} contacto(s) con mapa y auxilio",
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.9f),
              fontSize = 11.sp
            )
          }
        }
      }

      // Status indicator if sent
      if (lastSendStatus != null) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AlertSafeGreen.copy(alpha = 0.2f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AlertSafeGreen, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = lastSendStatus!!, color = AlertSafeGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Direct SMS Fallback & Native Chooser Options
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = {
            launchNativeSmsApp(context, enabledContacts.map { it.phone }, sosMessageText)
          },
          modifier = Modifier
            .weight(1f)
            .testTag("open_sms_app_btn"),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (uiState.isDarkTheme) Color(0xFF38BDF8) else Color(0xFF0284C7)
          )
        ) {
          Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Abrir en SMS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
          onClick = {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
              putExtra(Intent.EXTRA_TEXT, sosMessageText)
              type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Enviar SOS con GPS"))
          },
          modifier = Modifier
            .weight(1f)
            .testTag("share_sos_all_apps_btn"),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (uiState.isDarkTheme) Color(0xFF34D399) else Color(0xFF059669)
          )
        ) {
          Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Otras Apps", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }

  // 1. CONFIRM SOS TRANSMISSION DIALOG
  if (showConfirmSendDialog) {
    AlertDialog(
      onDismissRequest = { showConfirmSendDialog = false },
      icon = {
        Icon(Icons.Default.Warning, contentDescription = null, tint = AlertEmergencyRed, modifier = Modifier.size(36.dp))
      },
      title = {
        Text(
          text = "¿Enviar Alerta SOS por SMS?",
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      },
      text = {
        Column {
          Text(
            text = "Se enviará tu posición GPS en tiempo real a tus ${enabledContacts.size} contactos de emergencia preconfigurados:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(8.dp))
          enabledContacts.forEach { contact ->
            Text(
              text = "• ${contact.name} (${contact.phone}) - ${contact.relationship}",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .padding(8.dp)
          ) {
            Text(
              text = "GPS: Lat ${String.format("%.4f", uiState.userLatitude)}, Lon ${String.format("%.4f", uiState.userLongitude)}\nSector: ${uiState.detectedUserCommune}",
              style = MaterialTheme.typography.bodySmall,
              fontSize = 11.sp,
              color = Color(0xFF38BDF8)
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            showConfirmSendDialog = false
            smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
          },
          colors = ButtonDefaults.buttonColors(containerColor = AlertEmergencyRed),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("confirm_send_sos_sms_btn")
        ) {
          Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Enviar SMS Ahora", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showConfirmSendDialog = false }) {
          Text("Cancelar")
        }
      }
    )
  }

  // 2. MANAGE EMERGENCY CONTACTS DIALOG
  if (showContactsDialog) {
    AlertDialog(
      onDismissRequest = { showContactsDialog = false },
      title = {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Contactos de Emergencia",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          IconButton(onClick = { showAddDialog = true }) {
            Icon(Icons.Default.Add, contentDescription = "Agregar Contacto", tint = Color(0xFF38BDF8))
          }
        }
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Estos contactos recibirán tu ubicación GPS por SMS al presionar el Botón SOS:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(10.dp))

          if (sosContacts.isEmpty()) {
            Text(
              text = "No hay contactos agregados. Añade uno con el botón superior.",
              style = MaterialTheme.typography.bodyMedium,
              color = Color(0xFF94A3B8)
            )
          } else {
            sosContacts.forEach { contact ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant)
                  .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                  Checkbox(
                    checked = contact.isEnabled,
                    onCheckedChange = { onToggleContact(contact.id) },
                    colors = CheckboxDefaults.colors(checkedColor = AlertEmergencyRed)
                  )
                  Column {
                    Text(
                      text = contact.name,
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "${contact.phone} • ${contact.relationship}",
                      style = MaterialTheme.typography.bodySmall,
                      fontSize = 11.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                IconButton(
                  onClick = { onDeleteContact(contact.id) },
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            TextButton(onClick = onResetContacts) {
              Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Restablecer", fontSize = 11.sp)
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { showContactsDialog = false },
          shape = RoundedCornerShape(8.dp)
        ) {
          Text("Listo")
        }
      }
    )
  }

  // 3. ADD CONTACT DIALOG
  if (showAddDialog) {
    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("+569") }
    var newRelation by remember { mutableStateOf("Familiar") }

    AlertDialog(
      onDismissRequest = { showAddDialog = false },
      title = { Text("Nuevo Contacto de Emergencia", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = newName,
            onValueChange = { newName = it },
            label = { Text("Nombre Completo") },
            placeholder = { Text("Ej: Mamá, Hermano") },
            modifier = Modifier.fillMaxWidth().testTag("add_contact_name_input"),
            singleLine = true
          )
          OutlinedTextField(
            value = newPhone,
            onValueChange = { newPhone = it },
            label = { Text("Número de Teléfono") },
            placeholder = { Text("+569XXXXXXXX") },
            modifier = Modifier.fillMaxWidth().testTag("add_contact_phone_input"),
            singleLine = true
          )
          OutlinedTextField(
            value = newRelation,
            onValueChange = { newRelation = it },
            label = { Text("Relación o Parentesco") },
            placeholder = { Text("Ej: Familia, Pareja, Vecino") },
            modifier = Modifier.fillMaxWidth().testTag("add_contact_relation_input"),
            singleLine = true
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newName.isNotBlank() && newPhone.isNotBlank()) {
              onAddContact(newName, newPhone, newRelation)
              showAddDialog = false
            }
          },
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("save_new_contact_btn")
        ) {
          Text("Guardar")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddDialog = false }) {
          Text("Cancelar")
        }
      }
    )
  }
}

private fun launchNativeSmsApp(context: Context, phoneNumbers: List<String>, message: String) {
  try {
    val recipients = phoneNumbers.filter { it.isNotBlank() }.joinToString(";")
    val uri = if (recipients.isNotEmpty()) Uri.parse("smsto:$recipients") else Uri.parse("smsto:")
    val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
      putExtra("sms_body", message)
    }
    context.startActivity(intent)
  } catch (e: Exception) {
    // If smsto fails, fallback to general ACTION_SEND
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
      putExtra(Intent.EXTRA_TEXT, message)
      type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Enviar SMS de Emergencia"))
  }
}
