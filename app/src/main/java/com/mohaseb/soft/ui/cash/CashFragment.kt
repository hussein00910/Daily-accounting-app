package com.mohaseb.soft.ui.cash

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
import com.mohaseb.soft.data.entity.Customer
import com.mohaseb.soft.databinding.DialogAddTransactionBinding
import com.mohaseb.soft.databinding.FragmentCashBinding
import com.mohaseb.soft.ui.adapters.TransactionAdapter

class CashFragment : Fragment() {

    private var _binding: FragmentCashBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CashViewModel by viewModels()
    private val adapter = TransactionAdapter(emptyList())

    private var accounts: List<Account> = emptyList()
    private var customers: List<Customer> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvList.adapter = adapter
        binding.fabAdd.setOnClickListener { showAddCashDialog() }

        viewModel.cashTransactions.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.accounts.observe(viewLifecycleOwner) { accounts = it }
        viewModel.customers.observe(viewLifecycleOwner) { customers = it }
    }

    private fun showAddCashDialog() {
        if (accounts.isEmpty()) return
        val dialogBinding = DialogAddTransactionBinding.inflate(layoutInflater)

        dialogBinding.spinnerAccount.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, accounts.map { it.name }
        )
        val customerNames = listOf(getString(R.string.none)) + customers.map { it.name }
        dialogBinding.spinnerCustomer.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, customerNames
        )
        dialogBinding.tvCustomerLabel.text = "${getString(R.string.customer)} / ${getString(R.string.supplier)}"

        // Cash mode: hide item/quantity/price fields and the credit switch, show the amount field and the cash-direction toggle.
        dialogBinding.layoutItemFields.visibility = View.GONE
        dialogBinding.switchCredit.visibility = View.GONE
        dialogBinding.etAmount.visibility = View.VISIBLE
        dialogBinding.radioGroupCashType.visibility = View.VISIBLE

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.cash_movement)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val accountPos = dialogBinding.spinnerAccount.selectedItemPosition
                if (accountPos < 0) return@setPositiveButton
                val customerPos = dialogBinding.spinnerCustomer.selectedItemPosition
                val customerId = if (customerPos > 0) customers[customerPos - 1].id else null
                val amount = dialogBinding.etAmount.text.toString().toDoubleOrNull() ?: 0.0
                if (amount <= 0) return@setPositiveButton

                viewModel.addCashTransaction(
                    accountId = accounts[accountPos].id,
                    amount = amount,
                    customerId = customerId,
                    isCashIn = dialogBinding.rbCashIn.isChecked,
                    notes = dialogBinding.etNotes.text.toString()
                )
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
