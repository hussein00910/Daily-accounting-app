package com.mohaseb.soft.data.repository

import com.mohaseb.soft.data.AppDatabase
import com.mohaseb.soft.data.entity.*

class AppRepository(private val db: AppDatabase) {

    // Accounts
    fun getAllAccounts() = db.accountDao().getAllAccounts()
    fun getAccountsByType(type: String) = db.accountDao().getAccountsByType(type)
    suspend fun getAccountById(id: Long) = db.accountDao().getAccountById(id)
    suspend fun insertAccount(account: Account) = db.accountDao().insertAccount(account)
    suspend fun updateAccount(account: Account) = db.accountDao().updateAccount(account)
    suspend fun updateAccountBalance(accountId: Long, amount: Double) = db.accountDao().updateBalance(accountId, amount)
    suspend fun deleteAccount(account: Account) = db.accountDao().deleteAccount(account)
    suspend fun getTotalDebit() = db.accountDao().getTotalDebit() ?: 0.0
    suspend fun getTotalCredit() = db.accountDao().getTotalCredit() ?: 0.0

    // Transactions
    fun getAllTransactions() = db.transactionDao().getAllTransactions()
    fun getTransactionsByAccount(accountId: Long) = db.transactionDao().getTransactionsByAccount(accountId)
    fun getTransactionsByType(type: String) = db.transactionDao().getTransactionsByType(type)
    suspend fun insertTransaction(transaction: Transaction) = db.transactionDao().insertTransaction(transaction)
    suspend fun updateTransaction(transaction: Transaction) = db.transactionDao().updateTransaction(transaction)
    suspend fun deleteTransaction(transaction: Transaction) = db.transactionDao().deleteTransaction(transaction)
    suspend fun getTotalByType(type: String) = db.transactionDao().getTotalByType(type) ?: 0.0

    // Items
    fun getAllItems() = db.itemDao().getAllItems()
    fun searchItems(query: String) = db.itemDao().searchItems(query)
    suspend fun insertItem(item: Item) = db.itemDao().insertItem(item)
    suspend fun updateItem(item: Item) = db.itemDao().updateItem(item)
    suspend fun deleteItem(item: Item) = db.itemDao().deleteItem(item)
    suspend fun updateQuantity(itemId: Long, amount: Double) = db.itemDao().updateQuantity(itemId, amount)

    // Customers
    fun getAllCustomers() = db.customerDao().getAllCustomers()
    fun getCustomersByType(type: String) = db.customerDao().getCustomersByType(type)
    suspend fun insertCustomer(customer: Customer) = db.customerDao().insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = db.customerDao().updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = db.customerDao().deleteCustomer(customer)
    suspend fun updateBalance(customerId: Long, amount: Double) = db.customerDao().updateBalance(customerId, amount)

    // Settings
    fun getSettings() = db.settingsDao().getSettings()
    suspend fun insertSettings(settings: Settings) = db.settingsDao().insertSettings(settings)
    suspend fun updateSettings(settings: Settings) = db.settingsDao().updateSettings(settings)

    // Categories
    fun getAllCategories() = db.categoryDao().getAllCategories()
    suspend fun insertCategory(category: Category) = db.categoryDao().insertCategory(category)
    suspend fun deleteCategory(category: Category) = db.categoryDao().deleteCategory(category)

    // Currencies
    fun getAllCurrencies() = db.currencyDao().getAllCurrencies()
    suspend fun getDefaultCurrency() = db.currencyDao().getDefaultCurrency()
    suspend fun insertCurrency(currency: Currency) = db.currencyDao().insertCurrency(currency)
    suspend fun updateCurrency(currency: Currency) = db.currencyDao().updateCurrency(currency)
    suspend fun deleteCurrency(currency: Currency) = db.currencyDao().deleteCurrency(currency)

    // Taxes
    fun getAllTaxes() = db.taxDao().getAllTaxes()
    suspend fun getDefaultTax() = db.taxDao().getDefaultTax()
    suspend fun insertTax(tax: Tax) = db.taxDao().insertTax(tax)
    suspend fun updateTax(tax: Tax) = db.taxDao().updateTax(tax)
    suspend fun deleteTax(tax: Tax) = db.taxDao().deleteTax(tax)
}
