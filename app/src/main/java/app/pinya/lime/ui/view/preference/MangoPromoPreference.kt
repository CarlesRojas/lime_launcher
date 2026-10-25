package app.pinya.lime.ui.view.preference

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import app.pinya.lime.R

/** The Mango banner on top of the settings; a Lime PRO buyer also gets the promo code line. */
class MangoPromoPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {

    var isPro: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            notifyChanged()
        }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.findViewById(R.id.mangoBanner_pro)?.visibility = if (isPro) View.VISIBLE else View.GONE
    }
}
