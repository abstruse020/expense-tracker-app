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
import com.example.expensetrackerv2.data.deleteExpense

import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
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

import androidx.compose.ui.unit.dp

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
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(visibleExpenses) { expense ->
                ExpenseItem(
                    expense = expense,
                    onDelete = { toDelete ->
                        expenses = expenses.filter { it != toDelete }
                    }
                )
            }
        }
    }
}

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

@Composable
fun ExpenseItem(expense: Expense,
                onDelete: (Expense) -> Unit) {

    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete this expense?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        deleteExpense(context, expense.dateAdded)   // dummy function call
                        onDelete(expense)        // update UI list
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        elevation = CardDefaults.cardElevation(2.dp,),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        expense.category,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
//                Text(
//                    "₹${expense.amount}",
//                    style = MaterialTheme.typography.headlineSmall,
//                    fontWeight = FontWeight.Bold,
//                    color = MaterialTheme.colorScheme.onPrimary
//                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "₹${expense.amount}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(expense.subCategory, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(expense.date, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(expense.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(expense.paymentMode, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}