package com.tobibur.journey.presentation.components.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun MonthlyHeatmap(
    month: YearMonth, // Pass any month/year here
    doneDates: List<LocalDate>,
    modifier: Modifier = Modifier
) {
    val firstDayOfMonth = month.atDay(1)
    val daysInMonth = (0 until month.lengthOfMonth()).map {
        firstDayOfMonth.plusDays(it.toLong())
    }

    Column(modifier = modifier.padding(8.dp)) {
        // Month label
        Text(
            text = month.month.name.lowercase()
                .replaceFirstChar { it.uppercase() } + " " + month.year,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Days grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            content = {
                items(daysInMonth.size) { index ->
                    val date = daysInMonth[index]
                    val isDone = date in doneDates
                    val dayNumber = date.dayOfMonth

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f) // make squares
                            .padding(4.dp)
                            .background(
                                if (isDone) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayNumber.toString(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isDone) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        )
    }
}
