package com.example.expensetrackerv2.model

data class Expense(
    val category: String,
    val subCategory: String,
    val description: String,
    val amount: Float,
    val paymentMode: String,
    val date: String,
    val dateAdded: String
    )