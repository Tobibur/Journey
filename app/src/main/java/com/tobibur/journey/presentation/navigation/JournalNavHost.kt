package com.tobibur.journey.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tobibur.journey.presentation.screens.addentry.AddEntryScreen
import com.tobibur.journey.presentation.screens.home.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddEntry : Screen("add_entry/{entryId}") {
        fun createRoute(entryId: Int) = "add_entry/$entryId"
    }
}

@Composable
fun JournalNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(
            route = Screen.AddEntry.route,
            arguments = listOf(navArgument("entryId") { type = NavType.IntType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getInt("entryId") ?: 0
            AddEntryScreen(navController, entryId)
        }
    }
}
