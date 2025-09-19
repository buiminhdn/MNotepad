package com.example.mnotepad.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mnotepad.databinding.NoteItemBinding
import com.example.mnotepad.entities.models.Note
import com.example.mnotepad.helpers.DateTimeHelper


class NoteAdapter(
    private var notes: List<Note>,
    private val onItemClick: (Note) -> Unit
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
        holder.binding.apply {
            txtTitle.text = note.title
            txtUpdatedAt.text = DateTimeHelper.getFormatedDate(note.updatedAt)

            if (selectedItems.contains(note)) {
                itemNote.alpha = 0.7f
            } else {
                itemNote.alpha = 1.0f
            }

            root.setOnClickListener {
                onItemClick.invoke(note)
            }

            itemNote.setOnLongClickListener {
                if (!multiSelect) {
                    multiSelect = true
                    selectItem(holder, note)
                }
                true
            }

            itemNote.setOnClickListener {
                if (multiSelect) {
                    selectItem(holder, note)
                }
            }
        }
    }

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
}