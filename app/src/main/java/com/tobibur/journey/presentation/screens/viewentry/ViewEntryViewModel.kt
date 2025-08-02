package com.tobibur.journey.presentation.screens.viewentry

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tobibur.journey.domain.model.JournalEntry
import com.tobibur.journey.domain.usecase.DeleteEntryUseCase
import com.tobibur.journey.domain.usecase.GetEntryByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewEntryViewModel @Inject constructor(
    private val getEntryById: GetEntryByIdUseCase,
    private val deleteEntryUseCase: DeleteEntryUseCase
) : ViewModel() {

    private val _entry = mutableStateOf<JournalEntry?>(null)
    val entry: State<JournalEntry?> = _entry

    private val _timestamp = MutableStateFlow(System.currentTimeMillis())
    val timestamp: StateFlow<Long> = _timestamp

    fun loadEntry(id: Int) {
        viewModelScope.launch {
            _entry.value = getEntryById(id)
            _entry.value?.let {
                _timestamp.value = it.timestamp
            }
        }
    }

    fun deleteEntry(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _entry.value?.let {
                deleteEntryUseCase(it)
                onDeleted()
            }
        }
    }
}
