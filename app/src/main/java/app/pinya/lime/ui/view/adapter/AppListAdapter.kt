package app.pinya.lime.ui.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.pinya.lime.R
import app.pinya.lime.ui.view.holder.AppViewHolder
import app.pinya.lime.ui.viewmodel.AppViewModel

class AppListAdapter(
    private val context: Context,
    private val viewModel: AppViewModel,
    private val isTime: Boolean,
    private val onChangeAppCallback: (useDefault: Boolean, useNone: Boolean, newAppPackage: String) -> Unit
) : RecyclerView.Adapter<AppViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return AppViewHolder(
            inflater.inflate(R.layout.view_app, parent, false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {

        val imageView: ImageView = holder.itemView.findViewById(R.id.appIcon)
        val textView: TextView = holder.itemView.findViewById(R.id.appName)
        val linearLayout: LinearLayout = holder.itemView.findViewById(R.id.appLayout)

        val appList = viewModel.completeAppList

        when (position) {
            0 -> {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        context, if (isTime) R.drawable.icon_clock else R.drawable.icon_calendar
                    )
                )
                textView.text = if (isTime) "Default clock app" else "Default calendar app"

                linearLayout.setOnClickListener {
                    onChangeAppCallback(true, false, "")
                }

            }
            1 -> {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        context, R.drawable.icon_close
                    )
                )
                textView.text = "Don't open any app"

                linearLayout.setOnClickListener {
                    onChangeAppCallback(false, true, "")
                }
            }
            else -> {
                //if (position - 2 < 0 || position - 2 >= appList.size) return
                val currentApp = appList[position - 2]

                imageView.setImageDrawable(currentApp.icon)
                textView.text = currentApp.name

                linearLayout.setOnClickListener {
                    onChangeAppCallback(false, false, currentApp.packageName)
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return viewModel.completeAppList.size + 2
    }
}