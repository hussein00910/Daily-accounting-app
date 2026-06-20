package com.mohaseb.soft.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mohaseb.soft.R
import com.mohaseb.soft.data.entity.Item
import com.mohaseb.soft.databinding.ItemAccountBinding
import java.util.Locale

class ItemAdapter(
    private var items: List<Item>,
    private val onClick: (Item) -> Unit = {},
    private val onLongClick: (Item) -> Unit = {}
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    fun updateData(newItems: List<Item>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ItemViewHolder {
        val binding = ItemAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ItemViewHolder(private val binding: ItemAccountBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item) {
            val context = binding.root.context
            val lowStock = item.quantity <= item.minQuantity

            binding.tvTitle.text = item.name
            binding.tvSubtitle.text = item.unit
            binding.tvTrailing.text = String.format(Locale.US, "%.2f", item.quantity)
            binding.tvTrailing.setTextColor(
                context.getColor(if (lowStock) R.color.debit_color else R.color.text_primary)
            )

            binding.root.setOnClickListener { onClick(item) }
            binding.root.setOnLongClickListener { onLongClick(item); true }
        }
    }
}
