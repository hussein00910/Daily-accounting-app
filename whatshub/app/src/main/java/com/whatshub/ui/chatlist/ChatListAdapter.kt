package com.whatshub.ui.chatlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.whatshub.R
import com.whatshub.data.model.ChatListItem
import com.whatshub.data.model.TickState
import com.whatshub.databinding.ItemChatRowBinding
import com.whatshub.utils.DateTimeUtils

class ChatListAdapter(
    private val onClick: (ChatListItem) -> Unit
) : ListAdapter<ChatListItem, ChatListAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemChatRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatListItem) {
            binding.avatarImage.load(item.avatarUrl) {
                placeholder(R.drawable.ic_default_avatar)
                error(R.drawable.ic_default_avatar)
            }
            binding.nameText.text = item.displayName
            binding.timeText.text = DateTimeUtils.relativeChatTime(item.lastMessageAt)
            binding.lastMessageText.text = item.lastMessageBody.orEmpty()

            val tickRes = when (item.tickState) {
                TickState.SENT -> R.drawable.ic_check_single
                TickState.DELIVERED -> R.drawable.ic_check_double
                TickState.READ -> R.drawable.ic_check_double_read
                TickState.NONE -> null
            }
            if (tickRes != null) {
                binding.tickIcon.visibility = View.VISIBLE
                binding.tickIcon.setImageResource(tickRes)
            } else {
                binding.tickIcon.visibility = View.GONE
            }

            if (item.unreadCount > 0) {
                binding.unreadBadge.visibility = View.VISIBLE
                binding.unreadBadge.text = item.unreadCount.toString()
            } else {
                binding.unreadBadge.visibility = View.GONE
            }

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ChatListItem>() {
            override fun areItemsTheSame(oldItem: ChatListItem, newItem: ChatListItem): Boolean =
                oldItem.chatId == newItem.chatId

            override fun areContentsTheSame(oldItem: ChatListItem, newItem: ChatListItem): Boolean =
                oldItem == newItem
        }
    }
}
