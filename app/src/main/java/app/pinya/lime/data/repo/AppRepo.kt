package app.pinya.lime.data.repo

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import app.pinya.lime.data.memory.AppProvider
import app.pinya.lime.domain.model.AppModel
import app.pinya.lime.domain.model.IconRule
import app.pinya.lime.domain.model.StringPref
import app.pinya.lime.ui.utils.IconPackManager
import app.pinya.lime.ui.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AppRepo @Inject constructor() {

    @Suppress("DEPRECATION")
    suspend fun getAppList(): MutableList<AppModel> {
        return withContext(Dispatchers.IO) {
            val context = AppProvider.getContext()

            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val untreatedAppList =
                context.packageManager.queryIntentActivities(intent, 0)

            val packagesInfo =
                context.packageManager.getInstalledApplications(
                    PackageManager.GET_META_DATA
                )

            val appList = mutableListOf<AppModel>()

            val iconPackName = Utils.getStringPref(context, StringPref.GENERAL_ICON_PACK)
            val iconPackManager = IconPackManager(context)
            var iconPacks = mutableMapOf<String, IconPackManager.IconPack>()
            iconPackManager.isSupportedIconPacks(true).forEach {
                iconPacks[it.value.name] = it.value
            }
            val selectedIconPack = iconPacks[iconPackName]

            for (untreatedApp in untreatedAppList) {
                val name = untreatedApp.activityInfo.loadLabel(context.packageManager).toString()
                val packageName = untreatedApp.activityInfo.packageName
                var icon = untreatedApp.activityInfo.loadIcon(context.packageManager)
                val packageInfo = packagesInfo.find { it.packageName == packageName }

                if (packageInfo != null){
                    var newIcon: Drawable? = null
                    if (newIcon == null && selectedIconPack != null) newIcon = selectedIconPack.loadIcon(packageInfo)
                    if (newIcon != null) icon = newIcon
                }

                val app = AppModel(name, packageName, icon, name)

                app.system =
                    if (packageInfo != null) (packageInfo.flags and ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM else false

                if (!appList.contains(app)) appList.add(app)
            }

            appList.sortBy { it.name.lowercase() }
            appList
        }
    }
}