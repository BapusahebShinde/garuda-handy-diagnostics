package com.itek.rftaar.utils

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import com.itek.rftaar.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SoundUtils {
  val toneGenerator: ToneGenerator = ToneGenerator(AudioManager.STREAM_DTMF, 100)

  /**
   * Error beep.
   */
  fun errorBeep() {
    toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK)
  }

  /**
   * Success beep.
   */
  fun successBeep() {
    toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK)
  }

  /**
   * Beep.
   */
  fun beep() {
    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP)
  }

  /**
   * Beep notification.
   */
  fun beepNotification() {
    toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_REORDER)
  }

  /**
   * Stop beep.
   */
  fun stopBeep() {
    toneGenerator.stopTone()
  }

  fun searchBeep(commonActivity: Activity, searchPercentValue: Int) {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        when {
          searchPercentValue >= 90 -> playSound(commonActivity, R.raw.successbeep)
          searchPercentValue >= 66 -> playSound(commonActivity, R.raw.blep_300ms)
          searchPercentValue >= 33 -> playSound(commonActivity, R.raw.blep_100ms)
          searchPercentValue > 0 -> playSound(commonActivity, R.raw.mute)
          // searchPercentValue == 0 → no beep, silence
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  fun inventoryBeep(context: Activity, velocity: Int) {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        when {
          velocity >= 10 -> playSound(context, R.raw.successbeep)
          velocity >= 5  -> playSound(context, R.raw.blep_300ms)
          velocity >= 1  -> playSound(context, R.raw.blep_100ms)
          // velocity == 0 → no beep, silence
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  fun playSound(context: Context?, resId: Int) {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        if (resId != 0) {
          val sound1 = MediaPlayer.create(context, resId)
          if (sound1.isPlaying == true) {
            sound1.pause()
          } else {
            sound1.start()
          }
          sound1.setOnCompletionListener { mp -> mp.reset(); mp.release()}
        }
      } catch (e: java.lang.Exception) {
        e.printStackTrace()
      }
    }
  }
}