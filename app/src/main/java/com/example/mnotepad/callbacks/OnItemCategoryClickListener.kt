package com.example.mnotepad.callbacks

import androidx.recyclerview.widget.RecyclerView
import com.example.mnotepad.entities.models.Category

interface OnItemCategoryClickListener {
    fun onItemUpdate(category: Category)
    fun onItemDelete(id: Int)
    fun onItemDrag(viewHolder: RecyclerView.ViewHolder)
    fun onUpdateOrder()
}
