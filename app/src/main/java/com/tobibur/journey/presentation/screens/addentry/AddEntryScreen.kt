package com.tobibur.journey.presentation.screens.addentry

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tobibur.journey.presentation.components.JourneyTopAppBar
import com.tobibur.journey.presentation.components.StreakPopup
import com.tobibur.journey.utils.formatTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    navController: NavController,
    entryId: Int,
    setTopBar: (@Composable (() -> Unit)) -> Unit,
    viewModel: AddEntryViewModel = hiltViewModel()
) {
    val title by viewModel.title.collectAsState()
    val content by viewModel.content.collectAsState()

    val focusManager = LocalFocusManager.current

    var popupStreak by remember { mutableStateOf<Int?>(null) }

    val timestamp by viewModel.timestamp.collectAsState()
    val formattedDate = remember(timestamp) { formatTimestamp(timestamp) }

    LaunchedEffect(entryId) {
        if (entryId != 0) {
            viewModel.loadEntry(entryId)
        }
        setTopBar {
            JourneyTopAppBar(
                title = {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Text(
                        text = "Save",
                        modifier = Modifier
                            .clickable {
                                viewModel.saveEntry {
                                    focusManager.clearFocus()
                                    navController.popBackStack()
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        }
        viewModel.showStreakPopup.collect { streak ->
            popupStreak = streak
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .imePadding()
                .padding(16.dp)
        ) {
            BasicTextField(
                value = title,
                onValueChange = viewModel::onTitleChange,
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (title.isEmpty()) {
                        Text(
                            "Title", style = MaterialTheme.typography.headlineSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    innerTextField()
                }
            )

            BasicTextField(
                value = content,
                onValueChange = viewModel::onContentChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                maxLines = Int.MAX_VALUE,
                decorationBox = { innerTextField ->
                    if (content.isEmpty()) {
                        Text(
                            "Start writing your thoughts...",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    innerTextField()
                }
            )
        }
        popupStreak?.let { streak ->
            StreakPopup(streak) {
                popupStreak = null // dismiss on close
                navController.popBackStack()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddEntryScreenPreview() {
    AddEntryScreen(
        navController = rememberNavController(),
        entryId = 0,
        setTopBar = {}
    ) // Replace with a valid NavController context in real use
}
