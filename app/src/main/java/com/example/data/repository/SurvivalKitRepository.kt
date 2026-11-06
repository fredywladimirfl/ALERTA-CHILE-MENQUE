package com.example.data.repository

import android.content.Context
import com.example.data.ChileEmergencyData
import com.example.data.model.SurvivalCategory
import com.example.data.model.SurvivalItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SurvivalKitRepository(context: Context) {

  private val prefs = context.getSharedPreferences("chile_survival_kit_prefs", Context.MODE_PRIVATE)

  private val _items = MutableStateFlow<List<SurvivalItem>>(emptyList())
  val items: StateFlow<List<SurvivalItem>> = _items.asStateFlow()

  init {
    loadItems()
  }

  private fun loadItems() {
    val defaults = ChileEmergencyData.DEFAULT_SURVIVAL_ITEMS
    val loaded = defaults.map { item ->
      val isChecked = prefs.getBoolean("item_${item.id}", false)
      item.copy(isChecked = isChecked)
    }
    _items.value = loaded
  }

  fun toggleItem(id: String) {
    val current = _items.value
    val updated = current.map { item ->
      if (item.id == id) {
        val newState = !item.isChecked
        prefs.edit().putBoolean("item_${item.id}", newState).apply()
        item.copy(isChecked = newState)
      } else {
        item
      }
    }
    _items.value = updated
  }

  fun addNewItem(title: String, category: SurvivalCategory) {
    if (title.isBlank()) return
    val id = "custom_${System.currentTimeMillis()}"
    val newItem = SurvivalItem(
      id = id,
      title = title.trim(),
      description = "Elemento personalizado agregado por el usuario",
      category = category,
      isChecked = false,
      priorityLevel = "Personalizado"
    )
    prefs.edit().putBoolean("item_$id", false).apply()
    _items.value = _items.value + newItem
  }

  fun resetAll() {
    val editor = prefs.edit()
    _items.value.forEach { item ->
      editor.remove("item_${item.id}")
    }
    editor.apply()
    loadItems()
  }
}
