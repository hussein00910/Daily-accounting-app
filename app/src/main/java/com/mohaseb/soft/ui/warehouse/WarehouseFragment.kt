package com.mohaseb.soft.ui.warehouse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mohaseb.soft.R
import com.mohaseb.soft.data.entity.Item
import com.mohaseb.soft.databinding.DialogAdjustQuantityBinding
import com.mohaseb.soft.databinding.FragmentWarehouseBinding
import com.mohaseb.soft.ui.adapters.ItemAdapter
import java.util.Locale

class WarehouseFragment : Fragment() {

    private var _binding: FragmentWarehouseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WarehouseViewModel by viewModels()
    private lateinit var adapter: ItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWarehouseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ItemAdapter(emptyList(), onLongClick = { showAdjustQuantityDialog(it) })
        binding.rvList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvList.adapter = adapter

        viewModel.items.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showAdjustQuantityDialog(item: Item) {
        val dialogBinding = DialogAdjustQuantityBinding.inflate(layoutInflater)
        dialogBinding.tvCurrentQuantity.text =
            "${item.name} — ${getString(R.string.quantity)}: ${String.format(Locale.US, "%.2f", item.quantity)}"

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.adjust_quantity)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val delta = dialogBinding.etQuantityDelta.text.toString().toDoubleOrNull() ?: 0.0
                if (delta != 0.0) {
                    viewModel.adjustQuantity(item.id, delta)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
