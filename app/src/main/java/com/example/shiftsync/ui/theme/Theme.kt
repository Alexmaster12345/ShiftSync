package com.example.shiftsync.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.shiftsync.AppearanceMode

private val LightScheme = lightColorScheme(
    primary = ShiftBlue,
    secondary = ShiftBlueLight,
    tertiary = GreenAccent,
    background = LightBackground,
    surface = CardBackground,
    surfaceVariant = CardBackgroundAlt,
    onPrimary = CardBackground,
    onBackground = AppText,
    onSurface = AppText,
    onSurfaceVariant = TextSecondary,
    error = RedAccent,
    outline = BorderColor
)

@Composable
fun ShiftSyncTheme(
    appearanceMode: AppearanceMode = AppearanceMode.LIGHT,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = LightBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }
    MaterialTheme(
        colorScheme = LightScheme,
        typography = Typography,
        content = { ProvideDimens { content() } }
    )
}
