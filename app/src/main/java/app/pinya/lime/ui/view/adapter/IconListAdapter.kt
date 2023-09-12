package app.pinya.lime.ui.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import app.pinya.lime.R
import app.pinya.lime.domain.model.Icon
import app.pinya.lime.ui.view.holder.AppViewHolder

class IconListAdapter(
    private val iconPack: String,
    private val onIconClick: (iconPack: String?, icon: String?) -> Unit,
    ) : RecyclerView.Adapter<AppViewHolder>() {

    private var icons: List<Icon> = listOf()

    // ########################################
    //   GENERAL
    // ########################################

    @SuppressLint("NotifyDataSetChanged")
    fun updateIcons(newIcons: List<Icon>) {
        icons = newIcons
        notifyDataSetChanged()
    }

    // ########################################
    //   RECYCLER VIEW
    // ########################################

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return AppViewHolder(
            inflater.inflate(R.layout.view_icon, parent, false)
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val iconLayout: LinearLayout = holder.itemView.findViewById(R.id.iconLayout)
        val icon: ImageView = iconLayout.findViewById(R.id.icon)

        if (position < icons.size) {
            val currentIcon = icons[position]
            icon.setImageDrawable(currentIcon.drawable)

            icon.setOnClickListener {
                onIconClick(iconPack, currentIcon.name)
            }
        }
    }

    override fun getItemCount(): Int {
        return icons.size
    }
}