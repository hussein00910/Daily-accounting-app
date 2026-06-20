package com.mohaseb.soft.ui.purchases

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mohaseb.soft.R
import com.mohaseb.soft.data.entity.Account
import com.mohaseb.soft.data.entity.Customer
import com.mohaseb.soft.data.entity.Item
import com.mohaseb.soft.databinding.DialogAddTransactionBinding
import com.mohaseb.soft.databinding.FragmentPurchasesBinding
import com.mohaseb.soft.ui.adapters.TransactionAdapter
import java.util.Locale

class PurchasesFragment : Fragment() {

    private var _binding: FragmentPurchasesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PurchasesViewModel by viewModels()
    private val adapter = TransactionAdapter(emptyList())

    private var accounts: List<Account> = emptyList()
    private var items: List<Item> = emptyList()
    private var suppliers: List<Customer> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPurchasesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvList.adapter = adapter
        binding.fabAdd.setOnClickListener { showAddPurchaseDialog() }

        viewModel.purchases.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.accounts.observe(viewLifecycleOwner) { accounts = it }
        viewModel.items.observe(viewLifecycleOwner) { items = it }
        viewModel.suppliers.observe(viewLifecycleOwner) { suppliers = it }
    }

    private fun showAddPurchaseDialog() {
        if (accounts.isEmpty() || items.isEmpty()) return
        val dialogBinding = DialogAddTransactionBinding.inflate(layoutInflater)

        dialogBinding.spinnerAccount.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, accounts.map { it.name }
        )
        dialogBinding.spinnerItem.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, items.map { it.name }
        )
        val supplierNames = listOf(getString(R.string.none)) + suppliers.map { it.name }
        dialogBinding.spinnerCustomer.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, supplierNames
        )
        dialogBinding.tvCustomerLabel.text = getString(R.string.select_supplier)
        dialogBinding.etQuantity.setText("1")
        dialogBinding.etPrice.setText(String.format(Locale.US, "%.2f", items[0].purchasePrice))

        fun updateTotal() {
            val qty = dialogBinding.etQuantity.text.toString().toDoubleOrNull() ?: 0.0
            val price = dialogBinding.etPrice.text.toString().toDoubleOrNull() ?: 0.0
            dialogBinding.tvComputedTotal.text =
                "${getString(R.string.total)}: ${String.format(Locale.US, "%.2f", qty * price)}"
        }
        updateTotal()

        dialogBinding.spinnerItem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                dialogBinding.etPrice.setText(String.format(Locale.US, "%.2f", items[position].purchasePrice))
                updateTotal()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = updateTotal()
            override fun afterTextChanged(s: Editable?) {}
        }
        dialogBinding.etQuantity.addTextChangedListener(watcher)
        dialogBinding.etPrice.addTextChangedListener(watcher)

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.add_purchase)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val accountPos = dialogBinding.spinnerAccount.selectedItemPosition
                val itemPos = dialogBinding.spinnerItem.selectedItemPosition
                if (accountPos < 0 || itemPos < 0) return@setPositiveButton
                val supplierPos = dialogBinding.spinnerCustomer.selectedItemPosition
                val supplierId = if (supplierPos > 0) suppliers[supplierPos - 1].id else null

                viewModel.addPurchase(
                    accountId = accounts[accountPos].id,
                    itemId = items[itemPos].id,
                    quantity = dialogBinding.etQuantity.text.toString().toDoubleOrNull() ?: 1.0,
                    price = dialogBinding.etPrice.text.toString().toDoubleOrNull() ?: 0.0,
                    supplierId = supplierId,
                    isCredit = dialogBinding.switchCredit.isChecked,
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
