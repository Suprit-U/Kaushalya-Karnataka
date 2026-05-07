package com.kaushalyakarnataka.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Theme state holder — inject at root level
class ThemeState(initial: Boolean = false) {
    var isDark by mutableStateOf(initial)
    fun toggle() { isDark = !isDark }
}
val LocalThemeState = compositionLocalOf { ThemeState() }

private val LightColorScheme = lightColorScheme(
    primary              = Primary,
    onPrimary            = Color(0xFFFFFFFF),
    primaryContainer     = PrimaryTint,
    onPrimaryContainer   = PrimaryDark,
    secondary            = Secondary,
    onSecondary          = Color(0xFFFFFFFF),
    secondaryContainer   = SecondarySubtle,
    onSecondaryContainer = SecondaryDark,
    background           = Background,
    onBackground         = Text1,
    surface              = Surface,
    onSurface            = Text1,
    surfaceVariant       = Surface2,
    onSurfaceVariant     = Text3,
    error                = Error,
    onError              = Color(0xFFFFFFFF),
    errorContainer       = ErrorTint,
    onErrorContainer     = Color(0xFF410002),
    outline              = Border,
    outlineVariant       = BorderStrong,
    scrim                = Color(0x99000000),
)

private val DarkColorScheme = darkColorScheme(
    primary              = PrimaryLight,
    onPrimary            = Color(0xFFF8FAFC),
    primaryContainer     = Color(0xFF1D4ED8),
    onPrimaryContainer   = Color(0xFFE0EAFF),
    secondary            = Color(0xFFFF9A3D),
    onSecondary          = Color(0xFF2A1200),
    secondaryContainer   = Color(0xFF7C2D12),
    onSecondaryContainer = Color(0xFFFFE7D6),
    background           = Color(0xFF0B1120),
    onBackground         = Color(0xFFE5EDF8),
    surface              = Color(0xFF141C2F),
    onSurface            = Color(0xFFEAF1FA),
    surfaceVariant       = Color(0xFF202B40),
    onSurfaceVariant     = Color(0xFFB8C5D8),
    error                = Color(0xFFFF8A80),
    onError              = Color(0xFF330003),
    errorContainer       = Color(0xFF7F1D1D),
    onErrorContainer     = Color(0xFFFFDAD6),
    outline              = Color(0xFF4A5870),
    outlineVariant       = Color(0xFF2D3A50),
    scrim                = Color(0xCC000000),
)

@Composable
fun KaushalyaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeState: ThemeState = remember { ThemeState(darkTheme) },
    content: @Composable () -> Unit
) {
    val colorScheme = if (themeState.isDark) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !themeState.isDark
        }
    }

    CompositionLocalProvider(LocalThemeState provides themeState) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = KaushalyaTypography,
            shapes      = KaushalyaShapes,
            content     = content,
        )
    }
}
