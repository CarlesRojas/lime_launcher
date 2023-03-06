package app.pinya.lime.ui.utils.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.MutableLiveData

class NotificationsHandler(context: Context) {

    private var notificationsBroadcastReceiver: NotificationsBroadcasterReceiver? = null

    val notifications: MutableLiveData<MutableMap<String, Int>> = MutableLiveData(mutableMapOf())

    init {
        notificationsBroadcastReceiver =
            NotificationsBroadcasterReceiver(
                ::handleInitialNotificationsReceived,
                ::handleAddNotificationReceived,
                ::handleRemoveNotificationReceived
            )
        val intentFilter = IntentFilter()
        intentFilter.addAction(MyNotificationService.intentAction)
        context.registerReceiver(notificationsBroadcastReceiver, intentFilter)
    }


    private fun handleInitialNotificationsReceived(initialNotifications: Array<String>) {
        val newNotifications = mutableMapOf<String, Int>()

        initialNotifications.forEach { packageName ->
            if (newNotifications.contains(packageName))
                newNotifications[packageName] = newNotifications[packageName]!! + 1
            else newNotifications[packageName] = 1
        }

        val fixedNotifications = mutableMapOf<String, Int>()
        newNotifications.forEach { (key, value) ->

            if (value <= 1) fixedNotifications[key] = value
            else fixedNotifications[key] = value - 1
        }

        notifications.postValue(fixedNotifications)
    }

    private fun handleAddNotificationReceived(newNotification: String) {
        val newNotifications = notifications.value ?: return

        if (newNotifications.contains(newNotification)) newNotifications[newNotification] =
            newNotifications[newNotification]!! + 1
        else newNotifications[newNotification] = 1

        notifications.postValue(newNotifications)
    }

    private fun handleRemoveNotificationReceived(newNotification: String) {
        var newNotifications = notifications.value ?: return

        if (newNotifications.contains(newNotification)) {
            newNotifications[newNotification] =
                (newNotifications[newNotification]!! - 1).coerceAtLeast(0)

            if (newNotifications[newNotification] == 0) {
                newNotifications = newNotifications.filterKeys {
                    it != newNotification
                } as MutableMap<String, Int>
            }

            notifications.postValue(newNotifications)
        }
    }

    class NotificationsBroadcasterReceiver(
        private val handleInitialNotificationsReceived: (initialNotifications: Array<String>) -> Unit,
        private val handleAddNotificationReceived: (initialNotifications: String) -> Unit,
        private val handleRemoveNotificationReceived: (initialNotifications: String) -> Unit
    ) :
        BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val initialNotifications = intent.getStringArrayExtra("initialNotifications")
            val addNotification = intent.getStringExtra("addNotification")
            val removeNotification = intent.getStringExtra("removeNotification")

            if (initialNotifications != null)
                handleInitialNotificationsReceived(initialNotifications)

            if (addNotification != null)
                handleAddNotificationReceived(addNotification)

            if (removeNotification != null)
                handleRemoveNotificationReceived(removeNotification)
        }
    }
}