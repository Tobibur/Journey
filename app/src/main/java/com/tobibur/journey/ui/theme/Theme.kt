package com.tobibur.journey.ui.theme

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.tobibur.journey.presentation.screens.settings.SettingsViewModel

// The dynamicLight/DarkColorScheme calls below are correctly guarded by a runtime
// SDK_INT >= S check, but lint's NewApi flow analysis (AGP 8.13.2) fails to honor
// the guard here in any form (compound, sole-condition, or @RequiresApi). Suppress
// the false positive; the calls are unreachable below API 31.
@SuppressLint("NewApi")
@Composable
fun JourneyTheme(
    viewModel: SettingsViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val appThemeColor by viewModel.appThemeType.collectAsState()
    val useDynamicColor by viewModel.useDynamicColor.collectAsState()
    val darkThemeEnabled by viewModel.darkThemeEnabled.collectAsState()

    val context = LocalContext.current

    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkThemeEnabled) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }

        else -> appColorScheme(appThemeColor, darkThemeEnabled)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
