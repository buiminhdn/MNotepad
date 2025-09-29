package com.example.mnotepad.callbacks

import androidx.recyclerview.widget.DiffUtil
import com.example.mnotepad.entities.models.Category

object CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
    override fun areItemsTheSame(
        oldItem: Category,
        newItem: Category
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Category,
        newItem: Category
    ): Boolean {
        return oldItem == newItem
    }
}
