package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Volcano
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChileEmergencyData
import com.example.data.model.EmergencyProtocol
import com.example.ui.theme.AlertEmergencyRed
import com.example.ui.theme.ChileRedDark
import com.example.ui.theme.ChileRedLight

@Composable
fun ProtocolsScreen(
  modifier: Modifier = Modifier
) {
  val protocols = ChileEmergencyData.OFFICIAL_PROTOCOLS

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("protocols_list"),
    contentPadding = PaddingValues(16.dp, bottom = 96.dp)
  ) {
    // Header
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(10.dp)
            .background(Color(0xFF38BDF8), CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "MANUAL OFICIAL SENAPRED",
          style = MaterialTheme.typography.labelSmall,
          color = Color(0xFF38BDF8),
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
      }
      Text(
        text = "Guías de Actuación y Protocolos",
        style = MaterialTheme.typography.headlineMedium,
        color = Color.White,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Instrucciones de seguridad y autoprotección ante desastres naturales en Chile",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF94A3B8)
      )

      Spacer(modifier = Modifier.height(16.dp))
    }

    items(protocols, key = { it.id }) { protocol ->
      ProtocolCard(protocol = protocol)
      Spacer(modifier = Modifier.height(12.dp))
    }
  }
}

@Composable
fun ProtocolCard(
  protocol: EmergencyProtocol
) {
  var isExpanded by remember { mutableStateOf(false) }
  var selectedTabStage by remember { mutableStateOf(1) } // 0: Antes, 1: Durante, 2: Después

  val icon = when (protocol.iconName) {
    "sismo" -> Icons.Default.Warning
    "tsunami" -> Icons.Default.Waves
    "incendio" -> Icons.Default.LocalFireDepartment
    "volcan" -> Icons.Default.Volcano
    else -> Icons.Default.MenuBook
  }

  val accentColor = when (protocol.iconName) {
    "sismo" -> Color(0xFFEAB308)
    "tsunami" -> Color(0xFF0284C7)
    "incendio" -> AlertEmergencyRed
    "volcan" -> Color(0xFFF97316)
    else -> Color(0xFF38BDF8)
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .border(1.dp, Color(0xFF26374F), RoundedCornerShape(18.dp))
      .testTag("protocol_card_${protocol.id}"),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF141F30))
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { isExpanded = !isExpanded },
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(accentColor.copy(alpha = 0.2f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(26.dp)
          )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = protocol.title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = protocol.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8)
          )
        }

        Icon(
          imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
          contentDescription = null,
          tint = Color(0xFF94A3B8)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Golden Rule Box
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(10.dp)
      ) {
        Row(
          modifier = Modifier.padding(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFFFBBF24),
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = protocol.goldenRule,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFFDE68A),
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      AnimatedVisibility(visible = isExpanded) {
        Column(modifier = Modifier.padding(top = 14.dp)) {
          // Tab row for Antes / Durante / Después
          TabRow(
            selectedTabIndex = selectedTabStage,
            containerColor = Color(0xFF0F172A),
            contentColor = Color.White,
            indicator = { tabPositions ->
              TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabStage]),
                color = accentColor
              )
            },
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
          ) {
            listOf("Antes", "Durante", "Después").forEachIndexed { index, title ->
              Tab(
                selected = selectedTabStage == index,
                onClick = { selectedTabStage = index },
                text = {
                  Text(
                    text = title,
                    fontWeight = if (selectedTabStage == index) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp,
                    color = if (selectedTabStage == index) accentColor else Color(0xFF94A3B8)
                  )
                }
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          val currentSteps = when (selectedTabStage) {
            0 -> protocol.beforeSteps
            1 -> protocol.duringSteps
            else -> protocol.afterSteps
          }

          currentSteps.forEachIndexed { i, step ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.Top
            ) {
              Text(
                text = "${i + 1}.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                modifier = Modifier.width(22.dp)
              )
              Text(
                text = step,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE2E8F0)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          HorizontalDivider(color = Color(0xFF26374F))
          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Autoridad técnica: ${protocol.officialAuthority}",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF64748B),
            fontSize = 11.sp
          )
        }
      }
    }
  }
}
