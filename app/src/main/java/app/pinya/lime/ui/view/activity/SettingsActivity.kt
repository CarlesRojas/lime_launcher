package app.pinya.lime.ui.view.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import app.pinya.lime.R
import app.pinya.lime.data.repo.AppRepo
import app.pinya.lime.domain.usecase.RefreshAppList
import app.pinya.lime.ui.utils.MyAccessibilityService
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class SettingsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment(this))
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    class SettingsFragment(private val settingsContext: Context) : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            setSettingsValues()
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

        private fun setSettingsValues() {
            val prefs = PreferenceManager.getDefaultSharedPreferences(settingsContext)

            lifecycleScope.launch {
                val appList = RefreshAppList(AppRepo()).invoke()

                // DATE FORMAT
                val dateFormat = findPreference("preference_date_format") as ListPreference?
                dateFormat?.entries =
                    arrayOf(
                        getDateFormatResult(0),
                        getDateFormatResult(1),
                        getDateFormatResult(2),
                        getDateFormatResult(3)
                    )
                dateFormat?.entryValues = arrayOf("0", "1", "2", "3")

                // TIME FORMAT
                val timeFormat = findPreference("preference_time_format") as ListPreference?
                timeFormat?.entries =
                    arrayOf(
                        getTimeFormatResult(0),
                        getTimeFormatResult(1),
                        getTimeFormatResult(2),
                        getTimeFormatResult(3)
                    )
                timeFormat?.entryValues = arrayOf("0", "1", "2", "3")

                // DATE CLICK APP
                val dateClickApp = findPreference("preference_date_click_app") as ListPreference?
                if (dateClickApp != null) {
                    val entries: Array<CharSequence?> = arrayOfNulls(appList.size + 2)
                    val entryValues: Array<CharSequence?> = arrayOfNulls(appList.size + 2)

                    entries[0] = "Default calendar app"
                    entryValues[0] = "default"

                    entries[1] = "Don't open any app"
                    entryValues[1] = "none"

                    appList.forEachIndexed { i, app ->
                        entries[i + 2] = app.originalName
                        entryValues[i + 2] = app.packageName
                    }

                    dateClickApp.entries = entries
                    dateClickApp.entryValues = entryValues
                }

                // TIME CLICK APP
                val timeClickApp = findPreference("preference_time_click_app") as ListPreference?
                if (timeClickApp != null) {
                    val entries: Array<CharSequence?> = arrayOfNulls(appList.size + 2)
                    val entryValues: Array<CharSequence?> = arrayOfNulls(appList.size + 2)

                    entries[0] = "Default clock app"
                    entryValues[0] = "default"

                    entries[1] = "Don't open any app"
                    entryValues[1] = "none"

                    appList.forEachIndexed { i, app ->
                        entries[i + 2] = app.originalName
                        entryValues[i + 2] = app.packageName
                    }

                    timeClickApp.entries = entries
                    timeClickApp.entryValues = entryValues
                }

                // HOME GRID DEPENDENCIES
                val homeShowInGrid =
                    findPreference("preference_home_show_in_grid") as SwitchPreference?
                val homeShowIcons =
                    findPreference("preference_home_show_icons") as SwitchPreference?
                val homeShowLabels =
                    findPreference("preference_home_show_labels") as SwitchPreference?

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

                // DRAWER GRID DEPENDENCIES
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

                // DOUBLE TAP GESTURE
                val doubleTapGesture =
                    findPreference("preference_home_double_tap_gesture") as ListPreference?
                val doubleTapApp =
                    findPreference("preference_home_double_tap_app") as ListPreference?

                doubleTapApp?.isEnabled =
                    prefs.getString("preference_home_double_tap_gesture", "none") == "openApp"

                doubleTapGesture?.setOnPreferenceChangeListener { _, newValue ->
                    doubleTapApp?.isEnabled = newValue as String == "openApp"

                    if (newValue as String == "screenLock" && !isAccessServiceEnabled(
                            settingsContext
                        )
                    )
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))

                    true
                }

                if (doubleTapApp != null) {
                    val entries: Array<CharSequence?> = arrayOfNulls(appList.size + 1)
                    val entryValues: Array<CharSequence?> = arrayOfNulls(appList.size + 1)

                    entries[0] = "Don't open any app"
                    entryValues[0] = "none"

                    appList.forEachIndexed { i, app ->
                        entries[i + 1] = app.originalName
                        entryValues[i + 1] = app.packageName
                    }

                    doubleTapApp.entries = entries
                    doubleTapApp.entryValues = entryValues
                }


                // SWIPE DOWN GESTURE
                val swipeDownGesture =
                    findPreference("preference_home_swipe_down_gesture") as ListPreference?
                val swipeDownApp =
                    findPreference("preference_home_swipe_down_app") as ListPreference?

                swipeDownApp?.isEnabled =
                    prefs.getString("preference_home_swipe_down_gesture", "none") == "openApp"

                swipeDownGesture?.setOnPreferenceChangeListener { _, newValue ->
                    swipeDownApp?.isEnabled = newValue as String == "openApp"

                    if (newValue as String == "screenLock" && !isAccessServiceEnabled(
                            settingsContext
                        )
                    )
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))

                    true
                }

                if (swipeDownApp != null) {
                    val entries: Array<CharSequence?> = arrayOfNulls(appList.size + 1)
                    val entryValues: Array<CharSequence?> = arrayOfNulls(appList.size + 1)

                    entries[0] = "Don't open any app"
                    entryValues[0] = "none"

                    appList.forEachIndexed { i, app ->
                        entries[i + 1] = app.originalName
                        entryValues[i + 1] = app.packageName
                    }

                    swipeDownApp.entries = entries
                    swipeDownApp.entryValues = entryValues
                }

                // LOCK SCREEN ACCESSIBILITY
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val accessibilityActive = isAccessServiceEnabled(settingsContext)

                    if (!accessibilityActive) {
                        val lockSelectedOnSwipeDown = prefs.getString(
                            "preference_home_swipe_down_gesture",
                            "none"
                        ) == "screenLock"

                        val lockSelectedOnDoubleTap = prefs.getString(
                            "preference_home_double_tap_gesture",
                            "none"
                        ) == "screenLock"

                        if (lockSelectedOnSwipeDown) swipeDownGesture?.value = "none"
                        if (lockSelectedOnDoubleTap) doubleTapGesture?.value = "none"
                    }

                } else {
                    val entries: Array<CharSequence?> = arrayOfNulls(3)
                    val entryValues: Array<CharSequence?> = arrayOfNulls(3)

                    entries[0] = "None"
                    entryValues[0] = "none"
                    entries[1] = "Open app"
                    entryValues[1] = "openApp"
                    entries[2] = "Assistant"
                    entryValues[3] = "assistant"

                    swipeDownGesture?.entries = entries
                    swipeDownGesture?.entryValues = entryValues
                    doubleTapGesture?.entries = entries
                    doubleTapGesture?.entryValues = entryValues
                }
            }
        }

        private fun getDateFormatResult(setting: Int): CharSequence {
            return SimpleDateFormat.getDateInstance(mapFormat(setting))
                .format(Date())
        }

        private fun getTimeFormatResult(setting: Int): CharSequence {
            return SimpleDateFormat.getTimeInstance(mapFormat(setting))
                .format(Date())
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