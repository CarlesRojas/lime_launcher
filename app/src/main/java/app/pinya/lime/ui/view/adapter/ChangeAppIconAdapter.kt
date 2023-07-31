package app.pinya.lime.ui.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.pinya.lime.R
import app.pinya.lime.domain.model.BooleanPref
import app.pinya.lime.domain.model.StringPref
import app.pinya.lime.domain.model.menus.ChangeAppIconMenu
import app.pinya.lime.ui.utils.IconPackManager
import app.pinya.lime.ui.utils.Utils
import app.pinya.lime.ui.viewmodel.AppViewModel
import com.google.android.material.button.MaterialButton

class ChangeAppIconAdapter (
    private val context: Context, private val viewModel: AppViewModel
) {
    private var contextMenuWindow: PopupWindow? = null
    private var isMenuOpen = false
    private val iconPackManager = IconPackManager(context)

    fun handleChangeAppIconMenu(changeAppIconMenu: ChangeAppIconMenu?) {
        if (changeAppIconMenu == null) hide()
        else show(changeAppIconMenu)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun show(changeAppIconMenu: ChangeAppIconMenu) {
        if (isMenuOpen) return
        isMenuOpen = true

        val changeAppIconView = View.inflate(context, R.layout.view_change_app_icon_menu, null)
        val icon = changeAppIconView.findViewById<ImageView>(R.id.appIcon)
        val appName = changeAppIconView.findViewById<TextView>(R.id.appName)
        val closeButton = changeAppIconView.findViewById<ImageButton>(R.id.closeChooseAppIconMenuButton)

        val chooseIconPackContainer = changeAppIconView.findViewById<LinearLayout>(R.id.chooseIconPackContainer)
        val iconPackList = changeAppIconView.findViewById<LinearLayout>(R.id.iconPackList)
        val noIconPacksMessage = changeAppIconView.findViewById<TextView>(R.id.noIconPacksMessage)
        val recoverOriginalIconButton =
            changeAppIconView.findViewById<LinearLayout>(R.id.chooseAppIconMenu_recoverOriginalIcon)

        val chooseIconContainer = changeAppIconView.findViewById<ConstraintLayout>(R.id.chooseIconContainer)
        val searchIconBar = changeAppIconView.findViewById<EditText>(R.id.searchIconBar)
        val iconList = changeAppIconView.findViewById<RecyclerView>(R.id.iconList)
        val searchTip = changeAppIconView.findViewById<TextView>(R.id.searchTip)
        val noSearchResults = changeAppIconView.findViewById<TextView>(R.id.noSearchResults)

        icon.setImageDrawable(changeAppIconMenu.app.icon)
        appName.text = changeAppIconMenu.app.name

        val iconPackName = Utils.getStringPref(context, StringPref.GENERAL_ICON_PACK)
        var iconPacks = mutableMapOf<String, IconPackManager.IconPack>()
        iconPackManager.isSupportedIconPacks().forEach {
            iconPacks[it.value.name] = it.value
        }

        fun setIcon(iconPack: String?, icon: String?) {
            // TODO if iconPack and icon are null
            println(iconPackName)
            println(changeAppIconMenu.app.packageName)
            println(iconPack)
            println(icon)
            viewModel.changeAppIconMenu.postValue(null)
        }

        fun blurAndHideKeyboard() {
            searchIconBar.clearFocus()
            val inputManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(searchIconBar.windowToken, 0)
        }

        fun showStepTwo(iconPackName: String) {
            val iconPack = iconPacks[iconPackName]
            if (iconPack == null) {
                viewModel.changeAppIconMenu.postValue(null)
                return
            }

            val iconListAdapter = IconListAdapter(context, viewModel).also { adapter ->
                iconList!!.adapter = adapter
            }
            iconList?.layoutManager = GridLayoutManager(context, 3)

            var iconKeyword = ""
            searchIconBar.setText("")
            searchIconBar.focusAndShowKeyboard()

            chooseIconPackContainer.visibility = View.GONE
            chooseIconContainer.visibility = View.VISIBLE

            iconList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    blurAndHideKeyboard()
                }
            })

            fun searchIcon() {
                val icons = iconPack.getIconsForKeyword(iconKeyword)
                searchTip.visibility = if (icons.size > 0) TextView.GONE else TextView.VISIBLE
                noSearchResults.visibility = if (icons.size > 0) TextView.GONE else TextView.VISIBLE

                iconListAdapter.updateIcons(icons.toList())
            }

            searchIconBar.doAfterTextChanged {
                if (it.toString() == "") searchIconBar.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.icon_rename, 0, 0, 0
                ) else searchIconBar.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.icon_submit, 0
                )

                iconKeyword = it.toString()
            }

            searchIconBar.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    searchIcon()
                    return@OnEditorActionListener true
                }
                false
            })

            searchIconBar.setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_UP && iconKeyword != "") {
                    val padding: Int = searchIconBar.paddingRight * 2
                    val iconWidth: Int = searchIconBar.compoundDrawables[2].bounds.width()

                    if (event.rawX >= searchIconBar.right - iconWidth - padding)
                        searchIcon()
                }
                view.performClick()
            }

        }

        fun showStepOne() {
            iconPacks.forEach {
                val button = Button(context)
                val currIconPackName = it.value.name
                button.text = currIconPackName
                button.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                button.typeface = ResourcesCompat.getFont(context, R.font.montserrat)
                button.isSingleLine = true
                button.setTextColor(ContextCompat.getColor(context, R.color.white))
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                button.setPadding(0, Utils.dpToPx(context, 16f).toInt(), 0, Utils.dpToPx(context, 16f).toInt())

                button.setOnClickListener {
                    showStepTwo(currIconPackName)
                }

                iconPackList.addView(button)
            }

            chooseIconPackContainer.visibility = View.VISIBLE
            chooseIconContainer.visibility = View.GONE

            iconPackList.visibility = if (iconPacks.isNotEmpty()) View.VISIBLE else View.GONE
            noIconPacksMessage.visibility = if (iconPacks.isEmpty()) View.VISIBLE else View.GONE
            recoverOriginalIconButton.visibility = View.VISIBLE // TODO if (changeAppIconMenu.app.icon != changeAppIconMenu.app.originalIcon) View.VISIBLE else View.GONE

            recoverOriginalIconButton.setOnClickListener {
                setIcon(null, null)
            }
        }

        showStepOne()


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
}