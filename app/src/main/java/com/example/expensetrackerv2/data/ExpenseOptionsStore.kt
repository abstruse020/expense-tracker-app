package com.example.expensetrackerv2.data

import android.content.Context
import androidx.core.content.edit
import com.example.expensetrackerv2.model.Category
import org.json.JSONArray
import org.json.JSONObject

/**
 * User-editable lists of categories (with sub-categories) and payment modes,
 * persisted in SharedPreferences as JSON. Falls back to the built-in defaults
 * on first launch or if the stored data can't be read. Lists returned are never empty.
 */
object ExpenseOptionsStore {
    private const val PREFS = "expense_options"
    private const val KEY_CATEGORIES = "categories_json"
    private const val KEY_PAYMENT_MODES = "payment_modes_json"

    val defaultPaymentModes = listOf("Cash", "UPI", "SBI Elite", "SBI Cashback", "Scapia", "Splitwise")

    fun getCategories(context: Context): List<Category> {
        val json = prefs(context).getString(KEY_CATEGORIES, null)
            ?: return CategoryData.categories
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                val subs = obj.getJSONArray("subCategories")
                Category(obj.getString("name"), (0 until subs.length()).map { subs.getString(it) })
            }.filter { it.subCategories.isNotEmpty() }
                .ifEmpty { CategoryData.categories }
        } catch (e: Exception) {
            CategoryData.categories
        }
    }

    fun saveCategories(context: Context, categories: List<Category>) {
        val arr = JSONArray()
        categories.forEach { c ->
            arr.put(
                JSONObject()
                    .put("name", c.name)
                    .put("subCategories", JSONArray(c.subCategories))
            )
        }
        prefs(context).edit { putString(KEY_CATEGORIES, arr.toString()) }
    }

    fun getPaymentModes(context: Context): List<String> {
        val json = prefs(context).getString(KEY_PAYMENT_MODES, null) ?: return defaultPaymentModes
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getString(it) }.ifEmpty { defaultPaymentModes }
        } catch (e: Exception) {
            defaultPaymentModes
        }
    }

    fun savePaymentModes(context: Context, modes: List<String>) {
        prefs(context).edit { putString(KEY_PAYMENT_MODES, JSONArray(modes).toString()) }
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
