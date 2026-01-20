package com.tobibur.journey.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tobibur.journey.data.UiState
import com.tobibur.journey.domain.model.JournalEntriesByMonth
import com.tobibur.journey.domain.model.JournalEntry
import com.tobibur.journey.domain.model.StreakStats
import com.tobibur.journey.domain.usecase.DeleteEntryUseCase
import com.tobibur.journey.domain.usecase.GetJournalEntriesUseCase
import com.tobibur.journey.domain.usecase.GetJournalStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getEntriesUseCase: GetJournalEntriesUseCase,
    private val deleteEntryUseCase: DeleteEntryUseCase,
    private val getJournalStreakUseCase: GetJournalStreakUseCase
) : ViewModel() {

    private val _streakStats = MutableStateFlow(StreakStats(0, 0))
    val streakStats: StateFlow<StreakStats> = _streakStats.asStateFlow()
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    init {
        viewModelScope.launch {
            getJournalStreakUseCase().collect { stats ->
                _streakStats.value = stats
            }
        }

        viewModelScope.launch {
            try {
                //delay(3000) test delay to see loading state
                getEntriesUseCase().collect {
                    val groupedEntries =
                        withContext(Dispatchers.Default) {
                            groupEntriesByMonth(it)
                        }
                    _uiState.value = UiState.Success(groupedEntries)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = UiState.Error("No entries found")
            }
        }
    }

    fun groupEntriesByMonth(entries: List<JournalEntry>): List<JournalEntriesByMonth> {
        return entries.groupBy { entry ->
            val date = Instant.ofEpochMilli(entry.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            YearMonth.of(date.year, date.month)
        }.map { (yearMonth, monthEntries) ->
            JournalEntriesByMonth(yearMonth, monthEntries)
        }.sortedByDescending { it.yearMonth }
    }

    fun deleteEntry(entry: JournalEntry, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            deleteEntryUseCase(entry)
            onDeleted()
        }
    }
}