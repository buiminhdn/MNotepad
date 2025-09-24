package com.example.mnotepad.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.example.mnotepad.R
import com.example.mnotepad.callbacks.ItemMoveCallback
import com.example.mnotepad.databinding.CheckItemBinding
import com.example.mnotepad.entities.models.Category
import com.example.mnotepad.entities.models.ChecklistItem
import java.util.Collections

class CheckListAdapter (
    private var checklistItems: MutableList<ChecklistItem>,
    private val onStartDrag: (RecyclerView.ViewHolder) -> Unit
) : RecyclerView.Adapter<CheckListAdapter.ViewHolder>(), ItemMoveCallback.ItemTouchHelperContract {

    inner class ViewHolder(val binding: CheckItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CheckListAdapter.ViewHolder {
        val binding = CheckItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val checkListItem = checklistItems[position]
        holder.binding.apply {
            ckbContentItem.isChecked = checkListItem.isChecked
            edtContentItem.setText(checkListItem.text)

            ckbContentItem.setOnCheckedChangeListener { _, isChecked ->
                checklistItems[holder.bindingAdapterPosition].isChecked = isChecked
            }

            edtContentItem.addTextChangedListener { text ->
                checklistItems[holder.bindingAdapterPosition].text = text.toString()
            }

            btnDelete.setOnClickListener {
                checklistItems.remove(checkListItem)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, getItemCount());
            }

            btnDrag.setOnTouchListener { v, event ->
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> onStartDrag(holder);
                }

                v?.onTouchEvent(event) ?: true
            }
        }
    }

    override fun getItemCount(): Int = checklistItems.size

    @SuppressLint("NotifyDataSetChanged")
    fun setCheckListItems(newCheckListItems: MutableList<ChecklistItem>) {
        checklistItems = newCheckListItems
        notifyDataSetChanged()
    }

    fun addItem(item: ChecklistItem) {
        checklistItems.add(item)
        notifyItemInserted(checklistItems.size - 1)
    }


    fun getCheckListItems(): List<ChecklistItem> = checklistItems
    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(checklistItems, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(checklistItems, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRowClear(viewHolder: RecyclerView.ViewHolder) {

    }
}