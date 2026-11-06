package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ChileEmergencyData
import com.example.data.model.UvLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Alerta Chile", appName)
  }

  @Test
  fun `verify official chilean emergency numbers`() {
    val contacts = ChileEmergencyData.OFFICIAL_CONTACTS
    val samu = contacts.find { it.number == "131" }
    val bomberos = contacts.find { it.number == "132" }
    val carabineros = contacts.find { it.number == "133" }
    val pdi = contacts.find { it.number == "134" }
    val conaf = contacts.find { it.number == "130" }

    assertNotNull(samu)
    assertNotNull(bomberos)
    assertNotNull(carabineros)
    assertNotNull(pdi)
    assertNotNull(conaf)
  }

  @Test
  fun `verify chilean region detection from coordinates`() {
    val santiagoRegion = ChileEmergencyData.detectRegionFromCoordinates(-33.45, -70.66)
    assertEquals("Metropolitana", santiagoRegion)

    val valparaisoRegion = ChileEmergencyData.detectRegionFromCoordinates(-33.04, -71.62)
    assertEquals("Valparaíso", valparaisoRegion)

    val biobioRegion = ChileEmergencyData.detectRegionFromCoordinates(-36.82, -73.05)
    assertEquals("Biobío", biobioRegion)
  }

  @Test
  fun `verify biobio commune detection and distance calculation`() {
    val commune = ChileEmergencyData.detectBiobioCommune(-36.72, -73.11)
    assertEquals("Talcahuano", commune)

    // Distance between Concepción and Talcahuano
    val dist = ChileEmergencyData.calculateDistanceKm(-36.8270, -73.0503, -36.7249, -73.1168)
    assertTrue("Distancia esperada entre 10 y 16 km, calculada: $dist", dist in 10.0..16.0)
  }

  @Test
  fun `verify UV level categories`() {
    val uvBajo = UvLevel.BAJO
    val uvExtremo = UvLevel.EXTREMO

    assertEquals("Bajo", uvBajo.displayName)
    assertEquals("Extremo", uvExtremo.displayName)
  }

  @Test
  fun `verify CachedAlertEntity conversion to and from domain`() {
    val initialAlerts = ChileEmergencyData.getInitialOfficialAlerts()
    val alert = initialAlerts.first()

    val entity = com.example.data.local.entity.CachedAlertEntity.fromDomain(alert)
    assertEquals(alert.id, entity.id)
    assertEquals(alert.title, entity.title)
    assertEquals(alert.region, entity.region)

    val backToDomain = entity.toDomain()
    assertEquals(alert.id, backToDomain.id)
    assertEquals(alert.title, backToDomain.title)
    assertEquals(alert.category, backToDomain.category)
    assertEquals(alert.severity, backToDomain.severity)
    assertEquals(alert.region, backToDomain.region)
  }

  @Test
  fun `verify Room database alert dao insertion and retrieval`() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = androidx.room.Room.inMemoryDatabaseBuilder(
      context,
      com.example.data.local.EmergencyDatabase::class.java
    ).allowMainThreadQueries().build()

    try {
      val dao = db.alertDao()
      val initialAlerts = ChileEmergencyData.getInitialOfficialAlerts()
      val entities = initialAlerts.map { com.example.data.local.entity.CachedAlertEntity.fromDomain(it) }

      dao.insertAlerts(entities)

      val cached = dao.getAllAlerts()
      assertEquals(entities.size, cached.size)
      assertTrue(cached.any { it.region == "Biobío" })

      val count = dao.getAlertCount()
      assertEquals(entities.size, count)

      // Test inserting custom alert
      val custom = com.example.data.local.entity.CachedAlertEntity(
        id = "test_custom_sae_123",
        title = "Prueba SAE Offline",
        description = "Alerta guardada en base de datos local Room",
        categoryName = "SAE_BROADCAST",
        severityName = "ROJA",
        region = "Biobío",
        commune = "Concepción",
        timestampFormatted = "Ahora",
        timestampMillis = System.currentTimeMillis(),
        source = "SENAPRED Test",
        magnitude = null,
        depthKm = null,
        tsunamiThreatName = "NO_APLICA",
        instructionsDelimited = "Evacue con calma|||Lleve kit",
        isActive = true,
        isSaeBroadcast = true,
        latitude = -36.82,
        longitude = -73.05
      )

      dao.insertAlert(custom)
      val retrieved = dao.getAlertById("test_custom_sae_123")
      assertNotNull(retrieved)
      assertEquals("Prueba SAE Offline", retrieved?.title)
      assertEquals(2, retrieved?.toDomain()?.instructions?.size)
    } finally {
      db.close()
    }
  }

  @Test
  fun `verify MapAlertFilters active counts and toggles`() {
    val defaultFilters = com.example.ui.screens.MapAlertFilters()
    assertEquals(5, defaultFilters.activeCount)
    assertTrue(defaultFilters.allActive)
    org.junit.Assert.assertFalse(defaultFilters.noneActive)

    val onlySeismic = defaultFilters.copy(
      showWildfires = false,
      showWeatherWarnings = false,
      showSaeAlerts = false,
      showCoastalThreats = false
    )
    assertEquals(1, onlySeismic.activeCount)
    assertTrue(onlySeismic.showSeismic)
    org.junit.Assert.assertFalse(onlySeismic.showWildfires)

    val allDisabled = defaultFilters.copy(
      showSeismic = false,
      showWildfires = false,
      showWeatherWarnings = false,
      showSaeAlerts = false,
      showCoastalThreats = false
    )
    assertEquals(0, allDisabled.activeCount)
    assertTrue(allDisabled.noneActive)
  }

  @Test
  fun `verify theme switcher toggles between dark and light modes`() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.MainEmergencyViewModel(application)

    // Initial state defaults to Dark Mode for emergency tactical battery preservation
    val initialMode = viewModel.uiState.value.isDarkTheme

    // Toggle theme
    viewModel.toggleTheme()
    val toggledMode = viewModel.uiState.value.isDarkTheme
    assertEquals(!initialMode, toggledMode)

    // Toggle back
    viewModel.toggleTheme()
    val restoredMode = viewModel.uiState.value.isDarkTheme
    assertEquals(initialMode, restoredMode)

    // Explicit set
    viewModel.setTheme(false) // Light mode
    org.junit.Assert.assertFalse(viewModel.uiState.value.isDarkTheme)

    viewModel.setTheme(true) // Dark mode
    assertTrue(viewModel.uiState.value.isDarkTheme)
  }

  @Test
  fun `verify SosContactsRepository default contacts and persistence`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = com.example.data.repository.SosContactsRepository(context)
    repo.resetDefaults()

    val contacts = repo.contacts.value
    assertTrue("Debe tener al menos 3 contactos por defecto", contacts.size >= 3)
    val contact1 = contacts.find { it.phone == "+56912345678" }
    val contact2 = contacts.find { it.phone == "+56987654321" }
    val contact3 = contacts.find { it.phone == "+56944332211" }

    assertNotNull(contact1)
    assertNotNull(contact2)
    assertNotNull(contact3)
    assertTrue(contact1?.isEnabled == true)

    // Add new emergency contact
    repo.addContact("Mamá Concepción", "+56911223344", "Familia")
    val updatedList = repo.contacts.value
    val addedContact = updatedList.find { it.phone == "+56911223344" }
    assertNotNull(addedContact)
    assertEquals("Mamá Concepción", addedContact?.name)
    assertEquals("+56911223344", addedContact?.phone)
    assertTrue(addedContact?.isEnabled == true)

    val addedId = addedContact!!.id

    // Toggle contact enabled/disabled
    repo.toggleContact(addedId)
    val toggledList = repo.contacts.value
    val disabledContact = toggledList.find { it.id == addedId }
    org.junit.Assert.assertFalse(disabledContact?.isEnabled ?: true)

    // Delete contact
    repo.deleteContact(addedId)
    val afterDelete = repo.contacts.value
    org.junit.Assert.assertNull(afterDelete.find { it.id == addedId })
  }

  @Test
  fun `verify SOS emergency message generation contains GPS coordinates and map link`() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.MainEmergencyViewModel(application)

    val sosMessage = viewModel.getSosMessageText()
    assertTrue("El mensaje SOS debe contener prefijo de emergencia", sosMessage.contains("EMERGENCIA SOS CHILE"))
    assertTrue("El mensaje SOS debe contener coordenadas GPS", sosMessage.contains("maps.google.com/?q="))
    assertTrue("El mensaje SOS debe contener latitud", sosMessage.contains("Lat"))
    assertTrue("El mensaje SOS debe contener longitud", sosMessage.contains("Lon"))
    assertTrue("El mensaje SOS debe contener región o comuna", sosMessage.contains("Biobío") || sosMessage.contains("Concepción"))
  }

  @Test
  fun `verify SOS send logic detects active contacts`() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.MainEmergencyViewModel(application)
    viewModel.resetSosContacts()

    val initialContacts = viewModel.sosContacts.value
    assertTrue("Debe tener contactos activos", initialContacts.any { it.isEnabled })

    val result = viewModel.sendSosSmsToContacts(application)
    assertTrue("Debe contener teléfonos", result.recipientPhones.isNotEmpty())
    assertTrue("Mensaje generado no debe estar vacío", result.messageText.isNotBlank())
  }
}
