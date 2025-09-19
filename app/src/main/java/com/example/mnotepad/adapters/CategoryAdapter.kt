package com.example.mnotepad.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mnotepad.callbacks.ItemMoveCallback
import com.example.mnotepad.callbacks.OnItemCategoryClickListener
import com.example.mnotepad.databinding.CategoryItemBinding
import com.example.mnotepad.entities.models.Category
import java.util.Collections

class CategoryAdapter(
    private var categories: List<Category>,
    private val onCategoryClickListener: OnItemCategoryClickListener
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>(), ItemMoveCallback.ItemTouchHelperContract {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
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
        val category = categories[position]
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
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(categories, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(categories, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRowClear(viewHolder: RecyclerView.ViewHolder) {
        val position = viewHolder.bindingAdapterPosition
        onCategoryClickListener.onUpdateOrder()
    }

    override fun getItemCount(): Int = categories.size

    inner class ViewHolder(val binding: CategoryItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    @SuppressLint("NotifyDataSetChanged")
    fun setCategories(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }

}