package com.kaushalyakarnataka.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.kaushalyakarnataka.app.data.local.ThemePreferenceManager

// Theme state holder — reads from DataStore, persists on toggle
class ThemeState(
    initial: Boolean = false,
    private val preferenceManager: ThemePreferenceManager? = null
) {
    var isDark by mutableStateOf(initial)

    suspend fun toggle() {
        val newValue = !isDark
        isDark = newValue
        preferenceManager?.setDarkMode(newValue)
    }
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
    primaryContainer     = Color(0xFF1E40AF),
    onPrimaryContainer   = Color(0xFFDBEAFE),
    secondary            = Color(0xFFFB923C),
    onSecondary          = Color(0xFF2A1200),
    secondaryContainer   = Color(0xFF9A3412),
    onSecondaryContainer = Color(0xFFFFEDD5),
    background           = DarkBackground,
    onBackground         = DarkOnBackground,
    surface              = DarkSurface,
    onSurface            = DarkOnSurface,
    surfaceVariant       = DarkSurfaceVariant,
    onSurfaceVariant     = DarkOnSurfaceVariant,
    error                = Color(0xFFFF8A80),
    onError              = Color(0xFF330003),
    errorContainer       = Color(0xFF7F1D1D),
    onErrorContainer     = Color(0xFFFFDAD6),
    outline              = DarkOutline,
    outlineVariant       = DarkOutlineVariant,
    scrim                = Color(0xE6000000),
    inverseSurface       = DarkInverseSurface,
    inverseOnSurface     = DarkInverseOnSurface,
    inversePrimary       = DarkInversePrimary,
    surfaceTint          = PrimaryLight,
    surfaceDim           = DarkSurfaceDim,
    surfaceBright        = DarkSurfaceBright,
    surfaceContainer     = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
)

@Composable
fun KaushalyaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    preferenceManager: ThemePreferenceManager? = null,
    content: @Composable () -> Unit
) {
    // Read persisted preference; fallback to system default
    val persistedDark by preferenceManager?.isDarkMode?.collectAsState(initial = darkTheme)
        ?: remember { mutableStateOf(darkTheme) }

    val themeState = remember(persistedDark, preferenceManager) {
        ThemeState(persistedDark, preferenceManager)
    }

    // Sync state if preference changes externally
    LaunchedEffect(persistedDark) {
        themeState.isDark = persistedDark
    }

    val colorScheme = if (themeState.isDark) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
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
