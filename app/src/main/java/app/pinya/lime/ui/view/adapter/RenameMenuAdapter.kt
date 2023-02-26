package app.pinya.lime.ui.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.core.widget.doAfterTextChanged
import app.pinya.lime.R
import app.pinya.lime.domain.model.menus.RenameMenu
import app.pinya.lime.ui.viewmodel.AppViewModel

class RenameMenuAdapter(
    private val context: Context, private val viewModel: AppViewModel
) {
    private var contextMenuWindow: PopupWindow? = null
    private var isMenuOpen = false

    fun handleRenameMenu(renameMenu: RenameMenu?) {
        if (renameMenu == null) hide()
        else show(renameMenu)
    }

    fun View.focusAndShowKeyboard() {
        fun View.showTheKeyboardNow() {
            if (isFocused) {
                post {
                    // We still post the call, just in case we are being notified of the windows focus
                    // but InputMethodManager didn't get properly setup yet.
                    val imm =
                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }

        requestFocus()
        if (hasWindowFocus()) {
            // No need to wait for the window to get focus.
            showTheKeyboardNow()
        } else {
            // We need to wait until the window gets focus.
            viewTreeObserver.addOnWindowFocusChangeListener(object :
                ViewTreeObserver.OnWindowFocusChangeListener {
                override fun onWindowFocusChanged(hasFocus: Boolean) {
                    // This notification will arrive just before the InputMethodManager gets set up.
                    if (hasFocus) {
                        this@focusAndShowKeyboard.showTheKeyboardNow()
                        // It’s very important to remove this listener once we are done.
                        viewTreeObserver.removeOnWindowFocusChangeListener(this)
                    }
                }
            })
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun show(renameMenu: RenameMenu) {
        if (isMenuOpen) return
        isMenuOpen = true

        val renameView = View.inflate(context, R.layout.view_rename_menu, null)
        val icon = renameView.findViewById<ImageView>(R.id.appIcon)
        val appName = renameView.findViewById<TextView>(R.id.appName)
        val closeButton = renameView.findViewById<ImageButton>(R.id.closeRenameMenuButton)
        val recoverOriginalNameButton =
            renameView.findViewById<LinearLayout>(R.id.renameMenu_recoverOriginalName)

        val renameBar = renameView.findViewById<EditText>(R.id.renameBar)
        var renameText = ""
        renameBar.setText("")

        recoverOriginalNameButton.visibility =
            if (renameMenu.app.name != renameMenu.app.originalName) View.VISIBLE else View.GONE

        fun blurAndHideKeyboard() {
            renameBar.clearFocus()
            val inputManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(renameBar.windowToken, 0)
        }

        icon.setImageDrawable(renameMenu.app.icon)
        appName.text = renameMenu.app.name

        contextMenuWindow = PopupWindow(
            renameView,
            renameMenu.container.width - renameMenu.container.paddingRight - renameMenu.container.paddingLeft,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        contextMenuWindow?.animationStyle = R.style.TopPopupWindowAnimation

        contextMenuWindow?.showAtLocation(renameMenu.container, Gravity.TOP, 0, 0)
        dimBehindMenu(contextMenuWindow)

        contextMenuWindow?.setOnDismissListener {
            viewModel.renameMenu.postValue(null)
        }

        renameBar.focusAndShowKeyboard()

        closeButton.setOnClickListener {
            viewModel.renameMenu.postValue(null)
        }

        fun rename(recoverOriginal: Boolean = false) {
            renameApp(
                renameMenu.app.packageName,
                if (recoverOriginal) renameMenu.app.originalName else renameText
            )
            blurAndHideKeyboard()
            viewModel.renameMenu.postValue(null)
        }

        renameBar.doAfterTextChanged {
            if (it.toString() == "") renameBar.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.icon_rename, 0, 0, 0
            ) else renameBar.setCompoundDrawablesWithIntrinsicBounds(
                0, 0, R.drawable.icon_submit, 0
            )

            renameText = it.toString()
        }

        renameBar.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                rename()
                return@OnEditorActionListener true
            }
            false
        })

        renameBar.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP && renameText != "") {
                val padding: Int = renameBar.paddingRight * 2
                val iconWidth: Int = renameBar.compoundDrawables[2].bounds.width()

                if (event.rawX >= renameBar.right - iconWidth - padding) rename()
            }
            view.performClick()
        }

        recoverOriginalNameButton.setOnClickListener {
            rename(true)
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

    private fun renameApp(packageName: String, newName: String) {
        if (newName.isEmpty()) return

        val app = viewModel.completeAppList.find { it.packageName == packageName } ?: return
        app.name = newName

        val info = viewModel.info.value ?: return

        if (newName == app.originalName) info.renamedApps.remove(packageName)
        else info.renamedApps[packageName] = newName

        viewModel.updateInfo(info)
    }
}