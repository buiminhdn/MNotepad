package com.example.mnotepad.helpers

import android.content.Context
import android.os.Environment
import java.io.File

fun exportToTxtFile(context: Context, fileName: String, content: String) {
    try {
         val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
             "$fileName.txt"
         )
        file.writeText(content)
    } catch (e: Exception) {
    }
}
