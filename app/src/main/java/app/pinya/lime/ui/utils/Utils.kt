package app.pinya.lime.ui.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import app.pinya.lime.R
import app.pinya.lime.domain.model.AppModel
import app.pinya.lime.domain.model.BooleanPref
import app.pinya.lime.domain.model.IntPref
import app.pinya.lime.domain.model.StringPref
import app.pinya.lime.ui.view.holder.AppViewHolder
import kotlin.math.round

class Utils {
    companion object {
        fun pxToDp(context: Context, px: Float): Float {
            val displayMetrics: DisplayMetrics = context.resources.displayMetrics
            return px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)
        }

        fun dpToPx(context: Context, dp: Float): Float {
            val displayMetrics: DisplayMetrics = context.resources.displayMetrics
            return dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)
        }

        fun spToPx(context: Context, sp: Float): Float {
            val scaledDensity: Float = context.resources.displayMetrics.scaledDensity
            return sp * scaledDensity
        }

        @Suppress("DEPRECATION")
        fun vibrate(
            context: Context, timeInMs: Long = 1, amplitude: Int = VibrationEffect.DEFAULT_AMPLITUDE
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
                BooleanPref.GENERAL_HIDE_STATUS_BAR -> "preference_general_hide_status_bar"
                BooleanPref.GENERAL_CHANGE_WALLPAPER_DAILY -> "preference_general_change_wallpaper_daily"
                BooleanPref.GENERAL_ALSO_CHANGE_LOCK_SCREEN -> "preference_general_also_change_lockscreen"
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
                BooleanPref.GENERAL_HIDE_STATUS_BAR -> false
                BooleanPref.GENERAL_CHANGE_WALLPAPER_DAILY -> false
                BooleanPref.GENERAL_ALSO_CHANGE_LOCK_SCREEN -> false
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
                StringPref.GENERAL_NOTIFICATION_BADGES -> "preference_general_notification_general_badges"
                StringPref.GENERAL_ICON_PACK -> "preference_general_icon_pack"

                StringPref.HOME_ALIGNMENT -> "preference_home_alignment"
                StringPref.HOME_DOUBLE_TAP_ACTION -> "preference_home_double_tap_gesture"
                StringPref.HOME_DOUBLE_TAP_APP -> "preference_home_double_tap_app"
                StringPref.HOME_SWIPE_UP_ACTION -> "preference_home_swipe_up_gesture"
                StringPref.HOME_SWIPE_UP_APP -> "preference_home_swipe_up_app"
                StringPref.HOME_SWIPE_DOWN_ACTION -> "preference_home_swipe_down_gesture"
                StringPref.HOME_SWIPE_DOWN_APP -> "preference_home_swipe_down_app"

                StringPref.TIME_FORMAT -> "preference_time_format"
                StringPref.TIME_CLICK_APP -> "preference_time_click_app"

                StringPref.DATE_FORMAT -> "preference_date_format"
                StringPref.DATE_CLICK_APP -> "preference_date_click_app"
            }

            val defaultValue = when (preference) {
                StringPref.GENERAL_NOTIFICATION_BADGES -> "none"
                StringPref.GENERAL_ICON_PACK -> "None"

                StringPref.HOME_ALIGNMENT -> "left"
                StringPref.HOME_DOUBLE_TAP_ACTION -> "none"
                StringPref.HOME_DOUBLE_TAP_APP -> "none"
                StringPref.HOME_SWIPE_UP_ACTION -> "none"
                StringPref.HOME_SWIPE_UP_APP -> "none"
                StringPref.HOME_SWIPE_DOWN_ACTION -> "expandNotifications"
                StringPref.HOME_SWIPE_DOWN_APP -> "none"

                StringPref.TIME_FORMAT -> "0"
                StringPref.TIME_CLICK_APP -> "default"

                StringPref.DATE_FORMAT -> "1"
                StringPref.DATE_CLICK_APP -> "default"
            }

            return prefs.getString(key, defaultValue) ?: defaultValue
        }


        fun getIntPref(context: Context, preference: IntPref): Int {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)

            val key = when (preference) {
                IntPref.GENERAL_DATE_TIME_SCALE -> "preference_general_date_time_scale"
                IntPref.GENERAL_ICON_SCALE -> "preference_general_icon_scale"
                IntPref.GENERAL_TEXT_SCALE -> "preference_general_text_scale"
            }

            val defaultValue = when (preference) {
                IntPref.GENERAL_DATE_TIME_SCALE -> 3
                IntPref.GENERAL_ICON_SCALE -> 3
                IntPref.GENERAL_TEXT_SCALE -> 3

            }

            return prefs.getInt(key, defaultValue)
        }

        fun applyScale(value: Float, scale: Int): Float {
            val scaleFactor = when (scale) {
                0 -> 0.7f
                1 -> 0.8f
                2 -> 0.9f
                3 -> 1f
                4 -> 1.1f
                5 -> 1.2f
                6 -> 1.3f
                else -> 1f
            }

            return value * scaleFactor
        }

        fun isAccessServiceEnabled(context: Context): Boolean {
            val enabled = try {
                Settings.Secure.getInt(
                    context.applicationContext.contentResolver,
                    Settings.Secure.ACCESSIBILITY_ENABLED
                )
            } catch (e: Exception) {
                0
            }
            if (enabled == 1) {
                val enabledServicesString: String? = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                )
                return enabledServicesString?.contains(context.packageName + "/" + MyAccessibilityService::class.java.name)
                    ?: false
            }
            return false
        }

        fun isNotificationServiceEnabled(context: Context): Boolean {
            val notificationPackages = NotificationManagerCompat.getEnabledListenerPackages(context)

            return notificationPackages.contains(context.packageName)
        }

        fun setAppViewAccordingToOptions(
            context: Context,
            holder: AppViewHolder,
            currentApp: AppModel,
            isHome: Boolean,
            appNotifications: Int,
            isTutorial: Boolean
        ) {
            val showInGrid = getBooleanPref(
                context,
                if (isHome) BooleanPref.HOME_SHOW_IN_GRID else BooleanPref.DRAWER_SHOW_IN_GRID
            )
            val appLayout: LinearLayout = holder.itemView.findViewById(R.id.appLayout)
            val listFormat: LinearLayout = appLayout.findViewById(R.id.listFormat)
            val gridFormat: LinearLayout = appLayout.findViewById(R.id.gridFormat)

            listFormat.visibility = if (showInGrid) View.GONE else View.VISIBLE
            gridFormat.visibility = if (showInGrid) View.VISIBLE else View.GONE

            setAppViewLayoutAccordingToOptions(context, holder, currentApp, isHome)
            setAppViewIconAccordingToOptions(context, holder, currentApp, isHome)
            setAppViewNameAccordingToOptions(context, holder, currentApp, isHome, isTutorial)
            setAppViewNotificationsAccordingToOptions(context, holder, appNotifications)
        }


        private fun setAppViewLayoutAccordingToOptions(
            context: Context,
            holder: AppViewHolder,
            currentApp: AppModel,
            isHome: Boolean
        ) {
            val appLayout: LinearLayout = holder.itemView.findViewById(R.id.appLayout)
            val listFormat: LinearLayout = appLayout.findViewById(R.id.listFormat)
            val gridFormat: LinearLayout = appLayout.findViewById(R.id.gridFormat)

            listFormat.alpha = if (currentApp.hidden) 0.35f else 1f
            gridFormat.alpha = if (currentApp.hidden) 0.35f else 1f

            when (isHome) {
                true -> {
                    val alignment = getStringPref(context, StringPref.HOME_ALIGNMENT)
                    listFormat.gravity = when (alignment) {
                        "right" -> Gravity.END
                        "center" -> Gravity.CENTER
                        else -> Gravity.START
                    }
                }
                false -> {
                    listFormat.gravity = Gravity.START
                }
            }
        }

        private fun setAppViewIconAccordingToOptions(
            context: Context,
            holder: AppViewHolder,
            currentApp: AppModel,
            isHome: Boolean
        ) {
            val areIconsVisible = getBooleanPref(
                context,
                if (isHome) BooleanPref.HOME_SHOW_ICONS else BooleanPref.DRAWER_SHOW_ICONS
            )
            val iconScale = Utils.getIntPref(context, IntPref.GENERAL_ICON_SCALE)

            val appLayout: LinearLayout = holder.itemView.findViewById(R.id.appLayout)
            val listIcon: ImageView = appLayout.findViewById(R.id.listIcon)
            val gridIcon: ImageView = appLayout.findViewById(R.id.gridIcon)

            listIcon.setImageDrawable(currentApp.icon)
            gridIcon.setImageDrawable(currentApp.icon)

            listIcon.visibility = if (areIconsVisible) View.VISIBLE else View.GONE
            gridIcon.visibility = if (areIconsVisible) View.VISIBLE else View.GONE

            val listIconSize = Utils.dpToPx(context, Utils.applyScale(43f, iconScale)).toInt()
            val gridIconSize = Utils.dpToPx(context, Utils.applyScale(70f, iconScale)).toInt()

            listIcon.getLayoutParams().height = listIconSize
            listIcon.getLayoutParams().width = listIconSize

            gridIcon.getLayoutParams().height = gridIconSize
            gridIcon.getLayoutParams().width = gridIconSize
        }

        private fun setAppViewNameAccordingToOptions(
            context: Context,
            holder: AppViewHolder,
            currentApp: AppModel,
            isHome: Boolean,
            isTutorial: Boolean
        ) {
            val isTextBlack = getBooleanPref(context, BooleanPref.GENERAL_IS_TEXT_BLACK)
            val araLabelsVisible = getBooleanPref(
                context,
                if (isHome) BooleanPref.HOME_SHOW_LABELS else BooleanPref.DRAWER_SHOW_LABELS
            )
            val textScale = Utils.getIntPref(context, IntPref.GENERAL_TEXT_SCALE)

            val appLayout: LinearLayout = holder.itemView.findViewById(R.id.appLayout)
            val listName: TextView = appLayout.findViewById(R.id.listName)
            val gridName: TextView = appLayout.findViewById(R.id.gridName)

            listName.text = currentApp.name
            gridName.text = currentApp.name

            listName.textSize = Utils.applyScale(19f, textScale)
            gridName.textSize = Utils.applyScale(12f, textScale)

            listName.isSingleLine = !isTutorial
            gridName.isSingleLine = true

            listName.layoutParams.height =
                if (isTutorial) LayoutParams.WRAP_CONTENT else LayoutParams.MATCH_PARENT
            listName.gravity = if (isTutorial) Gravity.START else Gravity.CENTER

            val color =
                ContextCompat.getColor(context, if (isTextBlack) R.color.black else R.color.white)
            listName.setTextColor(color)
            gridName.setTextColor(color)

            gridName.alpha = if (araLabelsVisible) 1f else 0f
        }

        private fun setAppViewNotificationsAccordingToOptions(
            context: Context,
            holder: AppViewHolder,
            appNotifications: Int
        ) {
            val notificationType =
                getStringPref(context, StringPref.GENERAL_NOTIFICATION_BADGES)
            val appHasNotifications = notificationType != "none" && appNotifications > 0
            val showNumbers = notificationType == "showNumbers"

            val appLayout: LinearLayout = holder.itemView.findViewById(R.id.appLayout)

            val listNotification: LinearLayout = appLayout.findViewById(R.id.listNotification)
            val listNotificationMarker: LinearLayout =
                appLayout.findViewById(R.id.listNotificationMarker)
            val listNotificationNumber: TextView =
                appLayout.findViewById(R.id.listNotificationNumber)

            val gridNotification: LinearLayout = appLayout.findViewById(R.id.gridNotification)
            val gridNotificationMarker: LinearLayout =
                appLayout.findViewById(R.id.gridNotificationMarker)
            val gridNotificationNumber: TextView =
                appLayout.findViewById(R.id.gridNotificationNumber)

            val visibility = if (appHasNotifications) View.VISIBLE else View.GONE
            listNotification.visibility = visibility
            gridNotification.visibility = visibility

            val numbersVisibility = if (showNumbers) View.VISIBLE else View.GONE
            listNotificationNumber.visibility = numbersVisibility
            gridNotificationNumber.visibility = numbersVisibility

            if (appHasNotifications) {
                val number = if (appNotifications > 99) "+" else appNotifications.toString()
                listNotificationNumber.text = number
                gridNotificationNumber.text = number
            }

            val badgeSize = if (showNumbers) 24f else 16f
            listNotificationMarker.layoutParams.height = dpToPx(context, badgeSize).toInt()
            listNotificationMarker.layoutParams.width = dpToPx(context, badgeSize).toInt()
            gridNotificationMarker.layoutParams.height = dpToPx(context, badgeSize).toInt()
            gridNotificationMarker.layoutParams.width = dpToPx(context, badgeSize).toInt()
        }
    }
}

