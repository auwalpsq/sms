package com.sms.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ApplicationNumberGenerator {
    suspend fun generate(latestNumberForToday: String?): String {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val prefix = "APP-$today-"
        val next = if (latestNumberForToday != null && latestNumberForToday.startsWith(prefix)) {
            val lastSuffix = latestNumberForToday.removePrefix(prefix).toIntOrNull() ?: 0
            (lastSuffix + 1).toString().padStart(4, '0')
        } else {
            "0001"
        }
        return "$prefix$next"
    }
}