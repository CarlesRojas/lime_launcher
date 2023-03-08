package app.pinya.lime.ui.utils.notifications

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
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
        bundle.putStringArray("notificationsChange", value)
        val intent = Intent(intentAction)
        intent.putExtras(bundle)
        sendBroadcast(intent)
    }

    private fun getCurrentNotifications() {
        val notifications: Array<String> = Array(activeNotifications.size) { _ -> "" }
        activeNotifications.forEachIndexed { index, statusBarNotification ->
            notifications[index] = statusBarNotification.packageName
        }

        broadcastStringArray(notifications)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        startUpdates()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        stopUpdates()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn != null) getCurrentNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn != null) getCurrentNotifications()
    }

    private val handler: Handler = Handler(Looper.getMainLooper())
    private var updateInterval: Long = 2000

    private var checkForNotificationChangesRunnable: Runnable = object : Runnable {
        override fun run() {
            try {
                getCurrentNotifications()
            } finally {
                handler.postDelayed(this, updateInterval)
            }
        }
    }

    private fun startUpdates() {
        handler.removeCallbacks(checkForNotificationChangesRunnable)
        checkForNotificationChangesRunnable.run()
    }

    private fun stopUpdates() {
        handler.removeCallbacks(checkForNotificationChangesRunnable)
    }

}