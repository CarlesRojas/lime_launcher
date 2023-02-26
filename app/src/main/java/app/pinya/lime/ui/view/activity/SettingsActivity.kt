package app.pinya.lime.ui.view.activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import app.pinya.lime.R
import app.pinya.lime.data.repo.AppRepo
import app.pinya.lime.domain.usecase.RefreshAppList
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


    class SettingsFragment(val settingsContext: Context) : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            setSettingsValues()
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
                    val entries: Array<CharSequence?> = arrayOfNulls<CharSequence>(appList.size + 2)
                    val entryValues: Array<CharSequence?> =
                        arrayOfNulls<CharSequence>(appList.size + 2)

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
                    val entries: Array<CharSequence?> = arrayOfNulls<CharSequence>(appList.size + 2)
                    val entryValues: Array<CharSequence?> =
                        arrayOfNulls<CharSequence>(appList.size + 2)

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

                val prefsEditor = prefs.edit()

                prefsEditor.apply()
            }

        }

        private fun getDateFormatResult(setting: Int): CharSequence {
            return SimpleDateFormat.getDateInstance(SettingsActivity.mapFormat(setting))
                .format(Date())
        }

        private fun getTimeFormatResult(setting: Int): CharSequence {
            return SimpleDateFormat.getTimeInstance(SettingsActivity.mapFormat(setting))
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