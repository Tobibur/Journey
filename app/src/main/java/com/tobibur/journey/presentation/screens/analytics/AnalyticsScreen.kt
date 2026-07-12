package com.tobibur.journey.presentation.screens.analytics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.tobibur.journey.presentation.components.JourneyTopAppBar
import com.tobibur.journey.presentation.components.analytics.MonthCalendarGrid
import com.tobibur.journey.presentation.components.analytics.StatRow
import com.tobibur.journey.presentation.components.analytics.StreakHero
import com.tobibur.journey.presentation.components.analytics.WeeklyEntriesChart
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun AnalyticsScreen(
    setTopBar: (@Composable (() -> Unit)) -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val today = LocalDate.now()
    val thisMonth = YearMonth.from(today)

    var selectedMonth by remember { mutableStateOf(thisMonth) }

    val activeDays = remember(uiState.entriesByDate, selectedMonth) {
        uiState.entriesByDate.keys
            .filter { YearMonth.from(it) == selectedMonth }
            .map { it.dayOfMonth }
            .toSet()
    }

    LaunchedEffect(Unit) {
        setTopBar {
            JourneyTopAppBar(
                title = {
                    Text(
                        text = "Analytics",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        StreakHero(
            currentStreak = uiState.currentStreak,
            bestStreak = uiState.highestStreak,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(28.dp))

        StatRow(
            today = uiState.entriesToday,
            total = uiState.totalEntries,
            best = uiState.highestStreak,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(28.dp))

        MonthCalendarGrid(
            month = selectedMonth,
            activeDays = activeDays,
            todayDay = if (selectedMonth == thisMonth) today.dayOfMonth else null,
            canGoNext = selectedMonth < thisMonth,
            onPrev = { selectedMonth = selectedMonth.minusMonths(1) },
            onNext = { if (selectedMonth < thisMonth) selectedMonth = selectedMonth.plusMonths(1) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(28.dp))

        WeeklyEntriesChart(
            weeklyCounts = uiState.weeklyEntryCounts,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
