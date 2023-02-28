package app.pinya.lime.domain.usecase

import app.pinya.lime.domain.model.AppModel
import app.pinya.lime.domain.model.InfoModel
import javax.inject.Inject

class UpdateAppListWithInfo @Inject constructor() {
    operator fun invoke(appList: MutableList<AppModel>, info: InfoModel): MutableList<AppModel> {

        info.homeApps.removeAll { appPackage -> appList.find { appPackage == it.packageName } == null }
        info.hiddenApps.removeAll { appPackage -> appList.find { appPackage == it.packageName } == null }

        val renamedAppsIterator = info.renamedApps.entries.iterator()
        while (renamedAppsIterator.hasNext()) {
            val packageName = renamedAppsIterator.next().key
            if (appList.find { packageName == it.packageName } == null)
                renamedAppsIterator.remove()
        }

        info.homeApps.forEachIndexed { i, packageName ->
            val app = appList.find { packageName == it.packageName }
            if (app != null) app.homeOrderIndex = i
        }

        for (app in appList) {
            app.hidden = info.hiddenApps.find { it == app.packageName } != null
            app.home = info.homeApps.find { it == app.packageName } != null
            app.name = info.renamedApps[app.packageName] ?: app.originalName
            app.homeOrderIndex = info.homeApps.indexOf(app.packageName)
        }

        appList.sortBy { it.name.lowercase() }
        return appList
    }
}