package app.pinya.lime.ui.view.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import app.pinya.lime.R

class MangoPromoAdapter(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "MangoPromoPrefs"
        private const val KEY_DISMISSED = "dismissed"

        fun isDismissed(context: Context): Boolean {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_DISMISSED, false)
        }

        private fun setDismissed(context: Context) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_DISMISSED, true)
                .apply()
        }
    }

    private var popupWindow: PopupWindow? = null
    private var isOpen = false

    fun showIfNeeded(anchor: View) {
        if (isDismissed(context)) return
        if (isOpen) return
        if (anchor.width == 0) {
            anchor.post { showIfNeeded(anchor) }
            return
        }
        show(anchor)
    }

    private fun show(anchor: View) {
        isOpen = true

        val view = View.inflate(context, R.layout.view_mango_promo_menu, null)
        val dismissButton = view.findViewById<LinearLayout>(R.id.mangoPromo_dismiss)
        val ctaButton = view.findViewById<LinearLayout>(R.id.mangoPromo_cta)

        val width = (anchor.width - anchor.paddingLeft - anchor.paddingRight)
            .coerceAtLeast(LinearLayout.LayoutParams.WRAP_CONTENT)

        popupWindow = PopupWindow(
            view,
            width,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow?.animationStyle = R.style.TopPopupWindowAnimation
        popupWindow?.showAtLocation(anchor, Gravity.CENTER, 0, 0)
        dimBehind(popupWindow)

        popupWindow?.setOnDismissListener {
            isOpen = false
        }

        val dismissAction = {
            setDismissed(context)
            popupWindow?.dismiss()
        }

        dismissButton.setOnClickListener { dismissAction() }

        ctaButton.setOnClickListener {
            setDismissed(context)
            try {
                val url = context.getString(R.string.mango_promo_url)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            popupWindow?.dismiss()
        }
    }

    private fun dimBehind(menu: PopupWindow?) {
        if (menu == null) return
        val container = menu.contentView.rootView
        val ctx = menu.contentView.context
        val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val p = container.layoutParams as WindowManager.LayoutParams
        p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        p.dimAmount = 0.3f
        wm.updateViewLayout(container, p)
    }
}
