package com.tobibur.journey.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tobibur.journey.domain.model.JournalEntry
import com.tobibur.journey.domain.usecase.DeleteEntryUseCase
import com.tobibur.journey.domain.usecase.GetJournalEntriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getEntriesUseCase: GetJournalEntriesUseCase,
    private val deleteEntryUseCase: DeleteEntryUseCase
) : ViewModel() {

    val entries = getEntriesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteEntry(entry: JournalEntry, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            deleteEntryUseCase(entry)
            onDeleted()
        }
    }
}