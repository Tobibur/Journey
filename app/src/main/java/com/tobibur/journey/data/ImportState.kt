package com.tobibur.journey.data

sealed class ImportState {
    data object Idle : ImportState()
    object Loading : ImportState()
    data class Success(val count: Int) : ImportState()
    data object NoEntries : ImportState()
    data class Error(val message: String) : ImportState()
}