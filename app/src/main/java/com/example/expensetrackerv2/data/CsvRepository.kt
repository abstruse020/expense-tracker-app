package com.example.expensetrackerv2.data

import android.content.ContentResolver
import android.net.Uri
import java.io.OutputStreamWriter

class CsvRepository(
    private val resolver: ContentResolver
) {
    fun read(uri: Uri): List<List<String>> {
        return try {
            resolver.openInputStream(uri)?.use { input ->
                input.bufferedReader().readLines().map { it.split(",") }
            } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun overwrite(uri: Uri, rows: List<List<String>>): Boolean {
        return try {
            val outputStream = resolver.openOutputStream(uri, "wt") ?: return false
            outputStream.use { out ->
                OutputStreamWriter(out).use { writer ->
                    rows.forEach { writer.append(serializeRow(it)) }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun append(uri: Uri, row: List<String>): Boolean {
        return try {
            val outputStream = resolver.openOutputStream(uri, "wa") ?: return false
            outputStream.use { out ->
                OutputStreamWriter(out).use { writer ->
                    writer.append(serializeRow(row))
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteWhere(uri: Uri, delIdx: Int, deleteId: String): Boolean {
        val rows = read(uri)
        val remaining = rows.filter { it.getOrNull(delIdx) != deleteId }
        return overwrite(uri, remaining)
    }
}