package com.mohaseb.soft.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mohaseb.soft.R
import com.mohaseb.soft.data.entity.Currency
import com.mohaseb.soft.databinding.ItemAccountBinding
import java.util.Locale

class CurrencyAdapter(
    private var currencies: List<Currency>,
    private val onClick: (Currency) -> Unit = {},
    private val onLongClick: (Currency) -> Unit = {}
) : RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder>() {

    fun updateData(newCurrencies: List<Currency>) {
        currencies = newCurrencies
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): CurrencyViewHolder {
        val binding = ItemAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CurrencyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
        holder.bind(currencies[position])
    }

    override fun getItemCount() = currencies.size

    inner class CurrencyViewHolder(private val binding: ItemAccountBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(currency: Currency) {
            val context = binding.root.context
            binding.tvTitle.text = currency.name
            binding.tvSubtitle.text = if (currency.isDefault) {
                "${currency.symbol} · ${context.getString(R.string.default_currency)}"
            } else {
                currency.symbol
            }
            binding.tvTrailing.text = String.format(Locale.US, "%.2f", currency.rate)
            binding.tvTrailing.setTextColor(context.getColor(R.color.text_primary))

            binding.root.setOnClickListener { onClick(currency) }
            binding.root.setOnLongClickListener { onLongClick(currency); true }
        }
    }
}
