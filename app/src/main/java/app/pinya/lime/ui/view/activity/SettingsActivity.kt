package app.pinya.lime.ui.view.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import app.pinya.lime.LimeLauncherApp
import app.pinya.lime.R
import app.pinya.lime.data.repo.AppRepo
import app.pinya.lime.domain.model.AppModel
import app.pinya.lime.domain.model.menus.LockScreenMenu
import app.pinya.lime.domain.usecase.RefreshAppList
import app.pinya.lime.ui.utils.Utils
import app.pinya.lime.ui.utils.billing.BillingHelper
import app.pinya.lime.ui.view.adapter.LockScreenMenuAdapter
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class SettingsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)
        val layout = findViewById<ConstraintLayout>(R.id.contextMenuSettings_parent)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings, SettingsFragment(this, layout)).commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    class SettingsFragment(
        private val settingsContext: Context, private val layout: ConstraintLayout
    ) : PreferenceFragmentCompat() {

        private val billingHelper by lazy {
            (requireActivity().application as LimeLauncherApp).appContainer.billingHelper
        }

        private lateinit var lockScreenMenuAdapter: LockScreenMenuAdapter
        private var lockMenu: LockScreenMenu? = null

        private fun setLockScreenMenu(
            newLockMenu: LockScreenMenu?
        ) {
            lockMenu = newLockMenu
            lockScreenMenuAdapter.handleLockScreenMenu(lockMenu)

            if (lockMenu == null) {
                val prefs = PreferenceManager.getDefaultSharedPreferences(settingsContext)
                setSettingsAccordingToLockScreenAccessibilityStatus(prefs)
            }
        }

        private fun handleEnablePermissionClick() {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            lockScreenMenuAdapter = LockScreenMenuAdapter(
                settingsContext, ::setLockScreenMenu, ::handleEnablePermissionClick
            )

            val prefs = PreferenceManager.getDefaultSharedPreferences(settingsContext)

            billingHelper.purchaseState.observe(this) { purchaseState ->
                setIsPro(purchaseState == BillingHelper.ProPurchaseState.PURCHASED_AND_ACKNOWLEDGED)
            }

            lifecycleScope.launch {
                val appList = RefreshAppList(AppRepo()).invoke()

                setDateFormatSettings(appList)
                setTimeFormatSettings(appList)

                setHomeGridSettings(prefs)
                setDrawerGridSettings(prefs)

                setDoubleTapGestureSettings(prefs, appList)
                setSwipeUpGestureSettings(prefs, appList)
                setSwipeDownGestureSettings(prefs, appList)

                setIsPro(true)
            }
        }

        override fun onResume() {
            super.onResume()
            setLockScreenMenu(null)
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

                if (value == "screenLock" && !Utils.isAccessServiceEnabled(settingsContext)) setLockScreenMenu(
                    LockScreenMenu(layout)
                )

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

                if (value == "screenLock" && !Utils.isAccessServiceEnabled(settingsContext)) setLockScreenMenu(
                    LockScreenMenu(layout)
                )

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

                if (value == "screenLock" && !Utils.isAccessServiceEnabled(settingsContext)) setLockScreenMenu(
                    LockScreenMenu(layout)
                )

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
                val accessibilityActive = Utils.isAccessServiceEnabled(settingsContext)

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

        private fun launchPayForPremium() {
            billingHelper.startBillingFlow(requireActivity())
        }

        private fun setIsPro(isPro: Boolean = false) {
            requireActivity().setTitle(if (isPro) R.string.title_activity_settings_pro else R.string.title_activity_settings)

            val getPremiumMainButton = findPreference("preference_get_pro") as Preference?
            getPremiumMainButton?.isVisible = !isPro
            getPremiumMainButton?.setOnPreferenceClickListener {
                launchPayForPremium()
                true
            }

            val hideStatusBar =
                findPreference("preference_general_hide_status_bar") as SwitchPreference?
            val hideStatusBarPro =
                findPreference("preference_general_hide_status_bar_pro") as Preference?

            val homeShowInGrid = findPreference("preference_home_show_in_grid") as SwitchPreference?
            val homeShowInGridPro =
                findPreference("preference_home_show_in_grid_pro") as Preference?

            val homeAlignment = findPreference("preference_home_alignment") as ListPreference?
            val homeAlignmentPro =
                findPreference("preference_home_alignment_pro") as Preference?

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
                    launchPayForPremium()
                    true
                }
                homeShowInGridPro?.setOnPreferenceClickListener {
                    launchPayForPremium()
                    true
                }
                homeAlignmentPro?.setOnPreferenceClickListener {
                    launchPayForPremium()
                    true
                }
                doubleTapGesturePro?.setOnPreferenceClickListener {
                    launchPayForPremium()
                    true
                }
                swipeUpGesturePro?.setOnPreferenceClickListener {
                    launchPayForPremium()
                    true
                }
                swipeDownGesturePro?.setOnPreferenceClickListener {
                    launchPayForPremium()
                    true
                }
                drawerShowInGridPro?.setOnPreferenceClickListener {
                    launchPayForPremium()
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