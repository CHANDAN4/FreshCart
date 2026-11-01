package com.app.freshcart.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = FreshGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = FreshGreenContainer,
    onPrimaryContainer = FreshGreenOnContainer,
    secondary = FreshOrangeAccent,
    onSecondary = Color.White,
    secondaryContainer = FreshOrangeContainer,
    onSecondaryContainer = FreshOrangeDark,
    background = FreshBackground,
    onBackground = FreshTextPrimary,
    surface = FreshSurface,
    onSurface = FreshTextPrimary,
    surfaceVariant = FreshSurfaceVariant,
    onSurfaceVariant = FreshTextSecondary,
    outline = FreshCardBorder,
    error = StatusError,
    onError = Color.White,
    errorContainer = StatusErrorContainer,
    onErrorContainer = StatusError
)

private val DarkColorScheme = darkColorScheme(
    primary = FreshGreenLight,
    onPrimary = Color.Black,
    primaryContainer = FreshGreenDark,
    onPrimaryContainer = Color.White,
    secondary = FreshOrangeAccent,
    onSecondary = Color.Black,
    secondaryContainer = FreshOrangeDark,
    onSecondaryContainer = Color.White,
    background = Color(0xFF111712),
    onBackground = Color(0xFFE2EBE2),
    surface = Color(0xFF18221A),
    onSurface = Color(0xFFE2EBE2),
    surfaceVariant = Color(0xFF223025),
    onSurfaceVariant = Color(0xFFA5B8AA),
    outline = Color(0xFF2E3E32),
    error = StatusError,
    onError = Color.White,
    errorContainer = StatusErrorContainer,
    onErrorContainer = Color.White
)

@Composable
fun FreshCartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
