package app.pinya.lime.domain.usecase

import app.pinya.lime.domain.model.AppModel
import app.pinya.lime.domain.model.InfoModel
import javax.inject.Inject

class UpdateAppListWithInfo @Inject constructor() {
    operator fun invoke(appList: MutableList<AppModel>, info: InfoModel): MutableList<AppModel> {

        info.homeApps.removeAll { appPackage -> appList.find { appPackage == it.packageName } == null }

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

        return appList
    }
}