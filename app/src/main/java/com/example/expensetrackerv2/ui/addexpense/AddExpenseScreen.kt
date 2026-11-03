package com.example.expensetrackerv2.ui.addexpense

import com.example.expensetrackerv2.ui.components.AppCardContent
import com.example.expensetrackerv2.ui.components.AppHeader
import com.example.expensetrackerv2.ui.components.DropdownField
import com.example.expensetrackerv2.ui.theme.*
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List as ListIcon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import com.example.expensetrackerv2.data.ExpenseOptionsStore
import com.example.expensetrackerv2.data.CsvUriStore
import com.example.expensetrackerv2.data.saveExpense
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AddExpenseScreen(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    // Lists are read each time this screen opens, so edits made in Settings show up here.
    val categoryList = remember { ExpenseOptionsStore.getCategories(context) }
    val paymentModes = remember { ExpenseOptionsStore.getPaymentModes(context) }
    val categories = categoryList.map { it.name }

    var paymentMode by rememberSaveable {
        mutableStateOf(if ("UPI" in paymentModes) "UPI" else paymentModes.first())
    }
    var amount by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var category by rememberSaveable { mutableStateOf(categories.first()) }
    var subCategory by rememberSaveable { mutableStateOf("") }
    val subCategories = categoryList
        .find { it.name == category }
        ?.subCategories ?: emptyList()

    // Reset selections that no longer exist (e.g. deleted in Settings, or restored state).
    LaunchedEffect(categories) {
        if (category !in categories) category = categories.first()
    }
    LaunchedEffect(paymentModes) {
        if (paymentMode !in paymentModes) paymentMode = paymentModes.first()
    }
    LaunchedEffect(category) {
        if (subCategory !in subCategories){
            subCategory = subCategories.firstOrNull() ?: ""
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val purpleTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        cursorColor = MaterialTheme.colorScheme.primary
    )
    AppHeader(
            snackbarHostState = snackbarHostState,
            title = "Add Expense",
            subtitle = "Track your spending"
    )
    {
        AppCardContent {
            DropdownField(
                label = "Category",
                options = categories,
                customTextFieldColorObject = purpleTextFieldColors,
                selected = category,
                onSelected = {
                    category = it
                    //                subCategory = category.first() // Reset when category changes
                }
            )
            DropdownField(
                label = "Sub Category",
                options = subCategories,
                customTextFieldColorObject = purpleTextFieldColors,
                selected = subCategory,
                onSelected = { subCategory = it }
            )
            DropdownField(
                label = "Payment Mode",
                options = paymentModes,
                customTextFieldColorObject = purpleTextFieldColors,
                selected = paymentMode,
                onSelected = { paymentMode = it }
            )
            OutlinedTextField(
                value = amount,
                // onValueChange = { amount = it },
                onValueChange = { newValue ->
                    if (newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        amount = newValue
                    }
                },
                label = { Text("Amount") },
                colors = purpleTextFieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                colors = purpleTextFieldColors,
                modifier = Modifier.fillMaxWidth()
            )
            DateInputField(
                customTextFieldColorObject = purpleTextFieldColors,
                date = date,
                onDateChange = { date = it }
            )

//            val context = LocalContext.current

            Button(
                onClick = {
                    val hasFile = CsvUriStore.get(context) != null
                    val success = hasFile && saveExpense(
                        context,
                        category,
                        subCategory,
                        paymentMode,
                        amount,
                        description,
                        date.toString()
                    )
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            when {
                                success -> "Saved successfully ✅"
                                !hasFile -> "No expenses file set up. Go to Settings to set one up ⚙️"
                                else -> "Failed to save ❌"
                            }
                        )
                    }
                    if (success) {
                        amount = ""
                        description = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(52.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = expensePurple,
//                )
            ) {
                Text("Save Expense", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}


@Composable
fun ExpenseScaffold(
    snackbarHostState: SnackbarHostState,
    title: String = "Add Expense",
    subtitle: String = "Track your spending",
    content: @Composable () -> Unit
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            // ── Purple gradient header ────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF5C4FC3),
                                Color(0xFF7B6FD4)
                            )
                        )
                    )
            ) {
                // Wave at the bottom of the header
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    val path = Path().apply {
                        moveTo(0f, size.height)
                        cubicTo(
                            size.width * 0.25f, 0f,
                            size.width * 0.75f, 0f,
                            size.width, size.height
                        )
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    drawPath(path, color = Color(0xFFF5F5F5))
                }
            }

            // ── Full screen column ────────────────────────────
            Column(modifier = Modifier.fillMaxSize()) {

                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            title,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            subtitle,
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // ── White card with content ───────────────────
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        content() // 👈 your fields slot in here
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateInputField(
    customTextFieldColorObject: TextFieldColors,
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    var text by remember { mutableStateOf(date.format(formatter)) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = date
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )
    val customDatePickerColor = DatePickerDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surface,

        titleContentColor = MaterialTheme.colorScheme.onSurface,
        headlineContentColor = MaterialTheme.colorScheme.onSurface,

        weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        dayContentColor = MaterialTheme.colorScheme.onSurface,

        selectedDayContainerColor = MaterialTheme.colorScheme.primary,
        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,

        todayDateBorderColor = MaterialTheme.colorScheme.primary,
        todayContentColor = MaterialTheme.colorScheme.primary,

        navigationContentColor = MaterialTheme.colorScheme.secondary
    )

    // 🔹 Picker dialog
    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val picked = Instant.ofEpochMilli(it)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            text = picked.format(formatter)
                            onDateChange(picked)
                        }
                        showPicker = false
                    },
//                    colors = ButtonDefaults.textButtonColors(
//                        contentColor = MaterialTheme.colorScheme.secondary
//                    )
                ) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = customDatePickerColor
            )
        }
    }

    // 🔹 Input field
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            // try parsing manual input
            runCatching {
                val parsed = LocalDate.parse(it, formatter)
                onDateChange(parsed)
            }
        },
        label = { Text("Date (yyyy-MM-dd)") },
        colors = customTextFieldColorObject,
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Pick date",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
