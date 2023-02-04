package app.pinya.lime.data.memory

import android.app.Application
import android.content.Context

class AppProvider {
    companion object {

        @Volatile
        lateinit var app: Application

        fun initialize(application: Application) {
            if (!Companion::app.isInitialized) {
                synchronized(this) {
                    app = application
                }
            }
        }

        fun getContext(): Context {
            return app.applicationContext
        }
    }
}