package com.example.expensetrackerv2.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.expensetrackerv2.data.readExpenses
import com.example.expensetrackerv2.model.Expense
import java.time.format.DateTimeFormatter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.remember
import java.time.LocalDate


data class BarEntry(
    val label: String,
    val value: Float
)


@Composable
fun OverviewScreen(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    var expenses by remember { mutableStateOf(emptyList<Expense>()) }

    LaunchedEffect(Unit) {
        expenses = readExpenses(context).sortedByDescending { it.date }

        val totals = expenses
            .groupingBy { it.category to it.subCategory }
            .fold(0.0) { acc, expense ->
                acc + expense.amount
            }
    }

//    val chartData = remember(expenses) {
//        buildMonthlyBarData(expenses)
//    }
//
//    SpendingBarChart(
//        data = chartData,
//        modifier = Modifier.fillMaxWidth()
//    )
    CategorySpendingScreen(expenses)
}


@Composable
fun CategorySpendingScreen(
    expenses: List<Expense>
) {
    var monthText by remember { mutableStateOf(LocalDate.now().monthValue.toString()) }
    var yearText by remember { mutableStateOf(LocalDate.now().year.toString()) }

    val month = monthText.toIntOrNull()?.coerceIn(1, 12) ?: 1
    val year = yearText.toIntOrNull() ?: LocalDate.now().year

    val filtered = remember(expenses, month, year) {
        filterExpenses(expenses, month, year)
    }

    val chartData = remember(filtered) {
        buildCategoryBarData(filtered)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 38.dp)
    ) {

        // ---------------- HEADER ----------------
        Text(
            text = "Spending Breakdown",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        // ---------------- SIMPLE FILTER UI ----------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = monthText,
                onValueChange = { monthText = it },
                label = { Text("Month (1-12)") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = yearText,
                onValueChange = { yearText = it },
                label = { Text("Year") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ---------------- BAR CHART ----------------
        SpendingBarChart(
            data = chartData,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

fun filterExpenses(
    expenses: List<Expense>,
    month: Int,
    year: Int
): List<Expense> {
    return expenses.filter {
        val date = LocalDate.parse(it.date)
        date.monthValue == month && date.year == year
    }
}

fun buildCategoryBarData(
    expenses: List<Expense>
): List<BarEntry> {

    val grouped = expenses
        .groupBy { it.category to it.subCategory }
        .mapValues { (_, list) ->
            list.sumOf { it.amount.toDouble() }
        }

    return grouped.map { (key, total) ->
        BarEntry(
            label = "${key.first} - ${key.second}",
            value = total.toFloat()
        )
    }.sortedByDescending { it.value }
}


@Composable
fun SpendingBarChart(
    data: List<BarEntry>,
    modifier: Modifier = Modifier
) {
    val maxValue = (data.maxOfOrNull { it.value } ?: 0f).coerceAtLeast(1f)

    Column(modifier = modifier.padding(16.dp)) {

        Text(
            text = "Category Spending",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(data.size) { index ->

                val item = data[index]
                val ratio = item.value / maxValue

                Column {

                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(ratio)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "₹${item.value}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}


fun buildMonthlyBarData(expenses: List<Expense>): List<BarEntry> {
    val formatter = DateTimeFormatter.ofPattern("MMM yyyy")

    val monthlyTotals = expenses
        .groupBy { LocalDate.parse(it.date).withDayOfMonth(1) }
        .mapValues { (_, list: List<Expense>) ->
            list.sumOf { it.amount.toDouble() }
        }
        .toSortedMap()

    return monthlyTotals.map { (month, total) ->
        BarEntry(
            label = month.format(formatter),
            value = total.toFloat()
        )
    }
}

//@Composable
//fun SpendingBarChart(
//    data: List<BarEntry>,
//    modifier: Modifier = Modifier
//) {
//    val maxValue = (data.maxOfOrNull { it.value } ?: 0f).coerceAtLeast(1f)
//
//    Column(modifier = modifier.padding(16.dp)) {
//
//        Text(
//            text = "Monthly Spending",
//            style = MaterialTheme.typography.titleMedium,
//            fontWeight = FontWeight.Bold
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(200.dp),
//            horizontalArrangement = Arrangement.SpaceEvenly,
//            verticalAlignment = Alignment.Bottom
//        ) {
//
//            data.forEach { item ->
//
//                val barHeightRatio = item.value / maxValue
//
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.Bottom,
//                    modifier = Modifier.weight(1f)
//                ) {
//
//                    Box(
//                        modifier = Modifier
//                            .height((150 * barHeightRatio).dp)
//                            .width(16.dp)
//                            .background(MaterialTheme.colorScheme.primary)
//                    )
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    Text(
//                        text = item.label.take(3), // JAN, FEB
//                        style = MaterialTheme.typography.labelSmall
//                    )
//                }
//            }
//        }
//    }
//}