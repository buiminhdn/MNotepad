package com.example.mnotepad.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mnotepad.callbacks.CategoryDiffCallback
import com.example.mnotepad.callbacks.ItemMoveCallback
import com.example.mnotepad.callbacks.OnItemCategoryClickListener
import com.example.mnotepad.databinding.CategoryItemBinding
import com.example.mnotepad.entities.models.Category
import java.util.Collections

class CategoryAdapter(
    private val onCategoryClickListener: OnItemCategoryClickListener
) : ListAdapter<Category, CategoryAdapter.ViewHolder>(CategoryDiffCallback), ItemMoveCallback.ItemTouchHelperContract {

    inner class ViewHolder(val binding: CategoryItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CategoryItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val category = getItem(position)
        holder.binding.apply {
            edtName.setText(category.name)

            btnUpdate.setOnClickListener {
                onCategoryClickListener.onItemUpdate(category.copy(name = edtName.text.toString()))
            }

            btnDelete.setOnClickListener {
                onCategoryClickListener.onItemDelete(
                    category.id
                )
            }

            btnDrag.setOnTouchListener { v, event ->
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> onCategoryClickListener.onItemDrag(holder);
                }

                v?.onTouchEvent(event) ?: true
            }

        }
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        val currentList = currentList.toMutableList()
        Collections.swap(currentList, fromPosition, toPosition)
        submitList(currentList)
    }

    override fun onRowClear(viewHolder: RecyclerView.ViewHolder) {
        onCategoryClickListener.onUpdateOrder()
    }

}