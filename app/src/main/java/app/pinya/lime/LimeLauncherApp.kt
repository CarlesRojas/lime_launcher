package app.pinya.lime

import android.app.Application
import app.pinya.lime.ui.utils.billing.BillingHelper
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope

@HiltAndroidApp
class LimeLauncherApp : Application() {
    lateinit var appContainer: AppContainer

    inner class AppContainer {
        @OptIn(DelicateCoroutinesApi::class)
        val billingHelper = BillingHelper.getInstance(this@LimeLauncherApp, GlobalScope)
    }

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer()
    }
}