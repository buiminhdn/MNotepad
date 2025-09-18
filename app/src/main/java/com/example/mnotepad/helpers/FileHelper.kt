package com.example.mnotepad.helpers

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File

object FileHelper {

    // Đọc file txt từ Uri (file user chọn)
    fun readTextFromUri(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() } ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    // Ghi nội dung vào file txt (ghi đè)
    fun writeTextToUri(context: Context, uri: Uri, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.bufferedWriter().use {
                it?.write(content)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Xuất file txt ra bộ nhớ ngoài (Downloads dir)
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

}