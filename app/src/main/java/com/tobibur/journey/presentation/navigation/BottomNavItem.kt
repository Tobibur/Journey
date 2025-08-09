package com.tobibur.journey.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Home : BottomNavItem(Screen.Home.route, Icons.Default.Home, "Home")
    object Analytics : BottomNavItem(Screen.Analytics.route, Icons.Filled.BarChart, "Analytics")
    object Settings : BottomNavItem(Screen.Settings.route, Icons.Default.Settings, "Settings")

    companion object {
        val items = listOf(Home, Analytics, Settings)
    }
}
