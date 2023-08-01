@file:Suppress("ComplexRedundantLet")

package app.pinya.lime.ui.view.adapter

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupWindow
import app.pinya.lime.R
import app.pinya.lime.domain.model.BooleanPref
import app.pinya.lime.domain.model.menus.BuyProMenu
import app.pinya.lime.ui.utils.Utils
import app.pinya.lime.ui.viewmodel.AppViewModel

class BuyProMenuAdapter(
    private val context: Context,
    private val handleBuyProClick: () -> Unit,
    private val setBuyProMenu: ((menu: BuyProMenu?) -> Unit)? = null,
    private val viewModel: AppViewModel? = null
) {

    private var contextMenuWindow: PopupWindow? = null
    private var isMenuOpen = false

    fun handleBuyProMenu(buyProMenu: BuyProMenu?) {
        if (buyProMenu == null) hide()
        else show(buyProMenu)
    }

    private fun show(buyProMenu: BuyProMenu) {
        if (isMenuOpen) return
        isMenuOpen = true

        val buyProMenuView = View.inflate(context, R.layout.view_buy_pro_menu, null)
        val closeButton =
            buyProMenuView.findViewById<ImageButton>(R.id.closeBuyProMenuButton)
        val enableButton =
            buyProMenuView.findViewById<LinearLayout>(R.id.butProMenu_enable)
        val cancelButton =
            buyProMenuView.findViewById<LinearLayout>(R.id.buyProMenu_cancel)

        contextMenuWindow = PopupWindow(
            buyProMenuView,
            buyProMenu.container.width - buyProMenu.container.paddingRight - buyProMenu.container.paddingLeft,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        contextMenuWindow?.animationStyle = R.style.TopPopupWindowAnimation

        contextMenuWindow?.showAtLocation(buyProMenu.container, Gravity.TOP, 0, 0)
        dimBehindMenu(contextMenuWindow)

        contextMenuWindow?.setOnDismissListener {
            if (setBuyProMenu != null) setBuyProMenu.let { it(null) }
            else viewModel?.buyProMenu?.postValue(null)
        }

        closeButton.setOnClickListener {
            if (setBuyProMenu != null) setBuyProMenu.let { it(null) }
            else viewModel?.buyProMenu?.postValue(null)
        }

        enableButton.setOnClickListener {
            handleBuyProClick()
        }

        cancelButton.setOnClickListener {
            if (setBuyProMenu != null) setBuyProMenu.let { it(null) }
            else viewModel?.buyProMenu?.postValue(null)
        }
    }

    private fun hide() {
        if (!isMenuOpen) return
        isMenuOpen = false

        contextMenuWindow?.dismiss()
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

}