package app.pinya.lime.ui.view.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.view.*
import android.view.ViewGroup.MarginLayoutParams
import android.view.accessibility.AccessibilityEvent
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.pinya.lime.R
import app.pinya.lime.domain.model.AppModel
import app.pinya.lime.domain.model.BooleanPref
import app.pinya.lime.domain.model.StringPref
import app.pinya.lime.domain.model.menus.AppMenu
import app.pinya.lime.ui.utils.OnSwipeTouchListener
import app.pinya.lime.ui.utils.Utils
import app.pinya.lime.ui.view.activity.SettingsActivity
import app.pinya.lime.ui.view.holder.AppViewHolder
import app.pinya.lime.ui.viewmodel.AppViewModel
import com.makeramen.roundedimageview.RoundedImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor


class HomeAdapter(
    private val context: Context, private val layout: ViewGroup, private val viewModel: AppViewModel
) : RecyclerView.Adapter<AppViewHolder>() {

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
        onResume()
    }

    // ########################################
    //   GENERAL
    // ########################################

    @SuppressLint("NotifyDataSetChanged")
    fun onResume() {
        calculateMaxNumberOfAppsInHome()
        addDateListeners()
        addTimeListeners()
        updateTimeDateStyle()
        startTimerToUpdateDateTime()
        this.notifyDataSetChanged()
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
    private fun addDateListeners() {
        val alignment = Utils.getStringPref(context, StringPref.HOME_ALIGNMENT)

        date = layout.findViewById(R.id.homeDate)
        date?.gravity = when (alignment) {
            "right" -> Gravity.END
            "center" -> Gravity.CENTER
            else -> Gravity.START
        }

        date?.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onFlingDown() {
                when (Utils.getStringPref(context, StringPref.HOME_SWIPE_DOWN_ACTION)) {
                    "openApp" -> openApp("swipeDown")
                    "expandNotifications" -> expandNotificationBar()
                    "assistant" -> openAssistant()
                    "screenLock" -> lockScreen()
                }
            }

            override fun onFlingUp() {
                when (Utils.getStringPref(context, StringPref.HOME_SWIPE_UP_ACTION)) {
                    "openApp" -> openApp("swipeUp")
                    "expandNotifications" -> expandNotificationBar()
                    "assistant" -> openAssistant()
                    "screenLock" -> lockScreen()
                }
            }


            override fun onClick() {
                when (val dateClickApp = Utils.getStringPref(context, StringPref.DATE_CLICK_APP)) {
                    "default" -> {
                        val builder: Uri.Builder =
                            CalendarContract.CONTENT_URI.buildUpon().appendPath("time")
                        val intent = Intent(Intent.ACTION_VIEW).setData(builder.build())
                        context.startActivity(intent)
                    }
                    "none" -> return
                    else -> {
                        val launchAppIntent =
                            context.packageManager.getLaunchIntentForPackage(dateClickApp)
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
    private fun addTimeListeners() {
        val alignment = Utils.getStringPref(context, StringPref.HOME_ALIGNMENT)

        time = layout.findViewById(R.id.homeTime)
        time?.gravity = when (alignment) {
            "right" -> Gravity.END
            "center" -> Gravity.CENTER
            else -> Gravity.START
        }

        time?.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onFlingDown() {
                when (Utils.getStringPref(context, StringPref.HOME_SWIPE_DOWN_ACTION)) {
                    "openApp" -> openApp("swipeDown")
                    "expandNotifications" -> expandNotificationBar()
                    "assistant" -> openAssistant()
                    "screenLock" -> lockScreen()
                }
            }

            override fun onFlingUp() {
                when (Utils.getStringPref(context, StringPref.HOME_SWIPE_UP_ACTION)) {
                    "openApp" -> openApp("swipeUp")
                    "expandNotifications" -> expandNotificationBar()
                    "assistant" -> openAssistant()
                    "screenLock" -> lockScreen()
                }
            }

            override fun onClick() {
                when (val timeClickApp = Utils.getStringPref(context, StringPref.TIME_CLICK_APP)) {
                    "default" -> {
                        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                    }
                    "none" -> return
                    else -> {
                        val launchAppIntent =
                            context.packageManager.getLaunchIntentForPackage(timeClickApp)
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

    private fun startTimerToUpdateDateTime() {
        timer?.cancel()
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                (context as Activity).runOnUiThread {
                    val dateFormat =
                        Utils.getStringPref(context, StringPref.DATE_FORMAT).toIntOrNull() ?: 1
                    val timeFormat =
                        Utils.getStringPref(context, StringPref.TIME_FORMAT).toIntOrNull() ?: 0

                    date?.text =
                        SimpleDateFormat.getDateInstance(SettingsActivity.mapFormat(dateFormat))
                            .format(Date())

                    time?.text =
                        SimpleDateFormat.getTimeInstance(SettingsActivity.mapFormat(timeFormat))
                            .format(Date())
                }
            }
        }, 0, 1000)
    }


    private fun updateTimeDateStyle() {
        val isTextBlack = Utils.getBooleanPref(context, BooleanPref.GENERAL_IS_TEXT_BLACK)
        val isTimeVisible = Utils.getBooleanPref(context, BooleanPref.TIME_VISIBLE)
        val isDateVisible = Utils.getBooleanPref(context, BooleanPref.DATE_VISIBLE)

        date?.setTextColor(
            ContextCompat.getColor(
                context, if (isTextBlack) R.color.black else R.color.white
            )
        )
        date?.visibility = if (isDateVisible) View.VISIBLE else View.GONE

        time?.setTextColor(
            ContextCompat.getColor(
                context, if (isTextBlack) R.color.black else R.color.white
            )
        )
        time?.visibility = if (isTimeVisible) View.VISIBLE else View.GONE
    }

    // ########################################
    //   MAX APPS IN HOME
    // ########################################

    private fun calculateMaxNumberOfAppsInHome() {
        val isTimeVisible = Utils.getBooleanPref(context, BooleanPref.TIME_VISIBLE)
        val isDateVisible = Utils.getBooleanPref(context, BooleanPref.DATE_VISIBLE)
        val showInGrid = Utils.getBooleanPref(context, BooleanPref.HOME_SHOW_IN_GRID)
        val textSize = Utils.pxToDp(context, Utils.spToPx(context, 12).toInt())

        val homeAppListContainer =
            layout.findViewById<View>(R.id.homeAppListContainer) as ConstraintLayout

        val containerParams = homeAppListContainer.layoutParams as ConstraintLayout.LayoutParams
        containerParams.topToBottom = ConstraintLayout.LayoutParams.UNSET

        if (isTimeVisible && isDateVisible) containerParams.topToBottom = R.id.guidelineH2
        else if (!isTimeVisible && !isDateVisible) containerParams.topToBottom = R.id.guidelineH1
        else containerParams.topToBottom = R.id.guidelineHMiddle

        homeAppListContainer.layoutParams = containerParams


        layout.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                layout.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val heightInDp = Utils.pxToDp(context, homeAppListContainer.height)
                val appHeightInDp = if (showInGrid) 108f + textSize else 66f

                var maxNumberOfHomeApps =
                    (floor(heightInDp / appHeightInDp).toInt() - 1) * (if (showInGrid) 4 else 1)
                maxNumberOfHomeApps = maxNumberOfHomeApps.coerceAtMost(12)

                val info = viewModel.info.value ?: return
                info.maxNumberOfHomeApps = maxNumberOfHomeApps

                val newHomeApps =
                    info.homeApps.filterIndexed { index, _ -> index < maxNumberOfHomeApps }

                info.homeApps = newHomeApps.toMutableSet()
                viewModel.updateInfo(info, context)
            }
        })
    }

    // ########################################
    //   GESTURES
    // ########################################

    @SuppressLint("ClickableViewAccessibility")
    private fun initGestureDetector() {
        layout.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onFlingDown() {
                when (Utils.getStringPref(context, StringPref.HOME_SWIPE_DOWN_ACTION)) {
                    "openApp" -> openApp("swipeDown")
                    "expandNotifications" -> expandNotificationBar()
                    "assistant" -> openAssistant()
                    "screenLock" -> lockScreen()
                }
            }

            override fun onFlingUp() {
                when (Utils.getStringPref(context, StringPref.HOME_SWIPE_UP_ACTION)) {
                    "openApp" -> openApp("swipeUp")
                    "expandNotifications" -> expandNotificationBar()
                    "assistant" -> openAssistant()
                    "screenLock" -> lockScreen()
                }
            }

            override fun onDoubleClick() {
                when (Utils.getStringPref(context, StringPref.HOME_DOUBLE_TAP_ACTION)) {
                    "openApp" -> openApp("doubleTap")
                    "expandNotifications" -> expandNotificationBar()
                    "assistant" -> openAssistant()
                    "screenLock" -> lockScreen()
                }
            }

            override fun onLongClick() {
                Utils.vibrate(context)
                context.startActivity(Intent(context, SettingsActivity::class.java))
            }
        })
    }

    // ########################################
    //   CONTEXT MENU
    // ########################################

    private fun initContextMenu() {
        contextMenuContainer = layout.findViewById(R.id.contextMenuHome_parent)
    }

    // ########################################
    //   GESTURE ACTIONS
    // ########################################

    private fun openAssistant() {
        try {
            val intent = Intent(Intent.ACTION_VOICE_COMMAND)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            Utils.vibrate(context)
        } catch (_: Error) {
        }
    }


    private fun lockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && Utils.isAccessServiceEnabled(context)) {
            Utils.vibrate(context)
            val lock = layout.findViewById<TextView>(R.id.lock)
            lock.performClick()

            @Suppress("DEPRECATION") val event =
                AccessibilityEvent.obtain(AccessibilityEvent.TYPE_VIEW_CLICKED)
            lock.parent.requestSendAccessibilityEvent(lock, event)
        }
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

    private fun openApp(fromSwipeUp: String) {
        val appToOpen = Utils.getStringPref(
            context, when (fromSwipeUp) {
                "doubleTap" -> StringPref.HOME_DOUBLE_TAP_APP
                "swipeUp" -> StringPref.HOME_SWIPE_UP_APP
                else -> StringPref.HOME_SWIPE_DOWN_APP
            }
        )

        if (appToOpen != "none") {
            Utils.vibrate(context)
            val launchAppIntent = context.packageManager.getLaunchIntentForPackage(appToOpen)
            if (launchAppIntent != null) context.startActivity(launchAppIntent)
        }
    }


    // ########################################
    //   RECYCLER VIEW
    // ########################################

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return AppViewHolder(
            inflater.inflate(R.layout.view_app, parent, false)
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {

        val imageView: RoundedImageView = holder.itemView.findViewById(R.id.appIcon)
        val textView: TextView = holder.itemView.findViewById(R.id.appName)
        val linearLayout: LinearLayout = holder.itemView.findViewById(R.id.appLayout)

        val isTextBlack = Utils.getBooleanPref(context, BooleanPref.GENERAL_IS_TEXT_BLACK)
        val areIconsVisible = Utils.getBooleanPref(context, BooleanPref.HOME_SHOW_ICONS)
        val showInGrid = Utils.getBooleanPref(context, BooleanPref.HOME_SHOW_IN_GRID)
        val araLabelsVisible =
            !showInGrid || Utils.getBooleanPref(context, BooleanPref.HOME_SHOW_LABELS)
        val alignment = Utils.getStringPref(context, StringPref.HOME_ALIGNMENT)

        if (appList.size > 0) {
            val currentApp = appList.find { it.homeOrderIndex == position } ?: return

            linearLayout.alpha = if (currentApp.hidden) 0.35f else 1f

            imageView.setImageDrawable(currentApp.icon)
            imageView.visibility = if (areIconsVisible) View.VISIBLE else View.GONE
            imageView.background = ContextCompat.getDrawable(
                context, R.drawable.background_rounded
            )

            textView.text = currentApp.name
            textView.isSingleLine = true
            textView.setTextColor(
                ContextCompat.getColor(
                    context, if (isTextBlack) R.color.black else R.color.white
                )
            )
            textView.alpha = if (araLabelsVisible) 1f else 0f

            if (showInGrid) {
                imageView.layoutParams.height = Utils.dpToPx(context, 68)
                imageView.layoutParams.width = Utils.dpToPx(context, 68)

                textView.textSize = 12f
                textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

                linearLayout.orientation = LinearLayout.VERTICAL
                linearLayout.setPadding(0, Utils.dpToPx(context, 20), 0, Utils.dpToPx(context, 20))
                linearLayout.gravity = Gravity.CENTER

                val marginParams = MarginLayoutParams(imageView.layoutParams)
                marginParams.setMargins(0, 0, 0, Utils.dpToPx(context, 6))
                val layoutParams = LinearLayout.LayoutParams(marginParams)
                imageView.layoutParams = layoutParams
            } else {
                imageView.layoutParams.height = Utils.dpToPx(context, 42)
                imageView.layoutParams.width = Utils.dpToPx(context, 42)

                textView.textSize = 19.5f
                textView.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START

                linearLayout.orientation = LinearLayout.HORIZONTAL
                val padding = Utils.dpToPx(context, if (areIconsVisible) 12 else 18)
                linearLayout.setPadding(0, padding, 0, padding)
                linearLayout.gravity = when (alignment) {
                    "right" -> Gravity.END
                    "center" -> Gravity.CENTER
                    else -> Gravity.START
                }

                val marginParams = MarginLayoutParams(imageView.layoutParams)
                marginParams.setMargins(0, 0, Utils.dpToPx(context, 18), 0)
                val layoutParams = LinearLayout.LayoutParams(marginParams)
                imageView.layoutParams = layoutParams
            }

            linearLayout.setOnTouchListener(object : OnSwipeTouchListener(context) {
                override fun onFlingDown() {
                    when (Utils.getStringPref(context, StringPref.HOME_SWIPE_DOWN_ACTION)) {
                        "openApp" -> openApp("swipeDown")
                        "expandNotifications" -> expandNotificationBar()
                        "assistant" -> openAssistant()
                        "screenLock" -> lockScreen()
                    }
                }

                override fun onFlingUp() {
                    when (Utils.getStringPref(context, StringPref.HOME_SWIPE_UP_ACTION)) {
                        "openApp" -> openApp("swipeUp")
                        "expandNotifications" -> expandNotificationBar()
                        "assistant" -> openAssistant()
                        "screenLock" -> lockScreen()
                    }
                }

                override fun onClick() {
                    val launchAppIntent =
                        context.packageManager.getLaunchIntentForPackage(currentApp.packageName)
                    if (launchAppIntent != null) context.startActivity(launchAppIntent)
                }

                override fun onLongClick() {
                    if (contextMenuContainer != null) {
                        Utils.vibrate(context)
                        viewModel.appMenu.postValue(
                            AppMenu(
                                currentApp, true, contextMenuContainer!!
                            )
                        )
                    }
                }
            })
        } else {
            if (showInGrid) return


            val text = when (position) {
                0 -> "Long press above to open settings"
                1 -> "All your apps are to the right"
                else -> "Long press an app to add it here"
            }

            val icon = when (position) {
                0 -> R.drawable.icon_settings
                1 -> R.drawable.icon_arrow_right
                else -> R.drawable.icon_menu
            }

            imageView.setImageDrawable(
                ContextCompat.getDrawable(
                    context, icon
                )
            )
            imageView.background = null

            textView.text = text
            textView.isSingleLine = false
            textView.setTextColor(
                ContextCompat.getColor(
                    context, if (isTextBlack) R.color.black else R.color.white
                )
            )

            val marginParams = MarginLayoutParams(imageView.layoutParams)
            marginParams.setMargins(0, 0, Utils.dpToPx(context, 18), 0)
            val layoutParams = LinearLayout.LayoutParams(marginParams)
            imageView.layoutParams = layoutParams

            linearLayout.gravity = Gravity.START
            linearLayout.setOnTouchListener(object : OnSwipeTouchListener(context) {
                override fun onFlingDown() {
                    when (Utils.getStringPref(context, StringPref.HOME_SWIPE_DOWN_ACTION)) {
                        "openApp" -> openApp("swipeDown")
                        "expandNotifications" -> expandNotificationBar()
                        "assistant" -> openAssistant()
                        "screenLock" -> lockScreen()
                    }
                }

                override fun onFlingUp() {
                    when (Utils.getStringPref(context, StringPref.HOME_SWIPE_UP_ACTION)) {
                        "openApp" -> openApp("swipeUp")
                        "expandNotifications" -> expandNotificationBar()
                        "assistant" -> openAssistant()
                        "screenLock" -> lockScreen()
                    }
                }
            })
        }
    }

    override fun getItemCount(): Int {
        val info = viewModel.info.value ?: return appList.size
        val showInGrid = Utils.getBooleanPref(context, BooleanPref.HOME_SHOW_IN_GRID)

        return if (appList.size <= 0 && !info.tutorialDone && !showInGrid) 3 else appList.size
    }
}