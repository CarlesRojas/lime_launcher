package app.pinya.lime.ui.view.adapter

import android.content.Context
import android.view.WindowManager
import android.widget.PopupWindow
import app.pinya.lime.domain.model.menus.AppListMenu
import app.pinya.lime.ui.viewmodel.AppViewModel

class AppListMenuAdapter(
    private val context: Context, private val viewModel: AppViewModel
) {
    private var contextMenuWindow: PopupWindow? = null
    private var isMenuOpen = false

    fun handleAppMenu(appListMenu: AppListMenu?) {
        if (appListMenu == null) hide()
        else show(appListMenu)
    }

    private fun show(appListMenu: AppListMenu) {
        if (isMenuOpen) return
        isMenuOpen = true
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