package com.example.expensetrackerv2.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Switch
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.unit.dp
import com.example.expensetrackerv2.data.ExpenseOptionsStore
import com.example.expensetrackerv2.model.Category
import com.example.expensetrackerv2.ui.components.AppHeader

private sealed interface CategoryDialog {
    data object AddCategory : CategoryDialog
    data class RenameCategory(val name: String) : CategoryDialog
    data class DeleteCategory(val name: String) : CategoryDialog
    data class AddSub(val category: String) : CategoryDialog
    data class RenameSub(val category: String, val sub: String) : CategoryDialog
    data class DeleteSub(val category: String, val sub: String) : CategoryDialog
}

private const val DEFAULT_SUB = "Others"

@Composable
fun CategoriesPage(onBack: () -> Unit) {
    val context = LocalContext.current
    var categories by remember { mutableStateOf(ExpenseOptionsStore.getCategories(context)) }
    var expanded by remember { mutableStateOf<String?>(null) }
    var dialog by remember { mutableStateOf<CategoryDialog?>(null) }
    var newCategoryIsSaving by remember { mutableStateOf(false) }

    fun update(new: List<Category>) {
        categories = new
        ExpenseOptionsStore.saveCategories(context, new)
    }

    fun setSaving(category: String, saving: Boolean) {
        update(categories.map { if (it.name == category) it.copy(isSaving = saving) else it })
    }

    fun updateSubs(category: String, transform: (List<String>) -> List<String>) {
        update(categories.map {
            if (it.name == category) it.copy(subCategories = transform(it.subCategories)) else it
        })
    }

    val disabledTint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

    AppHeader(title = "Categories", subtitle = "Categories and sub-categories") {
        Row { TextButton(onClick = onBack) { Text("← Back") } }

        categories.forEach { category ->
            val isOpen = expanded == category.name
            ListItem(
                modifier = Modifier.clickable { expanded = if (isOpen) null else category.name },
                headlineContent = { Text(category.name) },
                supportingContent = {
                    Text(
                        "${category.subCategories.size} sub-categories" +
                            if (category.isSaving) " · Saving" else ""
                    )
                },
                leadingContent = {
                    Icon(
                        if (isOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isOpen) "Collapse" else "Expand"
                    )
                },
                trailingContent = {
                    Row {
                        IconButton(onClick = { dialog = CategoryDialog.RenameCategory(category.name) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Rename ${category.name}")
                        }
                        // Keep at least one category so Add Expense always has a choice.
                        IconButton(
                            enabled = categories.size > 1,
                            onClick = { dialog = CategoryDialog.DeleteCategory(category.name) }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete ${category.name}",
                                tint = if (categories.size > 1) MaterialTheme.colorScheme.error else disabledTint
                            )
                        }
                    }
                }
            )
            if (isOpen) {
                ListItem(
                    modifier = Modifier
                        .padding(start = 24.dp)
                        .clickable { setSaving(category.name, !category.isSaving) },
                    headlineContent = { Text("Counts as saving") },
                    supportingContent = { Text("Shown under Savings in the Overview, not in expenses") },
                    trailingContent = {
                        Switch(
                            checked = category.isSaving,
                            onCheckedChange = { setSaving(category.name, it) }
                        )
                    }
                )
                category.subCategories.forEach { sub ->
                    ListItem(
                        modifier = Modifier.padding(start = 24.dp),
                        headlineContent = { Text(sub) },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { dialog = CategoryDialog.RenameSub(category.name, sub) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Rename $sub")
                                }
                                // Keep at least one sub-category per category.
                                IconButton(
                                    enabled = category.subCategories.size > 1,
                                    onClick = { dialog = CategoryDialog.DeleteSub(category.name, sub) }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete $sub",
                                        tint = if (category.subCategories.size > 1)
                                            MaterialTheme.colorScheme.error else disabledTint
                                    )
                                }
                            }
                        }
                    )
                }
                ListItem(
                    modifier = Modifier
                        .padding(start = 24.dp)
                        .clickable { dialog = CategoryDialog.AddSub(category.name) },
                    leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                    headlineContent = { Text("Add sub-category") }
                )
            }
            HorizontalDivider()
        }

        ListItem(
            modifier = Modifier.clickable {
                newCategoryIsSaving = false
                dialog = CategoryDialog.AddCategory
            },
            leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
            headlineContent = { Text("Add category") },
            supportingContent = { Text("Starts with an \"$DEFAULT_SUB\" sub-category") }
        )
    }

    when (val d = dialog) {
        CategoryDialog.AddCategory -> NameDialog(
            title = "Add category",
            existing = categories.map { it.name },
            onConfirm = { name ->
                update(categories + Category(name, listOf(DEFAULT_SUB), isSaving = newCategoryIsSaving))
                expanded = name
                dialog = null
            },
            onDismiss = { dialog = null },
            extra = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { newCategoryIsSaving = !newCategoryIsSaving }
                        .padding(top = 8.dp)
                ) {
                    Checkbox(checked = newCategoryIsSaving, onCheckedChange = { newCategoryIsSaving = it })
                    Text("Counts as saving (e.g. SIP, FD)")
                }
            }
        )
        is CategoryDialog.RenameCategory -> NameDialog(
            title = "Rename category",
            initial = d.name,
            existing = categories.map { it.name },
            note = if (categories.find { it.name == d.name }?.isSaving == true) RENAME_SAVING_NOTE
                   else RENAME_NOTE,
            onConfirm = { new ->
                update(categories.map { if (it.name == d.name) it.copy(name = new) else it })
                if (expanded == d.name) expanded = new
                dialog = null
            },
            onDismiss = { dialog = null }
        )
        is CategoryDialog.DeleteCategory -> ConfirmDeleteDialog(
            title = "Delete \"${d.name}\"?",
            message = "This also removes its sub-categories from the picker. $DELETE_NOTE",
            onConfirm = {
                update(categories.filter { it.name != d.name })
                if (expanded == d.name) expanded = null
                dialog = null
            },
            onDismiss = { dialog = null }
        )
        is CategoryDialog.AddSub -> NameDialog(
            title = "Add sub-category",
            existing = categories.find { it.name == d.category }?.subCategories ?: emptyList(),
            onConfirm = { name -> updateSubs(d.category) { it + name }; dialog = null },
            onDismiss = { dialog = null }
        )
        is CategoryDialog.RenameSub -> NameDialog(
            title = "Rename sub-category",
            initial = d.sub,
            existing = categories.find { it.name == d.category }?.subCategories ?: emptyList(),
            note = RENAME_NOTE,
            onConfirm = { new ->
                updateSubs(d.category) { subs -> subs.map { if (it == d.sub) new else it } }
                dialog = null
            },
            onDismiss = { dialog = null }
        )
        is CategoryDialog.DeleteSub -> ConfirmDeleteDialog(
            title = "Delete \"${d.sub}\"?",
            message = DELETE_NOTE,
            onConfirm = { updateSubs(d.category) { it - d.sub }; dialog = null },
            onDismiss = { dialog = null }
        )
        null -> {}
    }
}
