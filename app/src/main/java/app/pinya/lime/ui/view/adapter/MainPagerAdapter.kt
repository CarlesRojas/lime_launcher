package app.pinya.lime.ui.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import app.pinya.lime.R

class MainPagerAdapter(private val context: Context) : PagerAdapter() {

    private var home: HomeAdapter? = null
    private var drawer: DrawerAdapter? = null


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
        val viewHome = layout.findViewById<View>(R.id.homeAppList) as RecyclerView

        this.home = HomeAdapter(context, layout).also { adapter ->
            viewHome.adapter = adapter
            viewHome.layoutManager = LinearLayoutManager(context)
        }

        collection.addView(layout)
        return layout
    }

    private fun instantiateDrawer(collection: ViewGroup): ViewGroup {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.view_drawer, collection, false) as ViewGroup
        val viewDrawer = layout.findViewById<View>(R.id.drawerAppList) as RecyclerView

        this.drawer = DrawerAdapter(context, layout).also { adapter ->
            viewDrawer.adapter = adapter
            viewDrawer.layoutManager = LinearLayoutManager(context)
        }

        collection.addView(layout)
        return layout
    }

    fun onHomePageSelected() {}

    fun onDrawerPageSelected() {}
}