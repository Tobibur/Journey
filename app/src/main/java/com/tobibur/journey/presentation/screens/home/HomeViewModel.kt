package com.tobibur.journey.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tobibur.journey.data.UiState
import com.tobibur.journey.domain.model.JournalEntry
import com.tobibur.journey.domain.model.StreakStats
import com.tobibur.journey.domain.usecase.DeleteEntryUseCase
import com.tobibur.journey.domain.usecase.GetJournalEntriesUseCase
import com.tobibur.journey.domain.usecase.GetJournalStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
            launch {
                getJournalStreakUseCase().collect { stats ->
                    _streakStats.value = stats
                }
            }

            launch {
                try {
                    //delay(3000) test delay to see loading state
                    getEntriesUseCase().collect {
                        _uiState.value = UiState.Success(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _uiState.value = UiState.Error("No entries found")
                }
            }
        }
    }


    fun deleteEntry(entry: JournalEntry, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            deleteEntryUseCase(entry)
            onDeleted()
        }
    }
}