package com.tobibur.journey.presentation.components.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val RECENT_WEEKS = 3
private val MAX_BAR_HEIGHT = 78.dp
private val MIN_BAR_HEIGHT = 4.dp

/**
 * A compact bar chart of per-week entry counts (oldest first). The most recent
 * [RECENT_WEEKS] bars are emphasised with the accent colour; bar heights scale
 * to the busiest week in the window.
 */
@Composable
fun WeeklyEntriesChart(
    weeklyCounts: List<Int>,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.BarChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = "WEEKLY ENTRIES",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(14.dp))

        val maxCount = (weeklyCounts.maxOrNull() ?: 0).coerceAtLeast(1)
        val recentStart = weeklyCounts.size - RECENT_WEEKS

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(MAX_BAR_HEIGHT),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            weeklyCounts.forEachIndexed { index, count ->
                val fraction = count.toFloat() / maxCount
                val barHeight = (MAX_BAR_HEIGHT * fraction).coerceAtLeast(MIN_BAR_HEIGHT)
                val isRecent = index >= recentStart
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(barHeight)
                        .background(
                            color = if (isRecent) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 2.dp, bottomEnd = 2.dp)
                        )
                )
            }
        }
    }
}
