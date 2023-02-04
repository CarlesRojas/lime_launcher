package app.pinya.lime.domain.usecase

import app.pinya.lime.domain.model.AlphabetModel
import app.pinya.lime.domain.model.AppModel
import javax.inject.Inject

class FilterAppListByAlphabet @Inject constructor() {
    operator fun invoke(
        completeAppList: MutableList<AppModel>,
        letter: Char
    ): MutableList<AppModel> {
        val filteredList = mutableListOf<AppModel>()

        completeAppList.forEach {
            val included = when (letter) {
                '#' -> !AlphabetModel.ALPHABET.contains(it.name.first().uppercaseChar())
                else -> it.name.startsWith(letter, true)
            }

            if (included) filteredList.add(it)
        }

        return filteredList
    }
}