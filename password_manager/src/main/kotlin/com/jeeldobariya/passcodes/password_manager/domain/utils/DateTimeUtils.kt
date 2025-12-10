package com.jeeldobariya.passcodes.password_manager.domain.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class DateTimeUtils {
    companion object {
        /** Returns current date-time in `yyyy-MM-dd HH:mm:ss` format */
        fun getCurrDateTime(): String {
            return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date())
        }

        /** Parse a String into a Date with a given pattern */
        fun parseDate(dateStr: String, pattern: String = "yyyy-MM-dd HH:mm:ss"): Date? {
            return try {
                SimpleDateFormat(pattern, Locale.getDefault()).parse(dateStr)
            } catch (e: Exception) {
                null
            }
        }

        fun getRelativeDays(dateStr: String, pattern: String = "yyyy-MM-dd"): String {
            return try {
                val parsedDate = parseDate(dateStr, pattern) ?: return "Invalid date"

                val now = System.currentTimeMillis()
                val diff = parsedDate.time - now
                val days = TimeUnit.MILLISECONDS.toDays(diff)

                when {
                    days == 0L -> "today"
                    days > 0L -> "in $days day${if (days > 1) "s" else ""}"
                    else -> "${-days} day${if (days < -1) "s" else ""} ago"
                }
            } catch (e: Exception) {
                "Invalid date format"
            }
        }
    }
}
