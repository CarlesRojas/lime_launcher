package app.pinya.lime.ui.view.activity

import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import app.pinya.lime.LimeLauncherApp
import app.pinya.lime.R
import app.pinya.lime.data.memory.AppProvider
import app.pinya.lime.databinding.ActivityMainBinding
import app.pinya.lime.domain.model.BooleanPref
import app.pinya.lime.domain.model.StringPref
import app.pinya.lime.ui.utils.CheckForChangesInAppList
import app.pinya.lime.ui.utils.DailyWallpaper
import app.pinya.lime.ui.utils.IconPackManager
import app.pinya.lime.ui.utils.Utils
import app.pinya.lime.ui.utils.notifications.NotificationsHandler
import app.pinya.lime.ui.view.adapter.*
import app.pinya.lime.ui.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val appViewModel: AppViewModel by viewModels()

    private lateinit var viewPager: ViewPager
    private lateinit var customPageAdapter: MainPagerAdapter

    private lateinit var appMenuAdapter: AppMenuAdapter
    private lateinit var renameMenuAdapter: RenameMenuAdapter
    private lateinit var reorderMenuAdapter: ReorderMenuAdapter
    private lateinit var buyProMenuAdapter: BuyProMenuAdapter
    private lateinit var changeAppIconAdapter: ChangeAppIconAdapter

    private var dailyWallpaper: DailyWallpaper? = null

    private var checkForChangesInAppList: CheckForChangesInAppList? = null

    private var notificationsHandler: NotificationsHandler? = null

    private var lastIconPackSelected: String = "None"
    private var prevShowHiddenApps: Boolean? = null

    private val iconPackManager: IconPackManager by lazy {
        IconPackManager(this)
    }

    private var iconPacks: MutableMap<String, IconPackManager.IconPack> = mutableMapOf()

    private val billingHelper by lazy {
        (this.application as LimeLauncherApp).appContainer.billingHelper
    }

    private fun handleBuyProClick() {
        billingHelper.startBillingFlow(this)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        AppProvider.initialize(this.application)

        setContentView(R.layout.activity_main)
        linkAdapter()

        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        appViewModel.info.observe(this) {
            appViewModel.updateAppList(this)
            customPageAdapter.home?.calculateMaxNumberOfAppsInHome()
        }

        appViewModel.getInfo()

        makeNavbarTransparent()

        notificationsHandler = NotificationsHandler(this)
        notificationsHandler?.notifications?.observe(this) { notifications ->
            handleNotificationChange(notifications)
        }

        appMenuAdapter = AppMenuAdapter(this, appViewModel, billingHelper)
        renameMenuAdapter = RenameMenuAdapter(this, appViewModel)
        reorderMenuAdapter = ReorderMenuAdapter(this, appViewModel)
        buyProMenuAdapter = BuyProMenuAdapter(this, ::handleBuyProClick, null, appViewModel)
        changeAppIconAdapter = ChangeAppIconAdapter(this, appViewModel, iconPacks)

        checkForChangesInAppList = CheckForChangesInAppList(this, appViewModel)

        dailyWallpaper = DailyWallpaper(this, appViewModel)

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

        appViewModel.buyProMenu.observe(this) { buyProMenu ->
            buyProMenuAdapter.handleBuyProMenu(buyProMenu)
        }

        appViewModel.changeAppIconMenu.observe(this) { changeAppIconMenu ->
            changeAppIconAdapter.handleChangeAppIconMenu(changeAppIconMenu)
        }

        appViewModel.reorderMenu.observe(this) { reorderMenu ->
            reorderMenuAdapter.handleReorderMenu(reorderMenu)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewPager.setCurrentItem(0, true)
            }
        })

        lastIconPackSelected = Utils.getStringPref(this, StringPref.GENERAL_ICON_PACK)

        if (!Utils.isMyLauncherDefault(this, packageManager)) askToSetAsDefaultLauncher()
    }

    override fun onResume() {
        super.onResume()
        viewPager.setCurrentItem(0, false)

        val hideStatusBar = Utils.getBooleanPref(this, BooleanPref.GENERAL_HIDE_STATUS_BAR)
        val dimBackground = Utils.getBooleanPref(this, BooleanPref.GENERAL_DIM_BACKGROUND)
        val isTextBlack = Utils.getBooleanPref(this, BooleanPref.GENERAL_IS_TEXT_BLACK)
        val showHiddenApps = Utils.getBooleanPref(this, BooleanPref.GENERAL_SHOW_HIDDEN_APPS)

        showStatusBar(!hideStatusBar)
        dimBackground(dimBackground, isTextBlack)
        changeWallpaper()

        customPageAdapter.onResume()

        hideContextMenus()
        checkForChangesInAppList?.startUpdates()

        val newIconPack = Utils.getStringPref(this, StringPref.GENERAL_ICON_PACK)
        if (newIconPack != lastIconPackSelected || prevShowHiddenApps != showHiddenApps) {
            lastIconPackSelected = newIconPack
            prevShowHiddenApps = showHiddenApps
            appViewModel.updateAppList(this)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            iconPacks.clear()
            iconPackManager.isSupportedIconPacks(true).forEach {
                iconPacks[it.value.name] = it.value
            }
        }
    }

    override fun onPause() {
        super.onPause()
        checkForChangesInAppList?.stopUpdates()
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

    private fun changeWallpaper() {
        val changeWallpaperDaily =
            Utils.getBooleanPref(this, BooleanPref.GENERAL_CHANGE_WALLPAPER_DAILY)
        val alsoChangeLockScreen =
            Utils.getBooleanPref(this, BooleanPref.GENERAL_ALSO_CHANGE_LOCK_SCREEN)

        if (!changeWallpaperDaily) return

        val wallpaperLastUpdatedDate = appViewModel.info.value?.wallpaperLastUpdatedDate ?: return

        val cal: Calendar = Calendar.getInstance()
        val date = cal.get(Calendar.DATE)

        if (wallpaperLastUpdatedDate != date) dailyWallpaper?.updateWallpaper(alsoChangeLockScreen)
    }

    private fun handleNotificationChange(notifications: MutableMap<String, Int>) {
        val showNotificationBadges =
            Utils.getStringPref(this, StringPref.GENERAL_NOTIFICATION_BADGES) != "none"

        if (showNotificationBadges && Utils.isNotificationServiceEnabled(this)) {
            if (viewPager.currentItem == 0)
                customPageAdapter.home?.handleNotificationsChange(notifications)
            else customPageAdapter.drawer?.handleNotificationsChange(notifications)
        }
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        println(activityResult.resultCode)
    }

    private fun askToSetAsDefaultLauncher() {
        val roleManager = this.getSystemService(Context.ROLE_SERVICE) as RoleManager
        if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME) && !roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
            startForResult.launch(intent)
        }
    }
}