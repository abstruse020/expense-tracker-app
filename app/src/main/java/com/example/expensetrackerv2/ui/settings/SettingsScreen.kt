package com.example.expensetrackerv2.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.expensetrackerv2.data.CsvUriStore
import com.example.expensetrackerv2.ui.components.AppHeader
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var fileName by remember { mutableStateOf(CsvUriStore.getFileName(context)) }
    var showFileDialog by remember { mutableStateOf(false) }

    val createLauncher = rememberCsvCreateLauncher {
        fileName = CsvUriStore.getFileName(context)
        showFileDialog = false
        scope.launch { snackbarHostState.showSnackbar("Expenses file set up") }
    }
    val openLauncher = rememberCsvOpenLauncher {
        fileName = CsvUriStore.getFileName(context)
        showFileDialog = false
        scope.launch { snackbarHostState.showSnackbar("Expenses file set up") }
    }

    AppHeader(
        snackbarHostState = snackbarHostState,
        title = "Settings",
        subtitle = "Manage your app"
    ) {
        ListItem(
            modifier = Modifier.clickable { showFileDialog = true },
            leadingContent = { Icon(Icons.Default.List, contentDescription = null) },
            headlineContent = { Text("Setup file") },
            supportingContent = { Text(fileName ?: "No file selected") }
        )
        HorizontalDivider()
    }

    if (showFileDialog) {
        AlertDialog(
            onDismissRequest = { showFileDialog = false },
            title = { Text("Set up your expenses file") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Current file: ${fileName ?: "none"}", style = MaterialTheme.typography.bodyMedium)
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
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
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
                        Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Existing")
                    }
                }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showFileDialog = false }) { Text("Cancel") } }
        )
    }
}
