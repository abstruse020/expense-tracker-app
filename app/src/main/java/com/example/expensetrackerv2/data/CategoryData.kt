package com.example.expensetrackerv2.data
import com.example.expensetrackerv2.model.Category

object CategoryData {
    val categories = listOf(
        Category("Essential", listOf(
            "Grocery", "Utility", "Rent",
            "Transportation", "Cook", "Others")),
        Category("Casual", listOf(
            "Outing", "Travel", "Others")),
        Category("Saving", listOf(
            "SIP", "Mutual Funds", "PPF",
            "FD", "Others")),
        Category("Family", listOf(
            "Baba Ma", "Ghar", "Us",
            "Others"))
    )
}