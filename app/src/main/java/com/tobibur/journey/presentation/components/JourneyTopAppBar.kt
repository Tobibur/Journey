package com.tobibur.journey.presentation.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyTopAppBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val bgColor = MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (bgColor.luminance() > 0.5f) Color.Black else Color.White

    TopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp),
        title = title,
        navigationIcon = {
            navigationIcon?.invoke()
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = bgColor,
            titleContentColor = contentColor,
            navigationIconContentColor = contentColor,
            actionIconContentColor = contentColor
        )
    )
}


// Preview function for quick testing
@Preview(showBackground = true)
@Composable
fun JourneyTopAppBarPreview() {
    JourneyTopAppBar(
        title = { Text(text = "Journey") },
        navigationIcon = null,
        actions = {}
    )
}
