package com.tobibur.journey.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tobibur.journey.presentation.components.BottomNavBar
import com.tobibur.journey.presentation.components.JourneyTopAppBar
import com.tobibur.journey.presentation.screens.addentry.AddEntryScreen
import com.tobibur.journey.presentation.screens.analytics.AnalyticsScreen
import com.tobibur.journey.presentation.screens.home.HomeScreen
import com.tobibur.journey.presentation.screens.settings.SettingsScreen
import com.tobibur.journey.presentation.screens.viewentry.ViewEntryScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddEntry : Screen("add_entry/{entryId}") {
        fun createRoute(entryId: Int) = "add_entry/$entryId"
    }

    // Assuming you have an Analytics screen
    object Analytics : Screen("analytics")
    object Settings : Screen("settings")

    object ViewEntry : Screen("view_entry/{entryId}") {
        fun createRoute(entryId: Int) = "view_entry/$entryId"
    }
}

@Composable
fun JournalNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val topBarState = remember {
        mutableStateOf<@Composable (() -> Unit)?>({
            JourneyTopAppBar(
                title = "Journey",
            )
        })
    }

    Scaffold(
        topBar = { topBarState.value?.invoke() },
        floatingActionButton = {
            if (currentRoute == Screen.Home.route) {
                FloatingActionButton(onClick = {
                    navController.navigate(
                        Screen.AddEntry.createRoute(
                            0
                        )
                    )
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Entry")
                }
            }
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onItemClick = { item ->
                    when (item) {
                        is BottomNavItem.Home -> navController.navigate(Screen.Home.route)
                        is BottomNavItem.Analytics -> navController.navigate(Screen.Analytics.route)
                        is BottomNavItem.Settings -> navController.navigate(Screen.Settings.route)
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController, setTopBar = { topBar ->
                    topBarState.value = topBar
                })
            }
            composable(
                route = Screen.AddEntry.route,
                arguments = listOf(navArgument("entryId") { type = NavType.IntType })
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getInt("entryId") ?: 0
                AddEntryScreen(navController, entryId, setTopBar = { topBar ->
                    topBarState.value = topBar
                })
            }
            composable(
                route = Screen.ViewEntry.route,
                arguments = listOf(navArgument("entryId") { type = NavType.IntType })
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getInt("entryId") ?: 0
                ViewEntryScreen(navController, entryId, setTopBar = { topBar ->
                    topBarState.value = topBar
                })
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(setTopBar = { topBar ->
                    topBarState.value = topBar
                })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(setTopBar = { topBar ->
                    topBarState.value = topBar
                })
            }
        }
    }
}
