package app.pinya.lime.ui.view.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import app.pinya.lime.R
import app.pinya.lime.data.memory.AppProvider
import app.pinya.lime.databinding.ActivityMainBinding
import app.pinya.lime.domain.model.BooleanPref
import app.pinya.lime.ui.utils.Utils
import app.pinya.lime.ui.view.adapter.*
import app.pinya.lime.ui.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val appViewModel: AppViewModel by viewModels()

    private lateinit var viewPager: ViewPager
    private lateinit var customPageAdapter: MainPagerAdapter

    private lateinit var appMenuAdapter: AppMenuAdapter
    private lateinit var renameMenuAdapter: RenameMenuAdapter
    private lateinit var reorderMenuAdapter: ReorderMenuAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        makeNavbarTransparent()
        AppProvider.initialize(this.application)

        appMenuAdapter = AppMenuAdapter(this, appViewModel)
        renameMenuAdapter = RenameMenuAdapter(this, appViewModel)
        reorderMenuAdapter = ReorderMenuAdapter(this, appViewModel)

        setContentView(R.layout.activity_main)
        linkAdapter()

        appViewModel.homeList.observe(this) { newHomeList ->
            customPageAdapter.home?.handleHomeListUpdate(newHomeList)
        }

        appViewModel.drawerList.observe(this) { newDrawerList ->
            customPageAdapter.drawer?.handleDrawerListUpdate(newDrawerList)
        }

        appViewModel.appMenu.observe(this) { appMenu ->
            appMenuAdapter.handleAppMenu(appMenu)
        }

        appViewModel.renameMenu.observe(this) { renameMenu ->
            renameMenuAdapter.handleRenameMenu(renameMenu)
        }

        appViewModel.reorderMenu.observe(this) { reorderMenu ->
            reorderMenuAdapter.handleReorderMenu(reorderMenu)
        }

        appViewModel.getInfo()
        appViewModel.updateAppList(this)

        // TODO update wallpaper daily
    }

    override fun onResume() {
        super.onResume()

        viewPager.setCurrentItem(0, false)

        val showStatusBar = Utils.getBooleanPref(this, BooleanPref.GENERAL_SHOW_STATUS_BAR)
        val dimBackground = Utils.getBooleanPref(this, BooleanPref.GENERAL_DIM_BACKGROUND)
        val isTextBlack = Utils.getBooleanPref(this, BooleanPref.GENERAL_IS_TEXT_BLACK)

        showStatusBar(showStatusBar)
        dimBackground(dimBackground, isTextBlack)

        customPageAdapter.onResume()

        hideContextMenus()
    }

    private fun linkAdapter() {
        viewPager = findViewById(R.id.viewPager)

        customPageAdapter = MainPagerAdapter(this, appViewModel).also { adapter ->
            viewPager.adapter = adapter

            viewPager.addOnPageChangeListener(object : OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int, positionOffset: Float, positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    if (position == 0) adapter.onHomePageSelected() else adapter.onDrawerPageSelected()
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })
        }
    }

    private fun dimBackground(dimBackground: Boolean, isTextBlack: Boolean) {
        viewPager.setBackgroundColor(
            ContextCompat.getColor(
                this,
                if (dimBackground) (if (isTextBlack) R.color.white_extra_low else R.color.black_extra_low) else R.color.transparent
            )
        )
    }

    private fun makeNavbarTransparent() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    }

    @Suppress("DEPRECATION")
    private fun showStatusBar(showStatusBar: Boolean) {
        if (showStatusBar) {
            if (Build.VERSION.SDK_INT >= 30) window.decorView.windowInsetsController!!.show(
                WindowInsets.Type.statusBars()
            )
            else window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        } else {
            if (Build.VERSION.SDK_INT >= 30) window.decorView.windowInsetsController!!.hide(
                WindowInsets.Type.statusBars()
            )
            else window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }

    private fun hideContextMenus() {
        appViewModel.appMenu.postValue(null)
        appViewModel.renameMenu.postValue(null)
        appViewModel.reorderMenu.postValue(null)
    }
}