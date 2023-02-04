package app.pinya.lime.data.memory

import android.app.Application
import app.pinya.lime.domain.model.AppModel

class AppState {
    companion object {

        @Volatile
        lateinit var app: Application
        var appList: MutableList<AppModel> = mutableListOf()

        fun initialize(application: Application) {
            if (!Companion::app.isInitialized) {
                synchronized(this) {
                    app = application
                }
            }
        }
    }
}