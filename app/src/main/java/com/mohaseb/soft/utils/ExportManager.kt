package com.mohaseb.soft.utils

import android.content.Context
import com.mohaseb.soft.R
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
        sb.append("\uFEFF")
        sb.append("التاريخ,النوع,الحساب,الكمية,السعر,الإجمالي,ملاحظات\n")
        transactions.forEach { t ->
            sb.append("${dateFormat.format(Date(t.date))},${t.type},${t.accountId},${t.quantity},${t.price},${t.amount},${t.notes}\n")
        }
        file.writeText(sb.toString())
        return file.absolutePath
    }

    fun exportToPDF(transactions: List<Transaction>, fileName: String): String {
        val document = PdfPrinter.buildReportPdf(
            title = "تقرير الحركات",
            transactions = transactions,
            typeLabel = ::transactionTypeLabel
        )
        return PdfPrinter.saveToFile(document, context, fileName)
    }

    private fun transactionTypeLabel(t: Transaction): String = when (t.type) {
        Constants.TYPE_CASH_IN -> context.getString(R.string.cash_in)
        Constants.TYPE_CASH_OUT -> context.getString(R.string.cash_out)
        Constants.TYPE_SALE ->
            context.getString(if (t.isCredit) R.string.credit_sale else R.string.cash_sale)
        Constants.TYPE_PURCHASE ->
            context.getString(if (t.isCredit) R.string.credit_purchase else R.string.cash_purchase)
        else -> t.type
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
        sb.append("\uFEFF")

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
