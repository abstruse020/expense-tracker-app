package com.example.expensetrackerv2.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensetrackerv2.data.OptionValidator

/** Dialog to enter a new name, or rename [initial]. Validates against [existing]. */
@Composable
fun NameDialog(
    title: String,
    existing: List<String>,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    initial: String = "",
    note: String? = null,
    extra: (@Composable () -> Unit)? = null,
) {
    var text by remember { mutableStateOf(initial) }
    val error = OptionValidator.validate(text, existing, ignore = initial.ifEmpty { null })
    // Don't flag an empty field before the user has typed anything.
    val showError = error != null && text.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    isError = showError,
                    supportingText = { if (showError) Text(error!!) },
                    modifier = Modifier.fillMaxWidth()
                )
                if (note != null) {
                    Text(
                        note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                extra?.invoke()
            }
        },
        confirmButton = {
            TextButton(enabled = error == null, onClick = { onConfirm(text.trim()) }) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

const val RENAME_NOTE = "Existing expenses keep the old name. Only new expenses use the new one."
const val RENAME_SAVING_NOTE = RENAME_NOTE +
    " Those old expenses will therefore be counted as expenses, not savings, in the Overview."
const val DELETE_NOTE = "Existing expenses keep this name and still appear in the list and overview."
