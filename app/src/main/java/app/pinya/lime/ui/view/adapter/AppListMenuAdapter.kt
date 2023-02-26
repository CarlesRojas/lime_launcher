package app.pinya.lime.ui.view.adapter

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.pinya.lime.R
import app.pinya.lime.domain.model.menus.AppListMenu
import app.pinya.lime.ui.viewmodel.AppViewModel

class AppListMenuAdapter(
    private val context: Context,
    private val viewModel: AppViewModel,
) {
    private var contextMenuWindow: PopupWindow? = null
    private var isMenuOpen = false

    fun handleAppListMenu(appListMenu: AppListMenu?) {
        if (appListMenu == null) hide()
        else show(appListMenu)
    }

    private fun show(appListMenu: AppListMenu) {
        if (isMenuOpen) return
        isMenuOpen = true

        val appListView = View.inflate(context, R.layout.view_app_list_menu, null) as ViewGroup
        val titleText = appListView.findViewById<TextView>(R.id.appListMenu_title)
        val closeButton = appListView.findViewById<ImageButton>(R.id.closeMenuButton)
        val appListRecyclerView =
            appListView.findViewById<RecyclerView>(R.id.appListMenu_recyclerView)

        titleText.text = if (appListMenu.isTime) "On clock click, open:" else "On date click, open:"

        fun onChangeApp(useDefault: Boolean, useNone: Boolean, newAppPackage: String) {
            viewModel.appListMenu.postValue(null)
            appListMenu.onChangeAppCallback(useDefault, useNone, newAppPackage)
        }

        AppListAdapter(context, viewModel, appListMenu.isTime, ::onChangeApp).also {
            appListRecyclerView.adapter = it
            appListRecyclerView.layoutManager = LinearLayoutManager(context)
        }

        contextMenuWindow = PopupWindow(
            appListView,
            appListMenu.container.width - appListMenu.container.paddingRight - appListMenu.container.paddingLeft,
            (appListMenu.container.height * 0.75f).toInt(),
            true
        )

        contextMenuWindow?.animationStyle = R.style.TopPopupWindowAnimation

        contextMenuWindow?.showAtLocation(appListMenu.container, Gravity.TOP, 0, 0)
        dimBehindMenu(contextMenuWindow)

        contextMenuWindow?.setOnDismissListener {
            viewModel.appListMenu.postValue(null)
        }

        closeButton.setOnClickListener {
            viewModel.appListMenu.postValue(null)
        }
    }

    private fun hide() {
        if (!isMenuOpen) return
        isMenuOpen = false

        contextMenuWindow?.dismiss()
    }

    private fun dimBehindMenu(menu: PopupWindow?) {
        if (menu == null) return
        val isTextBlack = viewModel.settings.value?.generalIsTextBlack ?: false

        val container = menu.contentView.rootView
        val context = menu.contentView.context
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val p = container.layoutParams as WindowManager.LayoutParams
        p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        p.dimAmount = if (isTextBlack) 0.5f else 0.8f
        wm.updateViewLayout(container, p)
    }
}