package com.example.mnotepad.helpers

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateTimeHelper {
    companion object {
        fun getFormatedDate(date: Long): String {
            val date = Date(date)
            return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)
        }
        fun getCurrentTime() : Long {
            return System.currentTimeMillis()
        }
    }
}