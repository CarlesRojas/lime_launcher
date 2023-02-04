package app.pinya.lime.ui.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import app.pinya.lime.R
import app.pinya.lime.domain.model.AppModel
import app.pinya.lime.ui.view.holder.ItemAppViewHolder
import app.pinya.lime.ui.viewmodel.AppViewModel

class DrawerAdapter(
    private val context: Context,
    private val layout: ViewGroup,
    private val viewModel: AppViewModel
) :
    RecyclerView.Adapter<ItemAppViewHolder>() {

    private var appList: MutableList<AppModel> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun updateAppList(newAppList: MutableList<AppModel>) {
        println("AHJHHHHHHHHHHHHHH")
        appList = newAppList
        this.notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAppViewHolder {
        val inflater = LayoutInflater.from(parent.context)


        return ItemAppViewHolder(
            inflater.inflate(R.layout.view_app, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemAppViewHolder, position: Int) {
        val currentApp = appList[position]

        val isTextBlack = false // TODO get from settings

        val imageView: ImageView = holder.itemView.findViewById(R.id.appIcon)
        val textView: TextView = holder.itemView.findViewById(R.id.appName)
        val linearLayout: LinearLayout = holder.itemView.findViewById(R.id.appLayout)

        linearLayout.alpha = if (currentApp.hidden) 0.35f else 1f

        imageView.setImageDrawable(currentApp.icon)
        val areIconsVisible = true // TODO get from settings
        imageView.visibility = if (areIconsVisible) View.VISIBLE else View.GONE

        textView.text = currentApp.name
        textView.setTextColor(
            ContextCompat.getColor(
                context,
                if (isTextBlack) R.color.black else R.color.white
            )
        )

        linearLayout.setOnClickListener {
            val launchAppIntent =
                context.packageManager.getLaunchIntentForPackage(currentApp.packageName)
            if (launchAppIntent != null) context.startActivity(launchAppIntent)
        }

        // TODO any toush on the list should close the keyboard
        // TODO long click to open menu
    }

    override fun getItemCount(): Int {
        println("MMMMMMMMMMMMMMMMMMMMMMMM")
        println(appList.size)
        return appList.size
    }
}