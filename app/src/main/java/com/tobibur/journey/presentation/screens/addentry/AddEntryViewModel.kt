package com.tobibur.journey.presentation.screens.addentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tobibur.journey.domain.model.JournalEntry
import com.tobibur.journey.domain.usecase.AddEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEntryViewModel @Inject constructor(
    private val addEntryUseCase: AddEntryUseCase
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content

    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
    }

    fun onContentChange(newContent: String) {
        _content.value = newContent
    }

    fun saveEntry(onSaved: () -> Unit) {
        if (title.value.isBlank() && content.value.isBlank()) return

        viewModelScope.launch {
            val entry = JournalEntry(
                title = title.value,
                content = content.value,
                timestamp = System.currentTimeMillis()
            )
            addEntryUseCase(entry)
            onSaved()
        }
    }
}
