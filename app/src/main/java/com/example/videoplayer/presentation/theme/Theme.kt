package com.example.videoplayer.presentation.theme

import android.app.Activity
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
    onSurfaceVariant = TextSecondary,
    surfaceVariant = DeepSpaceElevated,
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
        content = {
            CompositionLocalProvider(
                LocalContentColor provides TextPrimary,
            ) {
                content()
            }
        },
    )
}

object AuraIconDefaults {
    @Composable
    fun iconButtonColors() = IconButtonDefaults.iconButtonColors(
        contentColor = TextPrimary,
        disabledContentColor = TextSecondary,
    )
}
