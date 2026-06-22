package com.mohaseb.soft.ui.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mohaseb.soft.R
import com.mohaseb.soft.data.entity.Transaction
import com.mohaseb.soft.databinding.FragmentReportsBinding
import com.mohaseb.soft.databinding.ItemSettingBinding
import com.mohaseb.soft.utils.ExportManager
import java.util.Locale

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportsViewModel by viewModels()
    private lateinit var exportManager: ExportManager
    private var transactions: List<Transaction> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        exportManager = ExportManager(requireContext())

        viewModel.report.observe(viewLifecycleOwner) { report ->
            binding.layoutReportRows.removeAllViews()
            addRow(getString(R.string.total_debt), report.totalDebit)
            addRow(getString(R.string.total_credit_label), report.totalCredit)
            addRow(getString(R.string.total_sales), report.totalSales)
            addRow(getString(R.string.total_purchases), report.totalPurchases)
            addRow(getString(R.string.total_cash_in), report.totalCashIn)
            addRow(getString(R.string.total_cash_out), report.totalCashOut)
            addRow(getString(R.string.net_balance), report.netBalance)
        }
        viewModel.transactions.observe(viewLifecycleOwner) { transactions = it }

        binding.btnExportCsv.setOnClickListener {
            val path = exportManager.exportToCSV(transactions, "report_${System.currentTimeMillis()}")
            Toast.makeText(requireContext(), path, Toast.LENGTH_LONG).show()
        }
        binding.btnExportPdf.setOnClickListener {
            val path = exportManager.exportToPDF(transactions, "report_${System.currentTimeMillis()}")
            Toast.makeText(requireContext(), path, Toast.LENGTH_LONG).show()
        }
    }

    private fun addRow(label: String, value: Double) {
        val rowBinding = ItemSettingBinding.inflate(layoutInflater, binding.layoutReportRows, false)
        rowBinding.tvLabel.text = label
        rowBinding.tvValue.text = String.format(Locale.US, "%.2f", value)
        binding.layoutReportRows.addView(rowBinding.root)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
