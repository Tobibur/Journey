package com.tobibur.journey.presentation.screens.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.tobibur.journey.data.ExportType
import com.tobibur.journey.data.ImportState
import com.tobibur.journey.presentation.components.ExportDialog
import com.tobibur.journey.presentation.components.JourneyTopAppBar
import com.tobibur.journey.ui.theme.AppThemeType
import com.tobibur.journey.utils.BiometricAuthManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onImportClick: () -> Unit = {},
    onClearDataClick: () -> Unit = {},
    onReminderToggle: (Boolean) -> Unit = {},
    onReminderTimeClick: () -> Unit = {},
    onAppLockToggle: (Boolean) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
    setTopBar: (@Composable (() -> Unit)) -> Unit = {}
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Check biometric availability
    val isBiometricAvailable = remember { BiometricAuthManager.isBiometricAvailable(context) }

    // Export state
    val exportState by viewModel.exportState.collectAsState()

    // Import state
    val importState by viewModel.importState.collectAsState()

    val reminderEnabled = remember { mutableStateOf(true) }

    val appThemeColor by viewModel.appThemeType.collectAsState()
    val useDynamicColor by viewModel.useDynamicColor.collectAsState()
    val darkThemeEnabled by viewModel.darkThemeEnabled.collectAsState()
    val appLockEnabled by viewModel.appLockEnabled.collectAsState()

    var showColorPicker by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportLoader by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        setTopBar {
            JourneyTopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }

    // Handle export state changes
    LaunchedEffect(exportState) {
        when (val state = exportState) {
            is ExportUiState.Loading -> {

            }

            is ExportUiState.Success -> {
                snackbarHostState.showSnackbar(
                    message = "Exported ${state.entryCount} entries to Downloads"
                )

                val type =
                    if (state.type == ExportType.PDF) "application/pdf" else "application/json"
                // Open PDF
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(state.uri, type)
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    if (state.type == ExportType.PDF)
                        snackbarHostState.showSnackbar("No PDF viewer app found")
                    else
                        snackbarHostState.showSnackbar("No JSON viewer app found")
                }
                viewModel.resetExportState()
            }

            is ExportUiState.NoEntries -> {
                snackbarHostState.showSnackbar("No journal entries to export")
                viewModel.resetExportState()
            }

            is ExportUiState.Error -> {
                snackbarHostState.showSnackbar("Export failed: ${state.message}")
                viewModel.resetExportState()
            }

            else -> {}
        }
    }

    // Handle import state changes
    LaunchedEffect(importState) {
        when (val state = importState) {
            is ImportState.Loading -> {
                showImportLoader = true
            }

            is ImportState.Success -> {
                snackbarHostState.showSnackbar(
                    message = "Imported ${state.count} entries successfully"
                )
                viewModel.resetImportState()
            }

            is ImportState.NoEntries -> {
                snackbarHostState.showSnackbar("No entries found in the file")
                viewModel.resetImportState()
            }

            is ImportState.Error -> {
                snackbarHostState.showSnackbar("Import failed: ${state.message}")
                viewModel.resetImportState()
            }

            is ImportState.Idle -> {
                showImportLoader = false
            }

        }
    }




    if (showColorPicker) {
        AccentColorPickerDialog(
            currentColor = appThemeColor.name,
            onColorSelected = {
                viewModel.setAccentColor(it) // Save ARGB int
                viewModel.setDynamicColor(false)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    if (showImportLoader) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val bytes = context.contentResolver.openInputStream(it)?.readBytes()
            viewModel.importFromJson(bytes)
        }
    }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val dividerPadding = Modifier.padding(vertical = 8.dp)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
        ) {
            // Personalization
            item { SectionHeader("Personalization") }
            item {
                SettingsOption("Theme Color", "Tap to change") {
                    showColorPicker = true
                }
                HorizontalDivider(dividerPadding, DividerDefaults.Thickness, DividerDefaults.color)
                SwitchSetting("Use Dynamic Theme", useDynamicColor) {
                    viewModel.setDynamicColor(it)
                }
                HorizontalDivider(dividerPadding, DividerDefaults.Thickness, DividerDefaults.color)
                SwitchSetting("Dark Theme", darkThemeEnabled) {
                    viewModel.setDarkTheme(it)
                }
            }

            // Data & Backup
            item { SectionHeader("Data & Backup") }
            item {
                SettingsOption("Export Journal", "Save as PDF to Downloads") {
                    showExportDialog = true
                }
                HorizontalDivider(dividerPadding, DividerDefaults.Thickness, DividerDefaults.color)
            }
            item {
                SettingsOption("Import Journal", "Upload json file to add entries") {


                    // Trigger with:
                    launcher.launch(arrayOf("application/json"))
                }
                HorizontalDivider(dividerPadding, DividerDefaults.Thickness, DividerDefaults.color)
            }
            item {
                SettingsOption("Clear All Data") { onClearDataClick() }
            }

            // Privacy
            item { SectionHeader("Privacy & Security") }
            item {
                SwitchSetting(
                    title = "App Lock",
                    checked = appLockEnabled,
                    enabled = isBiometricAvailable,
                    onDisabledClick = {
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Biometric authentication not set up",
                                actionLabel = "Open Settings",
                                withDismissAction = true
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                // Open device security settings
                                val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                                context.startActivity(intent)
                            }
                        }
                    }
                ) { enabled ->
                    if (enabled) {
                        viewModel.setAppLockEnabled(true)
                        onAppLockToggle(true)
                    } else {
                        viewModel.setAppLockEnabled(false)
                        onAppLockToggle(false)
                    }
                }
            }

            // Notifications
            item { SectionHeader("Notifications") }
            item {
                SwitchSetting("Daily Reminder", reminderEnabled.value) {
                    reminderEnabled.value = it
                    onReminderToggle(it)
                }
                HorizontalDivider(dividerPadding, DividerDefaults.Thickness, DividerDefaults.color)
            }
            item {
                SettingsOption("Reminder Time", "Set time") { onReminderTimeClick() }
            }

            // About
            item { SectionHeader("About") }
            item {
                SettingsOption("App Version", "1.0.0")
                HorizontalDivider(dividerPadding, DividerDefaults.Thickness, DividerDefaults.color)
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showExportDialog) {
        ExportDialog(
            isLoading = exportState is ExportUiState.Loading,
            onAccept = { type ->
                if (type == "PDF")
                    viewModel.exportPDFJournal()
                else
                    viewModel.exportJsonJournal()
            },
            onDismiss = {
                showExportDialog = false
            }
        )
        if (exportState is ExportUiState.Success || exportState is ExportUiState.Error ||
            exportState is ExportUiState.NoEntries
        ) {
            showExportDialog = false
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
        )
    }
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
            .padding(horizontal = 16.dp)
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
    enabled: Boolean = true,
    onDisabledClick: (() -> Unit)? = null,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !enabled) {
                onDisabledClick?.invoke()
            }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            }
        )
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun AccentColorPickerDialog(
    currentColor: String,
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = mapOf<Color, String>(
        Color(0xFFEF5350) to AppThemeType.RED.name,// Red
        Color(0xFFAB47BC) to AppThemeType.PURPLE.name, // Purple
        Color(0xFF5C6BC0) to AppThemeType.INDIGO.name, // Indigo
        Color(0xFF29B6F6) to AppThemeType.LIGHT_BLUE.name, // Light Blue
        Color(0xFF66BB6A) to AppThemeType.GREEN.name, // Green
        Color(0xFFFFCA28) to AppThemeType.AMBER.name, // Amber
        Color(0xFFFF7043) to AppThemeType.DEEP_ORANGE.name,  // Deep Orange
        Color(0xFFFFB6C1) to AppThemeType.PINK.name// Light Pink
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
                            .background(color.key)
                            .border(
                                width = if (color.value == currentColor) 3.dp else 1.dp,
                                color = if (color.value == currentColor) MaterialTheme.colorScheme.onSurface else Color.Gray,
                                shape = CircleShape
                            )
                            .clickable { onColorSelected(color.value) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
