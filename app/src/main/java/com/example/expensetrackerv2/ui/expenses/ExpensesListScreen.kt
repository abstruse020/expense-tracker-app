package com.example.expensetrackerv2.ui.expenses

import com.example.expensetrackerv2.ui.components.AppCardContent
import com.example.expensetrackerv2.ui.components.AppHeader
import com.example.expensetrackerv2.ui.theme.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.items

import com.example.expensetrackerv2.model.Expense
import com.example.expensetrackerv2.data.readExpenses

import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Surface
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.text.style.TextOverflow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ExpensesListScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var expenses by remember { mutableStateOf(emptyList<Expense>()) }

    LaunchedEffect(Unit) {
        //expenses = readExpenses(context).sortedByDescending { it.date }
        expenses = readExpenses(context)
            .sortedWith(compareByDescending<Expense> {it.date}
                .thenByDescending{it.dateAdded}
            )
    }
    // The row id is Date-Added; selecting by id keeps working after the list is reloaded or filtered.
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val selected = expenses.firstOrNull { it.dateAdded == selectedId }
    if (selected != null) {
        BackHandler { selectedId = null }
        ExpenseDetailScreen(
            expense = selected,
            onBack = { selectedId = null },
            onDeleted = {
                expenses = expenses.filter { it.dateAdded != selected.dateAdded }
                selectedId = null
            },
            modifier = modifier
        )
        return
    }

    var query by remember { mutableStateOf("") }
    var searchFields by remember { mutableStateOf(DefaultSearchFields) }
    val visibleExpenses = remember(expenses, query, searchFields) {
        searchExpenses(expenses, query, searchFields)
    }

    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        SearchBar(
            query = query,
            onQueryChange = { query = it },
            selectedFields = searchFields,
            onToggleField = { field ->
                searchFields = when {
                    field !in searchFields -> searchFields + field
                    searchFields.size > 1 -> searchFields - field // keep at least one
                    else -> searchFields
                }
            }
        )
        if (visibleExpenses.isEmpty() && expenses.isNotEmpty()) {
            Text(
                "No expenses match your search",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        ExpensesList(
            expenses = visibleExpenses,
            onClick = { selectedId = it.dateAdded },
            listState = listState,
            modifier = Modifier.weight(1f)
        )
    }
}

/** Expenses grouped by day: a date divider, then one card per expense. Tapping a card opens its details. */
@Composable
fun ExpensesList(
    expenses: List<Expense>,
    onClick: (Expense) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    val groups = remember(expenses) { expenses.groupBy { it.date }.toList() }
    LazyColumn(state = listState, modifier = modifier, contentPadding = PaddingValues(bottom = 16.dp)) {
        items(groups, key = { it.first }) { (date, dayExpenses) ->
            DateDivider(date)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                dayExpenses.forEach { expense ->
                    ExpenseItem(expense = expense, onClick = { onClick(expense) })
                }
            }
        }
    }
}

@Composable
private fun DateDivider(date: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)
    ) {
        Text(
            formatDateLabel(date),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f).padding(start = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

private fun formatDateLabel(date: String): String {
    val parsed = runCatching { LocalDate.parse(date) }.getOrNull() ?: return date
    val text = parsed.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
    val today = LocalDate.now()
    return when (parsed) {
        today -> "Today · $text"
        today.minusDays(1) -> "Yesterday · $text"
        else -> text
    }
}

internal fun formatAmount(amount: Float): String =
    if (amount % 1f == 0f) "₹${amount.toLong()}" else "₹${"%.2f".format(amount)}"

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedFields: Set<SearchField>,
    onToggleField: (SearchField) -> Unit,
) {
    Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            label = { Text("Search expenses") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Search in",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SearchField.entries.forEach { field ->
                FilterChip(
                    selected = field in selectedFields,
                    onClick = { onToggleField(field) },
                    label = { Text(field.label) },
                )
            }
        }
    }
}

/**
 * Card colour: white in light mode (with a soft shadow, see [cardElevation]) so cards stand out from
 * the grey page; dark mode already has enough contrast with surfaceContainerLow.
 */
@Composable
internal fun cardContainerColor(): Color =
    if (isLightTheme()) MaterialTheme.colorScheme.surface
    else MaterialTheme.colorScheme.surfaceContainerLow

@Composable
internal fun isLightTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() > 0.5f

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpenseItem(expense: Expense, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLightTheme()) 1.dp else 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    expense.description.ifBlank { expense.subCategory },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    formatAmount(expense.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Tag(expense.category, MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.onPrimaryContainer)
                    Tag(expense.subCategory, MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    expense.paymentMode,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun Tag(text: String, container: Color, content: Color) {
    Surface(color = container, contentColor = content, shape = RoundedCornerShape(6.dp)) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
