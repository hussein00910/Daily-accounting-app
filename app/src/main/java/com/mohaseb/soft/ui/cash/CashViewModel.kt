package com.mohaseb.soft.ui.cash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.mohaseb.soft.MohasebApp
import com.mohaseb.soft.data.entity.Transaction
import com.mohaseb.soft.utils.Constants
import kotlinx.coroutines.launch

class CashViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as MohasebApp).repository

    private val cashIn = repository.getTransactionsByType(Constants.TYPE_CASH_IN).asLiveData()
    private val cashOut = repository.getTransactionsByType(Constants.TYPE_CASH_OUT).asLiveData()

    private var cashInList: List<Transaction> = emptyList()
    private var cashOutList: List<Transaction> = emptyList()

    val cashTransactions = MediatorLiveData<List<Transaction>>().apply {
        addSource(cashIn) { list ->
            cashInList = list
            value = (cashInList + cashOutList).sortedByDescending { it.date }
        }
        addSource(cashOut) { list ->
            cashOutList = list
            value = (cashInList + cashOutList).sortedByDescending { it.date }
        }
    }

    val accounts = repository.getAllAccounts().asLiveData()
    val customers = repository.getAllCustomers().asLiveData()

    fun addCashTransaction(
        accountId: Long,
        amount: Double,
        customerId: Long?,
        isCashIn: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            val type = if (isCashIn) Constants.TYPE_CASH_IN else Constants.TYPE_CASH_OUT
            repository.insertTransaction(
                Transaction(
                    accountId = accountId,
                    type = type,
                    amount = amount,
                    customerId = customerId,
                    notes = notes
                )
            )
            repository.updateAccountBalance(accountId, if (isCashIn) amount else -amount)
            if (customerId != null) {
                repository.updateBalance(customerId, -amount)
            }
        }
    }
}
