package com.example.util

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class FlashlightMode {
  STEADY,
  STROBE,
  SOS
}

class TorchManager(context: Context) {

  private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
  private var cameraId: String? = null
  private var torchJob: Job? = null
  private var isTorchOn = false

  init {
    try {
      cameraManager?.cameraIdList?.firstOrNull { id ->
        cameraManager.getCameraCharacteristics(id)
          .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
      }?.let { cameraId = it }
    } catch (_: Exception) {}
  }

  fun isAvailable(): Boolean = cameraId != null

  fun setFlashlight(
    scope: CoroutineScope,
    enabled: Boolean,
    mode: FlashlightMode = FlashlightMode.STEADY,
    onStateChange: (Boolean) -> Unit
  ) {
    torchJob?.cancel()
    torchJob = null

    if (!enabled || cameraId == null) {
      setHardwareTorch(false)
      isTorchOn = false
      onStateChange(false)
      return
    }

    isTorchOn = true
    onStateChange(true)

    when (mode) {
      FlashlightMode.STEADY -> {
        setHardwareTorch(true)
      }
      FlashlightMode.STROBE -> {
        torchJob = scope.launch(Dispatchers.Default) {
          try {
            while (isActive) {
              setHardwareTorch(true)
              delay(80)
              setHardwareTorch(false)
              delay(80)
            }
          } finally {
            setHardwareTorch(false)
            isTorchOn = false
            onStateChange(false)
          }
        }
      }
      FlashlightMode.SOS -> {
        torchJob = scope.launch(Dispatchers.Default) {
          try {
            val dot = 150L
            val dash = 450L
            val pause = 150L
            val charPause = 450L
            val wordPause = 1200L

            suspend fun pulse(duration: Long) {
              setHardwareTorch(true)
              delay(duration)
              setHardwareTorch(false)
              delay(pause)
            }

            while (isActive) {
              // 3 dots
              repeat(3) { if (isActive) pulse(dot) }
              delay(charPause)
              // 3 dashes
              repeat(3) { if (isActive) pulse(dash) }
              delay(charPause)
              // 3 dots
              repeat(3) { if (isActive) pulse(dot) }
              delay(wordPause)
            }
          } finally {
            setHardwareTorch(false)
            isTorchOn = false
            onStateChange(false)
          }
        }
      }
    }
  }

  private fun setHardwareTorch(on: Boolean) {
    val id = cameraId ?: return
    try {
      cameraManager?.setTorchMode(id, on)
    } catch (_: Exception) {}
  }

  fun release() {
    torchJob?.cancel()
    setHardwareTorch(false)
  }
}
