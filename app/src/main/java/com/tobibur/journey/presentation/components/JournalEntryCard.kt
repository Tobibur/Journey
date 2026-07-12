package com.tobibur.journey.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date badge
            Column(
                modifier = Modifier.width(44.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dayOfWeek.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = entry.title,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis
                )
                if (entry.content.isNotBlank()) {
                    Text(
                        text = entry.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
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
            "This is the content that gives a short preview of the entry body.",
            System.currentTimeMillis()
        ),
        onClick = {}
    )
}
