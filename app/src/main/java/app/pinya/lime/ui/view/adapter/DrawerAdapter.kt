package app.pinya.lime.ui.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import app.pinya.lime.R
import app.pinya.lime.domain.model.AlphabetModel
import app.pinya.lime.domain.model.AppModel
import app.pinya.lime.domain.model.BooleanPref
import app.pinya.lime.domain.model.IntPref
import app.pinya.lime.domain.model.menus.AppMenu
import app.pinya.lime.ui.utils.Utils
import app.pinya.lime.ui.view.holder.AppViewHolder
import app.pinya.lime.ui.viewmodel.AppViewModel
import kotlin.math.floor


class DrawerAdapter(
    private val context: Context, private val layout: ViewGroup, private val viewModel: AppViewModel
) : RecyclerView.Adapter<AppViewHolder>() {

    // APP LIST
    private var appList: MutableList<AppModel> = mutableListOf()
    private var appListView: RecyclerView? = null
    private var drawerConstraintLayout: ConstraintLayout? = null

    // SEARCH BAR
    private var searchBar: EditText? = null
    private var searchBarText: String = ""

    // ALPHABET
    private lateinit var alphabetLayoutContainer: ConstraintLayout
    private lateinit var alphabetLayout: LinearLayout
    private lateinit var currentLetter: TextView
    private val currentAlphabet: MutableList<Char> = mutableListOf()
    private var filteringByAlphabet = false
    private var lastFilterWasAlphabet = false

    // CONTEXT MENU
    private var contextMenuContainer: ConstraintLayout? = null

    init {
        initContextMenu()
        initSearchBar()
        hideKeyboardOnAnyTouchOutside()
        initAlphabet()
        onResume()
    }

    // ########################################
    //   GENERAL
    // ########################################

    @SuppressLint("NotifyDataSetChanged")
    fun onResume() {
        showHideElements()
        updateLettersIncludedInAlphabet()
        notifyDataSetChanged()
        updateScale()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun handleDrawerListUpdate(newDrawerList: MutableList<AppModel>) {
        appList = newDrawerList

        val autoOpenApps = Utils.getBooleanPref(context, BooleanPref.DRAWER_AUTO_OPEN_APPS)
        val moreThanOneInstalledApp = viewModel.completeAppList.size > 1

        if (!lastFilterWasAlphabet && autoOpenApps && moreThanOneInstalledApp && appList.size == 1) {
            clearText()

            val app = appList[0]
            val launchAppIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
            if (launchAppIntent != null) context.startActivity(launchAppIntent)
        }

        updateLettersIncludedInAlphabet()
        notifyDataSetChanged()
    }

    private fun showHideElements() {
        val showSearchBar = Utils.getBooleanPref(context, BooleanPref.DRAWER_SHOW_SEARCH_BAR)
        val showAlphabetFilter =
            Utils.getBooleanPref(context, BooleanPref.DRAWER_SHOW_ALPHABET_FILTER)

        searchBar?.visibility = if (showSearchBar) View.VISIBLE else View.GONE
        alphabetLayoutContainer.visibility = if (showAlphabetFilter) View.VISIBLE else View.GONE

        val constraintSet = ConstraintSet()
        constraintSet.clone(drawerConstraintLayout)

        constraintSet.connect(
            R.id.appListConstraintLayout,
            ConstraintSet.TOP,
            if (showSearchBar) R.id.guidelineH2 else R.id.guidelineH1,
            ConstraintSet.TOP,
            0
        )

        constraintSet.applyTo(drawerConstraintLayout)
    }

    // ########################################
    //   SEARCH BAR
    // ########################################

    @SuppressLint("ClickableViewAccessibility")
    private fun initSearchBar() {
        val textScale = Utils.getIntPref(context, IntPref.GENERAL_TEXT_SCALE)

        searchBar = layout.findViewById(R.id.drawerSearchBar)
        searchBar?.textSize = Utils.applyScale(18f, textScale)

        searchBar?.doAfterTextChanged {
            if (it.toString() == "") hideClearText() else showClearText()
            searchBarText = it.toString()

            if (filteringByAlphabet) viewModel.filterDrawerAppListByAlphabetLetter(
                searchBarText.first(), context
            )
            else viewModel.filterDrawerAppListBySearchedText(searchBarText, context)

            filteringByAlphabet = false
            lastFilterWasAlphabet = false
        }

        searchBar?.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP && searchBarText != "") {
                val padding: Int = searchBar!!.paddingRight * 2
                val iconWidth: Int = searchBar!!.compoundDrawables[2].bounds.width()

                if (event.rawX >= searchBar!!.right - iconWidth - padding) clearText()
            }

            view.performClick()
        }
    }

    fun clearText() {
        searchBar?.text?.clear()
    }

    private fun showClearText() {
        searchBar?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_close, 0)
    }

    private fun hideClearText() {
        searchBar?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_search, 0, 0, 0)
    }

    fun showKeyboard() {
        val autoOpenKeyboard = Utils.getBooleanPref(context, BooleanPref.DRAWER_AUTO_SHOW_KEYBOARD)

        if (autoOpenKeyboard) {
            searchBar?.requestFocus()
            val inputManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.showSoftInput(searchBar, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun hideKeyboard() {
        searchBar?.clearFocus()

        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (searchBar != null) inputManager.hideSoftInputFromWindow(searchBar!!.windowToken, 0)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun hideKeyboardOnAnyTouchOutside() {
        layout.setOnClickListener {
            hideKeyboard()
        }

        drawerConstraintLayout = layout.findViewById(R.id.drawerConstraintLayout)
        appListView = layout.findViewById(R.id.drawerAppList)

        appListView?.setOnTouchListener { view, _ ->
            hideKeyboard()
            view.performClick()
        }
    }

    // ########################################
    //   ALPHABET
    // ########################################

    private var lastSelectedChar: Char = '-'
    private var alphabetTop: Int? = null
    private var alphabetBottom: Int? = null


    @SuppressLint("ClickableViewAccessibility")
    fun initAlphabet() {
        alphabetLayoutContainer = layout.findViewById(R.id.alphabetLayoutContainer)
        alphabetLayout = layout.findViewById(R.id.alphabetLayout)
        currentLetter = layout.findViewById(R.id.currentLetter)

        alphabetLayout.setOnTouchListener { _, event ->
            clearText()
            hideKeyboard()

            val numLetters = alphabetLayout.childCount

            if (event.action == MotionEvent.ACTION_DOWN) {
                alphabetTop = alphabetLayout.getChildAt(0)?.top
                alphabetBottom = alphabetLayout.getChildAt(numLetters - 1)?.bottom
            }

            if (numLetters <= 0 || alphabetTop == null || alphabetBottom == null || alphabetBottom!! - alphabetTop!! <= 0)
                return@setOnTouchListener true

            val perc = (event.y - alphabetTop!!) / (alphabetBottom!! - alphabetTop!!)

            val letterHeight = 1f / currentAlphabet.size
            val currentSection = floor(perc / letterHeight).toInt()

            if (currentSection >= 0 && currentSection < currentAlphabet.size) {
                val currentChar = currentAlphabet[currentSection]
                if (currentChar != lastSelectedChar) {
                    lastSelectedChar = currentChar
                    Utils.vibrate(context)
                }

                currentLetter.visibility = View.VISIBLE
                currentLetter.text = currentChar.uppercase()
                currentLetter.animate().y(event.rawY - (currentLetter.height / 2)).setDuration(0)
                    .start()


                filteringByAlphabet = true
                searchBar?.setText(currentChar.toString())
                lastFilterWasAlphabet = true
            } else {
                lastSelectedChar = '-'
                currentLetter.visibility = View.GONE
            }

            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_OUTSIDE) {
                lastSelectedChar = '-'
                currentLetter.visibility = View.GONE
                alphabetTop = null
                alphabetBottom = null
            }

            true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun updateLettersIncludedInAlphabet() {
        currentAlphabet.clear()
        val showHiddenApps = Utils.getBooleanPref(context, BooleanPref.GENERAL_SHOW_HIDDEN_APPS)

        for (app in viewModel.completeAppList) {
            if (app.name.isEmpty() || (!showHiddenApps && app.hidden)) continue
            val char = app.name.first().uppercaseChar()

            if (currentAlphabet.contains(char)) continue
            if (AlphabetModel.ALPHABET.contains(char)) currentAlphabet.add(char)
            else if (!currentAlphabet.contains('#')) currentAlphabet.add(0, '#')
        }

        alphabetLayout.removeAllViews()
        val alphabetHeight = alphabetLayoutContainer.height
        val isTextBlack = Utils.getBooleanPref(context, BooleanPref.GENERAL_IS_TEXT_BLACK)

        for (char in currentAlphabet) {
            val textView = View.inflate(context, R.layout.view_alphabet_character, null) as TextView
            textView.text = char.toString()
            textView.height = alphabetHeight / (AlphabetModel.ALPHABET.size + 1)
            textView.typeface = ResourcesCompat.getFont(context, R.font.montserrat)
            textView.setTextColor(
                ContextCompat.getColor(
                    context, if (isTextBlack) R.color.black else R.color.white
                )
            )
            textView.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)
            textView.setAutoSizeTextTypeUniformWithConfiguration(
                8, 14, 1, TypedValue.COMPLEX_UNIT_SP
            )
            alphabetLayout.addView(textView)
        }
    }

    // ########################################
    //   CONTEXT MENU
    // ########################################

    private fun initContextMenu() {
        contextMenuContainer = layout.findViewById(R.id.contextMenuDrawer_parent)
    }

    // ########################################
    //   NOTIFICATION CHANGE
    // ########################################

    private var previousNotifications: MutableMap<String, Int> = mutableMapOf()
    private var currentNotifications: MutableMap<String, Int> = mutableMapOf()

    private fun findAppIndex(packageName: String): Int {
        return appList.indexOfFirst { it.packageName == packageName }
    }

    fun handleNotificationsChange(notifications: MutableMap<String, Int>) {
        currentNotifications = notifications

        val changes = mutableSetOf<Int>()

        currentNotifications.forEach { (key, value) ->
            if (!previousNotifications.contains(key) || value != previousNotifications[key]) {
                val index = findAppIndex(key)
                if (index >= 0) changes.add(index)
            }
        }

        previousNotifications.forEach { (key, _) ->
            if (!currentNotifications.contains(key)) {
                val index = findAppIndex(key)
                if (index >= 0) changes.add(index)
            }
        }

        previousNotifications = currentNotifications
        changes.forEach { index -> notifyItemChanged(index) }
    }

    // ########################################
    //   SCALE
    // ########################################

    private fun updateScale() {
        val textScale = Utils.getIntPref(context, IntPref.GENERAL_TEXT_SCALE)
        searchBar?.textSize = Utils.applyScale(18f, textScale)
    }


    // ########################################
    //   RECYCLER VIEW
    // ########################################

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val inflater = LayoutInflater.from(parent.context)


        return AppViewHolder(
            inflater.inflate(R.layout.view_app, parent, false)
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        if (position < 0 || position >= appList.size) return

        val currentApp = appList[position]
        val appLayout: LinearLayout = holder.itemView.findViewById(R.id.appLayout)

        val appHasNotifications = currentNotifications[currentApp.packageName] ?: 0

        Utils.setAppViewAccordingToOptions(
            context, holder, currentApp, false, appHasNotifications, false
        )

        appLayout.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) hideKeyboard()
            false
        }

        appLayout.setOnClickListener {
            val launchAppIntent =
                context.packageManager.getLaunchIntentForPackage(currentApp.packageName)
            if (launchAppIntent != null) context.startActivity(launchAppIntent)
        }

        appLayout.setOnLongClickListener {
            if (contextMenuContainer != null) {
                hideKeyboard()
                viewModel.appMenu.postValue(AppMenu(currentApp, false, contextMenuContainer!!))
            }
            true
        }
    }

    override fun getItemCount(): Int {
        return appList.size
    }
}