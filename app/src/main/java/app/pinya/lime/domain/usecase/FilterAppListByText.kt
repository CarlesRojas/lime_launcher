package app.pinya.lime.domain.usecase

import app.pinya.lime.domain.model.AppModel
import javax.inject.Inject

class FilterAppListByText @Inject constructor() {
    operator fun invoke(
        completeAppList: MutableList<AppModel>,
        text: String,
        showHiddenApps: Boolean
    ): MutableList<AppModel> {
        val filteredList = mutableListOf<AppModel>()

        completeAppList.forEach { app ->
            if (showHiddenApps || !app.hidden) {
                val included =
                    if (text == "") true else app.name.contains(text, true)

                if (included) filteredList.add(app)
            }
        }

        return filteredList
    }
}