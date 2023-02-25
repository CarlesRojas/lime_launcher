package app.pinya.lime.domain.usecase

import app.pinya.lime.domain.model.AppModel
import javax.inject.Inject

class FilterHomeApps @Inject constructor() {
    operator fun invoke(
        completeAppList: MutableList<AppModel>,
    ): MutableList<AppModel> {
        val filteredList = mutableListOf<AppModel>()

        completeAppList.forEach { app ->
            if (app.home) filteredList.add(app)
        }

        return filteredList
    }
}