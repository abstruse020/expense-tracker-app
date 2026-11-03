package com.example.expensetrackerv2.ui.expenses

import com.example.expensetrackerv2.model.Expense
import java.math.BigDecimal

/** Expense fields the user can include in a search. */
enum class SearchField(val label: String, val read: (Expense) -> String) {
    CATEGORY("Category", { it.category }),
    SUB_CATEGORY("Sub category", { it.subCategory }),
    DESCRIPTION("Description", { it.description }),
    AMOUNT("Amount", { amountText(it.amount) }),
    PAYMENT_MODE("Payment mode", { it.paymentMode }),
    DATE("Date", { it.date }),
}

val DefaultSearchFields = setOf(SearchField.DESCRIPTION)

/** Amount as plain text without a trailing ".0", so "250" matches 250.0. */
fun amountText(amount: Float): String =
    try {
        BigDecimal(amount.toString()).stripTrailingZeros().toPlainString()
    } catch (e: NumberFormatException) {
        amount.toString()
    }

/**
 * Case-insensitive search. Every whitespace-separated word in [query] must appear
 * in at least one of the selected [fields]. A blank query returns [expenses] unchanged.
 */
fun searchExpenses(
    expenses: List<Expense>,
    query: String,
    fields: Set<SearchField>
): List<Expense> {
    val terms = query.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (terms.isEmpty() || fields.isEmpty()) return expenses
    return expenses.filter { expense ->
        val values = fields.map { it.read(expense) }
        terms.all { term -> values.any { it.contains(term, ignoreCase = true) } }
    }
}
