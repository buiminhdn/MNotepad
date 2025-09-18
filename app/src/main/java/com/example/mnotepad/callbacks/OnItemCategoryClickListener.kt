package com.example.mnotepad.callbacks

import com.example.mnotepad.entities.models.Category

interface OnItemCategoryClickListener {
    fun onItemUpdate(category: Category)
    fun onItemDelete(id: Int)
}