package com.mohaseb.soft.ui.items

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mohaseb.soft.R
import com.mohaseb.soft.data.entity.Item
import com.mohaseb.soft.databinding.DialogAddItemBinding
import com.mohaseb.soft.databinding.FragmentItemsBinding
import com.mohaseb.soft.ui.adapters.ItemAdapter

class ItemsFragment : Fragment() {

    private var _binding: FragmentItemsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ItemsViewModel by viewModels()
    private lateinit var adapter: ItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ItemAdapter(emptyList(), showPrice = true, onClick = { showItemDialog(it) })
        binding.rvList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvList.adapter = adapter
        binding.fabAdd.setOnClickListener { showItemDialog(null) }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setQuery(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        viewModel.items.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showItemDialog(existing: Item?) {
        val dialogBinding = DialogAddItemBinding.inflate(layoutInflater)

        if (existing != null) {
            dialogBinding.etName.setText(existing.name)
            dialogBinding.etCode.setText(existing.code)
            dialogBinding.etUnit.setText(existing.unit)
            dialogBinding.etPurchasePrice.setText(existing.purchasePrice.toString())
            dialogBinding.etSalePrice.setText(existing.salePrice.toString())
            dialogBinding.etQuantity.setText(existing.quantity.toString())
            dialogBinding.etMinQuantity.setText(existing.minQuantity.toString())
            dialogBinding.etBarcode.setText(existing.barcode)
            dialogBinding.etNotes.setText(existing.notes)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (existing == null) R.string.add_item else R.string.edit_item)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = dialogBinding.etName.text.toString()
                if (name.isBlank()) return@setPositiveButton

                viewModel.saveItem(
                    Item(
                        id = existing?.id ?: 0L,
                        name = name,
                        code = dialogBinding.etCode.text.toString(),
                        unit = dialogBinding.etUnit.text.toString().ifBlank { "حبة" },
                        purchasePrice = dialogBinding.etPurchasePrice.text.toString().toDoubleOrNull() ?: 0.0,
                        salePrice = dialogBinding.etSalePrice.text.toString().toDoubleOrNull() ?: 0.0,
                        quantity = dialogBinding.etQuantity.text.toString().toDoubleOrNull() ?: 0.0,
                        minQuantity = dialogBinding.etMinQuantity.text.toString().toDoubleOrNull() ?: 0.0,
                        barcode = dialogBinding.etBarcode.text.toString(),
                        notes = dialogBinding.etNotes.text.toString()
                    )
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
