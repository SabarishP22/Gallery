package com.example.videoplayer.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AuraDarkScheme = darkColorScheme(
    primary = AuroraCyan,
    onPrimary = DeepSpace,
    secondary = AuroraViolet,
    onSecondary = TextPrimary,
    tertiary = AuroraMagenta,
    background = DeepSpace,
    onBackground = TextPrimary,
    surface = DeepSpaceSurface,
    onSurface = TextPrimary,
    surfaceVariant = DeepSpaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = GlassWhiteStrong,
)

@Composable
fun VideoPlayerTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DeepSpace.toArgb()
            window.navigationBarColor = DeepSpace.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = AuraDarkScheme,
        typography = Typography,
        content = content,
    )
}
