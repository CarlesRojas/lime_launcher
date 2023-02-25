package app.pinya.lime.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pinya.lime.domain.model.AppModel
import app.pinya.lime.domain.usecase.FilterAppListByAlphabet
import app.pinya.lime.domain.usecase.FilterAppListByText
import app.pinya.lime.domain.usecase.RefreshAppList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val refreshAppList: RefreshAppList,
    private val filterAppListByText: FilterAppListByText,
    private val filterAppListByAlphabet: FilterAppListByAlphabet
) : ViewModel() {

    var completeAppList: MutableList<AppModel> = mutableListOf()
    val drawerList = MutableLiveData<MutableList<AppModel>>()

    fun onCreate() {
        updateAppList()
    }

    // ########################################
    //   MAIN
    // ########################################

    fun updateAppList() {
        viewModelScope.launch {
            val result = refreshAppList()
            completeAppList = result
            drawerList.postValue(result)
        }
    }


    // ########################################
    //   DRAWER
    // ########################################

    fun filterDrawerAppListBySearchedText(searchedText: String) {
        val result = filterAppListByText(completeAppList, searchedText)
        drawerList.postValue(result)
    }

    fun filterDrawerAppListByAlphabetLetter(letter: Char) {
        val result = filterAppListByAlphabet(completeAppList, letter)
        drawerList.postValue(result)
    }
}