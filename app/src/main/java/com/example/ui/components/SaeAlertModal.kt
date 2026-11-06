package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AlertItem
import com.example.ui.theme.ChileRedDark
import com.example.ui.theme.ChileRedLight

@Composable
fun SaeAlertModal(
  alert: AlertItem,
  isSirenPlaying: Boolean,
  onSilenceSiren: () -> Unit,
  onDismiss: () -> Unit
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse_alert")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.14f,
    animationSpec = infiniteRepeatable(
      animation = tween(400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(
      dismissOnBackPress = false,
      dismissOnClickOutside = false,
      usePlatformDefaultWidth = false
    )
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .clip(RoundedCornerShape(24.dp))
        .background(Color(0xFF0F172A))
        .border(3.dp, ChileRedLight, RoundedCornerShape(24.dp))
        .padding(20.dp)
        .testTag("sae_alert_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Red flashing badge icon
        Box(
          modifier = Modifier
            .size(76.dp)
            .scale(pulseScale)
            .background(ChileRedLight.copy(alpha = 0.25f), CircleShape)
            .border(2.dp, ChileRedLight, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = "Alerta SAE Activa",
            tint = ChileRedLight,
            modifier = Modifier.size(42.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "SISTEMA DE ALERTA DE EMERGENCIA (SAE)",
          style = MaterialTheme.typography.labelLarge,
          color = ChileRedLight,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )

        Text(
          text = "SENAPRED - GOBIERNO DE CHILE",
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFF94A3B8),
          letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Big alert title
        Card(
          colors = CardDefaults.cardColors(containerColor = ChileRedDark),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = alert.title,
              style = MaterialTheme.typography.titleMedium,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = alert.description,
          style = MaterialTheme.typography.bodyMedium,
          color = Color(0xFFF1F5F9),
          textAlign = TextAlign.Start,
          modifier = Modifier.fillMaxWidth()
        )

        if (alert.instructions.isNotEmpty()) {
          Spacer(modifier = Modifier.height(14.dp))
          HorizontalDivider(color = Color(0xFF334155))
          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "INSTRUCCIONES OFICIALES DE EVACUACIÓN:",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFFFACC15),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(8.dp))

          alert.instructions.forEachIndexed { index, step ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.Top
            ) {
              Text(
                text = "${index + 1}.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = ChileRedLight,
                modifier = Modifier.width(22.dp)
              )
              Text(
                text = step,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE2E8F0)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Siren toggle button if playing
        if (isSirenPlaying) {
          OutlinedButton(
            onClick = onSilenceSiren,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("silence_siren_button"),
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = Color(0xFFF87171)
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.VolumeOff, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Silenciar Alarma Sonora")
          }
          Spacer(modifier = Modifier.height(10.dp))
        }

        // Confirm button
        Button(
          onClick = onDismiss,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("confirm_sae_button"),
          colors = ButtonDefaults.buttonColors(
            containerColor = ChileRedLight,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "ENTENDIDO / CONFIRMAR EVACUACIÓN",
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}
