package com.tobibur.journey.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.tobibur.journey.presentation.screens.settings.SettingsViewModel

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)


private fun bestTextColor(bg: Color): Color {
    return if (bg.luminance() > 0.5) Color.Black else Color.White
}

@Composable
fun JourneyTheme(
    viewModel: SettingsViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val accentColorInt by viewModel.accentColor.collectAsState()
    val useDynamicColor by viewModel.useDynamicColor.collectAsState()
    val darkThemeEnabled by viewModel.darkThemeEnabled.collectAsState()

    val accentColor = Color(accentColorInt)
    val context = LocalContext.current

    fun customLightColors(accent: Color) = lightColorScheme(
        primary = accent,
        onPrimary = bestTextColor(accent),
        primaryContainer = accent.copy(alpha = 0.9f),
        onPrimaryContainer = bestTextColor(accent.copy(alpha = 0.9f)),

        secondary = accent.copy(alpha = 0.85f),
        onSecondary = bestTextColor(accent.copy(alpha = 0.85f)),
        secondaryContainer = accent.copy(alpha = 0.75f),
        onSecondaryContainer = bestTextColor(accent.copy(alpha = 0.75f)),

        tertiary = accent.copy(alpha = 0.7f),
        onTertiary = bestTextColor(accent.copy(alpha = 0.7f)),
        tertiaryContainer = accent.copy(alpha = 0.6f),
        onTertiaryContainer = bestTextColor(accent.copy(alpha = 0.6f)),

        background = Color.White,
        onBackground = Color.Black,
        surface = Color.White,
        onSurface = Color.Black,
        surfaceVariant = Color(0xFFEDEDED),
        onSurfaceVariant = Color(0xFF333333),

        outline = accent.copy(alpha = 0.5f)
    )

    fun customDarkColors(accent: Color) = darkColorScheme(
        primary = accent,
        onPrimary = bestTextColor(accent),
        primaryContainer = accent.copy(alpha = 0.9f),
        onPrimaryContainer = bestTextColor(accent.copy(alpha = 0.9f)),

        secondary = accent.copy(alpha = 0.85f),
        onSecondary = bestTextColor(accent.copy(alpha = 0.85f)),
        secondaryContainer = accent.copy(alpha = 0.75f),
        onSecondaryContainer = bestTextColor(accent.copy(alpha = 0.75f)),

        tertiary = accent.copy(alpha = 0.7f),
        onTertiary = bestTextColor(accent.copy(alpha = 0.7f)),
        tertiaryContainer = accent.copy(alpha = 0.6f),
        onTertiaryContainer = bestTextColor(accent.copy(alpha = 0.6f)),

        background = Color(0xFF121212),
        onBackground = Color.White,
        surface = Color(0xFF1E1E1E),
        onSurface = Color.White,
        surfaceVariant = Color(0xFF2C2C2C),
        onSurfaceVariant = Color(0xFFE0E0E0),

        outline = accent.copy(alpha = 0.5f)
    )

    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkThemeEnabled) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }

        darkThemeEnabled -> customDarkColors(accentColor)
        else -> customLightColors(accentColor)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
