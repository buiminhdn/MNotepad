package com.example.mnotepad.helpers

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import java.io.*

object FileSAFHelper {
    fun importTxt(context: Context, uri: Uri): Pair<String, String>? {
        val contentResolver: ContentResolver = context.contentResolver

        // Lấy tên file (title)
        var fileName = "Untitled"
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                fileName = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            }
        }

        // Bỏ extension (vd: note.txt -> note)
        val title = fileName.substringBeforeLast(".")

        // Đọc nội dung
        val stringBuilder = StringBuilder()
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line).append("\n")
                    line = reader.readLine()
                }
            }
        }

        return Pair(title, stringBuilder.toString())
    }

    fun createTxtFileIntent(fileName: String, initialUri: Uri? = null): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, if (fileName.isNotBlank()) "$fileName.txt" else "note.txt")
            initialUri?.let {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, it)
            }
        }
    }

    fun exportTxt(context: Context, uri: Uri, content: String) {
        try {
            context.contentResolver.openFileDescriptor(uri, "w")?.use { pfd ->
                FileOutputStream(pfd.fileDescriptor).use { fos ->
                    fos.write(content.toByteArray())
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun createPdfFileIntent(fileName: String, initialUri: Uri? = null): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, if (fileName.isNotBlank()) "$fileName.pdf" else "note.pdf")
            initialUri?.let {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, it)
            }
        }
    }

    fun exportPdf(context: Context, uri: Uri, title: String, content: String) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)

        val canvas = page.canvas
        val paint = android.graphics.Paint()

        var y = 50f
        paint.textSize = 20f
        canvas.drawText(title, 30f, y, paint)

        paint.textSize = 14f
        val lines = content.split("\n")
        for (line in lines) {
            y += 25f
            canvas.drawText(line, 30f, y, paint)
        }

        pdfDocument.finishPage(page)

        try {
            context.contentResolver.openFileDescriptor(uri, "w")?.use { pfd ->
                FileOutputStream(pfd.fileDescriptor).use { fos ->
                    pdfDocument.writeTo(fos)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
    }

    fun createDirectoryIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
        }
    }

    fun exportSelectedNotesToTxt(context: Context, treeUri: Uri, notes: List<Pair<String, String>>) {
        val contentResolver = context.contentResolver
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        val dirUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)

        for ((title, content) in notes) {
            try {
                // Tạo file name hợp lệ (tránh ký tự đặc biệt)
                val safeTitle = title.replace("[^a-zA-Z0-9._-]".toRegex(), "_")

                // Tạo file txt trong thư mục đã chọn
                val newFileUri = DocumentsContract.createDocument(
                    contentResolver,
                    dirUri,
                    "text/plain",
                    "$safeTitle.txt"
                )

                if (newFileUri != null) {
                    contentResolver.openFileDescriptor(newFileUri, "w")?.use { pfd ->
                        FileOutputStream(pfd.fileDescriptor).use { fos ->
                            fos.write(content.toByteArray())
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}