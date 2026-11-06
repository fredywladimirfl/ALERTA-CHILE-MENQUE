package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SurvivalCategory
import com.example.data.model.SurvivalItem

@Composable
fun SurvivalKitScreen(
  items: List<SurvivalItem>,
  onToggleItem: (String) -> Unit,
  onAddItem: (String, SurvivalCategory) -> Unit,
  onReset: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedCategory by remember { mutableStateOf<SurvivalCategory?>(null) }
  var showAddDialog by remember { mutableStateOf(false) }
  var newItemTitle by remember { mutableStateOf("") }
  var newItemCategory by remember { mutableStateOf(SurvivalCategory.HERRAMIENTAS) }

  val totalCount = items.size
  val checkedCount = items.count { it.isChecked }
  val progressPercent = if (totalCount > 0) checkedCount.toFloat() / totalCount else 0f
  val animatedProgress by animateFloatAsState(targetValue = progressPercent, label = "kit_progress")

  val filteredItems = if (selectedCategory == null) {
    items
  } else {
    items.filter { it.category == selectedCategory }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("survival_kit_list"),
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
                .background(Color(0xFFF59E0B), CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "PREVENCIÓN Y AUTOPROTECCIÓN",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFFF59E0B),
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
          }
          Text(
            text = "Mochila de Emergencia (72h)",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
        }

        IconButton(
          onClick = onReset,
          modifier = Modifier.testTag("reset_kit_btn")
        ) {
          Icon(
            imageVector = Icons.Default.RestartAlt,
            contentDescription = "Reiniciar lista",
            tint = Color(0xFF94A3B8)
          )
        }
      }

      Text(
        text = "Kit recomendado por SENAPRED para sobrevivir las primeras 72 horas ante catástrofes.",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF94A3B8)
      )

      Spacer(modifier = Modifier.height(14.dp))
    }

    // Readiness Status Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .border(1.dp, Color(0xFF26374F), RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141F30))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(46.dp)
                  .background(Color(0xFF0039A6), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Backpack,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(24.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Estado de Preparación",
                  style = MaterialTheme.typography.titleMedium,
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "$checkedCount de $totalCount elementos listos",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color(0xFF94A3B8)
                )
              }
            }

            Text(
              text = "${(progressPercent * 100).toInt()}%",
              style = MaterialTheme.typography.headlineMedium,
              color = if (progressPercent >= 0.8f) Color(0xFF10B981) else Color(0xFFF59E0B),
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp)),
            color = if (progressPercent >= 0.8f) Color(0xFF10B981) else Color(0xFFF59E0B),
            trackColor = Color(0xFF243447)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
    }

    // Category Filter Chips
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.testTag("survival_category_chips")
      ) {
        item {
          val isSelected = selectedCategory == null
          FilterChip(
            selected = isSelected,
            onClick = { selectedCategory = null },
            label = { Text("Todos ($totalCount)", fontSize = 12.sp) },
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

        items(SurvivalCategory.values()) { cat ->
          val isSelected = selectedCategory == cat
          val catCount = items.count { it.category == cat }
          FilterChip(
            selected = isSelected,
            onClick = { selectedCategory = cat },
            label = { Text("${cat.displayName} ($catCount)", fontSize = 12.sp) },
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

      Spacer(modifier = Modifier.height(12.dp))
    }

    // Add item button
    item {
      Button(
        onClick = { showAddDialog = true },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("add_survival_item_btn"),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Agregar elemento personalizado", fontSize = 13.sp)
      }

      Spacer(modifier = Modifier.height(10.dp))
    }

    // Items list
    items(filteredItems, key = { it.id }) { item ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
          .clip(RoundedCornerShape(14.dp))
          .border(
            1.dp,
            if (item.isChecked) Color(0xFF059669).copy(alpha = 0.5f) else Color(0xFF26374F),
            RoundedCornerShape(14.dp)
          )
          .clickable { onToggleItem(item.id) }
          .testTag("survival_item_${item.id}"),
        colors = CardDefaults.cardColors(
          containerColor = if (item.isChecked) Color(0xFF0F261F) else Color(0xFF141F30)
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(
            checked = item.isChecked,
            onCheckedChange = { onToggleItem(item.id) },
            colors = CheckboxDefaults.colors(
              checkedColor = Color(0xFF10B981),
              uncheckedColor = Color(0xFF64748B)
            )
          )

          Spacer(modifier = Modifier.width(8.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (item.isChecked) Color(0xFFA7F3D0) else Color.White,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.width(6.dp))
              if (item.priorityLevel == "Crítico") {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFDC2626).copy(alpha = 0.25f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "CRÍTICO",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFCA5A5),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
              text = item.description,
              style = MaterialTheme.typography.bodySmall,
              color = if (item.isChecked) Color(0xFF6EE7B7) else Color(0xFF94A3B8)
            )
          }
        }
      }
    }
  }

  // Dialog to add custom survival kit item
  if (showAddDialog) {
    AlertDialog(
      onDismissRequest = { showAddDialog = false },
      title = {
        Text("Nuevo Elemento para Mochila", fontWeight = FontWeight.Bold, color = Color.White)
      },
      text = {
        Column {
          Text(
            text = "Escriba qué artículo desea sumar a su preparación de 72 horas.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8)
          )
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = newItemTitle,
            onValueChange = { newItemTitle = it },
            label = { Text("Nombre del artículo") },
            placeholder = { Text("Ej: Alimento para mascota, Pañales...") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White,
              focusedBorderColor = Color(0xFF38BDF8),
              unfocusedBorderColor = Color(0xFF334155)
            )
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newItemTitle.isNotBlank()) {
              onAddItem(newItemTitle, newItemCategory)
              newItemTitle = ""
              showAddDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0039A6))
        ) {
          Text("Guardar")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddDialog = false }) {
          Text("Cancelar", color = Color(0xFF94A3B8))
        }
      },
      containerColor = Color(0xFF141F30)
    )
  }
}
