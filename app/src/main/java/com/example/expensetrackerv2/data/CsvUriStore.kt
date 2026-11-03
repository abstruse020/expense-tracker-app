package com.example.expensetrackerv2.data
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
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

    /** Display name of the currently selected file, or null if none is set or it can't be read. */
    fun getFileName(context: Context): String? {
        val uri = get(context) ?: return null
        return try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
        } catch (e: Exception) {
            null
        } ?: uri.lastPathSegment
    }
}
