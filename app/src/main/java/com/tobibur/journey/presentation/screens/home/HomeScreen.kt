package com.tobibur.journey.presentation.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.tobibur.journey.R
import com.tobibur.journey.data.UiState
import com.tobibur.journey.domain.model.JournalEntry
import com.tobibur.journey.presentation.components.ActionIcon
import com.tobibur.journey.presentation.components.JournalEntryCard
import com.tobibur.journey.presentation.components.JourneyDialog
import com.tobibur.journey.presentation.components.JourneyTopAppBar
import com.tobibur.journey.presentation.components.SwipeableItemWithActions
import com.tobibur.journey.presentation.navigation.Screen
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    setTopBar: (@Composable (() -> Unit)) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val stats by viewModel.streakStats.collectAsStateWithLifecycle()
    var entryToDelete by remember { mutableStateOf<JournalEntry?>(null) }
    LaunchedEffect(Unit) {
        setTopBar {
            JourneyTopAppBar(title = {
                Text(
                    buildAnnotatedString {
                        append("Journey")
                        append(" | 🔥 ${stats.currentStreak} day streak")
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            })
        }
    }
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    when (uiState.value) {
        UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is UiState.Success -> {
            val entriesByMonth = (uiState.value as UiState.Success).entries
            if (entriesByMonth.isEmpty()) {
                NoEntriesToLoad()
            } else {
                val monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    entriesByMonth.forEach { (yearMonth, monthEntries) ->
                        // Month-Year header
                        item(key = "month_${yearMonth}") {
                            Text(
                                text = yearMonth.format(monthFormatter).uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp, bottom = 2.dp)
                            )
                        }

                        // Entries for this month
                        items(
                            items = monthEntries,
                            key = { entry -> entry.id }
                        ) { entry ->
                            SwipeableItemWithActions(
                                actions = {
                                    ActionIcon(
                                        onClick = {
                                            entryToDelete = entry
                                        },
                                        backgroundColor = MaterialTheme.colorScheme.errorContainer,
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        icon = Icons.Default.Delete,
                                        contentDescription = "Delete entry",
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            ) {
                                JournalEntryCard(
                                    entry = entry,
                                    onClick = {
                                        navController.navigate(
                                            Screen.ViewEntry.createRoute(
                                                entry.id
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        is UiState.Error -> {
            val message = (uiState.value as UiState.Error).message
            NoEntriesToLoad(message)
        }
    }

    entryToDelete?.let { entry ->
        JourneyDialog(
            lottieRes = R.raw.delete_anim,
            title = "Delete Entry?",
            description = "Are you sure you want to delete this journal entry? This action cannot be undone.",
            confirmButton = {
                viewModel.deleteEntry(entry)
                entryToDelete = null
            },
            dismissButton = {
                entryToDelete = null
            }
        )
    }
}

@Composable
private fun NoEntriesToLoad(message: String = "No journal entries found") {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.empty_ghost)
    )
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
