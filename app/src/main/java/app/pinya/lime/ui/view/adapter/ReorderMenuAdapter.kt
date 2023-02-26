package app.pinya.lime.ui.view.adapter

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import app.pinya.lime.R
import app.pinya.lime.domain.model.menus.ReorderMenu
import app.pinya.lime.ui.utils.Utils
import app.pinya.lime.ui.viewmodel.AppViewModel

class ReorderMenuAdapter(
    private val context: Context, private val viewModel: AppViewModel
) {

    private var contextMenuWindow: PopupWindow? = null
    private var isMenuOpen = false

    fun handleReorderMenu(reorderMenu: ReorderMenu?) {
        if (reorderMenu == null) hide()
        else show(reorderMenu)
    }

    private fun show(reorderMenu: ReorderMenu) {
        if (isMenuOpen) return
        isMenuOpen = true

        val reorderView = View.inflate(context, R.layout.view_reorder_menu, null)
        val closeButton = reorderView.findViewById<ImageButton>(R.id.closeMenuButton)
        val homeAppsContainer = reorderView.findViewById<LinearLayout>(R.id.homeAppsContainer)

        fun showHomeApps(container: LinearLayout) {
            container.removeAllViews()
            val info = viewModel.info.value ?: return
            val appList = viewModel.completeAppList

            for (homeApp in info.homeApps) {
                val appView = View.inflate(context, R.layout.view_reorder_app, null)
                val appIcon = appView.findViewById<ImageView>(R.id.appIcon)
                val appName = appView.findViewById<TextView>(R.id.appName)
                val moveUpButton = appView.findViewById<ImageButton>(R.id.reorderMenu_moveUpButton)
                val moveDownButton =
                    appView.findViewById<ImageButton>(R.id.reorderMenu_moveDownButton)
                val reorderMenuSpace = appView.findViewById<ImageButton>(R.id.reorderMenu_space)

                val app = appList.find { it.packageName == homeApp } ?: continue

                appIcon.setImageDrawable(app.icon)
                appName.text = app.name

                val areIconsVisible = viewModel.settings.value?.homeShowIcons ?: true

                appIcon.visibility = if (areIconsVisible) View.VISIBLE else View.GONE
                moveUpButton.visibility = if (app.homeOrderIndex <= 0) View.GONE else View.VISIBLE
                moveDownButton.visibility =
                    if (app.homeOrderIndex >= info.homeApps.size - 1) View.GONE else View.VISIBLE
                reorderMenuSpace.visibility =
                    if (app.homeOrderIndex <= 0) View.VISIBLE else View.GONE


                moveUpButton.setOnClickListener {
                    Utils.vibrate(context)
                    changeHomeAppOrder(app.packageName, true)
                    showHomeApps(container)
                }

                moveDownButton.setOnClickListener {
                    Utils.vibrate(context)
                    changeHomeAppOrder(app.packageName, false)
                    showHomeApps(container)
                }

                container.addView(appView)
            }
        }

        showHomeApps(homeAppsContainer)

        contextMenuWindow = PopupWindow(
            reorderView,
            reorderMenu.container.width - reorderMenu.container.paddingRight - reorderMenu.container.paddingLeft,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        contextMenuWindow?.animationStyle = R.style.TopPopupWindowAnimation

        contextMenuWindow?.showAtLocation(reorderMenu.container, Gravity.TOP, 0, 0)
        dimBehindMenu(contextMenuWindow)

        contextMenuWindow?.setOnDismissListener {
            viewModel.reorderMenu.postValue(null)
        }

        closeButton.setOnClickListener {
            viewModel.reorderMenu.postValue(null)
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

    private fun <T> MutableSet<T>.swap(index1: Int, index2: Int) {
        val list = this.toMutableList()

        val tmp = list[index1]
        list[index1] = list[index2]
        list[index2] = tmp
        this.clear()

        list.forEach { this.add(it) }
    }

    private fun changeHomeAppOrder(packageName: String, moveUp: Boolean) {
        val info = viewModel.info.value ?: return

        var index = -1
        info.homeApps.forEachIndexed { i, elem ->
            if (elem == packageName) index = i
        }

        if ((moveUp && index <= 0) || (!moveUp && index >= info.homeApps.size - 1) || index < 0 || index >= info.homeApps.size) return

        info.homeApps.swap(index, if (moveUp) index - 1 else index + 1)

        viewModel.updateInfo(info)
    }
}