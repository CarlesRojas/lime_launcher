package app.pinya.lime.ui.view.adapter

import MyWidgetHost
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RemoteViews
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import app.pinya.lime.R
import app.pinya.lime.ui.utils.OnSwipeTouchListener
import app.pinya.lime.ui.utils.Utils
import app.pinya.lime.ui.view.holder.AppViewHolder
import app.pinya.lime.ui.viewmodel.AppViewModel


class WidgetPageAdapter(
    private val context:
    Context, private val layout: ViewGroup,
    private val viewModel: AppViewModel,
    private val pickAppWidgetLauncher: ActivityResultLauncher<Intent>,
    private val configureAppWidgetLauncher : ActivityResultLauncher<Intent>
) : RecyclerView.Adapter<AppViewHolder>() {

    private var widgetHost: MyWidgetHost? = null
    private var appWidgetManager: AppWidgetManager? = null
    private var container: ConstraintLayout? = null

    init {
        onResume()
        appWidgetManager = AppWidgetManager.getInstance(context)

        widgetHost = MyWidgetHost(context, R.string.widget_screen_host)
        widgetHost?.startListening()
    }

    // ########################################
    //   GENERAL
    // ########################################

    @SuppressLint("NotifyDataSetChanged")
    fun onResume() {
        container = layout.findViewById(R.id.widgetConstraintLayout)

        container?.setOnTouchListener(object : OnSwipeTouchListener(context) {

            override fun onLongClick() {
                Utils.vibrate(context)
                addWidget()
            }
        })
    }

    private fun addWidget() {
        val appWidgetId = widgetHost?.allocateAppWidgetId()
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        pickAppWidgetLauncher.launch(intent)
    }
/*
    fun notifyWidgetAdded(appWidgetId: Int, appWidgetProviderInfo: AppWidgetProviderInfo) {
        appWidgetManager?.bindAppWidgetIdIfAllowed(appWidgetId, appWidgetProviderInfo.provider)


        if (appWidgetProviderInfo.configure != null) {
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
            intent.component = appWidgetProviderInfo.configure
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            startActivityForResult.launch(intent)
        } else {
            appWidgetManager?.bindAppWidgetIdIfAllowed(appWidgetId, appWidgetProviderInfo.provider)
        }
    }
*/
    fun notifyPickWidgetResult (result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val appWidgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
            val appWidgetProviderInfo = appWidgetManager?.getAppWidgetInfo(appWidgetId)
            if (appWidgetProviderInfo?.configure != null) {
                // If the widget requires configuration, launch the configuration activity
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
                intent.component = appWidgetProviderInfo.configure
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                configureAppWidgetLauncher.launch(intent)
            } else {
                // Bind the widget to the given appWidgetId using AppWidgetManager
                /*val componentName = appWidgetProviderInfo?.provider
                appWidgetManager?.bindAppWidgetIdIfAllowed(appWidgetId, componentName)
                val remoteViews = RemoteViews(context.packageName, R.layout.view_widget_page)
                appWidgetManager?.updateAppWidget(appWidgetId, remoteViews)*/
                createWidget(data)
            }
        }
    }

    fun notifyWidgetConfiguredResult (result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            createWidget(data)
            /*
            val appWidgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
            val appWidgetProviderInfo = appWidgetManager?.getAppWidgetInfo(appWidgetId)
            val componentName = appWidgetProviderInfo?.provider
            appWidgetManager?.bindAppWidgetIdIfAllowed(appWidgetId, componentName)
            val remoteViews = RemoteViews(context.packageName, R.layout.view_widget_page)
            appWidgetManager?.updateAppWidget(appWidgetId, remoteViews)
            */
        }
    }

    private fun createWidget(data: Intent?) {
        val appWidgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
        val appWidgetInfo = AppWidgetManager.getInstance(context).getAppWidgetInfo(appWidgetId)
        val widgetWidth = appWidgetInfo.minWidth
        val widgetHeight = appWidgetInfo.minHeight

        val hostView = widgetHost?.createView(context, appWidgetId, appWidgetInfo)
        hostView?.setAppWidget(appWidgetId, appWidgetInfo)
        val widgetLayout = layout.findViewById<LinearLayout>(R.id.widgetlayout)

        val layoutParams = LinearLayout.LayoutParams(
            widgetWidth,
            widgetHeight
        )
        widgetLayout.addView(hostView,layoutParams)
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

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {

    }


    override fun getItemCount(): Int {
        return 0
    }
}