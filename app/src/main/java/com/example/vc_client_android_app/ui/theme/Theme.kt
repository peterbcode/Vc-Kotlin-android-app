package com.example.vc_client_android_app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = OrangePrimary,
    onPrimary = DeepBlack,
    primaryContainer = OrangeVariant,
    onPrimaryContainer = TextWhite,
    secondary = OrangeSecondary,
    onSecondary = DeepBlack,
    background = DarkGrayBg,
    onBackground = TextWhite,
    surface = SurfaceGray,
    onSurface = TextWhite,
    outline = OutlineGray,
    error = ErrorRed,
    onError = DeepBlack
)

@Composable
fun VCClientAndroidAppTheme(
    darkTheme: Boolean = true, // Force dark theme for professional look
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Removed deprecated statusBarColor. Use enableEdgeToEdge() in Activity for modern Android 15+ support.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
