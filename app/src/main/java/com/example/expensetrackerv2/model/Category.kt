package com.example.expensetrackerv2.model

/**
 * [isSaving] marks a category whose spending is money put aside (SIP, FD, ...). The Overview
 * totals it separately from the other categories, which count as expenses.
 */
data class Category(
    val name: String,
    val subCategories: List<String>,
    val isSaving: Boolean = false
)
