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

                AnalyticsUiState(
                    currentStreak = streakInfo.currentStreak,
                    highestStreak = streakInfo.longestStreak,
                    totalEntries = entries.size,
                    entriesByDate = entriesByDate,
                    doneDatesThisMonth = doneDatesThisMonth
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}

data class AnalyticsUiState(
    val currentStreak: Int = 0,
    val highestStreak: Int = 0,
    val totalEntries: Int = 0,
    val entriesByDate: Map<LocalDate, Int> = emptyMap(),
    val doneDatesThisMonth: List<LocalDate> = emptyList() // for heatmap
)
