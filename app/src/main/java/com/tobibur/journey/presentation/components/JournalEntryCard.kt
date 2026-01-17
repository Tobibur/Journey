package com.tobibur.journey.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tobibur.journey.domain.model.JournalEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JournalEntryCard(
    entry: JournalEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val entryDateFormat = SimpleDateFormat("E dd", Locale.getDefault())
        .format(Date(entry.timestamp)).split(" ")
    val dayOfWeek = entryDateFormat.first()
    val date = entryDateFormat.last()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(color = MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)) {

        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
                    .padding(vertical = 4.dp, horizontal = 16.dp)
            ) {
                Text(
                    dayOfWeek,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            Text(
                date,
                style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = entry.title,
                maxLines = 1,
                style = MaterialTheme.typography.titleLarge,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// Preview 
@Preview
@Composable
private fun JournalEntryCardPreview() {
    JournalEntryCard(
        entry = JournalEntry(
            1,
            "This is the title",
            "This is the content",
            System.currentTimeMillis()
        ),
        onClick = {

        }
    )
}