package com.example.studybetter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ModuleAdapter(
    private var modules: List<Module>,
    private val onMoreClick: (Module, View) -> Unit
) : RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder>() {

    class ModuleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val number: TextView = view.findViewById(R.id.tv_module_number)
        val title: TextView = view.findViewById(R.id.tv_module_title)
        val description: TextView = view.findViewById(R.id.tv_module_description)
        val btnMore: ImageButton = view.findViewById(R.id.btn_more)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_module, parent, false)
        return ModuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val module = modules[position]
        holder.number.text = String.format("%02d", position + 1)
        holder.title.text = module.title
        holder.description.text = module.description
        
        holder.btnMore.setOnClickListener { view ->
            onMoreClick(module, view)
        }
    }

    override fun getItemCount(): Int = modules.size

    fun updateModules(newModules: List<Module>) {
        modules = newModules
        notifyDataSetChanged()
    }
}
