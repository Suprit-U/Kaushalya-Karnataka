package com.kaushalyakarnataka.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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

@Composable
fun KaushalyaTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = KaushalyaTypography,
        shapes      = KaushalyaShapes,
        content     = content,
    )
}
