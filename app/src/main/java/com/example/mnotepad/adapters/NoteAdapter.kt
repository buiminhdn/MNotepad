package com.example.mnotepad.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mnotepad.R
import com.example.mnotepad.callbacks.NoteDiffCallback
import com.example.mnotepad.databinding.NoteItemBinding
import com.example.mnotepad.entities.models.Note
import com.example.mnotepad.helpers.DateTimeHelper

class NoteAdapter(
    private val onItemClick: (Note) -> Unit,
    private val onSelectModeChange: (Boolean) -> Unit,
    private val notifySelectCount: (Int) -> Unit
) :
    ListAdapter<Note, NoteAdapter.ViewHolder>(NoteDiffCallback) {

    private var multiSelect = false
    private val selectedItems = arrayListOf<Note>()

    inner class ViewHolder(val binding: NoteItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note, isSelected: Boolean) {
            binding.apply {
                txtTitle.text = note.title
                txtUpdatedAt.text = DateTimeHelper.getFormatedDate(note.updatedAt)
                val background = ContextCompat.getDrawable(root.context, R.drawable.bg_note_item)
                background?.setTint(note.color?.toColorInt() ?: 0)
                root.background = background
                itemNote.isSelected = isSelected

                root.setOnLongClickListener {
                    if (!multiSelect) {
                        toggleSelectMode(true)
                        toggleSelection(note, bindingAdapterPosition)
                        notifySelectCount(getSelectedNotesCount())
                    }
                    true
                }

                root.setOnClickListener {
                    if (multiSelect) {
                        toggleSelection(note, bindingAdapterPosition)
                        notifySelectCount(getSelectedNotesCount())
                    } else {
                        onItemClick.invoke(note)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = NoteItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = getItem(position)
        val isSelected = isSelectedItem(note)
        holder.bind(note, isSelected)
    }

    private fun toggleSelection(note: Note, position: Int) {
        if (selectedItems.contains(note)) {
            selectedItems.remove(note)
        } else {
            selectedItems.add(note)
        }

        notifyItemChanged(position)

        // Nếu không còn item nào thì thoát select mode
        if (multiSelect && selectedItems.isEmpty()) {
            toggleSelectMode(false)
            notifySelectCount(0)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun toggleSelectMode(isEnable: Boolean) {
        multiSelect = isEnable
        if (!isEnable) {
            selectedItems.clear()
            notifyDataSetChanged()
        }
        onSelectModeChange(multiSelect)
    }

    private fun isSelectedItem(note: Note): Boolean = selectedItems.contains(note)

    fun getSelectedNotesCount(): Int = selectedItems.size

    @SuppressLint("NotifyDataSetChanged")
    fun selectAll() {
        selectedItems.clear()
        selectedItems.addAll(currentList)
        notifyDataSetChanged()
        if (currentList.isNotEmpty()) toggleSelectMode(true)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
        toggleSelectMode(false)
    }

    fun getSelectedNotes(): List<Note> = selectedItems.toList()

    fun isMultiSelected(): Boolean = multiSelect

    fun isAllSelected(): Boolean =
        currentList.isNotEmpty() && selectedItems.size == currentList.size
}
