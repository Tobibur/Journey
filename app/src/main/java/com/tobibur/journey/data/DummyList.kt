package com.tobibur.journey.data

import com.tobibur.journey.domain.model.JournalEntry

object DummyList {

    val dummyEntries = List(100) { index ->
        JournalEntry(
            id = index,
            title = "Dummy Entry $index",
            content = "This is a dummy entry number $index. It contains some placeholder text to simulate real journal entries.",
            timestamp = System.currentTimeMillis() - (index * 1000L) // Simulating different timestamps
        )
    }
}