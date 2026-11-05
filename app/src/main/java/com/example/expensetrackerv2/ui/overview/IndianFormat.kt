package com.example.expensetrackerv2.ui.overview

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Rupee amount with Indian digit grouping (last three digits, then pairs): 1,23,456.50.
 * Whole amounts have no decimals; otherwise two. Negative amounts keep their sign.
 */
fun formatRupees(value: Double): String {
    val amount = BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP)
    val negative = amount.signum() < 0
    val plain = amount.abs().toPlainString() // e.g. "123456.50"
    val whole = plain.substringBefore('.')
    val fraction = plain.substringAfter('.')

    val grouped = if (whole.length <= 3) {
        whole
    } else {
        val head = whole.dropLast(3).reversed().chunked(2).joinToString(",").reversed()
        "$head,${whole.takeLast(3)}"
    }
    val text = if (fraction == "00") grouped else "$grouped.$fraction"
    return (if (negative) "-" else "") + "\u20B9" + text
}
