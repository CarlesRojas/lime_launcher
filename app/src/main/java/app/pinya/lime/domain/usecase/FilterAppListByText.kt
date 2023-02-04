package app.pinya.lime.domain.usecase

import app.pinya.lime.domain.model.AppModel
import javax.inject.Inject

class FilterAppListByText @Inject constructor() {
    operator fun invoke(
        completeAppList: MutableList<AppModel>,
        text: String
    ): MutableList<AppModel> {
        val filteredList = mutableListOf<AppModel>()

        completeAppList.forEach {
            val included =
                if (text == "") true else it.name.contains(text, true)

            if (included) filteredList.add(it)
        }

        return filteredList
    }
}