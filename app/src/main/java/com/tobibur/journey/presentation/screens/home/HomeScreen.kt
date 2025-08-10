package com.tobibur.journey.presentation.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tobibur.journey.presentation.components.ActionIcon
import com.tobibur.journey.presentation.components.JournalEntryCard
import com.tobibur.journey.presentation.components.JourneyTopAppBar
import com.tobibur.journey.presentation.components.SwipeableItemWithActions
import com.tobibur.journey.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    setTopBar: (@Composable (() -> Unit)) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val stats by viewModel.streakStats.collectAsState()
    LaunchedEffect(Unit) {
        setTopBar {
            JourneyTopAppBar(title = "Journey | \uD83D\uDD25 ${stats.currentStreak} day streak")
        }
    }
    val entries = viewModel.entries.collectAsState().value
    if (entries.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No journal entries yet")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = entries,
                key = { it.id }) { entry ->
                SwipeableItemWithActions(
                    actions = {
                        ActionIcon(
                            onClick = {
                                navController.navigate(Screen.ViewEntry.createRoute(entry.id))
                            },
                            backgroundColor = Color.Blue,
                            icon = Icons.Default.Edit,
                            modifier = Modifier.fillMaxHeight()
                        )
                        ActionIcon(
                            onClick = {
                                viewModel.deleteEntry(entry)
                            },
                            backgroundColor = Color.Red,
                            icon = Icons.Default.Delete,
                            modifier = Modifier.fillMaxHeight()
                        )
                    }
                ) {
                    JournalEntryCard(
                        entry = entry, onClick = {
                            navController.navigate(Screen.ViewEntry.createRoute(entry.id))
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        navController = rememberNavController(),
        {}) // Replace with a valid NavController in real use
}
