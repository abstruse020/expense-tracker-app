package com.example.expensetrackerv2.data
import android.content.Context
import android.net.Uri
import androidx.core.content.edit

object CsvUriStore {
    private const val PREFS = "csv_prefs"
    private const val KEY_URI = "expenses_csv_uri"

    fun save(context: Context, uri: Uri) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
            putString(KEY_URI, uri.toString())
        }
    }

    fun get(context: Context): Uri? {
        val str = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_URI, null)
        return str?.let { Uri.parse(it) }
    }
}