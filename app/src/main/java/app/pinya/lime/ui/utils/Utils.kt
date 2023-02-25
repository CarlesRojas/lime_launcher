package app.pinya.lime.ui.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.DisplayMetrics
import kotlin.math.round

class Utils {
    companion object {
        fun pxToDp(context: Context, px: Int): Int {
            val displayMetrics: DisplayMetrics = context.resources.displayMetrics
            return round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).toInt()
        }


        fun dpToPx(context: Context, dp: Int): Int {
            val displayMetrics: DisplayMetrics = context.resources.displayMetrics
            return round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).toInt()
        }

        @Suppress("DEPRECATION")
        fun vibrate(
            context: Context,
            timeInMs: Long = 1,
            amplitude: Int = VibrationEffect.DEFAULT_AMPLITUDE
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createOneShot(timeInMs, amplitude))
            } else {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(VibrationEffect.createOneShot(timeInMs, amplitude))
            }
        }
    }

}