package com.tobibur.journey.presentation.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tobibur.journey.domain.usecase.GetJournalEntriesUseCase
import com.tobibur.journey.domain.usecase.GetJournalStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getJournalEntries: GetJournalEntriesUseCase,
    private val getJournalStreak: GetJournalStreakUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                getJournalEntries(),
                getJournalStreak()
            ) { entries, streakInfo ->

                val entriesByDate = entries
                    .groupingBy { entry ->
                        Instant.ofEpochMilli(entry.timestamp)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    .eachCount()

                val currentMonth = YearMonth.now()
                val doneDatesThisMonth = entriesByDate.keys
                    .filter { date ->
                        YearMonth.from(date) == currentMonth
                    }
                    .sorted()

                val today = LocalDate.now()
                val entriesToday = entriesByDate[today] ?: 0

                // Entry counts for the trailing WEEKS_SHOWN weeks (oldest first),
                // each week running Monday..Sunday.
                val startOfThisWeek = today.with(DayOfWeek.MONDAY)
                val weeklyEntryCounts = (WEEKS_SHOWN - 1 downTo 0).map { weeksAgo ->
                    val weekStart = startOfThisWeek.minusWeeks(weeksAgo.toLong())
                    val weekEnd = weekStart.plusDays(6)
                    entriesByDate.entries
                        .filter { (date, _) -> !date.isBefore(weekStart) && !date.isAfter(weekEnd) }
                        .sumOf { it.value }
                }

                AnalyticsUiState(
                    currentStreak = streakInfo.currentStreak,
                    highestStreak = streakInfo.longestStreak,
                    totalEntries = entries.size,
                    entriesToday = entriesToday,
                    entriesByDate = entriesByDate,
                    doneDatesThisMonth = doneDatesThisMonth,
                    weeklyEntryCounts = weeklyEntryCounts
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    companion object {
        /** Number of trailing weeks summarised in the weekly entries chart. */
        const val WEEKS_SHOWN = 11
    }
}

data class AnalyticsUiState(
    val currentStreak: Int = 0,
    val highestStreak: Int = 0,
    val totalEntries: Int = 0,
    val entriesToday: Int = 0,
    val entriesByDate: Map<LocalDate, Int> = emptyMap(),
    val doneDatesThisMonth: List<LocalDate> = emptyList(), // for calendar grid
    val weeklyEntryCounts: List<Int> = emptyList() // trailing weeks, oldest first
)
