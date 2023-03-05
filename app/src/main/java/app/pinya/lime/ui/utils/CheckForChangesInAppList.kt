package app.pinya.lime.ui.utils

import android.app.job.JobService
import android.content.Context
import android.os.Handler
import android.os.Looper
import app.pinya.lime.ui.viewmodel.AppViewModel


class CheckForChangesInAppList(
    private val context: Context,
    val appViewModel: AppViewModel
) {

    private val preferencesName = "LimeLauncherSharedPreferencesPackageSequence"
    private val sequencePreferenceName = "sequence_number"

    private val handler: Handler = Handler(Looper.getMainLooper())
    private var updateInterval: Long = 2000

    private var checkForChangesInAppListRunnable: Runnable = object : Runnable {
        override fun run() {
            try {
                checkForPackageChanges()
            } finally {
                handler.postDelayed(this, updateInterval)
            }
        }
    }

    private fun checkForPackageChanges() {
        val packageManager = context.packageManager
        val sequenceNumber = getSequenceNumber(context)

        val changedPackages = packageManager.getChangedPackages(sequenceNumber)

        if (changedPackages != null) {
            appViewModel.updateAppList(context)
            saveSequenceNumber(context, changedPackages.sequenceNumber)
        }
    }


    private fun getSequenceNumber(context: Context): Int {
        val sharedPrefFile = context.getSharedPreferences(preferencesName, JobService.MODE_PRIVATE)
        return sharedPrefFile.getInt(sequencePreferenceName, 0)
    }

    private fun saveSequenceNumber(context: Context, newSequenceNumber: Int) {
        val sharedPrefFile = context.getSharedPreferences(preferencesName, JobService.MODE_PRIVATE)
        val editor = sharedPrefFile.edit()
        editor.putInt(sequencePreferenceName, newSequenceNumber)
        editor.apply()
    }

    fun startUpdates() {
        checkForChangesInAppListRunnable.run()
    }

    fun stopUpdates() {
        handler.removeCallbacks(checkForChangesInAppListRunnable)
    }
}