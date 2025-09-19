package com.example.mnotepad.helpers

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileHelper {

    fun readTextFromUri(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() } ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun exportToTxtFile(context: Context, title: String, content: String) {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()

            val file = File(downloadsDir, "$title.txt")
            file.writeText(content)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun createPDF(context: Context?, textTitle: String?, textContent: String?) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        paint.textSize = 12f
        canvas.drawText(textContent!!, 10f, 10f, paint)
        pdfDocument.finishPage(page)

        // Save PDF to external storage
        val filePath = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            textTitle ?: "Notepad",
        )
        try {
            pdfDocument.writeTo(FileOutputStream(filePath))
            Toast.makeText(context, "PDF saved to " + filePath.absolutePath, Toast.LENGTH_SHORT)
                .show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to save PDF: " + e.message, Toast.LENGTH_SHORT).show()
        }
        pdfDocument.close()
    }

}