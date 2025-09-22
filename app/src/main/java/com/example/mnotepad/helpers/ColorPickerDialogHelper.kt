package com.example.mnotepad.helpers

import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mnotepad.adapters.ColorAdapter

object ColorPickerDialogHelper {
    fun show(
        activity: AppCompatActivity,
        colors: List<String>,
        onColorSelected: (Int) -> Unit,
        onReset: (() -> Unit)? = null
    ) {
        val recyclerView = RecyclerView(activity).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            layoutManager = GridLayoutManager(activity, 6)
            setPadding(50, 50, 50, 50)
            adapter = ColorAdapter(activity, colors) { selectedColor ->
                onColorSelected(selectedColor)
            }
        }

        AlertDialog.Builder(activity)
            .setTitle("Choose a color")
            .setView(recyclerView)
            .setPositiveButton("Reset") { dialog, _ ->
                onReset?.invoke()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}