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

class MainPagerAdapter(private val context: Context, private val viewModel: AppViewModel) :
    PagerAdapter() {

    var home: HomeAdapter? = null
    var drawer: DrawerAdapter? = null

    var viewHome: RecyclerView? = null
    var viewDrawer: RecyclerView? = null

    fun onResume() {
        setHomeLayoutManager()
        setDrawerLayoutManager()

        home?.onResume()
        drawer?.onResume()
    }


    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        return if (position == 0) instantiateHome(collection)
        else instantiateDrawer(collection)
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return 2
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getPageTitle(position: Int): CharSequence {
        return if (position == 0) "Home" else "Drawer"
    }


    private fun instantiateHome(collection: ViewGroup): ViewGroup {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.view_home, collection, false) as ViewGroup
        viewHome = layout.findViewById<View>(R.id.homeAppList) as RecyclerView

        this.home = HomeAdapter(context, layout, viewModel).also { adapter ->
            viewHome!!.adapter = adapter
        }
        setHomeLayoutManager()

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

        collection.addView(layout)
        return layout
    }

    private fun setDrawerLayoutManager() {
        val showInGrid = Utils.getBooleanPref(context, BooleanPref.DRAWER_SHOW_IN_GRID)

        viewDrawer?.layoutManager =
            if (showInGrid) GridLayoutManager(context, 3) else LinearLayoutManager(context)
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