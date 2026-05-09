package com.example.expensetrackerv2.model

data class Category(
    val name: String,
    val subCategories: List<String>
)