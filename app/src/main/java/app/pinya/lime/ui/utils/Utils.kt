package app.pinya.lime.ui.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.DisplayMetrics
import androidx.preference.PreferenceManager
import app.pinya.lime.domain.model.BooleanPref
import app.pinya.lime.domain.model.StringPref
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

        fun getBooleanPref(context: Context, preference: BooleanPref): Boolean {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)

            val key = when (preference) {
                BooleanPref.GENERAL_SHOW_HIDDEN_APPS -> "preference_general_show_hidden_apps"
                BooleanPref.GENERAL_SHOW_STATUS_BAR -> "preference_general_show_status_bar"
                BooleanPref.GENERAL_CHANGE_WALLPAPER_DAILY -> "preference_general_change_wallpaper_daily"
                BooleanPref.GENERAL_IS_TEXT_BLACK -> "preference_general_is_text_black"
                BooleanPref.GENERAL_DIM_BACKGROUND -> "preference_general_dim_wallpaper"

                BooleanPref.HOME_SHOW_IN_GRID -> "preference_home_show_in_grid"
                BooleanPref.HOME_SHOW_LABELS -> "preference_home_show_labels"
                BooleanPref.HOME_SHOW_ICONS -> "preference_home_show_icons"

                BooleanPref.DRAWER_SHOW_IN_GRID -> "preference_drawer_show_in_grid"
                BooleanPref.DRAWER_SHOW_LABELS -> "preference_drawer_show_labels"
                BooleanPref.DRAWER_SHOW_ICONS -> "preference_drawer_show_icons"
                BooleanPref.DRAWER_SHOW_SEARCH_BAR -> "preference_drawer_show_search_bar"
                BooleanPref.DRAWER_AUTO_SHOW_KEYBOARD -> "preference_drawer_auto_show_keyboard"
                BooleanPref.DRAWER_AUTO_OPEN_APPS -> "preference_drawer_auto_open_apps"
                BooleanPref.DRAWER_SHOW_ALPHABET_FILTER -> "preference_drawer_show_alphabet_filter"

                BooleanPref.TIME_VISIBLE -> "preference_time_visible"

                BooleanPref.DATE_VISIBLE -> "preference_date_visible"
            }


            val defaultValue = when (preference) {
                BooleanPref.GENERAL_SHOW_HIDDEN_APPS -> false
                BooleanPref.GENERAL_SHOW_STATUS_BAR -> false
                BooleanPref.GENERAL_CHANGE_WALLPAPER_DAILY -> false
                BooleanPref.GENERAL_IS_TEXT_BLACK -> false
                BooleanPref.GENERAL_DIM_BACKGROUND -> true

                BooleanPref.HOME_SHOW_IN_GRID -> false
                BooleanPref.HOME_SHOW_LABELS -> true
                BooleanPref.HOME_SHOW_ICONS -> true

                BooleanPref.DRAWER_SHOW_IN_GRID -> false
                BooleanPref.DRAWER_SHOW_LABELS -> true
                BooleanPref.DRAWER_SHOW_ICONS -> true
                BooleanPref.DRAWER_SHOW_SEARCH_BAR -> true
                BooleanPref.DRAWER_AUTO_SHOW_KEYBOARD -> true
                BooleanPref.DRAWER_AUTO_OPEN_APPS -> true
                BooleanPref.DRAWER_SHOW_ALPHABET_FILTER -> true

                BooleanPref.TIME_VISIBLE -> true

                BooleanPref.DATE_VISIBLE -> true
            }

            return prefs.getBoolean(key, defaultValue)
        }


        fun getStringPref(context: Context, preference: StringPref): String {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)

            val key = when (preference) {
                StringPref.TIME_FORMAT -> "preference_time_format"
                StringPref.TIME_CLICK_APP -> "preference_time_click_app"

                StringPref.DATE_FORMAT -> "preference_date_format"
                StringPref.DATE_CLICK_APP -> "preference_date_click_app"
            }


            val defaultValue = when (preference) {
                StringPref.TIME_FORMAT -> "0"
                StringPref.TIME_CLICK_APP -> "default"

                StringPref.DATE_FORMAT -> "1"
                StringPref.DATE_CLICK_APP -> "default"
            }

            return prefs.getString(key, defaultValue) ?: defaultValue
        }
    }
}