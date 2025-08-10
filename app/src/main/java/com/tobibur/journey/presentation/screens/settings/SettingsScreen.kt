package com.tobibur.journey.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tobibur.journey.presentation.components.JourneyTopAppBar

@Composable
fun SettingsScreen(
    currentTheme: String = "System",
    currentFontSize: String = "Medium",
    currentAccentColor: Color = MaterialTheme.colorScheme.primary,
    onThemeChange: (String) -> Unit = {},
    onFontSizeChange: (String) -> Unit = {},
    onAccentColorChange: (Color) -> Unit = {},
    onExportClick: () -> Unit = {},
    onImportClick: () -> Unit = {},
    onClearDataClick: () -> Unit = {},
    onReminderToggle: (Boolean) -> Unit = {},
    onReminderTimeClick: () -> Unit = {},
    onAppLockToggle: (Boolean) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
    setTopBar: (@Composable (() -> Unit)) -> Unit = {}
) {

    LaunchedEffect(Unit) {
        setTopBar {
            JourneyTopAppBar(
                title = "Analytics"
            )
        }
    }

    val reminderEnabled = remember { mutableStateOf(true) }
    val appLockEnabled = remember { mutableStateOf(false) }

    val accentColorInt by viewModel.accentColor.collectAsState()
    val useDynamicColor by viewModel.useDynamicColor.collectAsState()
    val darkThemeEnabled by viewModel.darkThemeEnabled.collectAsState()

    val accentColor = Color(accentColorInt)

    var showColorPicker by remember { mutableStateOf(false) }

    if (showColorPicker) {
        AccentColorPickerDialog(
            currentColor = accentColor,
            onColorSelected = {
                viewModel.setAccentColor(it) // Save ARGB int
                viewModel.setDynamicColor(false)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Personalization
        item { SectionHeader("Personalization") }
        item {
            SettingsOption("Accent Color", "Tap to change") {
                showColorPicker = true
            }
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            SwitchSetting("Use Dynamic Theme", useDynamicColor) {
                viewModel.setDynamicColor(it)
            }
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            SwitchSetting("Dark Theme", darkThemeEnabled) {
                viewModel.setDarkTheme(it)
            }
        }

        // Data & Backup
        item { SectionHeader("Data & Backup") }
        item {
            SettingsOption("Export Journal") { onExportClick() }
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        }
        item {
            SettingsOption("Import Journal") { onImportClick() }
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        }
        item {
            SettingsOption("Clear All Data") { onClearDataClick() }
        }

        // Privacy
        item { SectionHeader("Privacy & Security") }
        item {
            SwitchSetting("App Lock", appLockEnabled.value) {
                appLockEnabled.value = it
                onAppLockToggle(it)
            }
        }

        // Notifications
        item { SectionHeader("Notifications") }
        item {
            SwitchSetting("Daily Reminder", reminderEnabled.value) {
                reminderEnabled.value = it
                onReminderToggle(it)
            }
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        }
        item {
            SettingsOption("Reminder Time", "Set time") { onReminderTimeClick() }
        }

        // About
        item { SectionHeader("About") }
        item {
            SettingsOption("App Version", "1.0.0")
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
fun SettingsOption(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        if (!subtitle.isNullOrEmpty()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SwitchSetting(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun AccentColorPickerDialog(
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = listOf(
        Color(0xFFEF5350), // Red
        Color(0xFFAB47BC), // Purple
        Color(0xFF5C6BC0), // Indigo
        Color(0xFF29B6F6), // Light Blue
        Color(0xFF66BB6A), // Green
        Color(0xFFFFCA28), // Amber
        Color(0xFFFF7043)  // Deep Orange
    )

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Choose Accent Color") },
        text = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (color == currentColor) 3.dp else 1.dp,
                                color = if (color == currentColor) MaterialTheme.colorScheme.onSurface else Color.Gray,
                                shape = CircleShape
                            )
                            .clickable { onColorSelected(color) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
