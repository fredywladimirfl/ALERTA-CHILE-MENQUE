package com.example.data.repository

import android.content.Context
import com.example.data.model.SosContact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class SosContactsRepository(context: Context) {

  private val prefs = context.getSharedPreferences("chile_sos_contacts_prefs", Context.MODE_PRIVATE)

  private val _contacts = MutableStateFlow<List<SosContact>>(emptyList())
  val contacts: StateFlow<List<SosContact>> = _contacts.asStateFlow()

  init {
    loadContacts()
  }

  fun loadContacts() {
    val jsonString = prefs.getString("saved_sos_contacts_json", null)
    if (jsonString.isNullOrBlank()) {
      val defaults = getDefaultSosContacts()
      saveContactsToPrefs(defaults)
      _contacts.value = defaults
    } else {
      try {
        val array = JSONArray(jsonString)
        val list = mutableListOf<SosContact>()
        for (i in 0 until array.length()) {
          val obj = array.getJSONObject(i)
          list.add(
            SosContact(
              id = obj.getString("id"),
              name = obj.getString("name"),
              phone = obj.getString("phone"),
              relationship = obj.optString("relationship", "Contacto"),
              isEnabled = obj.optBoolean("isEnabled", true)
            )
          )
        }
        _contacts.value = if (list.isNotEmpty()) list else getDefaultSosContacts()
      } catch (e: Exception) {
        val defaults = getDefaultSosContacts()
        _contacts.value = defaults
      }
    }
  }

  fun addContact(name: String, phone: String, relationship: String) {
    if (name.isBlank() || phone.isBlank()) return
    val newContact = SosContact(
      id = "sos_${System.currentTimeMillis()}",
      name = name.trim(),
      phone = phone.trim(),
      relationship = relationship.trim().ifEmpty { "Contacto" },
      isEnabled = true
    )
    val updated = _contacts.value + newContact
    saveContactsToPrefs(updated)
    _contacts.value = updated
  }

  fun updateContact(contact: SosContact) {
    val updated = _contacts.value.map {
      if (it.id == contact.id) contact else it
    }
    saveContactsToPrefs(updated)
    _contacts.value = updated
  }

  fun deleteContact(id: String) {
    val updated = _contacts.value.filter { it.id != id }
    saveContactsToPrefs(updated)
    _contacts.value = updated
  }

  fun toggleContact(id: String) {
    val updated = _contacts.value.map {
      if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it
    }
    saveContactsToPrefs(updated)
    _contacts.value = updated
  }

  fun resetDefaults() {
    val defaults = getDefaultSosContacts()
    saveContactsToPrefs(defaults)
    _contacts.value = defaults
  }

  private fun saveContactsToPrefs(list: List<SosContact>) {
    val array = JSONArray()
    for (contact in list) {
      val obj = JSONObject().apply {
        put("id", contact.id)
        put("name", contact.name)
        put("phone", contact.phone)
        put("relationship", contact.relationship)
        put("isEnabled", contact.isEnabled)
      }
      array.put(obj)
    }
    prefs.edit().putString("saved_sos_contacts_json", array.toString()).apply()
  }

  private fun getDefaultSosContacts(): List<SosContact> {
    return listOf(
      SosContact(
        id = "sos_def_1",
        name = "Contacto de Confianza 1",
        phone = "+56912345678",
        relationship = "Familiar / Pareja",
        isEnabled = true
      ),
      SosContact(
        id = "sos_def_2",
        name = "Contacto de Confianza 2",
        phone = "+56987654321",
        relationship = "Hermano(a) / Padres",
        isEnabled = true
      ),
      SosContact(
        id = "sos_def_3",
        name = "Red de Apoyo Vecinal Biobío",
        phone = "+56944332211",
        relationship = "Vecino(a) Concepción",
        isEnabled = true
      )
    )
  }
}
