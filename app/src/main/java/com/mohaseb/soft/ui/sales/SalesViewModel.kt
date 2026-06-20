package com.mohaseb.soft.ui.sales

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.mohaseb.soft.MohasebApp
import com.mohaseb.soft.data.entity.Transaction
import com.mohaseb.soft.utils.Constants
import kotlinx.coroutines.launch

class SalesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as MohasebApp).repository

    val sales = repository.getTransactionsByType(Constants.TYPE_SALE).asLiveData()
    val accounts = repository.getAllAccounts().asLiveData()
    val items = repository.getAllItems().asLiveData()
    val customers = repository.getCustomersByType(Constants.CUSTOMER_TYPE).asLiveData()

    fun addSale(
        accountId: Long,
        itemId: Long,
        quantity: Double,
        price: Double,
        customerId: Long?,
        isCredit: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            val amount = quantity * price
            repository.insertTransaction(
                Transaction(
                    accountId = accountId,
                    type = Constants.TYPE_SALE,
                    amount = amount,
                    quantity = quantity,
                    price = price,
                    itemId = itemId,
                    customerId = customerId,
                    isCredit = isCredit,
                    notes = notes
                )
            )
            repository.updateQuantity(itemId, -quantity)
            repository.updateAccountBalance(accountId, amount)
            if (isCredit && customerId != null) {
                repository.updateBalance(customerId, amount)
            }
        }
    }
}
