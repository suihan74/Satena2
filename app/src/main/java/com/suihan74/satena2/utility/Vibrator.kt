package com.suihan74.satena2.utility

import android.content.Context
import android.os.*
import androidx.annotation.RequiresApi

object VibratorCompat {
    /**
     * 一回振動
     */
    fun vibrateOneShot(
        context: Context,
        duration: Long = 40L
    ) {
        if (duration == 0L) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                vibrateOneShotAPI31(context, vibrationEffect)
            }
            else {
                vibrateOneShotAPI26(context, vibrationEffect)
            }
        }
        else {
            vibrateOneShotLegacy(context, duration)
        }
    }

    /**
     * 振動キャンセル
     */
    fun cancel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            cancelAPI31(context)
        }
        else {
            cancelLegacy(context)
        }
    }

    // ------ //

    @RequiresApi(Build.VERSION_CODES.S)
    private fun vibrateOneShotAPI31(context: Context, vibrationEffect: VibrationEffect) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val combinedVibration = CombinedVibration.createParallel(vibrationEffect)
        vibratorManager.vibrate(combinedVibration)
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun vibrateOneShotAPI26(context: Context, vibrationEffect: VibrationEffect) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(vibrationEffect)
    }

    @Suppress("DEPRECATION")
    private fun vibrateOneShotLegacy(context: Context, duration: Long) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(duration)
    }

    // ------ //

    @RequiresApi(Build.VERSION_CODES.S)
    private fun cancelAPI31(context: Context) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.cancel()
    }

    @Suppress("DEPRECATION")
    private fun cancelLegacy(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.cancel()
    }
}
