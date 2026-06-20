package com.mohaseb.soft.ui.sales

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.mohaseb.soft.MohasebApp
import com.mohaseb.soft.data.entity.Transaction
import com.mohaseb.soft.utils.Constants
import kotlinx.coroutines.launch

data class InvoiceLine(
    val itemId: Long,
    val quantity: Double,
    val price: Double
)

class SalesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as MohasebApp).repository

    val sales = repository.getTransactionsByType(Constants.TYPE_SALE).asLiveData()
    val accounts = repository.getAllAccounts().asLiveData()
    val items = repository.getAllItems().asLiveData()
    val customers = repository.getCustomersByType(Constants.CUSTOMER_TYPE).asLiveData()

    fun addSale(
        accountId: Long,
        lines: List<InvoiceLine>,
        customerId: Long?,
        isCredit: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            val invoiceId = System.currentTimeMillis()
            var totalAmount = 0.0
            for (line in lines) {
                val amount = line.quantity * line.price
                totalAmount += amount
                repository.insertTransaction(
                    Transaction(
                        accountId = accountId,
                        type = Constants.TYPE_SALE,
                        amount = amount,
                        quantity = line.quantity,
                        price = line.price,
                        itemId = line.itemId,
                        customerId = customerId,
                        isCredit = isCredit,
                        notes = notes,
                        invoiceId = invoiceId
                    )
                )
                repository.updateQuantity(line.itemId, -line.quantity)
            }
            repository.updateAccountBalance(accountId, totalAmount)
            if (isCredit && customerId != null) {
                repository.updateBalance(customerId, totalAmount)
            }
        }
    }
}
