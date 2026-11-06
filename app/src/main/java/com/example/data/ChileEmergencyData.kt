package com.example.data

import com.example.data.model.AlertCategory
import com.example.data.model.AlertItem
import com.example.data.model.AlertSeverity
import com.example.data.model.EmergencyContact
import com.example.data.model.EmergencyProtocol
import com.example.data.model.SurvivalCategory
import com.example.data.model.SurvivalItem
import com.example.data.model.TsunamiThreat
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object ChileEmergencyData {

  // Centered in Biobío by default
  const val DEFAULT_REGION = "Biobío"
  const val DEFAULT_BIOBIO_LAT = -36.8270
  const val DEFAULT_BIOBIO_LON = -73.0503
  const val DEFAULT_BIOBIO_CITY = "Concepción / Gran Concepción"

  val CHILE_REGIONS = listOf(
    "Biobío",
    "Todas las Regiones",
    "Metropolitana",
    "Valparaíso",
    "Ñuble",
    "Araucanía",
    "Maule",
    "O'Higgins",
    "Coquimbo",
    "Atacama",
    "Antofagasta",
    "Tarapacá",
    "Arica y Parinacota",
    "Los Ríos",
    "Los Lagos",
    "Aysén",
    "Magallanes"
  )

  val BIOBIO_COMMUNES = listOf(
    "Concepción", "Talcahuano", "San Pedro de la Paz", "Coronel", "Hualpén",
    "Chiguayante", "Lota", "Penco", "Tomé", "Florida", "Santa Juana", "Hualqui",
    "Los Ángeles", "Cabrero", "Mulchén", "Nacimiento", "Yumbel", "Laja",
    "Lebu", "Arauco", "Cañete", "Curanilahue", "Contulmo", "Tirúa", "Los Álamos"
  )

  val OFFICIAL_CONTACTS = listOf(
    EmergencyContact(
      number = "131",
      name = "SAMU Ambulancia Biobío",
      organization = "Servicio de Salud Concepción / Biobío",
      description = "Urgencias médicas y rescate prehospitalario en la Región del Biobío",
      badgeColorHex = 0xFFD32F2F
    ),
    EmergencyContact(
      number = "132",
      name = "Bomberos del Biobío",
      organization = "Cuerpo de Bomberos Concepción / Talcahuano / Los Ángeles",
      description = "Incendios forestales, estructurales y rescate vehicular",
      badgeColorHex = 0xFFC62828
    ),
    EmergencyContact(
      number = "133",
      name = "Carabineros VIII Zona Biobío",
      organization = "Prefectura Concepción y Biobío",
      description = "Emergencias policiales, seguridad pública y auxilio",
      badgeColorHex = 0xFF1E7E34
    ),
    EmergencyContact(
      number = "134",
      name = "PDI Biobío",
      organization = "Policía de Investigaciones VIII Región",
      description = "Denuncias urgentes y brigadas de búsqueda",
      badgeColorHex = 0xFF0D47A1
    ),
    EmergencyContact(
      number = "137",
      name = "Gobernación Marítima de Talcahuano",
      organization = "Armada de Chile / Rescate Marítimo",
      description = "Emergencias en Golfo de Arauco, Bahía de Concepción, Lebu y San Vicente",
      badgeColorHex = 0xFF01579B
    ),
    EmergencyContact(
      number = "130",
      name = "CONAF Biobío Incendios",
      organization = "Corporación Nacional Forestal Región del Biobío",
      description = "Reporte urgente de focos de fuego forestal en cerros y plantaciones",
      badgeColorHex = 0xFFE65100
    ),
    EmergencyContact(
      number = "1488",
      name = "Violencia Familiar / Género",
      organization = "Carabineros de Chile",
      description = "Orientación y auxilio ante violencia intrafamiliar 24/7",
      badgeColorHex = 0xFF7B1FA2
    ),
    EmergencyContact(
      number = "1404",
      name = "Socorro Andino Biobío",
      organization = "Cuerpo de Socorro Andino Antuco / Cordillera",
      description = "Rescate en volcanes Antuco, Callaqui y cordillera de Biobío",
      badgeColorHex = 0xFF37474F
    ),
    EmergencyContact(
      number = "139",
      name = "Información Policial Rutas",
      organization = "Carabineros de Chile",
      description = "Estado Ruta 160, Ruta del Itata, Ruta 5 Sur y pasos fronterizos",
      badgeColorHex = 0xFF2E7D32
    )
  )

  fun getInitialOfficialAlerts(): List<AlertItem> {
    val now = System.currentTimeMillis()
    return listOf(
      // Priority 1: Biobío Wildfire alert
      AlertItem(
        id = "senapred_incendio_biobio_01",
        title = "Alerta Roja por Incendio Forestal en Comuna de Santa Juana y San Pedro de la Paz",
        description = "Incendio forestal con amenaza directa a viviendas e infraestructura crítica en interfaz de Santa Juana y Ruta 160. SENAPRED mantiene Alerta Roja comunal y provincial.",
        category = AlertCategory.INCENDIO_FORESTAL,
        severity = AlertSeverity.ROJA,
        region = "Biobío",
        commune = "Santa Juana / San Pedro de la Paz",
        timestampFormatted = "Hace 18 min",
        timestampMillis = now - 18 * 60 * 1000,
        source = "SENAPRED / CONAF Región del Biobío",
        latitude = -37.1731,
        longitude = -72.9419,
        instructions = listOf(
          "Si recibe mensaje SAE, evacúe de inmediato hacia zonas seguras señalizadas.",
          "Cierre llaves de paso de gas y corte suministro eléctrico.",
          "Cubra nariz y boca con paño húmedo para protegerse de humo denso.",
          "Despeje las rutas para el paso expedito de Bomberos y brigadas de CONAF."
        )
      ),
      // Priority 2: Biobío Coastal Waves / Tsunami Watch
      AlertItem(
        id = "senapred_marejadas_biobio_02",
        title = "Alerta Temprana Preventiva por Marejadas y Viento Fuerte en Borde Costero",
        description = "Centro Meteorológico de la Armada advierte tren de olas de hasta 4.5 metros en Golfo de Arauco, Bahía de Concepción, Talcahuano, Coronel y Lebu.",
        category = AlertCategory.METEOROLOGICA,
        severity = AlertSeverity.TEMPRANA_PREVENTIVA,
        region = "Biobío",
        commune = "Talcahuano, Coronel, Tomé, Penco, Lebu",
        timestampFormatted = "Hace 1 hora",
        timestampMillis = now - 60 * 60 * 1000,
        source = "DIRECTEMAR / Gobernación Marítima de Talcahuano",
        latitude = -36.7249,
        longitude = -73.1168,
        instructions = listOf(
          "Evite transitar por roqueríos, caletas y playas expuestas.",
          "Embarcaciones menores deben permanecer en puerto seguro bajo orden de Capitanía de Puerto.",
          "No ingrese al mar bajo ninguna circunstancia."
        )
      ),
      // Priority 3: Biobío River Basin & Precordillera Rainfall
      AlertItem(
        id = "senapred_rio_biobio_03",
        title = "Monitoreo Hidrológico por Aumento de Caudal en Cuenca del Río Biobío",
        description = "Precipitaciones en precordillera de Los Ángeles y Alto Biobío provocan incremento en caudales de los ríos Biobío y Laja. Vigilancia en puntos críticos.",
        category = AlertCategory.METEOROLOGICA,
        severity = AlertSeverity.AMARILLA,
        region = "Biobío",
        commune = "Alto Biobío, Santa Bárbara, Los Ángeles, Hualqui",
        timestampFormatted = "Hace 2 horas",
        timestampMillis = now - 2 * 3600 * 1000,
        source = "DGA / SENAPRED Biobío",
        latitude = -36.8500,
        longitude = -73.0100,
        instructions = listOf(
          "Aléjese de riberas y esteros susceptibles de desborde.",
          "Revise anclajes de bombas de agua en parcelas ribereñas.",
          "Infórmese de las vías de evacuación en sectores rurales."
        )
      ),
      // Other national alerts
      AlertItem(
        id = "senapred_incendio_valpo_01",
        title = "Alerta Amarilla por Incendio Forestal en Valparaíso",
        description = "Comportamiento extremo de fuego en cerros de la costa central.",
        category = AlertCategory.INCENDIO_FORESTAL,
        severity = AlertSeverity.AMARILLA,
        region = "Valparaíso",
        commune = "Valparaíso",
        timestampFormatted = "Hace 3 horas",
        timestampMillis = now - 3 * 3600 * 1000,
        source = "SENAPRED / CONAF",
        latitude = -33.0472,
        longitude = -71.6127,
        instructions = listOf("Atienda instrucciones de brigadistas.")
      ),
      AlertItem(
        id = "senapred_volcan_villarrica_03",
        title = "Alerta Amarilla para Volcán Villarrica por actividad sísmica interna",
        description = "SERNAGEOMIN reporta incremento en señal de tremor y emisiones piroclásticas.",
        category = AlertCategory.VOLCANICA,
        severity = AlertSeverity.AMARILLA,
        region = "Araucanía",
        commune = "Villarrica, Pucón",
        timestampFormatted = "Hoy 08:30",
        timestampMillis = now - 5 * 3600 * 1000,
        source = "SERNAGEOMIN / OVDAS",
        latitude = -39.4200,
        longitude = -71.9300,
        instructions = listOf("Respete perímetro de exclusión de 2 km.")
      )
    )
  }

  val DEFAULT_SURVIVAL_ITEMS = listOf(
    // Agua y Alimentos
    SurvivalItem(
      id = "water",
      title = "Agua potable (6 litros)",
      description = "2 litros por persona al día para un mínimo de 72 horas.",
      category = SurvivalCategory.AGUA_ALIMENTOS,
      priorityLevel = "Crítico"
    ),
    SurvivalItem(
      id = "food_canned",
      title = "Comida enlatada y no perecible",
      description = "Atún, legumbres preparadas, barras de cereal, frutos secos.",
      category = SurvivalCategory.AGUA_ALIMENTOS,
      priorityLevel = "Esencial"
    ),
    SurvivalItem(
      id = "can_opener",
      title = "Abrelatas manual",
      description = "Imprescindible para alimentos en conserva sin pestaña.",
      category = SurvivalCategory.AGUA_ALIMENTOS,
      priorityLevel = "Esencial"
    ),

    // Salud y Primeros Auxilios
    SurvivalItem(
      id = "first_aid_kit",
      title = "Botiquín de primeros auxilios",
      description = "Gasas estériles, vendas, suero fisiológico, tijeras, paracetamol y alcohol.",
      category = SurvivalCategory.PRIMEROS_AUXILIOS,
      priorityLevel = "Crítico"
    ),
    SurvivalItem(
      id = "chronic_meds",
      title = "Medicamentos crónicos para 5 días",
      description = "Insulina, antihipertensivos, inhaladores o tratamientos familiares.",
      category = SurvivalCategory.PRIMEROS_AUXILIOS,
      priorityLevel = "Crítico"
    ),
    SurvivalItem(
      id = "masks_n95",
      title = "Mascarillas (N95 / KN95)",
      description = "Protección contra humo de incendios forestales y ceniza.",
      category = SurvivalCategory.PRIMEROS_AUXILIOS,
      priorityLevel = "Esencial"
    ),

    // Herramientas y Comunicación
    SurvivalItem(
      id = "battery_radio",
      title = "Radio portátil a pilas + pilas de repuesto",
      description = "Para escuchar boletines de SENAPRED y Radio Bío Bío / Cooperativa durante cortes de luz.",
      category = SurvivalCategory.HERRAMIENTAS,
      priorityLevel = "Crítico"
    ),
    SurvivalItem(
      id = "flashlight",
      title = "Linterna LED y pilas adicionales",
      description = "No use velas en sismos por riesgo de fugas de gas.",
      category = SurvivalCategory.HERRAMIENTAS,
      priorityLevel = "Crítico"
    ),
    SurvivalItem(
      id = "whistle",
      title = "Silbato de supervivencia",
      description = "Permite pedir auxilio a rescatistas con mínimo gasto de energía.",
      category = SurvivalCategory.HERRAMIENTAS,
      priorityLevel = "Esencial"
    ),
    SurvivalItem(
      id = "power_bank",
      title = "Batería externa cargada (Power Bank)",
      description = "Cables de carga para mantener celular con batería para emergencias.",
      category = SurvivalCategory.HERRAMIENTAS,
      priorityLevel = "Esencial"
    ),
    SurvivalItem(
      id = "multitool",
      title = "Navaja multiuso o alicate",
      description = "Para cortar suministros de agua/gas y reparaciones rápidas.",
      category = SurvivalCategory.HERRAMIENTAS,
      priorityLevel = "Opcional"
    ),

    // Abrigo y Documentación
    SurvivalItem(
      id = "thermal_blanket",
      title = "Manta térmica aluminizada",
      description = "Mantiene el calor corporal y protege del frío y viento del Biobío.",
      category = SurvivalCategory.ABRIGO_DOCUMENTOS,
      priorityLevel = "Esencial"
    ),
    SurvivalItem(
      id = "docs_sealed",
      title = "Copias de cédula y documentos en bolsa hermética",
      description = "Cédula de identidad, escrituras, carnet de salud y recetas médicas.",
      category = SurvivalCategory.ABRIGO_DOCUMENTOS,
      priorityLevel = "Esencial"
    ),
    SurvivalItem(
      id = "cash",
      title = "Dinero en efectivo (billetes chicos y monedas)",
      description = "Cajeros automáticos y POS quedan inoperativos ante cortes de energía.",
      category = SurvivalCategory.ABRIGO_DOCUMENTOS,
      priorityLevel = "Esencial"
    ),
    SurvivalItem(
      id = "warm_clothes",
      title = "Ropa abrigada e impermeable",
      description = "Cortavientos, calcetines secos y calzado resistente.",
      category = SurvivalCategory.ABRIGO_DOCUMENTOS,
      priorityLevel = "Esencial"
    )
  )

  val OFFICIAL_PROTOCOLS = listOf(
    EmergencyProtocol(
      id = "protocol_sismo",
      title = "Terremotos y Grandes Sismos",
      subtitle = "Protocolo oficial chileno de autoprotección",
      iconName = "sismo",
      goldenRule = "AGÁCHATE, CÚBRETE Y AFÍRMATE. No corras ni uses ascensores.",
      beforeSteps = listOf(
        "Identifique zonas seguras de su casa, colegio o trabajo (lejos de ventanales).",
        "Asegure muebles altos y televisores a los muros.",
        "Mantenga despejadas las vías de evacuación y puertas de salida."
      ),
      duringSteps = listOf(
        "Mantenga la calma. Ubíquese bajo un mueble firme y afírmese.",
        "Si está en la calle, aléjese de edificios, postes de electricidad y cables caídos.",
        "Si va manejando por autopista, reduzca la velocidad gradualmente y deténgase lejos de puentes o pasos sobre nivel."
      ),
      afterSteps = listOf(
        "Cierre llaves de paso de agua y gas. Si huele a gas, no encienda luces ni fuego.",
        "Use linternas; no encienda velas ni fósforos.",
        "Infórmese únicamente por canales oficiales (SENAPRED, Radio Bío Bío / Radio Nacional). Utilice SMS para comunicarse."
      ),
      officialAuthority = "SENAPRED / Centro Sismológico Nacional (CSN)"
    ),
    EmergencyProtocol(
      id = "protocol_tsunami",
      title = "Tsunami en Costa del Biobío",
      subtitle = "Regla de los 30 metros de altura (Cota 30)",
      iconName = "tsunami",
      goldenRule = "Si no puede mantenerse en pie en la costa, EVACÚE DE INMEDIATO A PIE a la Cota 30.",
      beforeSteps = listOf(
        "Conozca las vías de evacuación señalizadas en Talcahuano, Coronel, Tomé, Penco y Lebu.",
        "Revise si su hogar se encuentra bajo la cota de inundación tsunámica.",
        "Tenga preparada su mochila de emergencia con zapatillas cómodas para caminar."
      ),
      duringSteps = listOf(
        "Al término del sismo, si percibe que fue muy fuerte o si el mar se retira, no espere la alerta oficial: comience a evacuar inmediatamente.",
        "Evacúe A PIE. No use vehículos ya que colapsan puentes y calles costeras.",
        "Diríjase a cerros y zonas sobre los 30 metros sobre el nivel del mar (Cota 30)."
      ),
      afterSteps = listOf(
        "Permanezca en la zona segura. Los tsunamis son series de olas que pueden llegar con horas de diferencia.",
        "No regrese a la playa hasta que SHOA y SENAPRED cancelen formalmente la alarma."
      ),
      officialAuthority = "SHOA (Servicio Hidrográfico y Oceanográfico de la Armada) / SENAPRED"
    ),
    EmergencyProtocol(
      id = "protocol_incendio",
      title = "Incendios Forestales en el Biobío",
      subtitle = "Evacuación preventiva ante fuego en interfaz forestal",
      iconName = "incendio",
      goldenRule = "Ante orden de evacuación SAE, salga de inmediato; su vida es irremplazable.",
      beforeSteps = listOf(
        "Mantenga despejada de maleza seca y ramas una franja de 10 metros alrededor de su vivienda.",
        "No use herramientas que generen chispas en días de altas temperaturas y viento puelche.",
        "Tenga a mano mangueras y reserva de agua."
      ),
      duringSteps = listOf(
        "Si las autoridades o la alerta SAE ordenan evacuar, acate de inmediato.",
        "Lleve a sus mascotas con correa o en caja de transporte.",
        "Use ropa de manga larga de algodón o lana (no sintética) y calzado cerrado.",
        "Cubra boca y nariz con un paño húmedo. Camine en sentido contrario al viento."
      ),
      afterSteps = listOf(
        "No ingrese al área quemada; pueden persistir focos subterráneos y riesgo de caída de postes.",
        "Preste atención a las instrucciones de Bomberos y Carabineros."
      ),
      officialAuthority = "CONAF (Fono 130) / Bomberos (Fono 132) / SENAPRED"
    ),
    EmergencyProtocol(
      id = "protocol_volcan",
      title = "Erupciones Volcánicas y Lahares",
      subtitle = "Volcanes Antuco, Callaqui y Copahue",
      iconName = "volcan",
      goldenRule = "Aléjese de valles y cauces de ríos que descienden del volcán (lahares).",
      beforeSteps = listOf(
        "Conozca el mapa de peligro volcánico de SERNAGEOMIN para su comuna.",
        "Tenga mascarillas N95 y gafas herméticas para cada miembro de la familia.",
        "Cubra pozos y estanques de agua potable para evitar contaminación con ceniza."
      ),
      duringSteps = listOf(
        "Si cae ceniza, permanezca bajo techo. Cierre puertas, ventanas y ventilaciones.",
        "Si debe salir, use mascarilla, ropa protectora y gafas protectoras (no lentes de contacto).",
        "Evite quebradas y ríos; los lahares son el mayor peligro mortal."
      ),
      afterSteps = listOf(
        "Barra la ceniza acumulada en techos para evitar colapsos; humedézcala levemente.",
        "No use agua para lavar techos si la ceniza puede colapsar canaletas."
      ),
      officialAuthority = "SERNAGEOMIN (Red Nacional de Vigilancia Volcánica) / SENAPRED"
    )
  )

  // Distance calculator using Haversine formula (km)
  fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0 // Earth radius in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
      cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
      sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    val dist = r * c
    return (dist * 10).toInt() / 10.0
  }

  fun detectRegionFromCoordinates(lat: Double, lon: Double): String {
    return when {
      // Biobío boundary check: approx -36.0 to -38.5 latitude
      lat in -38.5..-36.0 && lon in -74.0..-71.0 -> "Biobío"
      lat > -19.0 -> "Arica y Parinacota"
      lat > -21.5 -> "Tarapacá"
      lat > -25.5 -> "Antofagasta"
      lat > -29.0 -> "Atacama"
      lat > -32.0 -> "Coquimbo"
      lat > -33.5 && lon < -71.2 -> "Valparaíso"
      lat > -33.8 && lon >= -71.2 -> "Metropolitana"
      lat > -34.8 -> "O'Higgins"
      lat > -36.0 -> "Maule"
      lat > -37.0 && lon > -72.5 -> "Ñuble"
      lat in -38.5..-36.5 -> "Biobío"
      lat > -39.5 -> "Araucanía"
      lat > -40.5 -> "Los Ríos"
      lat > -44.0 -> "Los Lagos"
      lat > -49.0 -> "Aysén"
      else -> "Magallanes"
    }
  }

  fun detectBiobioCommune(lat: Double, lon: Double): String {
    return when {
      lat in -36.87..-36.78 && lon in -73.10..-73.00 -> "Concepción"
      lat in -36.78..-36.68 && lon in -73.18..-73.08 -> "Talcahuano"
      lat in -36.88..-36.80 && lon in -73.15..-73.05 -> "San Pedro de la Paz"
      lat in -37.05..-36.95 && lon in -73.20..-73.10 -> "Coronel"
      lat in -36.83..-36.76 && lon in -73.16..-73.10 -> "Hualpén"
      lat in -36.94..-36.88 && lon in -73.05..-72.98 -> "Chiguayante"
      lat in -37.12..-37.05 && lon in -73.20..-73.12 -> "Lota"
      lat in -36.68..-36.58 && lon in -73.00..-72.90 -> "Tomé"
      lat in -36.75..-36.68 && lon in -73.04..-72.96 -> "Penco"
      lat in -37.55..-37.40 && lon in -72.40..-72.25 -> "Los Ángeles"
      lat in -37.30..-37.10 && lon in -73.05..-72.85 -> "Santa Juana"
      lat in -37.65..-37.55 && lon in -73.70..-73.55 -> "Lebu"
      lat in -37.30..-37.15 && lon in -73.40..-73.25 -> "Arauco"
      lat in -37.85..-37.75 && lon in -73.45..-73.30 -> "Cañete"
      else -> "Gran Concepción / Biobío"
    }
  }

  fun estimateMercalli(magnitude: Double, depthKm: Double): String {
    return when {
      magnitude >= 7.5 -> "VIII - Destructivo"
      magnitude >= 6.5 -> "VII - Muy Fuerte"
      magnitude >= 5.8 -> "VI - Fuerte"
      magnitude >= 5.0 -> "V - Poco Fuerte"
      magnitude >= 4.2 -> "IV - Moderado"
      magnitude >= 3.5 -> "III - Leve"
      else -> "II - Muy Débil"
    }
  }
}
