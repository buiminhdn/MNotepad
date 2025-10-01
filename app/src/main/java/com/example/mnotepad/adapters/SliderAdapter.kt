package com.example.mnotepad.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.mnotepad.databinding.SliderItemBinding
import com.example.mnotepad.entities.models.SliderModel

class SliderAdapter(
    private var sliderItems: List<SliderModel>
) : RecyclerView.Adapter<SliderAdapter.SliderViewHolder>() {
    private lateinit var context: Context

    class SliderViewHolder(private val binding: SliderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setImage(sliderItems: SliderModel, context: Context) {
            Glide.with(context)
                .load(sliderItems.url)
                .apply(RequestOptions().transform(CenterCrop()))
                .into(binding.imageSlide)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SliderAdapter.SliderViewHolder {
        context = parent.context
        val binding = SliderItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SliderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SliderAdapter.SliderViewHolder, position: Int) {
        holder.setImage(sliderItems[position], context)
    }

    override fun getItemCount(): Int = sliderItems.size
}
