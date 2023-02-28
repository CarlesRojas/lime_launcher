package app.pinya.lime.ui.utils

import android.app.WallpaperManager
import android.content.Context
import app.pinya.lime.ui.viewmodel.AppViewModel
import java.net.URL
import java.util.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

const val BING_WALLPAPER_URL = "https://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1"

class DailyWallpaper(private val context: Context, private val appViewModel: AppViewModel) {


    private fun String.removeAll(charactersToRemove: Set<Char>): String {
        return filterNot { charactersToRemove.contains(it) }
    }

    fun updateWallpaper(alsoChangeLockScreen: Boolean) {
        val thread = Thread {
            try {
                val apiResponse = URL(BING_WALLPAPER_URL).readText()
                val jsonObject = Json.parseToJsonElement(apiResponse).jsonObject
                val images = jsonObject["images"] as JsonArray

                if (images.size > 0) {
                    val image: JsonObject = images[0].jsonObject
                    val url = "https://bing.com${image["url"].toString().removeAll(setOf('"'))}"

                    val inputStream = URL(url).openStream()
                    WallpaperManager.getInstance(context)
                        .setStream(inputStream, null, true, WallpaperManager.FLAG_SYSTEM)

                    if (alsoChangeLockScreen) WallpaperManager.getInstance(context)
                        .setStream(inputStream, null, false, WallpaperManager.FLAG_LOCK)

                    val cal: Calendar = Calendar.getInstance()
                    val date = cal.get(Calendar.DATE)
                    val info = appViewModel.info.value ?: return@Thread

                    info.wallpaperLastUpdatedDate = date
                    appViewModel.updateInfo(info, context)
                }

            } catch (_: Exception) {
            }
        }

        thread.start()
    }
}