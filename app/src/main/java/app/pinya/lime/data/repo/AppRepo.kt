package app.pinya.lime.data.repo

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import app.pinya.lime.data.memory.AppProvider
import app.pinya.lime.domain.model.AppModel
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

            for (untreatedApp in untreatedAppList) {
                val name = untreatedApp.activityInfo.loadLabel(context.packageManager).toString()
                val packageName = untreatedApp.activityInfo.packageName
                val icon = untreatedApp.activityInfo.loadIcon(context.packageManager)
                val app = AppModel(name, packageName, icon, name)

                val packageInfo = packagesInfo.find { it.packageName == packageName }

                app.system =
                    if (packageInfo != null) (packageInfo.flags and ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM else false

                // TODO do this in the use case
                //app.hidden = hiddenApps.find { it == packageName } != null
                //app.home = homeApps.find { it == packageName } != null
                //app.name = renamedApps[packageName] ?: app.originalName

                if (!appList.contains(app)) appList.add(app)
            }

            appList
        }
    }
}