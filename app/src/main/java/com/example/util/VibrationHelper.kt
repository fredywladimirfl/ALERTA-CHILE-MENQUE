package com.example.util

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class VibrationHelper(context: Context) {

  private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
    vibratorManager?.defaultVibrator
  } else {
    @Suppress("DEPRECATION")
    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
  }

  fun vibrateSaeAlarm() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val timings = longArrayOf(0, 400, 200, 400, 200, 800)
        val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
        vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(longArrayOf(0, 400, 200, 400, 200, 800), -1)
      }
    } catch (_: Exception) {}
  }

  fun vibrateSos() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val dot = 120L
        val dash = 360L
        val space = 120L
        val timings = longArrayOf(
          0,
          dot, space, dot, space, dot, space * 3,
          dash, space, dash, space, dash, space * 3,
          dot, space, dot, space, dot
        )
        vibrator?.vibrate(VibrationEffect.createWaveform(timings, -1))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(800)
      }
    } catch (_: Exception) {}
  }

  fun cancel() {
    try {
      vibrator?.cancel()
    } catch (_: Exception) {}
  }
}
