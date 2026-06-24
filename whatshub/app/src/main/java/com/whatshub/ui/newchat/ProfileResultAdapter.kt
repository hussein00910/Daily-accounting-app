package com.whatshub.ui.newchat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.whatshub.R
import com.whatshub.data.model.Profile
import com.whatshub.databinding.ItemProfileResultBinding

class ProfileResultAdapter(
    private val onClick: (Profile) -> Unit
) : ListAdapter<Profile, ProfileResultAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProfileResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemProfileResultBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(profile: Profile) {
            binding.avatarImage.load(profile.avatarUrl) {
                placeholder(R.drawable.ic_default_avatar)
                error(R.drawable.ic_default_avatar)
            }
            binding.nameText.text = profile.displayName ?: profile.phone ?: profile.username.orEmpty()
            binding.subtitleText.text = profile.username ?: profile.phone.orEmpty()
            binding.root.setOnClickListener { onClick(profile) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Profile>() {
            override fun areItemsTheSame(oldItem: Profile, newItem: Profile): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Profile, newItem: Profile): Boolean = oldItem == newItem
        }
    }
}
