package com.whatshub.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.whatshub.R
import com.whatshub.data.model.MessageUiModel
import com.whatshub.data.model.TickState
import com.whatshub.databinding.ItemMessageReceivedBinding
import com.whatshub.databinding.ItemMessageSentBinding
import com.whatshub.utils.DateTimeUtils

private const val VIEW_TYPE_SENT = 1
private const val VIEW_TYPE_RECEIVED = 2

class MessageAdapter : ListAdapter<MessageUiModel, RecyclerView.ViewHolder>(DIFF) {

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).isOwnMessage) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_SENT) {
            SentViewHolder(ItemMessageSentBinding.inflate(inflater, parent, false))
        } else {
            ReceivedViewHolder(ItemMessageReceivedBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is SentViewHolder -> holder.bind(item)
            is ReceivedViewHolder -> holder.bind(item)
        }
    }

    class SentViewHolder(private val binding: ItemMessageSentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MessageUiModel) {
            binding.bodyText.text = item.message.body.orEmpty()
            binding.timeText.text = DateTimeUtils.relativeChatTime(item.message.createdAt)
            val tickRes = when (item.tickState) {
                TickState.SENT -> R.drawable.ic_check_single
                TickState.DELIVERED -> R.drawable.ic_check_double
                TickState.READ -> R.drawable.ic_check_double_read
                TickState.NONE -> R.drawable.ic_check_single
            }
            binding.tickIcon.setImageResource(tickRes)
            binding.root.alpha = if (item.sendState.name == "FAILED") 0.5f else 1f
        }
    }

    class ReceivedViewHolder(private val binding: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MessageUiModel) {
            binding.bodyText.text = item.message.body.orEmpty()
            binding.timeText.text = DateTimeUtils.relativeChatTime(item.message.createdAt)
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MessageUiModel>() {
            override fun areItemsTheSame(oldItem: MessageUiModel, newItem: MessageUiModel): Boolean =
                oldItem.message.id == newItem.message.id

            override fun areContentsTheSame(oldItem: MessageUiModel, newItem: MessageUiModel): Boolean =
                oldItem == newItem
        }
    }
}
