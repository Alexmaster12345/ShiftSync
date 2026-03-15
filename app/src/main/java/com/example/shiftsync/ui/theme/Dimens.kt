
package com.example.shiftsync.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Responsive dimensions that scale proportionally to the screen width.
 *
 * Design baseline: 392dp wide (Pixel 4 / typical mid-range phone).
 * On smaller screens (e.g. 320dp Nexus S) values shrink proportionally.
 * On larger screens (e.g. 600dp+ tablets) values grow but are capped.
 *
 * Usage:
 *   val dimens = LocalDimens.current
 *   Text(fontSize = dimens.fontTitle)
 *   Spacer(Modifier.height(dimens.spaceMedium))
 */
data class Dimens(
    // ── Scale factor ──────────────────────────────────────────────
    val scaleFactor: Float,

    // ── Spacing ───────────────────────────────────────────────────
    val spaceXSmall: Dp,      // 4dp baseline
    val spaceSmall: Dp,       // 8dp baseline
    val spaceMedium: Dp,      // 16dp baseline
    val spaceLarge: Dp,       // 20dp baseline
    val spaceXLarge: Dp,      // 24dp baseline
    val spaceXXLarge: Dp,     // 32dp baseline

    // ── Horizontal padding ────────────────────────────────────────
    val screenPadding: Dp,    // 20dp baseline

    // ── Component sizes ───────────────────────────────────────────
    val iconSmall: Dp,        // 16dp baseline
    val iconMedium: Dp,       // 20dp baseline
    val iconLarge: Dp,        // 24dp baseline
    val iconXLarge: Dp,       // 32dp baseline
    val avatarSmall: Dp,      // 40dp baseline
    val avatarLarge: Dp,      // 90dp baseline
    val buttonHeight: Dp,     // 50dp baseline
    val cardRadius: Dp,       // 16dp baseline

    // ── Font sizes ────────────────────────────────────────────────
    val fontCaption: TextUnit,    // 10sp baseline
    val fontSmall: TextUnit,      // 12sp baseline
    val fontBody: TextUnit,       // 14sp baseline
    val fontButton: TextUnit,     // 16sp baseline
    val fontTitle: TextUnit,      // 22sp baseline
    val fontHeadline: TextUnit,   // 26sp baseline
    val fontHero: TextUnit,       // 34sp baseline

    // ── Week day strip ────────────────────────────────────────────
    val weekDayLabelSize: TextUnit,  // 8sp baseline
    val weekDayNumberSize: TextUnit, // 15sp baseline

    // ── Stats card value ──────────────────────────────────────────
    val statsValueSize: TextUnit,    // 22sp baseline

    // ── Bottom nav ────────────────────────────────────────────────
    val navIconSize: Dp,      // 22dp baseline
    val navLabelSize: TextUnit // 10sp baseline
)

val LocalDimens = compositionLocalOf { defaultDimens() }

private fun defaultDimens() = createDimens(1.0f)

private fun createDimens(scale: Float): Dimens {
    // Clamp scale to avoid extremes
    val s = scale.coerceIn(0.75f, 1.35f)
    return Dimens(
        scaleFactor = s,

        spaceXSmall  = (4 * s).dp,
        spaceSmall   = (8 * s).dp,
        spaceMedium  = (16 * s).dp,
        spaceLarge   = (20 * s).dp,
        spaceXLarge  = (24 * s).dp,
        spaceXXLarge = (32 * s).dp,

        screenPadding = (20 * s).dp,

        iconSmall  = (16 * s).dp,
        iconMedium = (20 * s).dp,
        iconLarge  = (24 * s).dp,
        iconXLarge = (32 * s).dp,
        avatarSmall = (40 * s).dp,
        avatarLarge = (90 * s).dp,
        buttonHeight = (50 * s).dp,
        cardRadius = (16 * s).dp,

        fontCaption  = (10 * s).sp,
        fontSmall    = (12 * s).sp,
        fontBody     = (14 * s).sp,
        fontButton   = (16 * s).sp,
        fontTitle    = (22 * s).sp,
        fontHeadline = (26 * s).sp,
        fontHero     = (34 * s).sp,

        weekDayLabelSize  = (8 * s).sp,
        weekDayNumberSize = (15 * s).sp,

        statsValueSize = (22 * s).sp,

        navIconSize  = (22 * s).dp,
        navLabelSize = (10 * s).sp
    )
}

/**
 * Call this at the root of your Compose tree (e.g. inside ShiftSyncTheme).
 * It measures the current screen width and provides scaled [Dimens].
 */
@Composable
fun ProvideDimens(content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    val dimens = remember(screenWidthDp) {
        val baselineWidth = 392f
        val scale = screenWidthDp / baselineWidth
        createDimens(scale)
    }

    CompositionLocalProvider(LocalDimens provides dimens) {
        content()
    }
}

