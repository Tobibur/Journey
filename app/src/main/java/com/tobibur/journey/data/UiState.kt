package com.tobibur.journey.data

import com.tobibur.journey.domain.model.JournalEntry

sealed class UiState {
    object Loading : UiState()
    data class Success(val entries: List<JournalEntry>) : UiState()
    data class Error(val message: String) : UiState()
}