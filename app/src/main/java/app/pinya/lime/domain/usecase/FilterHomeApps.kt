package app.pinya.lime.domain.usecase

import app.pinya.lime.domain.model.AppModel
import javax.inject.Inject

class FilterHomeApps @Inject constructor() {
    operator fun invoke(
        completeAppList: MutableList<AppModel>,
        showHiddenApps: Boolean
    ): MutableList<AppModel> {
        val filteredList = mutableListOf<AppModel>()

        completeAppList.forEach { app ->
            if (showHiddenApps || !app.hidden) {
                if (app.home) filteredList.add(app)
            }
        }

        return filteredList
    }
}