package app.pinya.lime.ui.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.preference.*
import app.pinya.lime.LimeLauncherApp
import app.pinya.lime.R
import app.pinya.lime.data.memory.AppProvider
import app.pinya.lime.data.repo.AppRepo
import app.pinya.lime.domain.model.AppModel
import app.pinya.lime.domain.model.menus.BuyProMenu
import app.pinya.lime.domain.model.menus.LockScreenMenu
import app.pinya.lime.domain.model.menus.NotificationAccessMenu
import app.pinya.lime.domain.usecase.RefreshAppList
import app.pinya.lime.ui.utils.Utils
import app.pinya.lime.ui.utils.billing.BillingHelper
import app.pinya.lime.ui.view.adapter.BuyProMenuAdapter
import app.pinya.lime.ui.view.adapter.LockScreenMenuAdapter
import app.pinya.lime.ui.view.adapter.NotificationAccessMenuAdapter
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class SettingsActivity : AppCompatActivity() {


    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppProvider.initialize(this.application)

        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }


    class SettingsFragment : PreferenceFragmentCompat() {

        private val billingHelper by lazy {
            (requireActivity().application as LimeLauncherApp).appContainer.billingHelper
        }

        private lateinit var lockScreenMenuAdapter: LockScreenMenuAdapter
        private lateinit var buyProMenuAdapter: BuyProMenuAdapter
        private lateinit var notificationAccessMenuAdapter: NotificationAccessMenuAdapter

        private var lockMenu: LockScreenMenu? = null
        private var buyProMenu: BuyProMenu? = null
        private var notificationAccessMenu: NotificationAccessMenu? = null

        private var constraintLayout: ConstraintLayout? = null

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            constraintLayout = requireActivity().findViewById(R.id.contextMenuSettings_parent)
        }

        private fun setLockScreenMenu(newLockMenu: LockScreenMenu?) {
            lockMenu = newLockMenu
            lockScreenMenuAdapter.handleLockScreenMenu(lockMenu)

            if (lockMenu == null) {
                val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
                setSettingsAccordingToLockScreenAccessibilityStatus(prefs)
            }
        }

        private fun handleEnablePermissionClick() {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        private fun setBuyProMenu(newBuyProMenu: BuyProMenu?) {
            buyProMenu = newBuyProMenu
            buyProMenuAdapter.handleBuyProMenu(buyProMenu)
        }

        private fun handleBuyProClick() {
            setBuyProMenu(null)
            billingHelper.startBillingFlow(requireActivity())
        }

        private fun setNotificationAccessMenu(newNotificationAccessMenu: NotificationAccessMenu?) {
            notificationAccessMenu = newNotificationAccessMenu
            notificationAccessMenuAdapter.handleNotificationAccessMenu(notificationAccessMenu)

            if (notificationAccessMenu == null) setSettingsAccordingToNotificationAccessStatus()
        }

        private fun handleEnableNotificationAccessClick() {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            lockScreenMenuAdapter = LockScreenMenuAdapter(
                requireContext(), ::setLockScreenMenu, ::handleEnablePermissionClick
            )
            buyProMenuAdapter =
                BuyProMenuAdapter(requireContext(), ::setBuyProMenu, ::handleBuyProClick)
            notificationAccessMenuAdapter = NotificationAccessMenuAdapter(
                requireContext(), ::setNotificationAccessMenu, ::handleEnableNotificationAccessClick
            )

            val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

            lifecycleScope.launch {
                val appList = RefreshAppList(AppRepo()).invoke()

                setDateFormatSettings(appList)
                setTimeFormatSettings(appList)

                setHomeGridSettings(prefs)
                setDrawerGridSettings(prefs)

                setDoubleTapGestureSettings(prefs, appList)
                setSwipeUpGestureSettings(prefs, appList)
                setSwipeDownGestureSettings(prefs, appList)

                setNotificationBadgesSettings()
            }

            billingHelper.purchaseState.observe(this) { purchaseState ->
                setIsPro(purchaseState == BillingHelper.ProPurchaseState.PURCHASED_AND_ACKNOWLEDGED)
            }
        }

        override fun onResume() {
            super.onResume()
            setLockScreenMenu(null)
            setBuyProMenu(null)
            setNotificationAccessMenu(null)
        }

        private fun setDateFormatSettings(appList: MutableList<AppModel>) {

            fun getDateFormatResult(setting: Int): CharSequence {
                return SimpleDateFormat.getDateInstance(mapFormat(setting)).format(Date())
            }

            val dateFormat = findPreference("preference_date_format") as ListPreference?
            dateFormat?.entries = arrayOf(
                getDateFormatResult(0),
                getDateFormatResult(1),
                getDateFormatResult(2),
                getDateFormatResult(3)
            )
            dateFormat?.entryValues = arrayOf("0", "1", "2", "3")


            val dateClickApp = findPreference("preference_date_click_app") as ListPreference?
            if (dateClickApp != null) {
                val entries: Array<CharSequence?> = arrayOfNulls(appList.size + 2)
                val entryValues: Array<CharSequence?> = arrayOfNulls(appList.size + 2)

                entries[0] = "Default calendar app"
                entryValues[0] = "default"

                entries[1] = "Don\'t open any app"
                entryValues[1] = "none"

                appList.forEachIndexed { i, app ->
                    entries[i + 2] = app.originalName
                    entryValues[i + 2] = app.packageName
                }

                dateClickApp.entries = entries
                dateClickApp.entryValues = entryValues
            }
        }

        private fun setTimeFormatSettings(appList: MutableList<AppModel>) {

            fun getTimeFormatResult(setting: Int): CharSequence {
                return SimpleDateFormat.getTimeInstance(mapFormat(setting)).format(Date())
            }

            val timeFormat = findPreference("preference_time_format") as ListPreference?
            timeFormat?.entries = arrayOf(
                getTimeFormatResult(0),
                getTimeFormatResult(1),
                getTimeFormatResult(2),
                getTimeFormatResult(3)
            )
            timeFormat?.entryValues = arrayOf("0", "1", "2", "3")


            val timeClickApp = findPreference("preference_time_click_app") as ListPreference?
            if (timeClickApp != null) {
                val entries: Array<CharSequence?> = arrayOfNulls(appList.size + 2)
                val entryValues: Array<CharSequence?> = arrayOfNulls(appList.size + 2)

                entries[0] = "Default clock app"
                entryValues[0] = "default"

                entries[1] = "Don\'t open any app"
                entryValues[1] = "none"

                appList.forEachIndexed { i, app ->
                    entries[i + 2] = app.originalName
                    entryValues[i + 2] = app.packageName
                }

                timeClickApp.entries = entries
                timeClickApp.entryValues = entryValues
            }
        }

        private fun setHomeGridSettings(prefs: SharedPreferences) {
            val homeShowInGrid = findPreference("preference_home_show_in_grid") as SwitchPreference?
            val homeShowIcons = findPreference("preference_home_show_icons") as SwitchPreference?
            val homeShowLabels = findPreference("preference_home_show_labels") as SwitchPreference?

            fun setHomeDependencies(showInGrid: Boolean) {
                if (showInGrid) {
                    homeShowIcons?.isEnabled = false
                    homeShowIcons?.isChecked = true
                    homeShowLabels?.isEnabled = true
                } else {
                    homeShowIcons?.isEnabled = true
                    homeShowLabels?.isEnabled = false
                    homeShowLabels?.isChecked = true
                }
            }

            setHomeDependencies(prefs.getBoolean("preference_home_show_in_grid", false))

            homeShowInGrid?.setOnPreferenceChangeListener { _, newValue ->
                setHomeDependencies(newValue as Boolean)
                true
            }
        }

        private fun setDrawerGridSettings(prefs: SharedPreferences) {
            val drawerShowInGrid =
                findPreference("preference_drawer_show_in_grid") as SwitchPreference?
            val drawerShowIcons =
                findPreference("preference_drawer_show_icons") as SwitchPreference?
            val drawerShowLabels =
                findPreference("preference_drawer_show_labels") as SwitchPreference?

            fun setDrawerDependencies(showInGrid: Boolean) {
                if (showInGrid) {
                    drawerShowIcons?.isEnabled = false
                    drawerShowIcons?.isChecked = true
                    drawerShowLabels?.isEnabled = true
                } else {
                    drawerShowIcons?.isEnabled = true
                    drawerShowLabels?.isEnabled = false
                    drawerShowLabels?.isChecked = true
                }
            }

            setDrawerDependencies(prefs.getBoolean("preference_drawer_show_in_grid", false))

            drawerShowInGrid?.setOnPreferenceChangeListener { _, newValue ->
                setDrawerDependencies(newValue as Boolean)
                true
            }
        }

        private fun setDoubleTapGestureSettings(
            prefs: SharedPreferences, appList: MutableList<AppModel>
        ) {

            val doubleTapGesture =
                findPreference("preference_home_double_tap_gesture") as ListPreference?
            val doubleTapApp = findPreference("preference_home_double_tap_app") as ListPreference?

            doubleTapApp?.isEnabled =
                prefs.getString("preference_home_double_tap_gesture", "none") == "openApp"

            doubleTapGesture?.setOnPreferenceChangeListener { _, newValue ->
                val value = newValue as String
                doubleTapApp?.isEnabled = value == "openApp"
                val accessServiceDisabled = !Utils.isAccessServiceEnabled(requireContext())

                if (constraintLayout != null && value == "screenLock" && accessServiceDisabled)
                    setLockScreenMenu(LockScreenMenu(constraintLayout!!))

                true
            }

            if (doubleTapApp != null) {
                val entries: Array<CharSequence?> = arrayOfNulls(appList.size + 1)
                val entryValues: Array<CharSequence?> = arrayOfNulls(appList.size + 1)

                entries[0] = "Don\'t open any app"
                entryValues[0] = "none"

                appList.forEachIndexed { i, app ->
                    entries[i + 1] = app.originalName
                    entryValues[i + 1] = app.packageName
                }

                doubleTapApp.entries = entries
                doubleTapApp.entryValues = entryValues
            }
        }

        private fun setSwipeUpGestureSettings(
            prefs: SharedPreferences, appList: MutableList<AppModel>
        ) {
            val swipeUpGesture =
                findPreference("preference_home_swipe_up_gesture") as ListPreference?
            val swipeUpApp = findPreference("preference_home_swipe_up_app") as ListPreference?

            swipeUpApp?.isEnabled =
                prefs.getString("preference_home_swipe_up_gesture", "none") == "openApp"

            swipeUpGesture?.setOnPreferenceChangeListener { _, newValue ->
                val value = newValue as String
                swipeUpApp?.isEnabled = value == "openApp"
                val accessServiceDisabled = !Utils.isAccessServiceEnabled(requireContext())

                if (constraintLayout != null && value == "screenLock" && accessServiceDisabled)
                    setLockScreenMenu(LockScreenMenu(constraintLayout!!))

                true
            }

            if (swipeUpApp != null) {
                val entries: Array<CharSequence?> = arrayOfNulls(appList.size + 1)
                val entryValues: Array<CharSequence?> = arrayOfNulls(appList.size + 1)

                entries[0] = "Don\'t open any app"
                entryValues[0] = "none"

                appList.forEachIndexed { i, app ->
                    entries[i + 1] = app.originalName
                    entryValues[i + 1] = app.packageName
                }

                swipeUpApp.entries = entries
                swipeUpApp.entryValues = entryValues
            }
        }

        private fun setSwipeDownGestureSettings(
            prefs: SharedPreferences, appList: MutableList<AppModel>
        ) {
            val swipeDownGesture =
                findPreference("preference_home_swipe_down_gesture") as ListPreference?
            val swipeDownApp = findPreference("preference_home_swipe_down_app") as ListPreference?

            swipeDownApp?.isEnabled =
                prefs.getString("preference_home_swipe_down_gesture", "none") == "openApp"

            swipeDownGesture?.setOnPreferenceChangeListener { _, newValue ->
                val value = newValue as String
                swipeDownApp?.isEnabled = value == "openApp"
                val accessServiceDisabled = !Utils.isAccessServiceEnabled(requireContext())

                if (constraintLayout != null && value == "screenLock" && accessServiceDisabled)
                    setLockScreenMenu(LockScreenMenu(constraintLayout!!))

                true
            }

            if (swipeDownApp != null) {
                val entries: Array<CharSequence?> = arrayOfNulls(appList.size + 1)
                val entryValues: Array<CharSequence?> = arrayOfNulls(appList.size + 1)

                entries[0] = "Don\'t open any app"
                entryValues[0] = "none"

                appList.forEachIndexed { i, app ->
                    entries[i + 1] = app.originalName
                    entryValues[i + 1] = app.packageName
                }

                swipeDownApp.entries = entries
                swipeDownApp.entryValues = entryValues
            }
        }

        private fun setSettingsAccordingToLockScreenAccessibilityStatus(prefs: SharedPreferences) {
            val swipeUpGesture =
                findPreference("preference_home_swipe_up_gesture") as ListPreference?
            val doubleTapGesture =
                findPreference("preference_home_double_tap_gesture") as ListPreference?
            val swipeDownGesture =
                findPreference("preference_home_swipe_down_gesture") as ListPreference?

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val accessibilityActive = Utils.isAccessServiceEnabled(requireContext())

                if (!accessibilityActive) {
                    val lockSelectedOnDoubleTap = prefs.getString(
                        "preference_home_double_tap_gesture", "none"
                    ) == "screenLock"

                    val lockSelectedOnSwipeUp = prefs.getString(
                        "preference_home_swipe_up_gesture", "none"
                    ) == "screenLock"

                    val lockSelectedOnSwipeDown = prefs.getString(
                        "preference_home_swipe_down_gesture", "expandNotifications"
                    ) == "screenLock"

                    if (lockSelectedOnDoubleTap) doubleTapGesture?.value = "none"
                    if (lockSelectedOnSwipeUp) swipeUpGesture?.value = "none"
                    if (lockSelectedOnSwipeDown) swipeDownGesture?.value = "expandNotifications"
                }

            } else {
                val entries: Array<CharSequence?> = arrayOfNulls(4)
                val entryValues: Array<CharSequence?> = arrayOfNulls(4)

                entries[0] = "None"
                entryValues[0] = "none"
                entries[1] = "Expand notifications"
                entryValues[1] = "expandNotifications"
                entries[2] = "Open app"
                entryValues[2] = "openApp"
                entries[3] = "Assistant"
                entryValues[3] = "assistant"

                doubleTapGesture?.entries = entries
                doubleTapGesture?.entryValues = entryValues
                swipeUpGesture?.entries = entries
                swipeUpGesture?.entryValues = entryValues
                swipeDownGesture?.entries = entries
                swipeDownGesture?.entryValues = entryValues
            }
        }


        private fun setNotificationBadgesSettings() {
            val notificationBadges =
                findPreference("preference_notification_general_badges") as ListPreference?

            notificationBadges?.setOnPreferenceChangeListener { _, newValue ->
                val value = newValue as String
                val notificationServiceDisabled =
                    !Utils.isNotificationServiceEnabled(requireContext())

                if (constraintLayout != null && value != "none" && notificationServiceDisabled)
                    setNotificationAccessMenu(NotificationAccessMenu(constraintLayout!!))

                true
            }
        }

        private fun setSettingsAccordingToNotificationAccessStatus() {
            val notificationBadges =
                findPreference("preference_notification_general_badges") as ListPreference?

            val notificationAccessActive = Utils.isNotificationServiceEnabled(requireContext())

            if (!notificationAccessActive) notificationBadges?.value = "none"
        }

        private fun openBuyProMenu() {
            if (constraintLayout != null) setBuyProMenu(BuyProMenu(constraintLayout!!))
        }

        private fun setIsPro(isPro: Boolean = false) {
            requireActivity().setTitle(if (isPro) R.string.title_activity_settings_pro else R.string.title_activity_settings)

            val getPremiumMainButton = findPreference("preference_get_pro") as Preference?
            getPremiumMainButton?.isVisible = !isPro
            getPremiumMainButton?.setOnPreferenceClickListener {
                openBuyProMenu()
                true
            }

            val hideStatusBar =
                findPreference("preference_general_hide_status_bar") as SwitchPreference?
            val hideStatusBarPro =
                findPreference("preference_general_hide_status_bar_pro") as Preference?

            val notificationBadges =
                findPreference("preference_notification_general_badges") as ListPreference?
            val notificationBadgesPro =
                findPreference("preference_general_notification_badges_pro") as Preference?

            val homeShowInGrid = findPreference("preference_home_show_in_grid") as SwitchPreference?
            val homeShowInGridPro =
                findPreference("preference_home_show_in_grid_pro") as Preference?

            val homeAlignment = findPreference("preference_home_alignment") as ListPreference?
            val homeAlignmentPro = findPreference("preference_home_alignment_pro") as Preference?

            val doubleTapGesture =
                findPreference("preference_home_double_tap_gesture") as ListPreference?
            val doubleTapGesturePro =
                findPreference("preference_home_double_tap_gesture_pro") as Preference?

            val swipeUpGesture =
                findPreference("preference_home_swipe_up_gesture") as ListPreference?
            val swipeUpGesturePro =
                findPreference("preference_home_swipe_up_gesture_pro") as Preference?

            val swipeDownGesture =
                findPreference("preference_home_swipe_down_gesture") as ListPreference?
            val swipeDownGesturePro =
                findPreference("preference_home_swipe_down_gesture_pro") as Preference?

            val drawerShowInGrid =
                findPreference("preference_drawer_show_in_grid") as SwitchPreference?
            val drawerShowInGridPro =
                findPreference("preference_drawer_show_in_grid_pro") as Preference?

            hideStatusBar?.isVisible = isPro
            hideStatusBarPro?.isVisible = !isPro

            notificationBadges?.isVisible = isPro
            notificationBadgesPro?.isVisible = !isPro

            homeShowInGrid?.isVisible = isPro
            homeShowInGridPro?.isVisible = !isPro

            homeAlignment?.isVisible = isPro
            homeAlignmentPro?.isVisible = !isPro

            doubleTapGesture?.isVisible = isPro
            doubleTapGesturePro?.isVisible = !isPro

            swipeUpGesture?.isVisible = isPro
            swipeUpGesturePro?.isVisible = !isPro

            swipeDownGesture?.isVisible = isPro
            swipeDownGesturePro?.isVisible = !isPro

            drawerShowInGrid?.isVisible = isPro
            drawerShowInGridPro?.isVisible = !isPro

            if (!isPro) {
                hideStatusBarPro?.setOnPreferenceClickListener {
                    openBuyProMenu()
                    true
                }
                notificationBadgesPro?.setOnPreferenceClickListener {
                    openBuyProMenu()
                    true
                }
                homeShowInGridPro?.setOnPreferenceClickListener {
                    openBuyProMenu()
                    true
                }
                homeAlignmentPro?.setOnPreferenceClickListener {
                    openBuyProMenu()
                    true
                }
                doubleTapGesturePro?.setOnPreferenceClickListener {
                    openBuyProMenu()
                    true
                }
                swipeUpGesturePro?.setOnPreferenceClickListener {
                    openBuyProMenu()
                    true
                }
                swipeDownGesturePro?.setOnPreferenceClickListener {
                    openBuyProMenu()
                    true
                }
                drawerShowInGridPro?.setOnPreferenceClickListener {
                    openBuyProMenu()
                    true
                }
            }
        }
    }

    companion object {
        fun mapFormat(num: Int): Int {
            return when (num) {
                0 -> DateFormat.SHORT
                1 -> DateFormat.MEDIUM
                2 -> DateFormat.LONG
                else -> DateFormat.FULL
            }
        }
    }
}