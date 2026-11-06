package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

enum class SirenSoundMode {
  SAE_ALARM,
  SOS_MORSE,
  SURVIVAL_WHISTLE
}

class AudioSirenManager {

  private var audioJob: Job? = null
  private var isPlaying = false

  fun isRunning(): Boolean = isPlaying

  fun startSound(scope: CoroutineScope, mode: SirenSoundMode, onStateChange: (Boolean) -> Unit) {
    stopSound(onStateChange)

    isPlaying = true
    onStateChange(true)

    audioJob = scope.launch(Dispatchers.Default) {
      val sampleRate = 44100
      val minBufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
      )

      val audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )
        .setAudioFormat(
          AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        )
        .setBufferSizeInBytes(minBufferSize * 2)
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()

      try {
        audioTrack.play()

        when (mode) {
          SirenSoundMode.SAE_ALARM -> playSaeAlarm(this, audioTrack, sampleRate)
          SirenSoundMode.SOS_MORSE -> playSosMorse(this, audioTrack, sampleRate)
          SirenSoundMode.SURVIVAL_WHISTLE -> playWhistle(this, audioTrack, sampleRate)
        }
      } catch (_: Exception) {
      } finally {
        try {
          audioTrack.stop()
          audioTrack.release()
        } catch (_: Exception) {}
        isPlaying = false
        onStateChange(false)
      }
    }
  }

  fun stopSound(onStateChange: ((Boolean) -> Unit)? = null) {
    audioJob?.cancel()
    audioJob = null
    isPlaying = false
    onStateChange?.invoke(false)
  }

  private fun playSaeAlarm(scope: CoroutineScope, track: AudioTrack, sampleRate: Int) {
    // Oscillating two-tone siren (850Hz and 1250Hz) classic SAE warning tone
    val bufferSize = sampleRate / 10
    val buffer = ShortArray(bufferSize)
    var phase = 0.0

    while (scope.isActive) {
      // 0.4s low, 0.4s high
      val cycleMs = 800
      val currentMs = (System.currentTimeMillis() % cycleMs).toInt()
      val freq = if (currentMs < 400) 853.0 else 1150.0

      for (i in 0 until bufferSize) {
        val angle = 2.0 * PI * freq / sampleRate
        phase += angle
        if (phase > 2.0 * PI) phase -= 2.0 * PI
        buffer[i] = (sin(phase) * Short.MAX_VALUE * 0.95).toInt().toShort()
      }
      track.write(buffer, 0, bufferSize)
    }
  }

  private fun playWhistle(scope: CoroutineScope, track: AudioTrack, sampleRate: Int) {
    // 2900 Hz loud resonant whistle tone
    val bufferSize = sampleRate / 10
    val buffer = ShortArray(bufferSize)
    var phase = 0.0
    val freq = 2900.0

    while (scope.isActive) {
      for (i in 0 until bufferSize) {
        val angle = 2.0 * PI * freq / sampleRate
        phase += angle
        if (phase > 2.0 * PI) phase -= 2.0 * PI
        buffer[i] = (sin(phase) * Short.MAX_VALUE * 0.95).toInt().toShort()
      }
      track.write(buffer, 0, bufferSize)
    }
  }

  private fun playSosMorse(scope: CoroutineScope, track: AudioTrack, sampleRate: Int) {
    // S: dot dot dot, O: dash dash dash, S: dot dot dot
    val dotMs = 120
    val dashMs = 360
    val pauseMs = 120
    val charPauseMs = 360
    val wordPauseMs = 1000

    val freq = 1000.0

    fun writeTone(durationMs: Int) {
      val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
      val buffer = ShortArray(numSamples)
      var phase = 0.0
      for (i in 0 until numSamples) {
        val angle = 2.0 * PI * freq / sampleRate
        phase += angle
        if (phase > 2.0 * PI) phase -= 2.0 * PI
        buffer[i] = (sin(phase) * Short.MAX_VALUE * 0.9).toInt().toShort()
      }
      track.write(buffer, 0, numSamples)
    }

    fun writeSilence(durationMs: Int) {
      val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
      val buffer = ShortArray(numSamples)
      track.write(buffer, 0, numSamples)
    }

    while (scope.isActive) {
      // S: 3 dots
      repeat(3) {
        if (!scope.isActive) return
        writeTone(dotMs)
        writeSilence(pauseMs)
      }
      writeSilence(charPauseMs)

      // O: 3 dashes
      repeat(3) {
        if (!scope.isActive) return
        writeTone(dashMs)
        writeSilence(pauseMs)
      }
      writeSilence(charPauseMs)

      // S: 3 dots
      repeat(3) {
        if (!scope.isActive) return
        writeTone(dotMs)
        writeSilence(pauseMs)
      }
      writeSilence(wordPauseMs)
    }
  }
}
