package com.tobibur.journey.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tobibur.journey.presentation.screens.addentry.AddEntryScreen
import com.tobibur.journey.presentation.screens.home.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddEntry : Screen("add_entry")
}

@Composable
fun JournalNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.AddEntry.route) {
            AddEntryScreen(navController)
        }
    }
}
