package com.example.expensetrackerv2

import com.example.expensetrackerv2.model.Expense
import com.example.expensetrackerv2.ui.overview.buildCategorySpending
import com.example.expensetrackerv2.ui.overview.totalSpent
import org.junit.Assert.assertEquals
import org.junit.Test

class OverviewDataTest {
    private fun e(cat: String, sub: String, amount: Float) =
        Expense(cat, sub, "", amount, "UPI", "2026-11-05", "id-$cat-$sub-$amount")

    private val expenses = listOf(
        e("Food", "Groceries", 300f),
        e("Food", "Dining out", 500f),
        e("Food", "Groceries", 100f),
        e("Transport", "Cab", 900f),
        e("Bills", "Electricity", 50f),
    )

    @Test fun totalIsSumOfAllExpenses() {
        assertEquals(1850.0, totalSpent(expenses), 0.001)
        assertEquals(0.0, totalSpent(emptyList()), 0.001)
    }

    @Test fun categoriesAreLargestFirstWithTotals() {
        val result = buildCategorySpending(expenses)
        assertEquals(listOf("Food", "Transport", "Bills"), result.map { it.name })
        assertEquals(listOf(900.0, 900.0, 50.0), result.map { it.total })
    }

    @Test fun subCategoriesAreGroupedAndLargestFirst() {
        val food = buildCategorySpending(expenses).first { it.name == "Food" }
        assertEquals(listOf("Dining out", "Groceries"), food.subCategories.map { it.name })
        assertEquals(listOf(500.0, 400.0), food.subCategories.map { it.total })
    }

    @Test fun noExpensesGivesNoCategories() {
        assertEquals(emptyList<Any>(), buildCategorySpending(emptyList()))
    }
}

class OverviewSummaryTest {
    private fun e(cat: String, sub: String, amount: Float) =
        Expense(cat, sub, "", amount, "UPI", "2026-11-05", "id-$cat-$sub-$amount")

    private val expenses = listOf(
        e("Food", "Groceries", 300f),
        e("Transport", "Cab", 200f),
        e("Saving", "SIP", 1000f),
        e("Saving", "FD", 500f),
        e("Retirement", "NPS", 250f),
    )

    @Test fun savingCategoriesAreTotalledSeparately() {
        val s = com.example.expensetrackerv2.ui.overview.buildOverviewSummary(expenses, setOf("Saving", "Retirement"))
        assertEquals(500.0, s.expenseTotal, 0.001)
        assertEquals(1750.0, s.savingTotal, 0.001)
        assertEquals(2250.0, s.grandTotal, 0.001)
        assertEquals(listOf("Food", "Transport"), s.expenseCategories.map { it.name })
        assertEquals(listOf("Saving", "Retirement"), s.savingCategories.map { it.name })
    }

    @Test fun matchingIgnoresCase() {
        val s = com.example.expensetrackerv2.ui.overview.buildOverviewSummary(expenses, setOf("saving"))
        assertEquals(1500.0, s.savingTotal, 0.001)
    }

    @Test fun withNoSavingCategoriesEverythingIsAnExpense() {
        val s = com.example.expensetrackerv2.ui.overview.buildOverviewSummary(expenses, emptySet())
        assertEquals(2250.0, s.expenseTotal, 0.001)
        assertEquals(0.0, s.savingTotal, 0.001)
        assertEquals(emptyList<Any>(), s.savingCategories)
    }
}
