package com.example.expensetrackerv2.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AppHeader(
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    title: String,
    subtitle: String = "",
    content: @Composable ColumnScope.() -> Unit
) {
    val expensePurple = MaterialTheme.colorScheme.primary
    val gradientTop = MaterialTheme.colorScheme.secondary
    val gradientBottom = MaterialTheme.colorScheme.primary
    val bgPrimary = MaterialTheme.colorScheme.background
    val titleColor = MaterialTheme.colorScheme.onPrimary
//    Color(0xFF5C4FC3),
//    Color(0xFF7B6FD4)
//    val bgPrimary = Color(0xFFF5F5F5)

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = bgPrimary
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Fixed purple header (never scrolls) ──────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                gradientTop,
                                gradientBottom
                            )
                        )
                    )
            ) {
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
                    drawPath(path, color = bgPrimary)

                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 120.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Title sits inside the purple box and scrolls with content
                // but the purple background stays fixed
                Column(
                    modifier = Modifier
//                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        title,
                        color = titleColor,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (subtitle.isNotBlank()) {
                        Text(
                            subtitle,
                            color = titleColor.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Column{
                    content()
                }
                // ── Scrollable content below ──────────────────────
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .background(Color(0xFFF5F5F5))
//                        .verticalScroll(rememberScrollState())
//                        .padding(bottom = 24.dp),
//                    verticalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//                    content()
//                }

            }
        }
    }
}
