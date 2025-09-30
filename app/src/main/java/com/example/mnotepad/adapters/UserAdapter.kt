package com.example.mnotepad.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mnotepad.databinding.UserItemBinding
import com.example.mnotepad.entities.models.User

class UserAdapter(
    private val users: List<User>
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserAdapter.ViewHolder {
        val binding = UserItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: UserAdapter.ViewHolder,
        position: Int
    ) {
        val user = users[position]
        holder.binding.apply {
            tvFirstName.text = user.firstName
        }
    }

    override fun getItemCount(): Int = users.size

    inner class ViewHolder(val binding: UserItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}