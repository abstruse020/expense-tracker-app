package com.example.expensetrackerv2.data
import android.content.Context
import java.io.File
import java.io.FileWriter

import com.example.expensetrackerv2.model.Expense
import android.util.Log
import java.io.OutputStreamWriter
import java.time.LocalDateTime

class ExpenseStorage {
}

private const val HEADER = "Date-Added,Category,Sub-Category,Description,Amount,PaymentMode,Date"
private val HEADER_ROW = HEADER.split(",")

fun saveExpense(
    context: Context,
    category: String,
    subCategory: String,
    paymentMode: String,
    amount: String,
    description: String,
    date: String
): Boolean {
    val uri = CsvUriStore.get(context) ?: return false
    val repo = CsvRepository(context.contentResolver)

    val safeDescription = description.replace(",", " ")
    val amountClean = try {
        amount.toFloat().toString()
    } catch (e: Exception) {
        Log.d("My DEBUG", "Invalid amount: $amount", e)
        return false
    }
    val dateAdded = LocalDateTime.now().toString()

    val row = listOf(dateAdded, category, subCategory, safeDescription, amountClean, paymentMode, date)

    // If file is empty/new, write header first
    val existingRows = repo.read(uri)
    if (existingRows.isEmpty()) {
        repo.append(uri, HEADER_ROW)
    }

    return repo.append(uri, row)
}

fun readExpenses(context: Context): List<Expense> {
    val uri = CsvUriStore.get(context) ?: return emptyList()
    val repo = CsvRepository(context.contentResolver)

    val rows = repo.read(uri)
    if (rows.isEmpty()) return emptyList()

    val expenses = mutableListOf<Expense>()

    rows.drop(1).forEach { parts -> // skip header
        try {
            if (parts.size >= 7) {
                expenses.add(
                    Expense(
                        dateAdded = cleanStr(parts, 0),
                        category = cleanStr(parts, 1),
                        subCategory = cleanStr(parts, 2),
                        description = cleanStr(parts, 3),
                        amount = cleanStr(parts, 4).toFloat(),
                        paymentMode = cleanStr(parts, 5),
                        date = cleanStr(parts, 6),
                    )
                )
            }
        } catch (e: Exception) {
            Log.d("My DEBUG", "Cant parse row: $parts", e)
        }
    }

    return expenses
}

fun deleteExpense(context: Context, dateAdded: String): Boolean {
    val uri = CsvUriStore.get(context) ?: return false
    val repo = CsvRepository(context.contentResolver)

    val rows = repo.read(uri)
    if (rows.isEmpty()) return false

    val header = rows.firstOrNull() ?: HEADER_ROW
    val remaining = rows.drop(1).filter { it.getOrNull(0) != dateAdded }

    return repo.overwrite(uri, listOf(header) + remaining)
}

fun clearAllExpenses(context: Context): Boolean {
    val uri = CsvUriStore.get(context) ?: return false
    val repo = CsvRepository(context.contentResolver)
    return repo.overwrite(uri, listOf(HEADER_ROW))
}

fun saveExpenseOld(
    context: Context,
    category: String,
    subCategory: String,
    paymentMode: String,
    amount: String,
    description: String,
    date: String
): Boolean {
    val uri = CsvUriStore.get(context) ?: return false // no file picked yet

    return try {
        val resolver = context.contentResolver

        // Check if file is empty (i.e. needs a header row)
        val isNewFile = resolver.openFileDescriptor(uri, "r")?.use { pfd ->
            pfd.statSize == 0L
        } ?: true

        resolver.openOutputStream(uri, "wa")?.use { out ->
            OutputStreamWriter(out).use { writer ->
                if (isNewFile) {
                    writer.append("Date-Added,Category,Sub-Category,Description,Amount,PaymentMode,Date\n")
                }

                val safeDescription = description.replace(",", " ")
                val amountClean = amount.toFloat().toString()
                val dateAdded = LocalDateTime.now().toString()

                val row = serializeRow(
                    listOf(dateAdded, category, subCategory, safeDescription, amountClean, paymentMode, date)
                )
                Log.d("My DEBUG", row)
                writer.append(row)
            }
        } ?: return false

        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}



//fun saveExpense(
//    context: Context,
//    category: String,
//    subCategory: String,
//    paymentMode: String,
//    amount: String,
//    description: String,
//    date: String
//): Boolean {
//
//    val uri = CsvUriStore.get(context) ?: return false // no file picked yet
//
//    val file = File(context.filesDir, "expenses.csv")
//    val isNewFile = !file.exists()
////    val logStr: String = subCategory + date + description + amount + paymentMode + category
////    Log.d("In save expense", logStr)
//    return try {
//        FileWriter(file, true).use { writer ->
//            if (isNewFile) {
//                writer.append("Date-Added,Category,Sub-Category,Description,Amount,PaymentMode,Date\n")
//            }
//
//            val safeDescription = description.replace(",", " ")
//            val amountClean = amount.toFloat().toString()
//            val dateAdded = LocalDateTime.now().toString()
//
//            val row = serializeRow(listOf(dateAdded, category, subCategory, safeDescription, amountClean, paymentMode, date))
//            Log.d("My DEBUG", row)
//            writer.append(row)
//        }
//        true
//    } catch (e: Exception) {
//        e.printStackTrace()
//        false
//    }
//}
//
//fun serializeExpense(expense: Expense): String {
//    val description = expense.description.replace(",", " ")
//    val amountClean = expense.amount.toString()
//    val dateAdded = expense.dateAdded ?: ""
//
//    val output = serializeRow(listOf(
//        dateAdded,
//        expense.category,
//        expense.subCategory,
//        description,
//        amountClean,
//        expense.paymentMode,
//        expense.date
//    ))
//    return output
//}
fun serializeRow(row: List<String>): String {

    val description = row[3].replace(",", "")
    val output = "${escapeCsv(row[0])}," +
            "${escapeCsv(row[1])}," +
            "${escapeCsv(row[2])}," +
            "${escapeCsv(description)}," +
            "${escapeCsv(row[4])}," +
            "${escapeCsv(row[5])}," +
            "${escapeCsv(row[6])}\n"
    return output
}
fun escapeCsv(value: String): String {
    val escaped = value.replace("\"", "\"\"")
    return if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
        "\"$escaped\""
    } else {
        escaped
    }
}

fun readExpensesOld(context: Context): List<Expense> {
    val expenses = mutableListOf<Expense>()
    val uri = CsvUriStore.get(context) ?: return expenses

    val lines = try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            input.bufferedReader().readLines()
        } ?: return expenses
    } catch (e: Exception) {
        Log.d("My DEBUG", "Cant open file", e)
        return expenses
    }

    lines.drop(1).forEach { line -> // skip header
        val parts = line.split(",")
        try {
            Log.d("My Debug", line)
            if (parts.size >= 5) {
                expenses.add(
                    Expense(
                        dateAdded = cleanStr(parts, 0),
                        category = cleanStr(parts, 1),
                        subCategory = cleanStr(parts, 2),
                        description = cleanStr(parts, 3),
                        amount = cleanStr(parts, 4).toFloat(),
                        paymentMode = cleanStr(parts, 5),
                        date = cleanStr(parts, 6),
                    )
                )
            }
        } catch (e: Exception) {
            Log.d("My DEBUG", "Cant read file", e)
        }
    }

    return expenses
}

fun readExpensesOldest(context: Context): List<Expense> {
//    val expenses = mutableListOf<Expense>()
//
//    val uri = CsvUriStore.get(context) ?: return expenses // no file picked yet
//    val resolver = context.contentResolver
//    val fileExists = CsvUriStore.get(context) != null
//    if (!fileExists) return expenses


    val file = File(context.filesDir, "expenses.csv")

    if (!file.exists()) return emptyList()

    val expenses = mutableListOf<Expense>()
//    resolver.openInputStream(uri)?.bufferedReader()?.readLines().drop(1).forEach { line ->
    file.readLines().drop(1).forEach { line -> // skip header
        val parts = line.split(",")

        try {

            Log.d("My Debug", line)
            // val amt: Float = parts[3].replace("\"","").toFloat()
            if (parts.size >= 5) {
                expenses.add(
                    Expense(
                        dateAdded = cleanStr(parts,0),
                        category = cleanStr(parts, 1),
                        subCategory = cleanStr(parts, 2),
                        description = cleanStr(parts,3),
                        amount = cleanStr(parts, 4).toFloat(),
                        paymentMode = cleanStr(parts,5),
                        date = cleanStr(parts,6),
                    )
                )
            }
        } catch (e: Exception){
            Log.d("My DEBUG", "Cant read file", e)
        }
    }

    return expenses
}
fun cleanStr(value: List<String>, pos: Int): String {
    val valueAtPos = value.getOrNull(pos) ?: "Empty"
    val escaped = valueAtPos.replace("\"", "")
    return escaped
}

fun deleteExpenses(context: Context, dateAdded: String){
    val file = File(context.filesDir, "expenses.csv")
    deleteCsv(csvFile = file, deleteId = dateAdded, delIdx = 0)
}
fun deleteCsv(csvFile: File, deleteId: String, delIdx: Int = 0): Boolean {
    val lines = readCsv(csvFile)
    try {
        FileWriter(csvFile, false).use { writer ->
            lines.forEach { row ->
                if (row[delIdx] != deleteId) {
                    val rowSerialized = serializeRow(row)
                    writer.append(rowSerialized)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
    return true
}

fun readCsv(csvFile: File): List<List<String>> {
    val data = mutableListOf<List<String>>()
    if (csvFile.exists()) {
        csvFile.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                data.add(line.split(","))
            }
        }
    }
    return data
}


// Deprecated
fun resetCsvFile(fileName: File): Boolean {
    val isNewFile = !fileName.exists()
    val deleted = deleteCsvFile(fileName)
    if (!deleted){
        return false
    }
    FileWriter(fileName, true).use { writer ->
        if (isNewFile) {
            writer.append("Date-Added,Category,Sub-Category,Description,Amount,PaymentMode,Date\n")
        }
    }
    return true
}
// Deprecated
fun deleteCsvFile(fileName: File): Boolean{
    if (fileName.exists()) {
        val deleted = fileName.delete()
        if (deleted) {
            return true
            // Success: File was deleted
        } else {
            return false
            // Failure: Could not delete file (ensure the file is not currently open)
        }
    }
    return true // File already not present
}