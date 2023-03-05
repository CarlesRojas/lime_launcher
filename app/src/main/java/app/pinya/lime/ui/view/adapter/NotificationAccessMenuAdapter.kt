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
import app.pinya.lime.domain.model.menus.NotificationAccessMenu
import app.pinya.lime.ui.utils.Utils

class NotificationAccessMenuAdapter(
    private val context: Context,
    private val setNotificationAccessMenu: (menu: NotificationAccessMenu?) -> Unit,
    private val handleEnableNotificationAccessClick: () -> Unit,
) {

    private var contextMenuWindow: PopupWindow? = null
    private var isMenuOpen = false

    fun handleNotificationAccessMenu(notificationAccessMenu: NotificationAccessMenu?) {
        if (notificationAccessMenu == null) hide()
        else show(notificationAccessMenu)
    }

    private fun show(notificationAccessMenu: NotificationAccessMenu) {
        if (isMenuOpen) return
        isMenuOpen = true

        val notificationAccessMenuView =
            View.inflate(context, R.layout.view_notification_access_menu, null)
        val closeButton =
            notificationAccessMenuView.findViewById<ImageButton>(R.id.closeNotificationAccessMenuButton)
        val enableButton =
            notificationAccessMenuView.findViewById<LinearLayout>(R.id.notificationAccessMenu_enable)
        val cancelButton =
            notificationAccessMenuView.findViewById<LinearLayout>(R.id.notificationAccessMenu_cancel)

        contextMenuWindow = PopupWindow(
            notificationAccessMenuView,
            notificationAccessMenu.container.width - notificationAccessMenu.container.paddingRight - notificationAccessMenu.container.paddingLeft,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        contextMenuWindow?.animationStyle = R.style.TopPopupWindowAnimation

        contextMenuWindow?.showAtLocation(notificationAccessMenu.container, Gravity.TOP, 0, 0)
        dimBehindMenu(contextMenuWindow)

        contextMenuWindow?.setOnDismissListener {
            setNotificationAccessMenu(null)
        }

        closeButton.setOnClickListener {
            setNotificationAccessMenu(null)
        }

        enableButton.setOnClickListener {
            handleEnableNotificationAccessClick()
        }

        cancelButton.setOnClickListener {
            setNotificationAccessMenu(null)
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