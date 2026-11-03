package com.example.expensetrackerv2

import com.example.expensetrackerv2.model.Expense
import com.example.expensetrackerv2.ui.expenses.DefaultSearchFields
import com.example.expensetrackerv2.ui.expenses.SearchField
import com.example.expensetrackerv2.ui.expenses.amountText
import com.example.expensetrackerv2.ui.expenses.searchExpenses
import org.junit.Assert.assertEquals
import org.junit.Test

class ExpenseSearchTest {
    private fun e(cat: String, sub: String, desc: String, amount: Float, mode: String = "UPI") =
        Expense(cat, sub, desc, amount, mode, "2026-01-01", "2026-01-01T10:00")

    private val groceries = e("Essential", "Grocery", "Weekly vegetables", 250f)
    private val rent = e("Essential", "Rent", "January rent", 12500.5f, "Cash")
    private val sip = e("Saving", "SIP", "Index fund", 5000f)
    private val all = listOf(groceries, rent, sip)

    @Test fun blankQueryReturnsEverything() =
        assertEquals(all, searchExpenses(all, "   ", DefaultSearchFields))

    @Test fun defaultsSearchOnlyDescription() {
        assertEquals(listOf(rent), searchExpenses(all, "rent", DefaultSearchFields))
        // "grocery" is a sub-category, not in any description
        assertEquals(emptyList<Expense>(), searchExpenses(all, "grocery", DefaultSearchFields))
    }

    @Test fun isCaseInsensitive() =
        assertEquals(listOf(groceries), searchExpenses(all, "VEGETABLES", DefaultSearchFields))

    @Test fun selectedFieldsAreSearched() {
        assertEquals(listOf(groceries), searchExpenses(all, "grocery", setOf(SearchField.SUB_CATEGORY)))
        assertEquals(listOf(groceries, rent), searchExpenses(all, "essential", setOf(SearchField.CATEGORY)))
    }

    @Test fun allWordsMustMatchSomeSelectedField() {
        val fields = setOf(SearchField.SUB_CATEGORY, SearchField.AMOUNT)
        assertEquals(listOf(groceries), searchExpenses(all, "grocery 250", fields))
        assertEquals(emptyList<Expense>(), searchExpenses(all, "grocery 5000", fields))
    }

    @Test fun amountIgnoresTrailingZeroDecimal() {
        assertEquals("250", amountText(250f))
        assertEquals("12500.5", amountText(12500.5f))
        // substring match: "250" also hits 12500.5
        assertEquals(listOf(groceries, rent), searchExpenses(all, "250", setOf(SearchField.AMOUNT)))
        assertEquals(listOf(sip), searchExpenses(all, "5000", setOf(SearchField.AMOUNT)))
    }

    @Test fun paymentModeIsSearchableWhenSelected() =
        assertEquals(listOf(rent), searchExpenses(all, "cash", setOf(SearchField.PAYMENT_MODE)))
}
