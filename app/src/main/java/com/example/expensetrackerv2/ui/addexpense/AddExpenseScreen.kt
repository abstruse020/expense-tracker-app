package com.example.expensetrackerv2.ui.addexpense

import com.example.expensetrackerv2.ui.components.AppCardContent
import com.example.expensetrackerv2.ui.components.AppHeader
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
import com.example.expensetrackerv2.data.CategoryData
import com.example.expensetrackerv2.data.CsvUriStore
import com.example.expensetrackerv2.data.saveExpense
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AddExpenseScreen(modifier: Modifier = Modifier) {

    var paymentMode by rememberSaveable { mutableStateOf("UPI") }
    var amount by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf(LocalDate.now()) }
    val categories = CategoryData.categories.map { it.name }
    var category by rememberSaveable { mutableStateOf(categories.first()) }
    var subCategory by rememberSaveable { mutableStateOf("") }
    val subCategories = CategoryData.categories
        .find { it.name == category }
        ?.subCategories ?: emptyList()
    LaunchedEffect(category) {
        if (subCategory !in subCategories){
            subCategory = subCategories.firstOrNull() ?: ""
        }
    }

    val context = LocalContext.current
    // Handle file picker
    var hasCsvUri by remember { mutableStateOf(CsvUriStore.get(context) != null) }

    val createLauncher = rememberCsvCreateLauncher { uri ->
        hasCsvUri = true // update UI state once picked
    }

    val openLauncher = rememberCsvOpenLauncher { uri ->
        hasCsvUri = true
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val expensePurple = MaterialTheme.colorScheme.onPrimary
    val purpleTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = expensePurple,
        unfocusedBorderColor = expensePurple.copy(alpha = 0.5f),
        focusedLabelColor = expensePurple,
        cursorColor = expensePurple
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
                options = listOf("Cash", "UPI", "SBI Elite", "SBI Cashback", "Scapia", "Splitwise"),
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
                    val success = saveExpense(
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
                            if (success) "Saved successfully ✅"
                            else "Failed to save ❌"
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

            if (true) {
                Text(
                    text = "Set up your expenses file",
                    style = MaterialTheme.typography.bodyMedium
                )
//                Spacer(modifier = Modifier.height(1.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { createLauncher.launch("expenses.csv") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New File")
                    }
                    OutlinedButton(
                        onClick = { openLauncher.launch(arrayOf("*/*")) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            Icons.Default.ListIcon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
//                        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Existing")
                    }
                }
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
fun DropdownField(
    label: String,
    options: List<String>,
    customTextFieldColorObject: TextFieldColors,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = customTextFieldColorObject,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option,
                            fontWeight = if (option == selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (option == selected)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                    // Highlight selected item background
                    modifier = if (option == selected)
                        Modifier.background(MaterialTheme.colorScheme.surface)
                    else
                        Modifier
                )
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

        todayDateBorderColor = MaterialTheme.colorScheme.onPrimary,
        todayContentColor = MaterialTheme.colorScheme.onPrimary,

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
                    Text("OK", color = MaterialTheme.colorScheme.secondary)
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
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    )
}

@Composable
fun rememberCsvCreateLauncher(
    onCreated: (Uri) -> Unit
): ManagedActivityResultLauncher<String, Uri?> {1
    val context = LocalContext.current
    return rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            CsvUriStore.save(context, uri)
            onCreated(uri)
        }
    }
}

@Composable
fun rememberCsvOpenLauncher(
    onOpened: (Uri) -> Unit
): ManagedActivityResultLauncher<Array<String>, Uri?> {
    val context = LocalContext.current
    return rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            CsvUriStore.save(context, uri)
            onOpened(uri)
        }
    }
}