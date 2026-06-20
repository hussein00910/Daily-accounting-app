package com.mohaseb.soft.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.mohaseb.soft.R
import com.mohaseb.soft.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupSectionToggles()
        observeData()
    }

    private fun setupClickListeners() {
        binding.btnSales.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_sales)
        }
        binding.btnPurchases.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_purchases)
        }
        binding.btnCash.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_cash)
        }
        binding.btnWarehouse.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_warehouse)
        }
        binding.btnAccounts.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_accounts)
        }
        binding.btnItems.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_items)
        }
        binding.btnReports.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_reports)
        }
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }

        // Warehouse operations
        binding.btnWarehouseOut.setOnClickListener { findNavController().navigate(R.id.action_home_to_warehouse) }
        binding.btnWarehouseIn.setOnClickListener { findNavController().navigate(R.id.action_home_to_warehouse) }
        binding.btnWarehouseAdjust.setOnClickListener { findNavController().navigate(R.id.action_home_to_warehouse) }
        binding.btnWarehouseInventory.setOnClickListener { findNavController().navigate(R.id.action_home_to_warehouse) }
        binding.btnWarehouseTransfer.setOnClickListener { showComingSoon() }
        binding.btnAddWarehouse.setOnClickListener { showComingSoon() }

        // Entries and accounts
        binding.btnAddAccountEntry.setOnClickListener { findNavController().navigate(R.id.action_home_to_accounts) }
        binding.btnCashMovement.setOnClickListener { findNavController().navigate(R.id.action_home_to_cash) }
        binding.btnChartOfAccounts.setOnClickListener { findNavController().navigate(R.id.action_home_to_accounts) }
        binding.btnDailyEntry.setOnClickListener { showComingSoon() }
        binding.btnOpeningEntry.setOnClickListener { showComingSoon() }
        binding.btnYearlyClose.setOnClickListener { showComingSoon() }

        // Items and prices
        binding.btnItemsList.setOnClickListener { findNavController().navigate(R.id.action_home_to_items) }
        binding.btnSalePrices.setOnClickListener { findNavController().navigate(R.id.action_home_to_items) }
        binding.btnItemUnits.setOnClickListener { showComingSoon() }
        binding.btnPriceQuote.setOnClickListener { showComingSoon() }
        binding.btnPurchaseOrder.setOnClickListener { showComingSoon() }

        // Currencies
        binding.btnAddCurrency.setOnClickListener { showComingSoon() }
        binding.btnCurrencyRates.setOnClickListener { showComingSoon() }
        binding.btnAccountLimit.setOnClickListener { showComingSoon() }

        // Reports
        binding.btnItemMovement.setOnClickListener { showComingSoon() }
        binding.btnTrialBalance.setOnClickListener { findNavController().navigate(R.id.action_home_to_reports) }
        binding.btnIncomeStatement.setOnClickListener { findNavController().navigate(R.id.action_home_to_reports) }
        binding.btnBalanceSheet.setOnClickListener { findNavController().navigate(R.id.action_home_to_reports) }
        binding.btnOtherReports.setOnClickListener { findNavController().navigate(R.id.action_home_to_reports) }
    }

    private fun setupSectionToggles() {
        binding.sectionWarehouseOps.setOnClickListener {
            toggle(binding.warehouseOpsContent)
        }
        binding.sectionEntries.setOnClickListener {
            toggle(binding.entriesContent)
        }
        binding.sectionItems.setOnClickListener {
            toggle(binding.itemsContent)
        }
        binding.sectionCurrencies.setOnClickListener {
            toggle(binding.currenciesContent)
        }
        binding.sectionReports.setOnClickListener {
            toggle(binding.reportsContent)
        }
    }

    private fun toggle(content: View) {
        content.visibility = if (content.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    private fun showComingSoon() {
        Snackbar.make(binding.root, R.string.coming_soon, Snackbar.LENGTH_SHORT).show()
    }

    private fun observeData() {
        viewModel.totalDebit.observe(viewLifecycleOwner) { value ->
            binding.tvTotalDebit.text = getString(R.string.total_debit, value)
        }
        viewModel.totalCredit.observe(viewLifecycleOwner) { value ->
            binding.tvTotalCredit.text = getString(R.string.total_credit, value)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
