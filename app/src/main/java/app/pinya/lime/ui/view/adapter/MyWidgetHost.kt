import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.view.View

class MyWidgetHost(context: Context, hostId: Int) : AppWidgetHost(context, hostId) {

    override fun onCreateView(
        context: Context,
        appWidgetId: Int,
        appWidgetInfo: AppWidgetProviderInfo
    ): AppWidgetHostView? {
        val view = super.onCreateView(context, appWidgetId, appWidgetInfo)
        // add any customization here
        return view
    }

    override fun onProviderChanged(appWidgetId: Int, appWidgetProviderInfo: AppWidgetProviderInfo) {
        super.onProviderChanged(appWidgetId, appWidgetProviderInfo)
        // update any relevant data or UI here
    }

}