package com.example.mnotepad.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mnotepad.databinding.HelpItemBinding
import com.example.mnotepad.entities.models.Help

class HelpAdapter(
    private val helps: List<Help>
) : RecyclerView.Adapter<HelpAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = HelpItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val helpItem = helps[position]
        holder.binding.apply {
            tvHelpTitle.text = helpItem.title
            tvHelpContent.text = helpItem.description

            root.setOnClickListener {
                if (helpItem.isShow) {
                    tvHelpContent.visibility = View.GONE
                    helpItem.isShow = false
                    tvHelpIcon.text = "+"
                } else {
                    tvHelpContent.visibility = View.VISIBLE
                    helpItem.isShow = true
                    tvHelpIcon.text = "-"
                }
            }
        }
    }

    override fun getItemCount(): Int = helps.size

    inner class ViewHolder(val binding: HelpItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}