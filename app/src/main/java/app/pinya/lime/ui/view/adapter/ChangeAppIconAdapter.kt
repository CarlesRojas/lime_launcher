package app.pinya.lime.ui.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.*
import android.widget.*
import app.pinya.lime.R
import app.pinya.lime.domain.model.BooleanPref
import app.pinya.lime.domain.model.menus.ChangeAppIconMenu
import app.pinya.lime.ui.utils.Utils
import app.pinya.lime.ui.viewmodel.AppViewModel

class ChangeAppIconAdapter (
    private val context: Context, private val viewModel: AppViewModel
) {
    private var contextMenuWindow: PopupWindow? = null
    private var isMenuOpen = false

    fun handleChangeAppIconMenu(changeAppIconMenu: ChangeAppIconMenu?) {
        if (changeAppIconMenu == null) hide()
        else show(changeAppIconMenu)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun show(changeAppIconMenu: ChangeAppIconMenu) {
        if (isMenuOpen) return
        isMenuOpen = true

        val changeAppIconView = View.inflate(context, R.layout.view_choose_app_icon_menu, null)
        val icon = changeAppIconView.findViewById<ImageView>(R.id.appIcon)
        val appName = changeAppIconView.findViewById<TextView>(R.id.appName)
        val closeButton = changeAppIconView.findViewById<ImageButton>(R.id.closeChooseAppIconMenuButton)
        val recoverOriginalIconButton =
            changeAppIconView.findViewById<LinearLayout>(R.id.chooseAppIconMenu_recoverOriginalIcon)

        recoverOriginalIconButton.visibility = View.GONE // TODO if (changeAppIconMenu.app.icon != changeAppIconMenu.app.originalIcon) View.VISIBLE else View.GONE

        icon.setImageDrawable(changeAppIconMenu.app.icon)
        appName.text = changeAppIconMenu.app.name

        contextMenuWindow = PopupWindow(
            changeAppIconView,
            changeAppIconMenu.container.width - changeAppIconMenu.container.paddingRight - changeAppIconMenu.container.paddingLeft,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        contextMenuWindow?.animationStyle = R.style.TopPopupWindowAnimation

        contextMenuWindow?.showAtLocation(changeAppIconMenu.container, Gravity.TOP, 0, 0)
        dimBehindMenu(contextMenuWindow)

        contextMenuWindow?.setOnDismissListener {
            viewModel.changeAppIconMenu.postValue(null)
        }

        closeButton.setOnClickListener {
            viewModel.changeAppIconMenu.postValue(null)
        }

        fun chooseIconPack(recoverOriginal: Boolean = false) {
            // TODO
            viewModel.changeAppIconMenu.postValue(null)
        }

        recoverOriginalIconButton.setOnClickListener {
            chooseIconPack(true)
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