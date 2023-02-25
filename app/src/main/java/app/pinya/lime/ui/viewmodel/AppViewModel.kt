package app.pinya.lime.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pinya.lime.domain.model.AppModel
import app.pinya.lime.domain.model.InfoModel
import app.pinya.lime.domain.model.SettingsModel
import app.pinya.lime.domain.usecase.*
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

    private val getSettingsUseCase: GetSettings,
    private val updateSettingsUseCase: UpdateSettings
) : ViewModel() {

    val settings = MutableLiveData<SettingsModel>()
    val info = MutableLiveData<InfoModel>()

    val drawerList = MutableLiveData<MutableList<AppModel>>()
    val homeList = MutableLiveData<MutableList<AppModel>>()

    var completeAppList: MutableList<AppModel> = mutableListOf()


    // ########################################
    //   MAIN
    // ########################################

    fun updateAppList() {
        viewModelScope.launch {
            val result = refreshAppListUseCase()
            updateAppListWithInfoUseCase(result, info.value ?: InfoModel())
            completeAppList = result

            updateHomeList()
            filterByLastValue()
        }
    }


    // ########################################
    //   SETTINGS
    // ########################################

    fun getSettings() {
        viewModelScope.launch {
            val newSettings = getSettingsUseCase()
            settings.postValue(newSettings)
        }
    }

    fun updateSettings(newSettings: SettingsModel) {
        updateSettingsUseCase(newSettings)
        settings.postValue(newSettings)

        updateHomeList()
        filterByLastValue()
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

    fun updateInfo(newInfo: InfoModel) {
        updateInfoUseCase(newInfo)
        info.postValue(newInfo)

        updateAppListWithInfoUseCase(completeAppList, info.value ?: InfoModel())

        updateHomeList()
        filterByLastValue()
    }

    // ########################################
    //   HOME
    // ########################################

    private fun updateHomeList() {
        val result =
            filterHomeAppsUseCase(completeAppList, settings.value?.generalShowHiddenApps ?: false)
        homeList.postValue(result)
    }


    // ########################################
    //   DRAWER
    // ########################################

    private var lastFilterWasChar: Boolean = false
    private var lastSearchedText: String = ""
    private var lastFilterLetter: Char = 'a'

    fun filterDrawerAppListBySearchedText(searchedText: String) {
        lastFilterWasChar = false
        lastSearchedText = searchedText

        val result = filterAppListByTextUseCase(
            completeAppList,
            searchedText,
            settings.value?.generalShowHiddenApps ?: false
        )
        drawerList.postValue(result)
    }

    fun filterDrawerAppListByAlphabetLetter(letter: Char) {
        lastFilterWasChar = true
        lastSearchedText = ""
        lastFilterLetter = letter

        val result = filterAppListByAlphabetUseCase(
            completeAppList,
            letter,
            settings.value?.generalShowHiddenApps ?: false
        )
        drawerList.postValue(result)
    }

    private fun filterByLastValue() {
        if (lastFilterWasChar) filterDrawerAppListByAlphabetLetter(lastFilterLetter)
        else filterDrawerAppListBySearchedText(lastSearchedText)
    }
}