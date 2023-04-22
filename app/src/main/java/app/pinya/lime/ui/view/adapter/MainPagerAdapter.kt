package app.pinya.lime.ui.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import app.pinya.lime.R
import app.pinya.lime.domain.model.BooleanPref
import app.pinya.lime.ui.utils.Utils
import app.pinya.lime.ui.viewmodel.AppViewModel
import kotlinx.coroutines.*
import java.util.ArrayList

class MainPagerAdapter(private val context: Context, private val viewModel: AppViewModel) :
    PagerAdapter() {

    var widget: WidgetPageAdapter? = null
    var home: HomeAdapter? = null
    var drawer: DrawerAdapter? = null

    private var viewWidget: RecyclerView? = null
    private var viewHome: RecyclerView? = null
    private var viewDrawer: RecyclerView? = null

    fun onResume() {
        setWidgetLayoutManager()
        setHomeLayoutManager()
        setDrawerLayoutManager()

        val showWidgetPage = Utils.getBooleanPref(context, BooleanPref.GENERAL_SHOW_WIDGET_PAGE)

        if (showWidgetPage) widget?.onResume()
        home?.onResume()
        drawer?.onResume()
    }


    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val showWidgetPage = Utils.getBooleanPref(context, BooleanPref.GENERAL_SHOW_WIDGET_PAGE)

        return if (showWidgetPage) when (position) {
            0 -> instantiateWidgetPage(collection)
            1 -> instantiateHome(collection)
            2 -> instantiateDrawer(collection)
            else -> instantiateHome(collection)
        } else when (position) {
            0 -> instantiateHome(collection)
            1 -> instantiateDrawer(collection)
            else -> instantiateHome(collection)
        }
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        val showWidgetPage = Utils.getBooleanPref(context, BooleanPref.GENERAL_SHOW_WIDGET_PAGE)
        return if (showWidgetPage) 3 else 2
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getPageTitle(position: Int): CharSequence {
        val showWidgetPage = Utils.getBooleanPref(context, BooleanPref.GENERAL_SHOW_WIDGET_PAGE)

        if (showWidgetPage) return when (position) {
            0 -> "Widget"
            1 -> "Home"
            2 -> "Drawer"
            else -> "Unknown"
        }
        else return if (position == 0) "Home" else "Drawer"
    }

    private fun instantiateWidgetPage(collection: ViewGroup): ViewGroup {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.view_widget_page, collection, false) as ViewGroup
        viewWidget = layout.findViewById<View>(R.id.widgetList) as RecyclerView

        this.widget = WidgetPageAdapter(context, layout, viewModel).also { adapter ->
            viewWidget!!.adapter = adapter
        }
        setWidgetLayoutManager()

        collection.addView(layout)
        return layout
    }

    private fun setWidgetLayoutManager() {
        viewWidget?.layoutManager = LinearLayoutManager(context)
    }


    private fun instantiateHome(collection: ViewGroup): ViewGroup {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.view_home, collection, false) as ViewGroup
        viewHome = layout.findViewById<View>(R.id.homeAppList) as RecyclerView

        this.home = HomeAdapter(context, layout, viewModel).also { adapter ->
            viewHome!!.adapter = adapter
        }
        setHomeLayoutManager()
        this.home?.handleHomeListUpdate(viewModel.homeList.value ?: ArrayList())

        collection.addView(layout)
        return layout
    }

    private fun setHomeLayoutManager() {
        val showInGrid = Utils.getBooleanPref(context, BooleanPref.HOME_SHOW_IN_GRID)

        viewHome?.layoutManager =
            if (showInGrid) object : GridLayoutManager(context, 3) {
                override fun canScrollVertically() = false
            } else object : LinearLayoutManager(context) {
                override fun canScrollVertically() = false
            }
    }

    private fun instantiateDrawer(collection: ViewGroup): ViewGroup {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.view_drawer, collection, false) as ViewGroup
        viewDrawer = layout.findViewById<View>(R.id.drawerAppList) as RecyclerView

        this.drawer = DrawerAdapter(context, layout, viewModel).also { adapter ->
            viewDrawer!!.adapter = adapter
        }
        setDrawerLayoutManager()
        this.drawer?.handleDrawerListUpdate(viewModel.drawerList.value ?: ArrayList())

        collection.addView(layout)
        return layout
    }

    private fun setDrawerLayoutManager() {
        val showInGrid = Utils.getBooleanPref(context, BooleanPref.DRAWER_SHOW_IN_GRID)

        viewDrawer?.layoutManager =
            if (showInGrid) GridLayoutManager(context, 3) else LinearLayoutManager(context)
    }

    fun onWidgetPageSelected() {
        println("WIDGET PAGE SELECTED")
    }


    @OptIn(DelicateCoroutinesApi::class)
    fun onHomePageSelected() {
        drawer?.hideKeyboard()

        GlobalScope.launch(Dispatchers.Main) {
            delay(400)
            drawer?.clearText()
        }
    }

    fun onDrawerPageSelected() {
        this.drawer?.clearText()
        this.drawer?.showKeyboard()
    }
}