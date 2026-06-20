package com.mohaseb.soft.utils

import android.content.Context
import com.google.gson.GsonBuilder
import com.mohaseb.soft.data.entity.Account
import com.mohaseb.soft.data.entity.Category
import com.mohaseb.soft.data.entity.Currency
import com.mohaseb.soft.data.entity.Customer
import com.mohaseb.soft.data.entity.Item
import com.mohaseb.soft.data.entity.Settings
import com.mohaseb.soft.data.entity.Tax
import com.mohaseb.soft.data.entity.Transaction
import com.mohaseb.soft.data.repository.AppRepository
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupData(
    val accounts: List<Account>,
    val transactions: List<Transaction>,
    val items: List<Item>,
    val customers: List<Customer>,
    val categories: List<Category>,
    val currencies: List<Currency>,
    val taxes: List<Tax>,
    val settings: Settings?,
    val version: Int = Constants.DATABASE_VERSION,
    val backupDate: Long = System.currentTimeMillis()
)

class BackupManager(private val context: Context, private val repository: AppRepository) {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun backupData(): String {
        val data = BackupData(
            accounts = repository.getAllAccounts().first(),
            transactions = repository.getAllTransactions().first(),
            items = repository.getAllItems().first(),
            customers = repository.getAllCustomers().first(),
            categories = repository.getAllCategories().first(),
            currencies = repository.getAllCurrencies().first(),
            taxes = repository.getAllTaxes().first(),
            settings = repository.getSettings().first()
        )

        val backupDir = File(context.getExternalFilesDir(null), Constants.BACKUP_FOLDER)
        if (!backupDir.exists()) backupDir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(backupDir, "$timestamp-mohaseb_backup.json")
        file.writeText(gson.toJson(data))
        return file.absolutePath
    }

    suspend fun restoreData(filePath: String): BackupData? {
        val file = File(filePath)
        if (!file.exists()) return null

        val data = gson.fromJson(file.readText(), BackupData::class.java) ?: return null

        data.accounts.forEach { repository.insertAccount(it) }
        data.transactions.forEach { repository.insertTransaction(it) }
        data.items.forEach { repository.insertItem(it) }
        data.customers.forEach { repository.insertCustomer(it) }
        data.categories.forEach { repository.insertCategory(it) }
        data.currencies.forEach { repository.insertCurrency(it) }
        data.taxes.forEach { repository.insertTax(it) }
        data.settings?.let { repository.insertSettings(it) }

        return data
    }
}
