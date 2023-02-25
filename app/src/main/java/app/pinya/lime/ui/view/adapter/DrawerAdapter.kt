package app.pinya.lime.ui.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
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
import app.pinya.lime.ui.view.holder.ItemAppViewHolder
import app.pinya.lime.ui.viewmodel.AppViewModel
import kotlin.math.floor


class DrawerAdapter(
    private val context: Context,
    private val layout: ViewGroup,
    private val viewModel: AppViewModel
) : RecyclerView.Adapter<ItemAppViewHolder>() {

    // APP LIST
    private var appList: MutableList<AppModel> = mutableListOf()
    private var appListView: RecyclerView? = null
    private var drawerConstraintLayout: ConstraintLayout? = null

    // SEARCH BAR
    private var searchBar: EditText? = null
    private var searchBarText: String = ""

    // ALPHABET
    private var alphabetLayout: LinearLayout? = null
    private val currentAlphabet: MutableList<Char> = mutableListOf()
    private var filteringByAlphabet = false
    private var lastFilterWasAlphabet = false

    init {
        initSearchBar()
        hideKeyboardOnAnyTouchOutside()
        initAlphabet()
        showHideElements()
    }

    // ########################################
    //   GENERAL
    // ########################################

    @SuppressLint("NotifyDataSetChanged")
    fun updateAppList(newAppList: MutableList<AppModel>) {
        appList = newAppList

        val autoOpenApps = viewModel.settings.value?.drawerAutoOpenApps ?: true
        val moreThanOneInstalledApp = viewModel.completeAppList.size > 1

        if (!lastFilterWasAlphabet && autoOpenApps && moreThanOneInstalledApp && appList.size == 1) {
            clearText()

            val app = appList[0]
            val launchAppIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
            if (launchAppIntent != null) context.startActivity(launchAppIntent)
        }

        updateLettersIncludedInAlphabet()
        this.notifyDataSetChanged()
    }

    private fun showHideElements() {
        val showSearchBar = viewModel.settings.value?.drawerShowSearchBar ?: true
        val showAlphabetFilter = viewModel.settings.value?.drawerShowAlphabetFilter ?: true

        searchBar?.visibility = if (showSearchBar) View.VISIBLE else View.GONE
        alphabetLayout?.visibility = if (showAlphabetFilter) View.VISIBLE else View.GONE

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
        searchBar = layout.findViewById(R.id.drawerSearchBar)

        searchBar?.doAfterTextChanged {
            if (it.toString() == "") hideClearText() else showClearText()
            searchBarText = it.toString()

            if (filteringByAlphabet) viewModel.filterDrawerAppListByAlphabetLetter(searchBarText.first())
            else viewModel.filterDrawerAppListBySearchedText(searchBarText)

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
        val autoOpenKeyboard = viewModel.settings.value?.drawerAutoShowKeyboard ?: true

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

    @SuppressLint("ClickableViewAccessibility")
    fun initAlphabet() {
        alphabetLayout = layout.findViewById(R.id.alphabetLayout)

        val currentLetter = layout.findViewById<TextView>(R.id.currentLetter)

        alphabetLayout?.setOnTouchListener { _, event ->
            clearText()
            hideKeyboard()
            val startY = alphabetLayout!!.top
            val endY = alphabetLayout!!.bottom
            val perc = (event.rawY - startY) / (endY - startY)
            val letterHeight = 1f / (AlphabetModel.ALPHABET.size + 1)
            val currentSection = floor(perc / letterHeight).toInt()


            if (currentSection >= 0 && currentSection < currentAlphabet.size) {
                val currentChar = currentAlphabet[currentSection]

                currentLetter.visibility = View.VISIBLE
                currentLetter.text = currentChar.uppercase()
                currentLetter.animate().y(event.rawY - (currentLetter.height / 2)).setDuration(0)
                    .start()


                filteringByAlphabet = true
                searchBar?.setText(currentChar.toString())
                lastFilterWasAlphabet = true
            }

            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_OUTSIDE) currentLetter.visibility =
                View.GONE

            true
        }
    }

    private fun updateLettersIncludedInAlphabet() {
        currentAlphabet.clear()

        for (app in viewModel.completeAppList) {
            val char = app.name.first().uppercaseChar()

            if (currentAlphabet.contains(char)) continue
            if (AlphabetModel.ALPHABET.contains(char)) currentAlphabet.add(char)
            else if (!currentAlphabet.contains('#')) currentAlphabet.add(0, '#')
        }

        alphabetLayout?.removeAllViews()
        val alphabetHeight = alphabetLayout?.height ?: 20
        val isTextBlack = viewModel.settings.value?.generalIsTextBlack ?: false

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
            alphabetLayout?.addView(textView)
        }
    }

    // ########################################
    //   RECYCLER VIEW
    // ########################################

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAppViewHolder {
        val inflater = LayoutInflater.from(parent.context)


        return ItemAppViewHolder(
            inflater.inflate(R.layout.view_app, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemAppViewHolder, position: Int) {
        val currentApp = appList[position]

        val isTextBlack = viewModel.settings.value?.generalIsTextBlack ?: false

        val imageView: ImageView = holder.itemView.findViewById(R.id.appIcon)
        val textView: TextView = holder.itemView.findViewById(R.id.appName)
        val linearLayout: LinearLayout = holder.itemView.findViewById(R.id.appLayout)

        linearLayout.alpha = if (currentApp.hidden) 0.35f else 1f

        imageView.setImageDrawable(currentApp.icon)
        val areIconsVisible = viewModel.settings.value?.drawerShowIcons ?: true
        imageView.visibility = if (areIconsVisible) View.VISIBLE else View.GONE

        textView.text = currentApp.name
        textView.setTextColor(
            ContextCompat.getColor(
                context, if (isTextBlack) R.color.black else R.color.white
            )
        )

        linearLayout.setOnClickListener {
            val launchAppIntent =
                context.packageManager.getLaunchIntentForPackage(currentApp.packageName)
            if (launchAppIntent != null) context.startActivity(launchAppIntent)
        }

        // TODO long click to open menu
    }

    override fun getItemCount(): Int {
        return appList.size
    }
}