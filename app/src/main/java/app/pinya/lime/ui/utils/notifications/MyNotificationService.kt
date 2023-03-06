package app.pinya.lime.ui.utils.notifications

import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification


class MyNotificationService : NotificationListenerService() {

    companion object {
        const val intentAction = "app.pinya.lime.ui.utils.notifications"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }

    private fun broadcastString(key: String, value: String) {
        val intent = Intent(intentAction)
        intent.putExtra(key, value)
        sendBroadcast(intent)
    }

    private fun broadcastStringArray(value: Array<String>) {
        val bundle = Bundle()
        bundle.putStringArray("initialNotifications", value)
        val intent = Intent(intentAction)
        intent.putExtras(bundle)
        sendBroadcast(intent)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()

        val notifications: Array<String> = Array(activeNotifications.size) { _ -> "" }
        activeNotifications.forEachIndexed { index, statusBarNotification ->
            notifications[index] = statusBarNotification.packageName
        }

        broadcastStringArray(notifications)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn != null) broadcastString("addNotification", sbn.packageName)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn != null) broadcastString("removeNotification", sbn.packageName)
    }
}