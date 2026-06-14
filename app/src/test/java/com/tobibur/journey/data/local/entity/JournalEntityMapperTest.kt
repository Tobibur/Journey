package com.tobibur.journey.data.local.entity

import com.tobibur.journey.domain.model.JournalEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class JournalEntityMapperTest {

    @Test
    fun `toDomain maps all fields`() {
        val entity = JournalEntity(id = 7, title = "T", content = "C", timestamp = 123L)

        val domain = entity.toDomain()

        assertEquals(7, domain.id)
        assertEquals("T", domain.title)
        assertEquals("C", domain.content)
        assertEquals(123L, domain.timestamp)
    }

    @Test
    fun `toEntity maps all fields`() {
        val domain = JournalEntry(id = 9, title = "Title", content = "Body", timestamp = 999L)

        val entity = domain.toEntity()

        assertEquals(9, entity.id)
        assertEquals("Title", entity.title)
        assertEquals("Body", entity.content)
        assertEquals(999L, entity.timestamp)
    }

    @Test
    fun `entity to domain and back is lossless`() {
        val original = JournalEntity(id = 3, title = "Hello", content = "World", timestamp = 42L)

        val roundTripped = original.toDomain().toEntity()

        assertEquals(original, roundTripped)
    }
}
