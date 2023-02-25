package app.pinya.lime.data.repo

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import app.pinya.lime.data.memory.AppProvider
import app.pinya.lime.domain.model.SettingsModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SettingsRepo @Inject constructor() {
    private val sharedPreferences: SharedPreferences
    private val oldSharedPreferences: SharedPreferences

    private val oldPreferencesName = "LimeLauncherPreferences"
    private val preferencesName = "LimeLauncherSharedPreferences"

    private enum class SettingsDataKey {
        SETTINGS, OLD_SETTINGS_RETRIEVED,
    }

    init {
        val context = AppProvider.getContext()

        this.sharedPreferences =
            context.getSharedPreferences(this.preferencesName, Context.MODE_PRIVATE)

        this.oldSharedPreferences =
            context.getSharedPreferences(this.oldPreferencesName, Context.MODE_PRIVATE)
    }

    suspend fun getSettingsFromMemory(): SettingsModel {
        return withContext(Dispatchers.IO) {
            val gson = Gson()
            val json: String =
                sharedPreferences.getString(SettingsDataKey.SETTINGS.toString(), "") ?: ""
            var settings = gson.fromJson(json, SettingsModel::class.java)

            if (settings == null) settings = SettingsModel()

            settings = getOldPreferences(settings)
            settings
        }
    }

    fun setSettingsToMemory(newSettings: SettingsModel) {
        val prefsEditor: Editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(newSettings)
        prefsEditor.putString(SettingsDataKey.SETTINGS.toString(), json)
        prefsEditor.apply()
    }

    // ########################################
    //   OLD SETTINGS
    // ########################################

    enum class OldSettingsDataKey {
        DATE_FORMAT, TIME_FORMAT, AUTO_SHOW_KEYBOARD, AUTO_OPEN_APPS, ICONS_IN_HOME, ICONS_IN_DRAWER, SHOW_SEARCH_BAR, SHOW_ALPHABET_FILTER, HOME_APPS, HIDDEN_APPS, SHOW_HIDDEN_APPS, RENAMED_APPS, BLACK_TEXT, DIM_BACKGROUND, DAILY_WALLPAPER, WALLPAPER_DATE, TIME_CLICK_APP, DATE_CLICK_APP
    }

    private fun getOldPreferences(settings: SettingsModel): SettingsModel {

        val oldSettingsRetrieved = sharedPreferences.getBoolean(
            SettingsDataKey.OLD_SETTINGS_RETRIEVED.toString(), false
        )

        if (oldSettingsRetrieved) return settings

        val editor = sharedPreferences.edit()
        editor.putBoolean(SettingsDataKey.OLD_SETTINGS_RETRIEVED.toString(), true)
        editor.apply()

        settings.dateFormat = getData(OldSettingsDataKey.DATE_FORMAT, settings.dateFormat)
        settings.timeFormat = getData(OldSettingsDataKey.TIME_FORMAT, settings.timeFormat)
        settings.drawerAutoShowKeyboard =
            getData(OldSettingsDataKey.AUTO_SHOW_KEYBOARD, settings.drawerAutoShowKeyboard)
        settings.drawerAutoOpenApps =
            getData(OldSettingsDataKey.AUTO_OPEN_APPS, settings.drawerAutoOpenApps)
        settings.homeShowIcons = getData(OldSettingsDataKey.ICONS_IN_HOME, settings.homeShowIcons)
        settings.drawerShowIcons =
            getData(OldSettingsDataKey.ICONS_IN_DRAWER, settings.drawerShowIcons)
        settings.drawerShowSearchBar =
            getData(OldSettingsDataKey.SHOW_SEARCH_BAR, settings.drawerShowSearchBar)
        settings.drawerShowAlphabetFilter =
            getData(OldSettingsDataKey.SHOW_ALPHABET_FILTER, settings.drawerShowAlphabetFilter)
        settings.generalShowHiddenApps =
            getData(OldSettingsDataKey.SHOW_HIDDEN_APPS, settings.generalShowHiddenApps)
        settings.generalIsTextBlack =
            getData(OldSettingsDataKey.BLACK_TEXT, settings.generalIsTextBlack)
        settings.generalDimBackground =
            getData(OldSettingsDataKey.DIM_BACKGROUND, settings.generalDimBackground)
        settings.generalChangeWallpaperDaily =
            getData(OldSettingsDataKey.DAILY_WALLPAPER, settings.generalChangeWallpaperDaily)
        settings.timeClickApp = getData(OldSettingsDataKey.TIME_CLICK_APP, settings.timeClickApp)
        settings.dateClickApp = getData(OldSettingsDataKey.DATE_CLICK_APP, settings.dateClickApp)

        setSettingsToMemory(settings)
        return settings
    }

    private fun getData(key: OldSettingsDataKey, defaultValue: String): String {
        val value =
            this.oldSharedPreferences.getString(key.toString(), defaultValue) ?: defaultValue
        removeKey(key)
        return value
    }

    private fun getData(key: OldSettingsDataKey, defaultValue: Boolean): Boolean {
        val value = this.oldSharedPreferences.getBoolean(key.toString(), defaultValue)
        removeKey(key)
        return value
    }

    private fun getData(key: OldSettingsDataKey, defaultValue: Int): Int {
        val value = this.oldSharedPreferences.getInt(key.toString(), defaultValue)
        removeKey(key)
        return value
    }

    private fun removeKey(key: OldSettingsDataKey) {
        val prefsEditor: Editor = oldSharedPreferences.edit()
        prefsEditor.remove(key.toString())
        prefsEditor.apply()
    }
}