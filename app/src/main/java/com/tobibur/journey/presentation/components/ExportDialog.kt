package com.tobibur.journey.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tobibur.journey.ui.theme.Typography

@Composable
fun ExportDialog(
    isLoading: Boolean = false,
    onAccept: (exportType: String) -> Unit,
    onDismiss: () -> Unit
) {
    val exportTypes = listOf("PDF", "JSON (To import journals later)")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(exportTypes[0]) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Card(modifier = Modifier.padding(8.dp), shape = RoundedCornerShape(8.dp)) {
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .selectableGroup()
                    .padding(16.dp)
            ) {
                Text("Choose export type:", style = Typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                exportTypes.forEach { text ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (text == selectedOption),
                                onClick = { onOptionSelected(text) },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (text == selectedOption),
                            onClick = null // null recommended for accessibility with screen readers
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }


                Spacer(Modifier.height(16.dp))
                LoadingProgressButton(
                    text = "Export & Download",
                    isLoading = isLoading,
                    onClick = { onAccept(selectedOption) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp
                        )
                )
                TextButton(modifier = Modifier.fillMaxWidth(), onClick = onDismiss) {
                    Text("Dismiss", color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.height(8.dp))
            }
        }

    }
}

@Composable
fun LoadingProgressButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = !isLoading // Disable button while loading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp), // Adjust size as needed
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = text)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExportDialogPreview() {
    ExportDialog(false,{}, {})
}