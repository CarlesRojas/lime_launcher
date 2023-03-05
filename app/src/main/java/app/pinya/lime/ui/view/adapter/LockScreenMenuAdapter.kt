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
import app.pinya.lime.domain.model.menus.LockScreenMenu
import app.pinya.lime.ui.utils.Utils

class LockScreenMenuAdapter(
    private val context: Context,
    private val setLockScreenMenu: (menu: LockScreenMenu?) -> Unit,
    private val handleEnablePermissionClick: () -> Unit,
) {

    private var contextMenuWindow: PopupWindow? = null
    private var isMenuOpen = false

    fun handleLockScreenMenu(lockScreenMenu: LockScreenMenu?) {
        if (lockScreenMenu == null) hide()
        else show(lockScreenMenu)
    }

    private fun show(lockScreenMenu: LockScreenMenu) {
        if (isMenuOpen) return
        isMenuOpen = true

        val lockScreenMenuView = View.inflate(context, R.layout.view_lock_screen_menu, null)
        val closeButton =
            lockScreenMenuView.findViewById<ImageButton>(R.id.closeLockScreenMenuButton)
        val enableButton =
            lockScreenMenuView.findViewById<LinearLayout>(R.id.lockScreenMenu_enable)
        val cancelButton =
            lockScreenMenuView.findViewById<LinearLayout>(R.id.lockScreenMenu_cancel)

        contextMenuWindow = PopupWindow(
            lockScreenMenuView,
            lockScreenMenu.container.width - lockScreenMenu.container.paddingRight - lockScreenMenu.container.paddingLeft,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        contextMenuWindow?.animationStyle = R.style.TopPopupWindowAnimation

        contextMenuWindow?.showAtLocation(lockScreenMenu.container, Gravity.TOP, 0, 0)
        dimBehindMenu(contextMenuWindow)

        contextMenuWindow?.setOnDismissListener {
            setLockScreenMenu(null)
        }

        closeButton.setOnClickListener {
            setLockScreenMenu(null)
        }

        enableButton.setOnClickListener {
            handleEnablePermissionClick()
        }

        cancelButton.setOnClickListener {
            setLockScreenMenu(null)
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