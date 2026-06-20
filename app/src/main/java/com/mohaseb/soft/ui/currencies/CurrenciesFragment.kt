package com.mohaseb.soft.ui.currencies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mohaseb.soft.R
import com.mohaseb.soft.data.entity.Currency
import com.mohaseb.soft.databinding.DialogAddCurrencyBinding
import com.mohaseb.soft.databinding.FragmentCurrenciesBinding
import com.mohaseb.soft.ui.adapters.CurrencyAdapter

class CurrenciesFragment : Fragment() {

    private var _binding: FragmentCurrenciesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CurrenciesViewModel by viewModels()
    private lateinit var adapter: CurrencyAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCurrenciesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = CurrencyAdapter(
            emptyList(),
            onClick = { showCurrencyDialog(it) },
            onLongClick = { showDeleteConfirm(it) }
        )
        binding.rvList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvList.adapter = adapter
        binding.fabAdd.setOnClickListener { showCurrencyDialog(null) }

        viewModel.currencies.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showCurrencyDialog(existing: Currency?) {
        val dialogBinding = DialogAddCurrencyBinding.inflate(layoutInflater)

        if (existing != null) {
            dialogBinding.etName.setText(existing.name)
            dialogBinding.etSymbol.setText(existing.symbol)
            dialogBinding.etRate.setText(existing.rate.toString())
            dialogBinding.switchDefault.isChecked = existing.isDefault
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (existing == null) R.string.add_currency_entry else R.string.edit_currency)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = dialogBinding.etName.text.toString()
                if (name.isBlank()) return@setPositiveButton
                val symbol = dialogBinding.etSymbol.text.toString()
                val rate = dialogBinding.etRate.text.toString().toDoubleOrNull() ?: 1.0

                viewModel.saveCurrency(
                    Currency(
                        id = existing?.id ?: 0L,
                        name = name,
                        symbol = symbol,
                        rate = rate,
                        isDefault = dialogBinding.switchDefault.isChecked
                    )
                )
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showDeleteConfirm(currency: Currency) {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.delete_currency_confirm)
            .setPositiveButton(R.string.delete) { _, _ -> viewModel.deleteCurrency(currency) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
