package com.example.mnotepad.helpers

import android.content.Context
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
import java.io.FileOutputStream

object PrintHelper {
    fun print(context: Context, title: String, content: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = title

        val adapter = object : PrintDocumentAdapter() {
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
                val info = PrintDocumentInfo.Builder("$jobName.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .build()
                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                try {
                    destination?.fileDescriptor?.let { fd ->
                        PdfDocument().apply {
                            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
                            val page = startPage(pageInfo)

                            val canvas = page.canvas
                            val paint = Paint()

                            var y = 50f
                            paint.textSize = 20f
                            canvas.drawText(title, 30f, y, paint)

                            paint.textSize = 14f
                            val lines = content.split("\n")
                            for (line in lines) {
                                y += 25f
                                canvas.drawText(line, 30f, y, paint)
                            }

                            finishPage(page)
                            writeTo(FileOutputStream(fd))
                            close()
                        }
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                }
            }
        }

        printManager.print(jobName, adapter, null)
    }
}
