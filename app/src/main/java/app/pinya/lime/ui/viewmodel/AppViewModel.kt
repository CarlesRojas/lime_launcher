package app.pinya.lime.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pinya.lime.domain.model.AppModel
import app.pinya.lime.domain.usecase.RefreshAppList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val refreshAppList: RefreshAppList
) : ViewModel() {

    val drawerList = MutableLiveData<MutableList<AppModel>>()

    fun onCreate() {
        viewModelScope.launch {
            val result = refreshAppList()
            drawerList.postValue(result)
        }
    }
}