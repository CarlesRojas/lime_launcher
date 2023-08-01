package app.pinya.lime.domain.usecase

import android.content.Context
import android.graphics.drawable.Drawable
import app.pinya.lime.domain.model.AppModel
import app.pinya.lime.domain.model.IconRule
import app.pinya.lime.domain.model.InfoModel
import app.pinya.lime.domain.model.StringPref
import app.pinya.lime.ui.utils.IconPackManager
import app.pinya.lime.ui.utils.Utils
import javax.inject.Inject

class UpdateAppListWithInfo @Inject constructor() {
    operator fun invoke(appList: MutableList<AppModel>, info: InfoModel, context: Context): MutableList<AppModel> {

        info.homeApps.removeAll { appPackage -> appList.find { appPackage == it.packageName } == null }
        info.hiddenApps.removeAll { appPackage -> appList.find { appPackage == it.packageName } == null }
        info.iconRules.removeAll { iconRuleJson ->
            val iconRule = IconRule.deserialize(iconRuleJson)
            appList.find {iconRule.packageName == it.packageName } == null
        }

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


        val iconPackName = Utils.getStringPref(context, StringPref.GENERAL_ICON_PACK)
        val iconPackManager = IconPackManager(context)
        val iconPacks = mutableMapOf<String, IconPackManager.IconPack>()
        iconPackManager.isSupportedIconPacks(true).forEach {
            iconPacks[it.value.name] = it.value
        }
        val rules = info.iconRules.map { IconRule.deserialize(it) }.groupBy { it.packageName }

        fun getIconFromRule(packageName: String): Drawable? {
            val appRules = rules[packageName] ?: return null

            for (rule in appRules) {
                if (rule.iconPackContext != iconPackName) continue
                val iconPack = iconPacks[rule.iconPackName] ?: return null
                return iconPack.loadIconFromRule(rule)
            }

            return null
        }

        for (app in appList) {
            app.hidden = info.hiddenApps.find { it == app.packageName } != null
            app.home = info.homeApps.find { it == app.packageName } != null
            app.name = info.renamedApps[app.packageName] ?: app.originalName
            app.homeOrderIndex = info.homeApps.indexOf(app.packageName)

            if (app.packageName in rules) {
                val iconFromRule = getIconFromRule(app.packageName)
                if (iconFromRule != null) app.icon = iconFromRule
            }
        }

        appList.sortBy { it.name.lowercase() }
        return appList
    }
}