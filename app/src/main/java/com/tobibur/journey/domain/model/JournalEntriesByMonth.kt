package com.tobibur.journey.domain.model

import java.time.YearMonth

data class JournalEntriesByMonth(
    val yearMonth: YearMonth,
    val entries: List<JournalEntry>
)
