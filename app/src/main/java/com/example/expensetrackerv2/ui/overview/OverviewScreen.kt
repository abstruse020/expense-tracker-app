package com.example.expensetrackerv2.ui.overview

import com.example.expensetrackerv2.ui.components.AppCardContent
import com.example.expensetrackerv2.ui.components.AppHeader
import com.example.expensetrackerv2.ui.theme.*
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.example.expensetrackerv2.ui.expenses.cardContainerColor
import com.example.expensetrackerv2.ui.expenses.formatAmount
import com.example.expensetrackerv2.ui.expenses.isLightTheme
import kotlin.math.roundToInt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.expensetrackerv2.ui.components.DropdownField


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
    expenses: List<Expense>,
    initiallyExpanded: String? = null
) {
    var month by rememberSaveable { mutableStateOf(LocalDate.now().monthValue) }
    var year by rememberSaveable { mutableStateOf(LocalDate.now().year) }

    val monthNames = remember {
        Month.entries.map { it.getDisplayName(TextStyle.FULL, Locale.getDefault()) }
    }
    // Years that have expenses, plus the current and selected year so there's always a choice.
    val years = remember(expenses, year) {
        (expenses.mapNotNull { runCatching { LocalDate.parse(it.date).year }.getOrNull() } +
            LocalDate.now().year + year)
            .distinct()
            .sortedDescending()
    }

    val filtered = remember(expenses, month, year) {
        filterExpenses(expenses, month, year)
    }

    val categories = remember(filtered) { buildCategorySpending(filtered) }
    val total = remember(filtered) { totalSpent(filtered) }
    // Accordion: only one category is open at a time.
    var expandedCategory by rememberSaveable { mutableStateOf(initiallyExpanded) }

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

            val fieldColors = OutlinedTextFieldDefaults.colors()
            Box(modifier = Modifier.weight(1f)) {
                DropdownField(
                    label = "Month",
                    options = monthNames,
                    customTextFieldColorObject = fieldColors,
                    selected = monthNames[month - 1],
                    onSelected = { month = monthNames.indexOf(it) + 1 }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                DropdownField(
                    label = "Year",
                    options = years.map { it.toString() },
                    customTextFieldColorObject = fieldColors,
                    selected = year.toString(),
                    onSelected = { year = it.toInt() }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ---------------- TOTAL + CATEGORY BARS ----------------
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TotalSpentPanel(
                    total = total,
                    expenseCount = filtered.size,
                    period = "${monthNames[month - 1]} $year"
                )
            }
            if (categories.isEmpty()) {
                item {
                    Text(
                        "No expenses in this month",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                item {
                    Text(
                        "Spending by category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                val maxCategory = categories.first().total
                items(categories, key = { it.name }) { category ->
                    CategoryCard(
                        category = category,
                        maxCategoryTotal = maxCategory,
                        grandTotal = total,
                        expanded = expandedCategory == category.name,
                        onToggle = {
                            expandedCategory =
                                if (expandedCategory == category.name) null else category.name
                        }
                    )
                }
            }
        }
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

data class SubCategorySpend(val name: String, val total: Double)

data class CategorySpend(
    val name: String,
    val total: Double,
    val subCategories: List<SubCategorySpend>
)

fun totalSpent(expenses: List<Expense>): Double = expenses.sumOf { it.amount.toDouble() }

/** Spend per category (largest first), each with its sub-categories (largest first). */
fun buildCategorySpending(expenses: List<Expense>): List<CategorySpend> =
    expenses
        .groupBy { it.category }
        .map { (category, list) ->
            CategorySpend(
                name = category,
                total = totalSpent(list),
                subCategories = list
                    .groupBy { it.subCategory }
                    .map { (sub, subList) -> SubCategorySpend(sub, totalSpent(subList)) }
                    .sortedByDescending { it.total }
            )
        }
        .sortedByDescending { it.total }

private fun money(value: Double) = formatAmount(value.toFloat())

/** The month's total, shown above all categories. */
@Composable
private fun TotalSpentPanel(total: Double, expenseCount: Int, period: String) {
    val content = MaterialTheme.colorScheme.onPrimaryContainer
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = content,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
            Text("Total spent", style = MaterialTheme.typography.labelLarge)
            Text(
                money(total),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "$period · $expenseCount ${if (expenseCount == 1) "expense" else "expenses"}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/** A category bar; tap it to show its sub-category bars underneath. */
@Composable
private fun CategoryCard(
    category: CategorySpend,
    maxCategoryTotal: Double,
    grandTotal: Double,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val share = if (grandTotal > 0) (category.total / grandTotal * 100).roundToInt() else 0
    val chevronAngle by animateFloatAsState(if (expanded) 180f else 0f, label = "chevron")

    Card(
        onClick = onToggle,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLightTheme()) 1.dp else 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    category.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    money(category.total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp).rotate(chevronAngle)
                )
            }
            Spacer(Modifier.height(8.dp))
            SpendBar(
                ratio = (category.total / maxCategoryTotal).toFloat(),
                height = 14.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "$share% of total",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            AnimatedVisibility(visible = expanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    category.subCategories.forEach { sub ->
                        Column {
                            Row {
                                Text(
                                    sub.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(money(sub.total), style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(Modifier.height(4.dp))
                            // Relative to the category total, so the bar shows the sub-category's share of it.
                            SpendBar(
                                ratio = (sub.total / category.total).toFloat(),
                                height = 8.dp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpendBar(ratio: Float, height: Dp, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(ratio.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(50))
                .background(color)
        )
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