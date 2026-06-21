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
import com.mohaseb.soft.databinding.ItemInvoiceLineBinding
import com.mohaseb.soft.ui.adapters.TransactionAdapter
import com.mohaseb.soft.utils.PdfPrinter
import java.util.Date
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
        val lineBindings = mutableListOf<ItemInvoiceLineBinding>()

        dialogBinding.spinnerAccount.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, accounts.map { it.name }
        )
        val supplierNames = listOf(getString(R.string.none)) + suppliers.map { it.name }
        dialogBinding.spinnerCustomer.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, supplierNames
        )
        dialogBinding.tvCustomerLabel.text = getString(R.string.select_supplier)

        fun updateGrandTotal() {
            val grandTotal = lineBindings.sumOf { line ->
                val qty = line.etQuantity.text.toString().toDoubleOrNull() ?: 0.0
                val price = line.etPrice.text.toString().toDoubleOrNull() ?: 0.0
                qty * price
            }
            dialogBinding.tvComputedTotal.text =
                "${getString(R.string.total)}: ${String.format(Locale.US, "%.2f", grandTotal)}"
        }

        fun updateLineTotal(lineBinding: ItemInvoiceLineBinding) {
            val qty = lineBinding.etQuantity.text.toString().toDoubleOrNull() ?: 0.0
            val price = lineBinding.etPrice.text.toString().toDoubleOrNull() ?: 0.0
            lineBinding.tvLineTotal.text =
                "${getString(R.string.total)}: ${String.format(Locale.US, "%.2f", qty * price)}"
            updateGrandTotal()
        }

        fun addLine() {
            val lineBinding = ItemInvoiceLineBinding.inflate(
                layoutInflater, dialogBinding.layoutItemLines, false
            )
            lineBinding.spinnerItem.adapter = ArrayAdapter(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, items.map { it.name }
            )
            lineBinding.etQuantity.setText("1")
            lineBinding.etPrice.setText(String.format(Locale.US, "%.2f", items[0].purchasePrice))

            lineBinding.spinnerItem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    lineBinding.etPrice.setText(String.format(Locale.US, "%.2f", items[position].purchasePrice))
                    updateLineTotal(lineBinding)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            val watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) =
                    updateLineTotal(lineBinding)
                override fun afterTextChanged(s: Editable?) {}
            }
            lineBinding.etQuantity.addTextChangedListener(watcher)
            lineBinding.etPrice.addTextChangedListener(watcher)

            lineBinding.btnRemoveLine.setOnClickListener {
                if (lineBindings.size <= 1) return@setOnClickListener
                lineBindings.remove(lineBinding)
                dialogBinding.layoutItemLines.removeView(lineBinding.root)
                updateGrandTotal()
            }

            lineBindings.add(lineBinding)
            dialogBinding.layoutItemLines.addView(lineBinding.root)
            updateLineTotal(lineBinding)
        }

        fun savePurchase(thenPrint: Boolean) {
            val accountPos = dialogBinding.spinnerAccount.selectedItemPosition
            if (accountPos < 0) return
            val supplierPos = dialogBinding.spinnerCustomer.selectedItemPosition
            val supplierId = if (supplierPos > 0) suppliers[supplierPos - 1].id else null
            val supplierName = if (supplierPos > 0) suppliers[supplierPos - 1].name else getString(R.string.none)

            val lines = lineBindings.mapNotNull { line ->
                val itemPos = line.spinnerItem.selectedItemPosition
                if (itemPos < 0) return@mapNotNull null
                InvoiceLine(
                    itemId = items[itemPos].id,
                    quantity = line.etQuantity.text.toString().toDoubleOrNull() ?: 1.0,
                    price = line.etPrice.text.toString().toDoubleOrNull() ?: 0.0
                )
            }
            if (lines.isEmpty()) return
            val isCredit = dialogBinding.switchCredit.isChecked
            val notes = dialogBinding.etNotes.text.toString()

            viewModel.addPurchase(
                accountId = accounts[accountPos].id,
                lines = lines,
                supplierId = supplierId,
                isCredit = isCredit,
                notes = notes
            )

            if (thenPrint) {
                val itemsById = items.associateBy { it.id }
                val pdfLines = lines.map { line ->
                    PdfPrinter.InvoiceLineData(
                        name = itemsById[line.itemId]?.name.orEmpty(),
                        quantity = line.quantity,
                        price = line.price
                    )
                }
                val document = PdfPrinter.buildInvoicePdf(
                    companyName = getString(R.string.app_name),
                    invoiceTitle = getString(R.string.invoice_purchase_title),
                    invoiceDate = Date(),
                    partyLabel = getString(R.string.supplier),
                    partyName = supplierName,
                    paymentTypeLabel = if (isCredit) getString(R.string.credit_switch) else getString(R.string.cash_payment),
                    notes = notes,
                    lines = pdfLines,
                    total = pdfLines.sumOf { it.total }
                )
                PdfPrinter.print(requireContext(), getString(R.string.invoice_purchase_title), document)
            }
        }

        addLine()
        dialogBinding.btnAddLine.setOnClickListener { addLine() }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.add_purchase)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ -> savePurchase(thenPrint = false) }
            .setNeutralButton(R.string.print) { _, _ -> savePurchase(thenPrint = true) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
