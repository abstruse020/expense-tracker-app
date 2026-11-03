package com.example.expensetrackerv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.example.expensetrackerv2.ui.theme.ExpenseTrackerV2Theme
import com.example.expensetrackerv2.ui.expenses.ExpensesListScreen

import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Info
import com.example.expensetrackerv2.ui.addexpense.AddExpenseScreen
import com.example.expensetrackerv2.ui.overview.OverviewScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpenseTrackerV2Theme {
                ExpenseTrackerV2App()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun ExpenseTrackerV2App() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.ADD) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentDestination) {
                AppDestinations.ADD -> AddExpenseScreen(Modifier.padding(innerPadding))
                AppDestinations.EXPENSES -> ExpensesListScreen(Modifier.padding(innerPadding))
                AppDestinations.OVERVIEW -> OverviewScreen( Modifier.padding(innerPadding))
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    ADD("Add", Icons.Default.AddCircle),
    EXPENSES("List", Icons.Default.Menu),
    OVERVIEW("Overview", Icons.Default.Info),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ExpenseTrackerV2Theme {
        Greeting("Android")
    }
}
