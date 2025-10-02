package com.example.mnotepad.helpers

import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mnotepad.adapters.ColorAdapter

fun Int.toHexColor(): String {
    return String.format("#%06X", 0xFFFFFF and this)
}

object ColorPickerDialogHelper {
    fun show(
        activity: AppCompatActivity,
        colors: List<String>,
        currentColor: String? = null,
        onColorSelected: (String) -> Unit,
        onReset: (() -> Unit)? = null
    ) {
        val adapter = ColorAdapter(activity, colors)

        currentColor?.let {
            val index = colors.indexOf(it)
            if (index != -1) adapter.selectedPosition = index
        }

        val recyclerView = RecyclerView(activity).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            layoutManager = GridLayoutManager(activity, 6)
            setPadding(50, 50, 50, 50)
            this.adapter = adapter
        }

        AlertDialog.Builder(activity)
            .setTitle("Choose a color")
            .setView(recyclerView)
            .setPositiveButton("Ok") { dialog, _ ->
                if (adapter.selectedPosition != RecyclerView.NO_POSITION) {
                    onColorSelected(colors[adapter.selectedPosition])
                }
                dialog.dismiss()
            }
            .setNeutralButton("Reset") { dialog, _ ->
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