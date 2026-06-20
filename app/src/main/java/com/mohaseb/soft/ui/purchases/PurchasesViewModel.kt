package com.mohaseb.soft.ui.purchases

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.mohaseb.soft.MohasebApp
import com.mohaseb.soft.data.entity.Transaction
import com.mohaseb.soft.utils.Constants
import kotlinx.coroutines.launch

class PurchasesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as MohasebApp).repository

    val purchases = repository.getTransactionsByType(Constants.TYPE_PURCHASE).asLiveData()
    val accounts = repository.getAllAccounts().asLiveData()
    val items = repository.getAllItems().asLiveData()
    val suppliers = repository.getCustomersByType(Constants.SUPPLIER_TYPE).asLiveData()

    fun addPurchase(
        accountId: Long,
        itemId: Long,
        quantity: Double,
        price: Double,
        supplierId: Long?,
        isCredit: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            val amount = quantity * price
            repository.insertTransaction(
                Transaction(
                    accountId = accountId,
                    type = Constants.TYPE_PURCHASE,
                    amount = amount,
                    quantity = quantity,
                    price = price,
                    itemId = itemId,
                    customerId = supplierId,
                    isCredit = isCredit,
                    notes = notes
                )
            )
            repository.updateQuantity(itemId, quantity)
            repository.updateAccountBalance(accountId, -amount)
            if (isCredit && supplierId != null) {
                repository.updateBalance(supplierId, amount)
            }
        }
    }
}
