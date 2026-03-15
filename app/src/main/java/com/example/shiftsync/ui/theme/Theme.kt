package com.example.shiftsync.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AppColorScheme = darkColorScheme(
    primary            = ShiftBlue,
    onPrimary          = TextPrimary,
    primaryContainer   = DarkCardAlt,
    onPrimaryContainer = TextPrimary,
    secondary          = ShiftBlueLight,
    onSecondary        = TextPrimary,
    background         = DarkBg,
    onBackground       = TextPrimary,
    surface            = DarkSurface,
    onSurface          = TextPrimary,
    surfaceVariant     = DarkCard,
    onSurfaceVariant   = TextSecondary,
    outline            = TextMuted,
    error              = RedAccent,
)

@Composable
fun ShiftSyncTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography  = Typography,
        content     = {
            ProvideDimens {
                content()
            }
        }
    )
}