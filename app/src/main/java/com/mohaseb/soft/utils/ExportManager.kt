package com.mohaseb.soft.utils

import android.content.Context
import com.mohaseb.soft.data.entity.Account
import com.mohaseb.soft.data.entity.Customer
import com.mohaseb.soft.data.entity.Item
import com.mohaseb.soft.data.entity.Transaction
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportManager(private val context: Context) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    fun exportToCSV(transactions: List<Transaction>, fileName: String): String {
        val exportDir = File(context.getExternalFilesDir(null), Constants.EXPORT_FOLDER)
        if (!exportDir.exists()) exportDir.mkdirs()

        val file = File(exportDir, "$fileName.csv")
        val sb = StringBuilder()
        sb.append('﻿')
        sb.append("التاريخ,النوع,الحساب,الكمية,السعر,الإجمالي,ملاحظات\n")
        transactions.forEach { t ->
            sb.append("${dateFormat.format(Date(t.date))},${t.type},${t.accountId},${t.quantity},${t.price},${t.amount},${t.notes}\n")
        }
        file.writeText(sb.toString())
        return file.absolutePath
    }

    // A real PDF would use a library such as iText; this writes a plain-text report instead.
    fun exportToPDF(transactions: List<Transaction>, fileName: String): String {
        val exportDir = File(context.getExternalFilesDir(null), Constants.EXPORT_FOLDER)
        if (!exportDir.exists()) exportDir.mkdirs()

        val file = File(exportDir, "$fileName.txt")
        val sb = StringBuilder()
        sb.append("تقرير الحركات\n")
        sb.append("================\n\n")
        transactions.forEach { t ->
            sb.append("${dateFormat.format(Date(t.date))} | ${t.type} | ${t.amount}\n")
        }
        file.writeText(sb.toString())
        return file.absolutePath
    }

    // Distinct from exportToCSV/exportToPDF above (transactions only, for the Reports screen):
    // this dumps every entity table to one CSV, for the Settings screen's full-dataset export.
    fun exportFullDataset(
        accounts: List<Account>,
        items: List<Item>,
        customers: List<Customer>,
        transactions: List<Transaction>,
        fileName: String
    ): String {
        val exportDir = File(context.getExternalFilesDir(null), Constants.EXPORT_FOLDER)
        if (!exportDir.exists()) exportDir.mkdirs()

        val file = File(exportDir, "$fileName.csv")
        val sb = StringBuilder()
        sb.append('﻿')

        sb.append("الحسابات\nالاسم,النوع,الرصيد,ملاحظات\n")
        accounts.forEach { a -> sb.append("${a.name},${a.type},${a.balance},${a.notes}\n") }

        sb.append("\nالأصناف\nالاسم,الكود,الوحدة,سعر الشراء,سعر البيع,الكمية,الحد الأدنى\n")
        items.forEach { i ->
            sb.append("${i.name},${i.code},${i.unit},${i.purchasePrice},${i.salePrice},${i.quantity},${i.minQuantity}\n")
        }

        sb.append("\nالعملاء والموردين\nالاسم,النوع,الهاتف,الرصيد\n")
        customers.forEach { c -> sb.append("${c.name},${c.type},${c.phone},${c.balance}\n") }

        sb.append("\nالحركات\nالتاريخ,النوع,الحساب,الكمية,السعر,الإجمالي,ملاحظات\n")
        transactions.forEach { t ->
            sb.append("${dateFormat.format(Date(t.date))},${t.type},${t.accountId},${t.quantity},${t.price},${t.amount},${t.notes}\n")
        }

        file.writeText(sb.toString())
        return file.absolutePath
    }
}
