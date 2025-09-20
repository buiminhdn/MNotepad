package com.example.mnotepad.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mnotepad.R
import com.example.mnotepad.databinding.NoteItemBinding
import com.example.mnotepad.entities.models.Note
import com.example.mnotepad.helpers.DateTimeHelper


class NoteAdapter(
    private var notes: List<Note>,
    private val onItemClick: (Note) -> Unit,
    private val onSelectModeChange: (Boolean) -> Unit
) :
    RecyclerView.Adapter<NoteAdapter.ViewHolder>() {

    private var multiSelect = false
    private val selectedItems = arrayListOf<Note>()

    inner class ViewHolder(val binding: NoteItemBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = NoteItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notes[position]
        val isSelected = isSelectedItem(note)

        holder.binding.apply {
            txtTitle.text = note.title
            txtUpdatedAt.text = DateTimeHelper.getFormatedDate(note.updatedAt)

            itemNote.isSelected = isSelected

            root.setOnLongClickListener {
                if (!multiSelect) {
                    toggleSelectMode(true)
                    toggleSelection(note, position)
                }
                true
            }

            root.setOnClickListener {
                if (multiSelect) {
                    toggleSelection(note, position)
                } else {
                    onItemClick.invoke(note)
                }
            }
        }
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


    override fun getItemCount(): Int = notes.size

    @SuppressLint("NotifyDataSetChanged")
    fun setNotes(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }

    @SuppressLint("ResourceAsColor")
    private fun selectItem(holder: RecyclerView.ViewHolder, note: Note) {
        if (selectedItems.contains(note)) {
            selectedItems.remove(note)
            holder.itemView.alpha = 1.0f
        } else {
            selectedItems.add(note)
            holder.itemView.alpha = 0.7f
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun selectAll() {
        selectedItems.clear()
        selectedItems.addAll(notes)
        notifyDataSetChanged()
        if (notes.isNotEmpty()) toggleSelectMode(true)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
        toggleSelectMode(false)
    }

    fun getSelectedNotes(): List<Note> = selectedItems.toList()


    fun isAllSelected(): Boolean = notes.isNotEmpty() && selectedItems.size == notes.size

}