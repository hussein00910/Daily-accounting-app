package com.mohaseb.soft.ui.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mohaseb.soft.R
import com.mohaseb.soft.data.entity.Account
import com.mohaseb.soft.databinding.DialogAddAccountBinding
import com.mohaseb.soft.databinding.FragmentAccountsBinding
import com.mohaseb.soft.ui.adapters.AccountAdapter
import com.mohaseb.soft.utils.Constants

class AccountsFragment : Fragment() {

    private var _binding: FragmentAccountsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AccountsViewModel by viewModels()
    private lateinit var adapter: AccountAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = AccountAdapter(
            emptyList(),
            onClick = { showAccountDialog(it) },
            onLongClick = { showDeleteConfirm(it) }
        )
        binding.rvList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvList.adapter = adapter
        binding.fabAdd.setOnClickListener { showAccountDialog(null) }

        viewModel.accounts.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showAccountDialog(existing: Account?) {
        val dialogBinding = DialogAddAccountBinding.inflate(layoutInflater)
        val typeLabels = listOf(getString(R.string.debit), getString(R.string.credit))
        dialogBinding.spinnerType.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, typeLabels
        )

        if (existing != null) {
            dialogBinding.etName.setText(existing.name)
            dialogBinding.etBalance.setText(existing.balance.toString())
            dialogBinding.etNotes.setText(existing.notes)
            dialogBinding.spinnerType.setSelection(if (existing.type == Constants.ACCOUNT_CREDIT) 1 else 0)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (existing == null) R.string.add_account else R.string.edit_account)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = dialogBinding.etName.text.toString()
                if (name.isBlank()) return@setPositiveButton
                val type = if (dialogBinding.spinnerType.selectedItemPosition == 1) {
                    Constants.ACCOUNT_CREDIT
                } else {
                    Constants.ACCOUNT_DEBIT
                }
                val balance = dialogBinding.etBalance.text.toString().toDoubleOrNull() ?: 0.0
                val notes = dialogBinding.etNotes.text.toString()

                viewModel.saveAccount(
                    Account(
                        id = existing?.id ?: 0L,
                        name = name,
                        type = type,
                        balance = balance,
                        notes = notes,
                        createdAt = existing?.createdAt ?: System.currentTimeMillis()
                    )
                )
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showDeleteConfirm(account: Account) {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.delete_account_confirm)
            .setPositiveButton(R.string.delete) { _, _ -> viewModel.deleteAccount(account) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
