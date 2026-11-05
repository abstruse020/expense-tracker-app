package com.example.expensetrackerv2

import com.example.expensetrackerv2.data.ExpenseOptionsStore
import com.example.expensetrackerv2.ui.overview.formatRupees
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IndianFormatTest {
    private val r = "₹"

    @Test fun smallAmountsHaveNoSeparator() {
        assertEquals("${r}0", formatRupees(0.0))
        assertEquals("${r}640", formatRupees(640.0))
        assertEquals("${r}999", formatRupees(999.0))
    }

    @Test fun groupsLastThreeThenPairs() {
        assertEquals("${r}1,000", formatRupees(1000.0))
        assertEquals("${r}13,010", formatRupees(13010.0))
        assertEquals("${r}1,00,000", formatRupees(100000.0))
        assertEquals("${r}1,23,456", formatRupees(123456.0))
        assertEquals("${r}12,34,567", formatRupees(1234567.0))
        assertEquals("${r}1,23,45,678", formatRupees(12345678.0))
    }

    @Test fun decimalsOnlyWhenNeeded() {
        assertEquals("${r}13,010.50", formatRupees(13010.5))
        assertEquals("${r}1,23,456.75", formatRupees(123456.75))
        assertEquals("${r}5,000", formatRupees(5000.001))
        assertEquals("${r}0.50", formatRupees(0.5))
    }

    @Test fun negativeKeepsSign() {
        assertEquals("-${r}1,500", formatRupees(-1500.0))
    }

    @Test fun savingAndSavingsAreRecognisedIgnoringCase() {
        listOf("Saving", "saving", "SAVING", "Savings", "savings", " Savings ").forEach {
            assertTrue(it, ExpenseOptionsStore.looksLikeSaving(it))
        }
        listOf("Essential", "Savings Account", "Casual").forEach {
            assertFalse(it, ExpenseOptionsStore.looksLikeSaving(it))
        }
    }
}
