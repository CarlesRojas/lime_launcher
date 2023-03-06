package app.pinya.lime.ui.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import app.pinya.lime.R
import app.pinya.lime.domain.model.AppModel
import app.pinya.lime.domain.model.BooleanPref
import app.pinya.lime.domain.model.StringPref
import app.pinya.lime.ui.view.holder.AppViewHolder
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

        fun spToPx(context: Context, sp: Int): Float {
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
                StringPref.GENERAL_NOTIFICATION_BADGES -> "preference_notification_general_badges"

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
            appNotifications: Int
        ) {
            setAppViewLayoutAccordingToOptions(context, holder, currentApp, isHome)
            setAppViewIconAccordingToOptions(context, holder, currentApp, isHome)
            setAppViewNameAccordingToOptions(context, holder, currentApp, isHome)
            setAppViewNotificationsAccordingToOptions(context, holder, isHome, appNotifications)
        }


        private fun setAppViewLayoutAccordingToOptions(
            context: Context,
            holder: AppViewHolder,
            currentApp: AppModel,
            isHome: Boolean
        ) {
            val showInGrid = getBooleanPref(
                context,
                if (isHome) BooleanPref.HOME_SHOW_IN_GRID else BooleanPref.DRAWER_SHOW_IN_GRID
            )
            val areIconsVisible = getBooleanPref(
                context,
                if (isHome) BooleanPref.HOME_SHOW_ICONS else BooleanPref.DRAWER_SHOW_ICONS
            )

            val linearLayout: LinearLayout = holder.itemView.findViewById(R.id.appLayout)

            linearLayout.alpha = if (currentApp.hidden) 0.35f else 1f

            when (showInGrid) {
                true -> {
                    linearLayout.orientation = LinearLayout.VERTICAL
                    val padding = dpToPx(context, 20)
                    linearLayout.setPadding(0, padding, 0, padding)
                    linearLayout.gravity = Gravity.CENTER
                }
                false -> {
                    linearLayout.orientation = LinearLayout.HORIZONTAL
                    val padding = dpToPx(context, if (areIconsVisible) 12 else 18)
                    linearLayout.setPadding(0, padding, 0, padding)

                    when (isHome) {
                        true -> {
                            val alignment = getStringPref(context, StringPref.HOME_ALIGNMENT)

                            linearLayout.gravity = when (alignment) {
                                "right" -> Gravity.END
                                "center" -> Gravity.CENTER
                                else -> Gravity.START
                            }
                        }
                        false -> {
                            linearLayout.gravity = Gravity.START
                        }
                    }

                }
            }
        }

        private fun setAppViewIconAccordingToOptions(
            context: Context,
            holder: AppViewHolder,
            currentApp: AppModel,
            isHome: Boolean
        ) {

            val showInGrid = getBooleanPref(
                context,
                if (isHome) BooleanPref.HOME_SHOW_IN_GRID else BooleanPref.DRAWER_SHOW_IN_GRID
            )
            val areIconsVisible = getBooleanPref(
                context,
                if (isHome) BooleanPref.HOME_SHOW_ICONS else BooleanPref.DRAWER_SHOW_ICONS
            )

            val imageView: ImageView = holder.itemView.findViewById(R.id.appIcon)

            imageView.setImageDrawable(currentApp.icon)
            imageView.visibility = if (areIconsVisible) View.VISIBLE else View.GONE

            when (showInGrid) {
                true -> {
                    imageView.layoutParams.height = dpToPx(context, 68)
                    imageView.layoutParams.width = dpToPx(context, 68)

                    val marginParams = MarginLayoutParams(imageView.layoutParams)
                    marginParams.setMargins(0, 0, 0, dpToPx(context, 6))
                    val layoutParams = LinearLayout.LayoutParams(marginParams)
                    imageView.layoutParams = layoutParams
                }
                false -> {

                    imageView.layoutParams.height = dpToPx(context, 42)
                    imageView.layoutParams.width = dpToPx(context, 42)

                    val marginParams = MarginLayoutParams(imageView.layoutParams)
                    marginParams.setMargins(0, 0, dpToPx(context, 18), 0)
                    val layoutParams = LinearLayout.LayoutParams(marginParams)
                    imageView.layoutParams = layoutParams
                }
            }
        }

        private fun setAppViewNameAccordingToOptions(
            context: Context,
            holder: AppViewHolder,
            currentApp: AppModel,
            isHome: Boolean
        ) {
            val isTextBlack = getBooleanPref(context, BooleanPref.GENERAL_IS_TEXT_BLACK)
            val showInGrid = getBooleanPref(
                context,
                if (isHome) BooleanPref.HOME_SHOW_IN_GRID else BooleanPref.DRAWER_SHOW_IN_GRID
            )
            val araLabelsVisible =
                !showInGrid || getBooleanPref(
                    context,
                    if (isHome) BooleanPref.HOME_SHOW_LABELS else BooleanPref.DRAWER_SHOW_LABELS
                )

            val textView: TextView = holder.itemView.findViewById(R.id.appName)

            textView.text = currentApp.name
            textView.isSingleLine = true
            textView.layoutParams.height = LayoutParams.MATCH_PARENT
            textView.gravity = Gravity.CENTER
            textView.setTextColor(
                ContextCompat.getColor(context, if (isTextBlack) R.color.black else R.color.white)
            )
            textView.alpha = if (araLabelsVisible) 1f else 0f

            when (showInGrid) {
                true -> {
                    textView.textSize = 12f
                    textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                }
                false -> {
                    textView.textSize = 19.5f
                    textView.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
                }
            }
        }

        private fun setAppViewNotificationsAccordingToOptions(
            context: Context,
            holder: AppViewHolder,
            isHome: Boolean,
            appNotifications: Int
        ) {
            val showInGrid = getBooleanPref(
                context,
                if (isHome) BooleanPref.HOME_SHOW_IN_GRID else BooleanPref.DRAWER_SHOW_IN_GRID
            )
            val notificationType =
                getStringPref(context, StringPref.GENERAL_NOTIFICATION_BADGES)
            val appHasNotifications = notificationType != "none" && appNotifications > 0
            val showNumbers = notificationType == "showNumbers"

            val notificationBadgeList: LinearLayout =
                holder.itemView.findViewById(R.id.notificationBadgeList)
            val notificationBadgeListNumber: TextView =
                holder.itemView.findViewById(R.id.notificationBadgeListNumber)
            val notificationBadgeListMarker: LinearLayout =
                holder.itemView.findViewById(R.id.notificationBadgeListMarker)
            val notificationBadgeGrid: LinearLayout =
                holder.itemView.findViewById(R.id.notificationBadgeGrid)
            val notificationBadgeGridNumber: TextView =
                holder.itemView.findViewById(R.id.notificationBadgeGridNumber)
            val notificationBadgeGridMarker: LinearLayout =
                holder.itemView.findViewById(R.id.notificationBadgeGridMarker)

            val badgeSize = if (showNumbers) 22 else 14

            when (showInGrid) {
                true -> {
                    notificationBadgeList.visibility = View.GONE
                    notificationBadgeGrid.visibility =
                        if (appHasNotifications) View.VISIBLE else View.GONE

                    notificationBadgeGridNumber.visibility =
                        if (showNumbers) View.VISIBLE else View.GONE

                    if (appHasNotifications) notificationBadgeGridNumber.text =
                        if (appNotifications > 99) "+" else appNotifications.toString()

                    val badgeMarginParams = MarginLayoutParams(notificationBadgeGrid.layoutParams)
                    badgeMarginParams.setMargins(0, dpToPx(context, -68 - badgeSize), 0, 0)
                    val badgeLayoutParams = LinearLayout.LayoutParams(badgeMarginParams)
                    notificationBadgeGrid.layoutParams = badgeLayoutParams

                    notificationBadgeGridMarker.layoutParams.height = dpToPx(context, badgeSize)
                    notificationBadgeGridMarker.layoutParams.width = dpToPx(context, badgeSize)
                }
                false -> {

                    notificationBadgeGrid.visibility = View.GONE
                    notificationBadgeList.visibility =
                        if (appHasNotifications) View.VISIBLE else View.GONE

                    notificationBadgeListNumber.visibility =
                        if (showNumbers) View.VISIBLE else View.GONE

                    if (appHasNotifications) notificationBadgeListNumber.text =
                        if (appNotifications > 99) "+" else appNotifications.toString()

                    val badgeMarginParams = MarginLayoutParams(notificationBadgeList.layoutParams)
                    badgeMarginParams.setMargins(dpToPx(context, 18), 0, 0, 0)
                    val badgeLayoutParams = LinearLayout.LayoutParams(badgeMarginParams)
                    notificationBadgeList.layoutParams = badgeLayoutParams

                    notificationBadgeListMarker.layoutParams.height = dpToPx(context, badgeSize)
                    notificationBadgeListMarker.layoutParams.width = dpToPx(context, badgeSize)
                }
            }
        }

    }
}
