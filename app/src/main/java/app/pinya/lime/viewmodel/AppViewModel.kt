package app.pinya.lime.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.pinya.lime.model.AppModel
import app.pinya.lime.model.AppProvider

class AppViewModel : ViewModel() {

    val appModel = MutableLiveData<MutableList<AppModel>>()

    fun getAppList() {
        val appList = AppProvider.refreshAppList()
        appModel.postValue(appList)
    }
}