package com.example.mnotepad.adapters

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.mnotepad.R

class ColorAdapter(
    private val context: Context,
    private val colors: List<String>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    inner class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorView: View = itemView.findViewById(R.id.colorView)

        fun bind(colorHex: String) {
            val colorInt = colorHex.toColorInt()
            val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.square_rounded_corners)?.mutate()
            backgroundDrawable?.colorFilter = PorterDuffColorFilter(colorInt, PorterDuff.Mode.SRC_IN)
            colorView.background = backgroundDrawable
            itemView.setOnClickListener { onItemClick(colorInt) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.color_item, parent, false)
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val color = colors[position]
        holder.bind(color)
    }

    override fun getItemCount(): Int {
        return colors.size
    }
}
