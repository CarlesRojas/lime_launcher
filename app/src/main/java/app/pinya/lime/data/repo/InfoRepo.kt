package app.pinya.lime.data.repo

import android.content.Context
import android.content.SharedPreferences
import app.pinya.lime.data.memory.AppProvider
import app.pinya.lime.domain.model.InfoModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class InfoRepo @Inject constructor() {
    private val sharedPreferences: SharedPreferences
    private val oldSharedPreferences: SharedPreferences

    private val oldPreferencesName = "LimeLauncherPreferences"
    private val preferencesName = "LimeLauncherSharedPreferences"

    private enum class InfoDataKey {
        HOME_APPS,
        HIDDEN_APPS,
        RENAMED_APPS,
        WALLPAPER_LAST_UPDATED_DATE,
        MAX_NUMBER_OF_HOME_APPS,
        TUTORIAL_DONE,
        OLD_INFO_RETRIEVED,
    }

    init {
        val context = AppProvider.getContext()

        this.sharedPreferences =
            context.getSharedPreferences(this.preferencesName, Context.MODE_PRIVATE)

        this.oldSharedPreferences =
            context.getSharedPreferences(this.oldPreferencesName, Context.MODE_PRIVATE)
    }

    suspend fun getInfoFromMemory(): InfoModel {
        return withContext(Dispatchers.IO) {
            var info = InfoModel()
            info.homeApps = getData(InfoDataKey.HOME_APPS, info.homeApps)
            info.hiddenApps = getData(InfoDataKey.HIDDEN_APPS, info.hiddenApps)
            info.renamedApps =
                getData(InfoDataKey.RENAMED_APPS, info.renamedApps)
            info.wallpaperLastUpdatedDate =
                getData(InfoDataKey.WALLPAPER_LAST_UPDATED_DATE, info.wallpaperLastUpdatedDate)
            info.maxNumberOfHomeApps =
                getData(InfoDataKey.MAX_NUMBER_OF_HOME_APPS, info.maxNumberOfHomeApps)
            info.tutorialDone =
                getData(InfoDataKey.TUTORIAL_DONE, info.tutorialDone)

            info = getOldPreferences(info)
            info
        }
    }

    fun setInfoToMemory(newInfo: InfoModel) {
        saveData(InfoDataKey.HOME_APPS, newInfo.homeApps)
        saveData(InfoDataKey.HIDDEN_APPS, newInfo.hiddenApps)
        saveData(InfoDataKey.RENAMED_APPS, newInfo.renamedApps)
        saveData(InfoDataKey.WALLPAPER_LAST_UPDATED_DATE, newInfo.wallpaperLastUpdatedDate)
        saveData(InfoDataKey.MAX_NUMBER_OF_HOME_APPS, newInfo.maxNumberOfHomeApps)
    }


    private fun getData(key: InfoDataKey, defaultValue: Int): Int {
        return sharedPreferences.getInt(key.toString(), defaultValue)
    }

    private fun getData(key: InfoDataKey, defaultValue: Boolean): Boolean {
        return this.sharedPreferences.getBoolean(key.toString(), defaultValue)
    }

    private fun getData(
        key: InfoDataKey, defaultValue: MutableSet<String>
    ): MutableSet<String> {
        val jsonDefaultValue: String = Gson().toJson(defaultValue)
        val result = this.sharedPreferences.getString(key.toString(), jsonDefaultValue)
            ?: jsonDefaultValue
        val listType = object : TypeToken<MutableSet<String>>() {}.type
        return Gson().fromJson(result, listType)
    }

    private fun getData(
        key: InfoDataKey, defaultValue: MutableMap<String, String>
    ): MutableMap<String, String> {
        val jsonDefaultValue: String = Gson().toJson(defaultValue)
        val result = this.sharedPreferences.getString(key.toString(), jsonDefaultValue)
            ?: jsonDefaultValue
        val listType = object : TypeToken<MutableMap<String, String>>() {}.type
        return Gson().fromJson(result, listType)
    }

    private fun saveData(key: InfoDataKey, data: Int) {
        val editor = this.sharedPreferences.edit()
        editor.putInt(key.toString(), data)
        editor.apply()
    }

    private fun saveData(key: InfoDataKey, data: MutableSet<String>) {
        val editor = this.sharedPreferences.edit()
        val jsonData: String = Gson().toJson(data)
        editor.putString(key.toString(), jsonData)
        editor.apply()
    }

    private fun saveData(key: InfoDataKey, data: MutableMap<String, String>) {
        val editor = this.sharedPreferences.edit()
        val jsonData: String = Gson().toJson(data)
        editor.putString(key.toString(), jsonData)
        editor.apply()
    }


    // ########################################
    //   OLD INFO
    // ########################################

    private fun getOldPreferences(info: InfoModel): InfoModel {

        val oldInfoRetrieved = sharedPreferences.getBoolean(
            InfoDataKey.OLD_INFO_RETRIEVED.toString(),
            false
        )

        if (oldInfoRetrieved) return info

        val editor = sharedPreferences.edit()
        editor.putBoolean(InfoDataKey.OLD_INFO_RETRIEVED.toString(), true)
        editor.apply()

        info.homeApps = getOldData(SettingsRepo.OldSettingsDataKey.HOME_APPS, info.homeApps)
        info.hiddenApps = getOldData(SettingsRepo.OldSettingsDataKey.HIDDEN_APPS, info.hiddenApps)
        info.renamedApps =
            getOldData(SettingsRepo.OldSettingsDataKey.RENAMED_APPS, info.renamedApps)
        info.wallpaperLastUpdatedDate =
            getOldData(
                SettingsRepo.OldSettingsDataKey.WALLPAPER_DATE,
                info.wallpaperLastUpdatedDate
            )

        setInfoToMemory(info)
        return info
    }


    private fun getOldData(key: SettingsRepo.OldSettingsDataKey, defaultValue: Int): Int {
        val value = this.oldSharedPreferences.getInt(key.toString(), defaultValue)
        removeKey(key)
        return value
    }

    private fun getOldData(
        key: SettingsRepo.OldSettingsDataKey, defaultValue: MutableSet<String>
    ): MutableSet<String> {
        val jsonDefaultValue: String = Gson().toJson(defaultValue)
        val result = this.oldSharedPreferences.getString(key.toString(), jsonDefaultValue)
            ?: jsonDefaultValue
        val listType = object : TypeToken<MutableSet<String>>() {}.type
        removeKey(key)
        return Gson().fromJson(result, listType)
    }

    private fun getOldData(
        key: SettingsRepo.OldSettingsDataKey, defaultValue: MutableMap<String, String>
    ): MutableMap<String, String> {
        val jsonDefaultValue: String = Gson().toJson(defaultValue)
        val result = this.oldSharedPreferences.getString(key.toString(), jsonDefaultValue)
            ?: jsonDefaultValue
        val listType = object : TypeToken<MutableMap<String, String>>() {}.type
        removeKey(key)
        return Gson().fromJson(result, listType)
    }


    private fun removeKey(key: SettingsRepo.OldSettingsDataKey) {
        val prefsEditor: SharedPreferences.Editor = oldSharedPreferences.edit()
        prefsEditor.remove(key.toString())
        prefsEditor.apply()
    }
}