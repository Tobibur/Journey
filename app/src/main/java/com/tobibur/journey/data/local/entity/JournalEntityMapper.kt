package com.tobibur.journey.data.local.entity

import com.tobibur.journey.data.local.entity.JournalEntity


fun JournalEntity.toDomain(): com.tobibur.journey.domain.model.JournalEntry =
    com.tobibur.journey.domain.model.JournalEntry(
        id = id,
        title = title,
        content = content,
        timestamp = timestamp
    )

fun com.tobibur.journey.domain.model.JournalEntry.toEntity(): JournalEntity = JournalEntity(
    id = id,
    title = title,
    content = content,
    timestamp = timestamp
)
