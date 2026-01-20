package com.tobibur.journey.data

import com.tobibur.journey.domain.model.JournalEntriesByMonth

sealed class UiState {
    object Loading : UiState()
    data class Success(val entries: List<JournalEntriesByMonth>) : UiState()
    data class Error(val message: String) : UiState()
}