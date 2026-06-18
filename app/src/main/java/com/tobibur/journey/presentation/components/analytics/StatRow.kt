package com.tobibur.journey.presentation.components.analytics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A clean, borderless summary row of three stats divided by hairlines,
 * with a rule above and below the whole row.
 */
@Composable
fun StatRow(
    today: Int,
    total: Int,
    best: Int,
    modifier: Modifier = Modifier
) {
    val divider = MaterialTheme.colorScheme.outlineVariant
    Column(modifier) {
        HorizontalDivider(color = divider)
        Row(Modifier.height(IntrinsicSize.Min)) {
            StatItem(label = "Today", value = today, leadingPadding = 0.dp)
            VerticalDivider(color = divider, modifier = Modifier.fillMaxHeight())
            StatItem(label = "Total", value = total, leadingPadding = 16.dp)
            VerticalDivider(color = divider, modifier = Modifier.fillMaxHeight())
            StatItem(label = "Best", value = best, leadingPadding = 16.dp)
        }
        HorizontalDivider(color = divider)
    }
}

@Composable
private fun RowScope.StatItem(
    label: String,
    value: Int,
    leadingPadding: androidx.compose.ui.unit.Dp
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .padding(start = leadingPadding, top = 16.dp, bottom = 16.dp)
    ) {
        Text(
            text = value.toString(),
            fontSize = 21.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}
