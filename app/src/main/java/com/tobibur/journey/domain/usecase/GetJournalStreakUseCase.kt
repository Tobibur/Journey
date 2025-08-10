package com.tobibur.journey.domain.usecase

import com.tobibur.journey.domain.model.StreakStats
import com.tobibur.journey.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class GetJournalStreakUseCase(
    private val repository: JournalRepository
) {
    operator fun invoke(): Flow<StreakStats> {
        return repository.getAllEntryDatesFlow()
            .map { dates ->
                if (dates.isEmpty()) return@map StreakStats(0, 0)

                val sortedDates = dates.distinct().sortedDescending()
                var streak = 1
                var longestStreak = 1

                for (i in 1 until sortedDates.size) {
                    val diff = ChronoUnit.DAYS.between(sortedDates[i], sortedDates[i - 1])
                    if (diff == 1L) {
                        streak++
                        if (streak > longestStreak) longestStreak = streak
                    } else if (diff > 1) {
                        break
                    }
                }

                val today = LocalDate.now()
                if (!sortedDates.contains(today)) {
                    // If no entry today, streak is based on yesterday
                    val yesterday = today.minusDays(1)
                    streak = if (sortedDates.first() == yesterday) streak else 0
                }

                StreakStats(streak, longestStreak)
            }
    }
}
