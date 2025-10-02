package com.example.mnotepad.helpers

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateTimeHelper {
    fun getFormatedDate(date: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
        return sdf.format(Date(date))
    }

    fun getCurrentTime(): Long {
        return System.currentTimeMillis()
    }
}
