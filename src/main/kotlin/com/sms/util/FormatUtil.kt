package com.sms.util

import java.text.NumberFormat
import java.util.*

object FormatUtil {

    private val nigeriaCurrencyFormat: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale("en", "NG"))

    fun formatCurrency(amount: Double?): String {
        return if (amount == null) "" else nigeriaCurrencyFormat.format(amount)
    }
}