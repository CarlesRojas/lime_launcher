package app.pinya.lime.ui.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.pinya.lime.R
import app.pinya.lime.ui.view.holder.ItemAppViewHolder
import app.pinya.lime.ui.viewmodel.AppViewModel

class HomeAdapter(
    private val context: Context,
    private val layout: ViewGroup,
    private val viewModel: AppViewModel
) :
    RecyclerView.Adapter<ItemAppViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAppViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ItemAppViewHolder(
            inflater.inflate(R.layout.view_app, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemAppViewHolder, position: Int) {
    }

    override fun getItemCount(): Int {
        return 0
    }
}