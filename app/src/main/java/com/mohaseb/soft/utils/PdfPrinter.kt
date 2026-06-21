package com.mohaseb.soft.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import com.mohaseb.soft.data.entity.Transaction
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Draws real PDF content with android.graphics.pdf.PdfDocument (no third-party library needed),
// and can either save it to a file or hand it to the system Print framework, whose dialog also
// offers a built-in "Save as PDF" destination.
object PdfPrinter {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    data class InvoiceLineData(val name: String, val quantity: Double, val price: Double) {
        val total: Double get() = quantity * price
    }

    private fun titlePaint() = Paint().apply {
        color = Color.BLACK; textSize = 18f; isFakeBoldText = true; textAlign = Paint.Align.RIGHT
    }

    private fun headerPaint() = Paint().apply {
        color = Color.BLACK; textSize = 11f; isFakeBoldText = true; textAlign = Paint.Align.RIGHT
    }

    private fun labelPaint() = Paint().apply {
        color = Color.DKGRAY; textSize = 11f; textAlign = Paint.Align.RIGHT
    }

    private fun cellPaint() = Paint().apply {
        color = Color.BLACK; textSize = 11f; textAlign = Paint.Align.RIGHT
    }

    private fun linePaint() = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }

    private fun fitText(paint: Paint, text: String, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        val count = paint.breakText(text, true, maxWidth, null)
        return if (count <= 1) text.take(1) else text.substring(0, count - 1) + "…"
    }

    fun buildInvoicePdf(
        companyName: String,
        invoiceTitle: String,
        invoiceDate: Date,
        partyLabel: String,
        partyName: String,
        paymentTypeLabel: String,
        notes: String,
        lines: List<InvoiceLineData>,
        total: Double
    ): PdfDocument {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        var page = document.startPage(pageInfo)
        var canvas: Canvas = page.canvas
        var y: Float
        val right = PAGE_WIDTH - MARGIN
        val nameRight = right
        val qtyRight = right - 230f
        val priceRight = right - 320f
        val totalRight = right - 410f
        val nameMaxWidth = right - qtyRight - 10f

        fun drawTableHeader(): Float {
            var cursor = y
            canvas.drawLine(MARGIN, cursor, right, cursor, linePaint())
            cursor += 16f
            canvas.drawText("الصنف", nameRight, cursor, headerPaint())
            canvas.drawText("الكمية", qtyRight, cursor, headerPaint())
            canvas.drawText("السعر", priceRight, cursor, headerPaint())
            canvas.drawText("الإجمالي", totalRight, cursor, headerPaint())
            cursor += 10f
            canvas.drawLine(MARGIN, cursor, right, cursor, linePaint())
            return cursor + 18f
        }

        y = MARGIN + 20f
        canvas.drawText(companyName, right, y, titlePaint())
        y += 26f
        canvas.drawText(invoiceTitle, right, y, headerPaint())
        y += 20f
        canvas.drawText("التاريخ: ${dateFormat.format(invoiceDate)}", right, y, labelPaint())
        y += 18f
        canvas.drawText("$partyLabel: $partyName", right, y, labelPaint())
        y += 18f
        canvas.drawText("نوع الدفع: $paymentTypeLabel", right, y, labelPaint())
        y += 24f
        y = drawTableHeader()

        val rowHeight = 22f
        for (line in lines) {
            if (y > PAGE_HEIGHT - MARGIN - rowHeight - 60f) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = MARGIN + 20f
                y = drawTableHeader()
            }
            val paint = cellPaint()
            canvas.drawText(fitText(paint, line.name, nameMaxWidth), nameRight, y, paint)
            canvas.drawText(String.format(Locale.US, "%.2f", line.quantity), qtyRight, y, paint)
            canvas.drawText(String.format(Locale.US, "%.2f", line.price), priceRight, y, paint)
            canvas.drawText(String.format(Locale.US, "%.2f", line.total), totalRight, y, paint)
            y += rowHeight
        }

        y += 8f
        canvas.drawLine(MARGIN, y, right, y, linePaint())
        y += 22f
        canvas.drawText("الإجمالي الكلي: ${String.format(Locale.US, "%.2f", total)}", right, y, titlePaint())

        if (notes.isNotBlank()) {
            y += 28f
            canvas.drawText("ملاحظات: $notes", right, y, labelPaint())
        }

        document.finishPage(page)
        return document
    }

    fun buildReportPdf(
        title: String,
        transactions: List<Transaction>,
        typeLabel: (Transaction) -> String
    ): PdfDocument {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        var page = document.startPage(pageInfo)
        var canvas: Canvas = page.canvas
        var y: Float
        val right = PAGE_WIDTH - MARGIN
        val dateRight = right
        val typeRight = right - 150f
        val qtyRight = right - 260f
        val priceRight = right - 350f
        val totalRight = right - 440f
        val typeMaxWidth = dateRight - typeRight - 100f

        fun drawTableHeader(): Float {
            var cursor = y
            canvas.drawLine(MARGIN, cursor, right, cursor, linePaint())
            cursor += 16f
            canvas.drawText("التاريخ", dateRight, cursor, headerPaint())
            canvas.drawText("النوع", typeRight, cursor, headerPaint())
            canvas.drawText("الكمية", qtyRight, cursor, headerPaint())
            canvas.drawText("السعر", priceRight, cursor, headerPaint())
            canvas.drawText("الإجمالي", totalRight, cursor, headerPaint())
            cursor += 10f
            canvas.drawLine(MARGIN, cursor, right, cursor, linePaint())
            return cursor + 18f
        }

        y = MARGIN + 20f
        canvas.drawText(title, right, y, titlePaint())
        y += 30f
        y = drawTableHeader()

        val rowHeight = 20f
        for (t in transactions) {
            if (y > PAGE_HEIGHT - MARGIN - rowHeight) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = MARGIN + 20f
                y = drawTableHeader()
            }
            val paint = cellPaint()
            canvas.drawText(dateFormat.format(Date(t.date)), dateRight, y, paint)
            canvas.drawText(fitText(paint, typeLabel(t), typeMaxWidth), typeRight, y, paint)
            canvas.drawText(String.format(Locale.US, "%.2f", t.quantity), qtyRight, y, paint)
            canvas.drawText(String.format(Locale.US, "%.2f", t.price), priceRight, y, paint)
            canvas.drawText(String.format(Locale.US, "%.2f", t.amount), totalRight, y, paint)
            y += rowHeight
        }

        document.finishPage(page)
        return document
    }

    fun saveToFile(document: PdfDocument, context: Context, fileName: String): String {
        val exportDir = File(context.getExternalFilesDir(null), Constants.EXPORT_FOLDER)
        if (!exportDir.exists()) exportDir.mkdirs()
        val file = File(exportDir, "$fileName.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file.absolutePath
    }

    fun print(context: Context, jobName: String, document: PdfDocument) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        printManager.print(jobName, PdfPrintAdapter(document, jobName), PrintAttributes.Builder().build())
    }

    private class PdfPrintAdapter(
        private val document: PdfDocument,
        private val docName: String
    ) : PrintDocumentAdapter() {

        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes?,
            cancellationSignal: CancellationSignal?,
            callback: LayoutResultCallback?,
            extras: Bundle?
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback?.onLayoutCancelled()
                return
            }
            val info = PrintDocumentInfo.Builder("$docName.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(document.pages.size)
                .build()
            callback?.onLayoutFinished(info, true)
        }

        override fun onWrite(
            pages: Array<PageRange>?,
            destination: ParcelFileDescriptor?,
            cancellationSignal: CancellationSignal?,
            callback: WriteResultCallback?
        ) {
            try {
                FileOutputStream(destination?.fileDescriptor).use { out -> document.writeTo(out) }
                callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            } catch (e: IOException) {
                callback?.onWriteFailed(e.message)
            }
        }

        override fun onFinish() {
            document.close()
        }
    }
}
