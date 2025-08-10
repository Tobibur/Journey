package com.tobibur.journey.presentation.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tobibur.journey.presentation.components.JourneyTopAppBar

@Composable
fun SettingsScreen(
    setTopBar: (@Composable (() -> Unit)) -> Unit,
) {
    LaunchedEffect(Unit) {
        setTopBar {
            JourneyTopAppBar(
                title = "Settings"
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Settings Screen")
    }
}