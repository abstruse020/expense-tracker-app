package com.example.expensetrackerv2.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.expensetrackerv2.data.ExpenseOptionsStore
import com.example.expensetrackerv2.ui.components.AppHeader

private sealed interface ModeDialog {
    data object Add : ModeDialog
    data class Rename(val name: String) : ModeDialog
    data class Delete(val name: String) : ModeDialog
}

@Composable
fun PaymentModesPage(onBack: () -> Unit) {
    val context = LocalContext.current
    var modes by remember { mutableStateOf(ExpenseOptionsStore.getPaymentModes(context)) }
    var dialog by remember { mutableStateOf<ModeDialog?>(null) }

    fun update(new: List<String>) {
        modes = new
        ExpenseOptionsStore.savePaymentModes(context, new)
    }

    AppHeader(title = "Payment modes", subtitle = "Choices on the Add Expense screen") {
        Row { TextButton(onClick = onBack) { Text("← Back") } }
        modes.forEach { mode ->
            ListItem(
                headlineContent = { Text(mode) },
                trailingContent = {
                    Row {
                        IconButton(onClick = { dialog = ModeDialog.Rename(mode) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Rename $mode")
                        }
                        // Keep at least one payment mode so Add Expense always has a choice.
                        IconButton(
                            enabled = modes.size > 1,
                            onClick = { dialog = ModeDialog.Delete(mode) }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete $mode",
                                tint = if (modes.size > 1) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                    }
                }
            )
            HorizontalDivider()
        }
        ListItem(
            modifier = Modifier.clickable { dialog = ModeDialog.Add },
            leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
            headlineContent = { Text("Add payment mode") }
        )
    }

    when (val d = dialog) {
        ModeDialog.Add -> NameDialog(
            title = "Add payment mode",
            existing = modes,
            onConfirm = { update(modes + it); dialog = null },
            onDismiss = { dialog = null }
        )
        is ModeDialog.Rename -> NameDialog(
            title = "Rename payment mode",
            initial = d.name,
            existing = modes,
            note = RENAME_NOTE,
            onConfirm = { new -> update(modes.map { if (it == d.name) new else it }); dialog = null },
            onDismiss = { dialog = null }
        )
        is ModeDialog.Delete -> ConfirmDeleteDialog(
            title = "Delete \"${d.name}\"?",
            message = DELETE_NOTE,
            onConfirm = { update(modes - d.name); dialog = null },
            onDismiss = { dialog = null }
        )
        null -> {}
    }
}
