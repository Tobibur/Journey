package com.tobibur.journey.presentation.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tobibur.journey.presentation.components.JourneyTopAppBar
import com.tobibur.journey.presentation.components.analytics.MonthlyHeatmap
import com.tobibur.journey.presentation.components.analytics.StatCard
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun AnalyticsScreen(
    setTopBar: (@Composable (() -> Unit)) -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val today = LocalDate.now()
    val currentMonth = YearMonth.of(today.year, today.month)

    LaunchedEffect(Unit) {
        setTopBar {
            JourneyTopAppBar(
                title = "Analytics"
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Big circle streak display
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${uiState.currentStreak}",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }

        val dayText = if (uiState.currentStreak == 1) "day" else "days"
        Text(
            text = "$dayText streak!",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.Medium,
        )

        Spacer(Modifier.height(24.dp))

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard("Total Entries", uiState.totalEntries)
            StatCard("Highest Streak", uiState.highestStreak)
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Activity Heatmap",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        MonthlyHeatmap(
            month = currentMonth,
            doneDates = uiState.doneDatesThisMonth, // from ViewModel
            modifier = Modifier.fillMaxWidth()
        )
    }
}