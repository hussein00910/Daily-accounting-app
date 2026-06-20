package com.mohaseb.soft.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mohaseb.soft.R
import com.mohaseb.soft.data.entity.Transaction
import com.mohaseb.soft.databinding.ItemTransactionBinding
import com.mohaseb.soft.utils.Constants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(
    private var transactions: List<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    fun updateData(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount() = transactions.size

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            val context = binding.root.context
            val isIncoming = transaction.type == Constants.TYPE_SALE || transaction.type == Constants.TYPE_CASH_IN

            binding.tvType.text = when (transaction.type) {
                Constants.TYPE_CASH_IN -> context.getString(R.string.cash_in)
                Constants.TYPE_CASH_OUT -> context.getString(R.string.cash_out)
                Constants.TYPE_SALE ->
                    context.getString(if (transaction.isCredit) R.string.credit_sale else R.string.cash_sale)
                Constants.TYPE_PURCHASE ->
                    context.getString(if (transaction.isCredit) R.string.credit_purchase else R.string.cash_purchase)
                else -> transaction.type
            }

            binding.tvSubtitle.text = when {
                transaction.notes.isNotBlank() -> transaction.notes
                transaction.type == Constants.TYPE_SALE || transaction.type == Constants.TYPE_PURCHASE ->
                    "${transaction.quantity} × ${transaction.price}"
                else -> ""
            }

            binding.tvDate.text = dateFormat.format(Date(transaction.date))
            binding.tvAmount.text = String.format(Locale.US, "%.2f", transaction.amount)
            binding.tvAmount.setTextColor(
                context.getColor(if (isIncoming) R.color.credit_color else R.color.debit_color)
            )
        }
    }
}
