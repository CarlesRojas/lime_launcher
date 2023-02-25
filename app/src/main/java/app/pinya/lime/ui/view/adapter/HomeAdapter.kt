package app.pinya.lime.ui.view.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.pinya.lime.R
import app.pinya.lime.domain.model.AppModel
import app.pinya.lime.domain.model.SettingsModel
import app.pinya.lime.ui.view.activity.SettingsActivity
import app.pinya.lime.ui.view.holder.ItemAppViewHolder
import app.pinya.lime.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Timer
import java.util.TimerTask

class HomeAdapter(
    private val context: Context,
    private val layout: ViewGroup,
    private val viewModel: AppViewModel
) : RecyclerView.Adapter<ItemAppViewHolder>() {

    // DATE & TIME
    private var date: TextView? = null
    private var time: TextView? = null
    private var timer: Timer? = Timer()

    private var homeAppList: List<AppModel> = mutableListOf()

    init {
        // TODO initContextMenu()
        // TODO initGestureDetector()
        // TODO getHomeAppList()
    }

    // ########################################
    //   GENERAL
    // ########################################

    fun handleSettingsUpdate(settings: SettingsModel) {
        addDateListeners(settings)
        addTimeListeners(settings)
        startTimerToUpdateDateTime(settings)
    }

    // ########################################
    //   DATE & TIME
    // ########################################

    private fun addDateListeners(settings: SettingsModel) {
        date = layout.findViewById(R.id.homeDate)
        date?.setOnClickListener {
            when (val stateValue = settings.dateClickApp) {
                "default" -> {
                    val builder: Uri.Builder =
                        CalendarContract.CONTENT_URI.buildUpon().appendPath("time")
                    val intent = Intent(Intent.ACTION_VIEW).setData(builder.build())
                    context.startActivity(intent)
                }
                "none" -> return@setOnClickListener
                else -> {
                    val launchAppIntent =
                        context.packageManager.getLaunchIntentForPackage(stateValue)
                    if (launchAppIntent != null) context.startActivity(launchAppIntent)
                }
            }
        }

        date?.setOnLongClickListener {
            context.startActivity(Intent(context, SettingsActivity::class.java))
            true
        }
    }

    private fun addTimeListeners(settings: SettingsModel) {
        time = layout.findViewById(R.id.homeTime)
        time?.setOnClickListener {
            when (val stateValue = settings.timeClickApp) {
                "default" -> {
                    val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }
                "none" -> return@setOnClickListener
                else -> {
                    val launchAppIntent =
                        context.packageManager.getLaunchIntentForPackage(stateValue)
                    if (launchAppIntent != null) context.startActivity(launchAppIntent)
                }
            }
        }

        time?.setOnLongClickListener {
            context.startActivity(Intent(context, SettingsActivity::class.java))
            true
        }
    }

    private fun startTimerToUpdateDateTime(settings: SettingsModel) {
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                (context as Activity).runOnUiThread {
                    date?.text =
                        SimpleDateFormat.getDateInstance(SettingsActivity.mapFormat(settings.dateFormat))
                            .format(Date())

                    time?.text =
                        SimpleDateFormat.getTimeInstance(SettingsActivity.mapFormat(settings.timeFormat))
                            .format(Date())
                }
            }
        }, 0, 1000)
    }

    // ########################################
    //   RECYCLER VIEW
    // ########################################

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAppViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ItemAppViewHolder(
            inflater.inflate(R.layout.view_app, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemAppViewHolder, position: Int) {
    }

    override fun getItemCount(): Int {
        return 0
    }
}