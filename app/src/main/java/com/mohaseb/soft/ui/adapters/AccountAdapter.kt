package com.mohaseb.soft.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mohaseb.soft.R
import com.mohaseb.soft.data.entity.Account
import com.mohaseb.soft.databinding.ItemAccountBinding
import com.mohaseb.soft.utils.Constants
import java.util.Locale

class AccountAdapter(
    private var accounts: List<Account>,
    private val onClick: (Account) -> Unit = {},
    private val onLongClick: (Account) -> Unit = {}
) : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    fun updateData(newAccounts: List<Account>) {
        accounts = newAccounts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): AccountViewHolder {
        val binding = ItemAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(accounts[position])
    }

    override fun getItemCount() = accounts.size

    inner class AccountViewHolder(private val binding: ItemAccountBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(account: Account) {
            val context = binding.root.context
            val isDebit = account.type == Constants.ACCOUNT_DEBIT

            binding.tvTitle.text = account.name
            binding.tvSubtitle.text = context.getString(if (isDebit) R.string.debit else R.string.credit)
            binding.tvTrailing.text = String.format(Locale.US, "%.2f", account.balance)
            binding.tvTrailing.setTextColor(
                context.getColor(if (isDebit) R.color.debit_color else R.color.credit_color)
            )

            binding.root.setOnClickListener { onClick(account) }
            binding.root.setOnLongClickListener { onLongClick(account); true }
        }
    }
}
