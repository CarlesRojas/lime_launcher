package app.pinya.lime.ui.view.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import app.pinya.lime.R
import app.pinya.lime.domain.model.BooleanPref
import app.pinya.lime.domain.model.menus.*
import app.pinya.lime.ui.utils.Utils
import app.pinya.lime.ui.utils.billing.BillingHelper
import app.pinya.lime.ui.view.activity.SettingsActivity
import app.pinya.lime.ui.viewmodel.AppViewModel

class AppMenuAdapter(
    private val context: Context, private val viewModel: AppViewModel, private val billingHelper: BillingHelper
) {

    private var contextMenuWindow: PopupWindow? = null
    private var isMenuOpen = false

    fun handleAppMenu(appMenu: AppMenu?) {
        if (appMenu == null) hide()
        else show(appMenu)
    }

    private fun show(appMenu: AppMenu) {
        if (isMenuOpen) return
        isMenuOpen = true

        val isPro = billingHelper.isPro()

        val contextMenuView = View.inflate(context, R.layout.view_context_menu, null)
        val icon = contextMenuView.findViewById<ImageView>(R.id.appIcon)
        val appName = contextMenuView.findViewById<TextView>(R.id.appName)
        val closeButton = contextMenuView.findViewById<ImageButton>(R.id.closeContextMenuButton)
        val settingsButton = contextMenuView.findViewById<ImageButton>(R.id.settingsButton)

        val reorderButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_reorder)
        val addToHomeButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_addToHome)
        val removeFromHomeButton =
            contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_removeFromHome)
        val showAppButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_showApp)
        val hideAppButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_hideApp)
        val appInfoButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_appInfo)
        val uninstallButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_uninstall)
        val renameButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_renameApp)
        val changeAppIconButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_changeAppIcon)
        val changeAppIconPro = changeAppIconButton.findViewById<LinearLayout>(R.id.changeAppIconPro)

        val homeApps = viewModel.info.value?.homeApps ?: mutableSetOf()

        icon.setImageDrawable(appMenu.app.icon)
        appName.text = appMenu.app.name

        reorderButton.visibility =
            if (appMenu.fromHome && homeApps.size > 1) View.VISIBLE else View.GONE
        addToHomeButton.visibility =
            if (appMenu.app.home || isHomeFull()) View.GONE else View.VISIBLE
        removeFromHomeButton.visibility = if (appMenu.app.home) View.VISIBLE else View.GONE
        showAppButton.visibility = if (appMenu.app.hidden) View.VISIBLE else View.GONE
        hideAppButton.visibility = if (appMenu.app.hidden) View.GONE else View.VISIBLE
        uninstallButton.visibility = if (appMenu.app.system) View.GONE else View.VISIBLE
        changeAppIconPro.visibility = if (isPro) View.GONE else View.VISIBLE

        contextMenuWindow = PopupWindow(
            contextMenuView,
            appMenu.container.width - appMenu.container.paddingRight - appMenu.container.paddingLeft,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        contextMenuWindow?.animationStyle = R.style.BottomPopupWindowAnimation

        contextMenuWindow?.showAtLocation(appMenu.container, Gravity.BOTTOM, 0, 0)
        dimBehindMenu(contextMenuWindow)

        contextMenuWindow?.setOnDismissListener {
            viewModel.appMenu.postValue(null)
        }

        closeButton.setOnClickListener {
            viewModel.appMenu.postValue(null)
        }

        settingsButton.setOnClickListener {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }

        reorderButton.setOnClickListener {
            viewModel.appMenu.postValue(null)
            viewModel.reorderMenu.postValue(ReorderMenu(appMenu.container))
        }

        addToHomeButton.setOnClickListener {
            toggleHomeInApp(appMenu.app.packageName, true)
            viewModel.appMenu.postValue(null)
            if (!appMenu.fromHome) showToast("${appMenu.app.name} added to Home")
        }

        removeFromHomeButton.setOnClickListener {
            toggleHomeInApp(appMenu.app.packageName, false)
            viewModel.appMenu.postValue(null)
            if (!appMenu.fromHome) showToast("${appMenu.app.name} removed from Home")
        }

        showAppButton.setOnClickListener {
            toggleHiddenApp(appMenu.app.packageName, false)
            viewModel.appMenu.postValue(null)
        }

        hideAppButton.setOnClickListener {
            toggleHiddenApp(appMenu.app.packageName, true)
            viewModel.appMenu.postValue(null)
        }

        appInfoButton.setOnClickListener {
            viewModel.appMenu.postValue(null)

            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Uri.parse("package:" + appMenu.app.packageName)
            context.startActivity(intent)
        }

        uninstallButton.setOnClickListener {
            viewModel.appMenu.postValue(null)

            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.parse("package:" + appMenu.app.packageName)
            context.startActivity(intent)
        }

        renameButton.setOnClickListener {
            viewModel.appMenu.postValue(null)
            viewModel.renameMenu.postValue(RenameMenu(appMenu.app, appMenu.container))
        }

        changeAppIconButton.setOnClickListener {
            viewModel.appMenu.postValue(null)

            if (isPro) viewModel.changeAppIconMenu.postValue(ChangeAppIconMenu(appMenu.app, appMenu.container))
            else viewModel.buyProMenu.postValue(BuyProMenu(appMenu.container))
        }
    }

    private fun hide() {
        if (!isMenuOpen) return
        isMenuOpen = false

        contextMenuWindow?.dismiss()
    }

    private fun isHomeFull(): Boolean {
        val homeList = viewModel.homeList.value ?: return false
        val maxNumberOfHomeApps = viewModel.info.value?.maxNumberOfHomeApps ?: 8
        return homeList.size >= maxNumberOfHomeApps
    }

    private fun dimBehindMenu(menu: PopupWindow?) {
        if (menu == null) return

        val isTextBlack = Utils.getBooleanPref(context, BooleanPref.GENERAL_IS_TEXT_BLACK)

        val container = menu.contentView.rootView
        val context = menu.contentView.context
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val p = container.layoutParams as WindowManager.LayoutParams
        p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        p.dimAmount = if (isTextBlack) 0.5f else 0.8f
        wm.updateViewLayout(container, p)
    }

    private fun showToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    private fun toggleHomeInApp(packageName: String, homeNewValue: Boolean) {
        val info = viewModel.info.value ?: return
        info.tutorialDone = true

        when (homeNewValue) {
            true -> info.homeApps.add(packageName)
            false -> info.homeApps.remove(packageName)
        }

        viewModel.updateInfo(info, context)
    }

    private fun toggleHiddenApp(packageName: String, hiddenNewValue: Boolean) {
        val info = viewModel.info.value ?: return

        when (hiddenNewValue) {
            true -> info.hiddenApps.add(packageName)
            false -> info.hiddenApps.remove(packageName)
        }

        viewModel.updateInfo(info, context)
    }
}