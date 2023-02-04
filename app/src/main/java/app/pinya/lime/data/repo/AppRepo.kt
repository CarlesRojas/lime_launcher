package app.pinya.lime.data.repo

import android.content.Intent
import android.content.pm.PackageManager
import app.pinya.lime.data.memory.AppState
import app.pinya.lime.domain.model.AppModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AppRepo @Inject constructor() {

    @Suppress("DEPRECATION")
    suspend fun refreshAppList(): MutableList<AppModel> {
        return withContext(Dispatchers.IO) {
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val untreatedAppList =
                AppState.app.applicationContext.packageManager.queryIntentActivities(intent, 0)

            val packagesInfo =
                AppState.app.applicationContext.packageManager.getInstalledApplications(
                    PackageManager.GET_META_DATA
                )

            AppState.appList = mutableListOf<AppModel>()
            AppState.appList
        }
    }

    suspend fun getAppList(): MutableList<AppModel> {
        if (AppState.appList.size <= 0) return refreshAppList()
        return AppState.appList
    }


}