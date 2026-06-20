package com.mohaseb.soft.ui.reports

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.mohaseb.soft.MohasebApp
import com.mohaseb.soft.utils.Constants
import kotlinx.coroutines.launch

data class ReportData(
    val totalDebit: Double,
    val totalCredit: Double,
    val totalSales: Double,
    val totalPurchases: Double,
    val totalCashIn: Double,
    val totalCashOut: Double,
    val netBalance: Double
)

class ReportsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as MohasebApp).repository

    val transactions = repository.getAllTransactions().asLiveData()

    private val _report = MutableLiveData<ReportData>()
    val report: LiveData<ReportData> = _report

    init {
        loadReport()
    }

    fun loadReport() {
        viewModelScope.launch {
            val totalDebit = repository.getTotalDebit()
            val totalCredit = repository.getTotalCredit()
            val totalSales = repository.getTotalByType(Constants.TYPE_SALE)
            val totalPurchases = repository.getTotalByType(Constants.TYPE_PURCHASE)
            val totalCashIn = repository.getTotalByType(Constants.TYPE_CASH_IN)
            val totalCashOut = repository.getTotalByType(Constants.TYPE_CASH_OUT)
            _report.value = ReportData(
                totalDebit = totalDebit,
                totalCredit = totalCredit,
                totalSales = totalSales,
                totalPurchases = totalPurchases,
                totalCashIn = totalCashIn,
                totalCashOut = totalCashOut,
                netBalance = totalCredit - totalDebit
            )
        }
    }
}
