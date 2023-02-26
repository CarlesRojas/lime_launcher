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
    private lateinit var appListMenuAdapter: AppListMenuAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        makeNavbarTransparent()
        AppProvider.initialize(this.application)

        appMenuAdapter = AppMenuAdapter(this, appViewModel)
        renameMenuAdapter = RenameMenuAdapter(this, appViewModel)
        reorderMenuAdapter = ReorderMenuAdapter(this, appViewModel)
        appListMenuAdapter = AppListMenuAdapter(this, appViewModel)

        setContentView(R.layout.activity_main)
        linkAdapter()
    }

    override fun onResume() {
        super.onResume()

        appViewModel.homeList.observe(this) { newHomeList ->
            customPageAdapter.home?.handleHomeListUpdate(newHomeList)
        }

        appViewModel.drawerList.observe(this) { newDrawerList ->
            customPageAdapter.drawer?.handleDrawerListUpdate(newDrawerList)
        }

        appViewModel.settings.observe(this) { settings ->
            showStatusBar(settings.generalShowStatusBar)
            dimBackground(settings.generalDimBackground, settings.generalIsTextBlack)
            customPageAdapter.home?.handleSettingsUpdate(settings)
            customPageAdapter.drawer?.handleSettingsUpdate(settings)
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

        appViewModel.appListMenu.observe(this) { appListMenu ->
            appListMenuAdapter.handleAppListMenu(appListMenu)
        }

        appViewModel.getSettings()
        appViewModel.getInfo()
        appViewModel.updateAppList()

        viewPager.setCurrentItem(0, false)

        // TODO update wallpaper daily
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
        appViewModel.appListMenu.postValue(null)
    }
}