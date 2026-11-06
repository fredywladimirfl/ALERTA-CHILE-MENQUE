package com.example.data.model

enum class AlertCategory(val displayName: String) {
  SISMO("Sismo"),
  TSUNAMI("Tsunami"),
  METEOROLOGICA("Meteorológica"),
  INCENDIO_FORESTAL("Incendio Forestal"),
  VOLCANICA("Volcánica"),
  ALUVION("Aluvión / Remoción en Masa"),
  SAE_BROADCAST("Alerta SAE")
}

enum class AlertSeverity(val displayName: String, val levelPriority: Int) {
  ROJA("Alerta Roja", 1),
  AMARILLA("Alerta Amarilla", 2),
  TEMPRANA_PREVENTIVA("Alerta Temprana Preventiva", 3),
  INFORMATIVA("Informativa", 4)
}

enum class TsunamiThreat(val description: String) {
  NO_AMENAZA("No reúne condiciones para tsunami"),
  PRECAUCION("Estado de Precaución (Ola de hasta 1m)"),
  ALERTA_TSUNAMI("Alerta de Tsunami (Evacuar sobre Cota 30)"),
  NO_APLICA("No aplica")
}

data class AlertItem(
  val id: String,
  val title: String,
  val description: String,
  val category: AlertCategory,
  val severity: AlertSeverity,
  val region: String,
  val commune: String? = null,
  val timestampFormatted: String,
  val timestampMillis: Long,
  val source: String, // e.g., "SENAPRED", "CSN", "SHOA", "DMC", "SERNAGEOMIN"
  val magnitude: Double? = null,
  val depthKm: Double? = null,
  val tsunamiThreat: TsunamiThreat = TsunamiThreat.NO_APLICA,
  val instructions: List<String> = emptyList(),
  val isActive: Boolean = true,
  val isSaeBroadcast: Boolean = false,
  val latitude: Double? = null,
  val longitude: Double? = null,
  val distanceKmFromUser: Double? = null
)

data class EarthquakeItem(
  val id: String,
  val place: String,
  val magnitude: Double,
  val depthKm: Double,
  val timeMillis: Long,
  val formattedTime: String,
  val latitude: Double,
  val longitude: Double,
  val chileRegion: String,
  val tsunamiThreat: TsunamiThreat,
  val estimatedIntensityMercalli: String,
  val source: String = "CSN / USGS Red Sismológica",
  val distanceKmFromUser: Double? = null
)

data class EmergencyContact(
  val number: String,
  val name: String,
  val organization: String,
  val description: String,
  val isOfficial: Boolean = true,
  val badgeColorHex: Long = 0xFFD32F2F
)

data class SurvivalItem(
  val id: String,
  val title: String,
  val description: String,
  val category: SurvivalCategory,
  val isChecked: Boolean = false,
  val priorityLevel: String = "Esencial"
)

enum class SurvivalCategory(val displayName: String) {
  AGUA_ALIMENTOS("Agua y Alimentos"),
  PRIMEROS_AUXILIOS("Salud y Primeros Auxilios"),
  HERRAMIENTAS("Herramientas y Comunicación"),
  ABRIGO_DOCUMENTOS("Abrigo y Documentación")
}

data class EmergencyProtocol(
  val id: String,
  val title: String,
  val subtitle: String,
  val iconName: String,
  val goldenRule: String,
  val beforeSteps: List<String>,
  val duringSteps: List<String>,
  val afterSteps: List<String>,
  val officialAuthority: String
)

data class SosContact(
  val id: String,
  val name: String,
  val phone: String,
  val relationship: String,
  val isEnabled: Boolean = true
)
