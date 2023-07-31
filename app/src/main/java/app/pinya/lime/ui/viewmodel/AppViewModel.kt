package app.pinya.lime.ui.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pinya.lime.domain.model.AppModel
import app.pinya.lime.domain.model.BooleanPref
import app.pinya.lime.domain.model.InfoModel
import app.pinya.lime.domain.model.menus.*
import app.pinya.lime.domain.usecase.*
import app.pinya.lime.ui.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val refreshAppListUseCase: RefreshAppList,
    private val updateAppListWithInfoUseCase: UpdateAppListWithInfo,

    private val filterAppListByTextUseCase: FilterAppListByText,
    private val filterAppListByAlphabetUseCase: FilterAppListByAlphabet,
    private val filterHomeAppsUseCase: FilterHomeApps,

    private val getInfoUseCase: GetInfo,
    private val updateInfoUseCase: UpdateInfo,
) : ViewModel() {

    val info = MutableLiveData<InfoModel>()

    val drawerList = MutableLiveData<MutableList<AppModel>>()
    val homeList = MutableLiveData<MutableList<AppModel>>()

    var completeAppList: MutableList<AppModel> = mutableListOf()

    // CONTEXT MENUS
    val appMenu = MutableLiveData<AppMenu?>(null)
    val renameMenu = MutableLiveData<RenameMenu?>(null)
    val reorderMenu = MutableLiveData<ReorderMenu?>(null)
    val changeAppIconMenu = MutableLiveData<ChangeAppIconMenu?>(null)
    val buyProMenu = MutableLiveData<BuyProMenu?>(null)


    // ########################################
    //   MAIN
    // ########################################

    fun updateAppList(context: Context) {
        viewModelScope.launch {
            val result = refreshAppListUseCase()
            if (info.value != null) updateAppListWithInfoUseCase(result, info.value!!, context)
            completeAppList = result

            updateHomeList()
            filterByLastValue(context)
        }
    }


    // ########################################
    //   INFO
    // ########################################

    fun getInfo() {
        viewModelScope.launch {
            val newInfo = getInfoUseCase()
            info.postValue(newInfo)
        }
    }

    fun updateInfo(newInfo: InfoModel, context: Context) {
        updateAppListWithInfoUseCase(completeAppList, newInfo, context)

        updateInfoUseCase(newInfo)
        info.postValue(newInfo)

        updateHomeList()
        filterByLastValue(context)
    }

    // ########################################
    //   HOME
    // ########################################

    private fun updateHomeList() {
        val result = filterHomeAppsUseCase(completeAppList)
        homeList.postValue(result)
    }


    // ########################################
    //   DRAWER
    // ########################################

    private var lastFilterWasChar: Boolean = false
    private var lastSearchedText: String = ""
    private var lastFilterLetter: Char = 'a'

    fun filterDrawerAppListBySearchedText(searchedText: String, context: Context) {
        lastFilterWasChar = false
        lastSearchedText = searchedText

        val showHiddenApps = Utils.getBooleanPref(context, BooleanPref.GENERAL_SHOW_HIDDEN_APPS)

        val result = filterAppListByTextUseCase(
            completeAppList,
            searchedText,
            showHiddenApps
        )
        drawerList.postValue(result)
    }

    fun filterDrawerAppListByAlphabetLetter(letter: Char, context: Context) {
        lastFilterWasChar = true
        lastSearchedText = ""
        lastFilterLetter = letter

        val showHiddenApps = Utils.getBooleanPref(context, BooleanPref.GENERAL_SHOW_HIDDEN_APPS)

        val result = filterAppListByAlphabetUseCase(
            completeAppList,
            letter,
            showHiddenApps
        )
        drawerList.postValue(result)
    }

    private fun filterByLastValue(context: Context) {
        if (lastFilterWasChar) filterDrawerAppListByAlphabetLetter(lastFilterLetter, context)
        else filterDrawerAppListBySearchedText(lastSearchedText, context)
    }

}