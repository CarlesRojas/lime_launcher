package app.pinya.lime.ui.utils.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData

class NotificationsHandler(context: Context) {

    private var notificationsBroadcastReceiver: NotificationsBroadcasterReceiver? = null

    val notifications: MutableLiveData<MutableMap<String, Int>> = MutableLiveData(mutableMapOf())

    init {
        notificationsBroadcastReceiver =
            NotificationsBroadcasterReceiver(::handleNotificationsChange)
        val intentFilter = IntentFilter()
        intentFilter.addAction(MyNotificationService.intentAction)
        ContextCompat.registerReceiver(
            context,
            notificationsBroadcastReceiver,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }


    private fun handleNotificationsChange(newNotifications: Array<String>) {
        val parsedNewNotifications = mutableMapOf<String, Int>()

        newNotifications.forEach { packageName ->
            if (parsedNewNotifications.contains(packageName))
                parsedNewNotifications[packageName] = parsedNewNotifications[packageName]!! + 1
            else parsedNewNotifications[packageName] = 1
        }

        // There is a bug that when there is more than one notification of an app, it shows an extra one
        val fixedNotifications = mutableMapOf<String, Int>()
        parsedNewNotifications.forEach { (key, value) ->
            if (value <= 1) fixedNotifications[key] = value
            else fixedNotifications[key] = value - 1
        }

        notifications.postValue(fixedNotifications)
    }

    class NotificationsBroadcasterReceiver(
        private val onNotificationsChange: (newNotifications: Array<String>) -> Unit,
    ) :
        BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val newNotifications = intent.getStringArrayExtra("notificationsChange")
            if (newNotifications != null) onNotificationsChange(newNotifications)
        }
    }
}