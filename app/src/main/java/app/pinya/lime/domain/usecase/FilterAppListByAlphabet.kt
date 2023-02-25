package app.pinya.lime.domain.usecase

import app.pinya.lime.domain.model.AlphabetModel
import app.pinya.lime.domain.model.AppModel
import javax.inject.Inject

class FilterAppListByAlphabet @Inject constructor() {
    operator fun invoke(
        completeAppList: MutableList<AppModel>,
        letter: Char,
        showHiddenApps: Boolean
    ): MutableList<AppModel> {
        val filteredList = mutableListOf<AppModel>()

        completeAppList.forEach { app ->
            if (showHiddenApps || !app.hidden) {
                val included = when (letter) {
                    '#' -> !AlphabetModel.ALPHABET.contains(app.name.first().uppercaseChar())
                    else -> app.name.startsWith(letter, true)
                }

                if (included) filteredList.add(app)
            }
        }

        return filteredList
    }
}