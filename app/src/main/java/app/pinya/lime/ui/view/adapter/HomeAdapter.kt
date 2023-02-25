package app.pinya.lime.ui.view.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.pinya.lime.R
import app.pinya.lime.domain.model.AppModel
import app.pinya.lime.domain.model.SettingsModel
import app.pinya.lime.domain.model.menus.AppMenu
import app.pinya.lime.ui.utils.OnSwipeTouchListener
import app.pinya.lime.ui.utils.Utils
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

    // APP LIST
    private var appList: MutableList<AppModel> = mutableListOf()

    // CONTEXT MENU
    private var contextMenuContainer: ConstraintLayout? = null

    init {
        initContextMenu()
        initGestureDetector()
    }

    // ########################################
    //   GENERAL
    // ########################################

    fun handleSettingsUpdate(settings: SettingsModel) {
        addDateListeners(settings)
        addTimeListeners(settings)
        updateTimeDateStyle(settings)
        startTimerToUpdateDateTime(settings)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun handleHomeListUpdate(newHomeList: MutableList<AppModel>) {
        appList = newHomeList
        this.notifyDataSetChanged()
    }

    // ########################################
    //   DATE & TIME
    // ########################################

    @SuppressLint("ClickableViewAccessibility")
    private fun addDateListeners(settings: SettingsModel) {
        date = layout.findViewById(R.id.homeDate)
        date?.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onFlingDown() {
                expandNotificationBar()
            }

            override fun onClick() {
                when (val stateValue = settings.dateClickApp) {
                    "default" -> {
                        val builder: Uri.Builder =
                            CalendarContract.CONTENT_URI.buildUpon().appendPath("time")
                        val intent = Intent(Intent.ACTION_VIEW).setData(builder.build())
                        context.startActivity(intent)
                    }
                    "none" -> return
                    else -> {
                        val launchAppIntent =
                            context.packageManager.getLaunchIntentForPackage(stateValue)
                        if (launchAppIntent != null) context.startActivity(launchAppIntent)
                    }
                }
            }

            override fun onLongClick() {
                Utils.vibrate(context)
                context.startActivity(Intent(context, SettingsActivity::class.java))
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addTimeListeners(settings: SettingsModel) {
        time = layout.findViewById(R.id.homeTime)
        time?.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onFlingDown() {
                expandNotificationBar()
            }

            override fun onClick() {
                when (val stateValue = settings.timeClickApp) {
                    "default" -> {
                        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                    }
                    "none" -> return
                    else -> {
                        val launchAppIntent =
                            context.packageManager.getLaunchIntentForPackage(stateValue)
                        if (launchAppIntent != null) context.startActivity(launchAppIntent)
                    }
                }
            }

            override fun onLongClick() {
                Utils.vibrate(context)
                context.startActivity(Intent(context, SettingsActivity::class.java))
            }
        })
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


    private fun updateTimeDateStyle(settings: SettingsModel) {
        val blackTextValue = settings.generalIsTextBlack

        date?.setTextColor(
            ContextCompat.getColor(
                context,
                if (blackTextValue) R.color.black else R.color.white
            )
        )

        time?.setTextColor(
            ContextCompat.getColor(
                context,
                if (blackTextValue) R.color.black else R.color.white
            )
        )
    }

    // ########################################
    //   GESTURES
    // ########################################

    @SuppressLint("ClickableViewAccessibility")
    private fun initGestureDetector() {
        layout.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onFlingDown() {
                expandNotificationBar()
            }

            override fun onLongClick() {
                Utils.vibrate(context)
                context.startActivity(Intent(context, SettingsActivity::class.java))
            }
        })
    }

    @SuppressLint("WrongConstant", "PrivateApi")
    private fun expandNotificationBar() {
        try {
            val statusBarService = context.getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val method = statusBarManager.getMethod("expandNotificationsPanel")
            method.invoke(statusBarService)
        } catch (_: Error) {
        }
    }

    // ########################################
    //   CONTEXT MENU
    // ########################################

    private fun initContextMenu() {
        contextMenuContainer = layout.findViewById(R.id.contextMenuHome_parent)
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

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ItemAppViewHolder, position: Int) {
        val currentApp = appList[position]

        val imageView: ImageView = holder.itemView.findViewById(R.id.appIcon)
        val textView: TextView = holder.itemView.findViewById(R.id.appName)
        val linearLayout: LinearLayout = holder.itemView.findViewById(R.id.appLayout)

        linearLayout.alpha = if (currentApp.hidden) 0.35f else 1f

        imageView.setImageDrawable(currentApp.icon)
        val areIconsVisible = viewModel.settings.value?.drawerShowIcons ?: true
        imageView.visibility = if (areIconsVisible) View.VISIBLE else View.GONE

        textView.text = currentApp.name
        val isTextBlack = viewModel.settings.value?.generalIsTextBlack ?: false
        textView.setTextColor(
            ContextCompat.getColor(
                context,
                if (isTextBlack) R.color.black else R.color.white
            )
        )

        linearLayout.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onFlingDown() {
                expandNotificationBar()
            }

            override fun onClick() {
                val launchAppIntent =
                    context.packageManager.getLaunchIntentForPackage(currentApp.packageName)
                if (launchAppIntent != null) context.startActivity(launchAppIntent)
            }

            override fun onLongClick() {
                if (contextMenuContainer != null) {
                    Utils.vibrate(context)
                    viewModel.appMenu.postValue(AppMenu(currentApp, true, contextMenuContainer!!))
                }
            }
        })
    }

    override fun getItemCount(): Int {
        return appList.size
    }
}