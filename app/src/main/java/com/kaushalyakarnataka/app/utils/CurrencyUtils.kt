package com.kaushalyakarnataka.app.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {

    /**
     * Formats an integer amount to Indian Rupee representation.
     * E.g., 450 -> ₹450
     * 1500 -> ₹1,500
     */
    fun formatRupees(amount: Int): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        format.maximumFractionDigits = 0
        return format.format(amount)
    }

    /**
     * Formats a double amount to Indian Rupee representation.
     */
    fun formatRupees(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        format.maximumFractionDigits = 2
        return format.format(amount)
    }
}
