
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
    val spaceXSmall: Dp,      // 3dp baseline
    val spaceSmall: Dp,       // 6dp baseline
    val spaceMedium: Dp,      // 12dp baseline
    val spaceLarge: Dp,       // 16dp baseline
    val spaceXLarge: Dp,      // 20dp baseline
    val spaceXXLarge: Dp,     // 28dp baseline

    // ── Horizontal padding ────────────────────────────────────────
    val screenPadding: Dp,    // 16dp baseline

    // ── Component sizes ───────────────────────────────────────────
    val iconSmall: Dp,        // 14dp baseline
    val iconMedium: Dp,       // 18dp baseline
    val iconLarge: Dp,        // 22dp baseline
    val iconXLarge: Dp,       // 28dp baseline
    val avatarSmall: Dp,      // 34dp baseline
    val avatarLarge: Dp,      // 78dp baseline
    val buttonHeight: Dp,     // 44dp baseline
    val cardRadius: Dp,       // 14dp baseline

    // ── Font sizes ────────────────────────────────────────────────
    val fontCaption: TextUnit,    // 9sp baseline
    val fontSmall: TextUnit,      // 11sp baseline
    val fontBody: TextUnit,       // 13sp baseline
    val fontButton: TextUnit,     // 14sp baseline
    val fontTitle: TextUnit,      // 20sp baseline
    val fontHeadline: TextUnit,   // 24sp baseline
    val fontHero: TextUnit,       // 30sp baseline

    // ── Week day strip ────────────────────────────────────────────
    val weekDayLabelSize: TextUnit,  // 7sp baseline
    val weekDayNumberSize: TextUnit, // 13sp baseline

    // ── Stats card value ──────────────────────────────────────────
    val statsValueSize: TextUnit,    // 18sp baseline

    // ── Bottom nav ────────────────────────────────────────────────
    val navIconSize: Dp,      // 18dp baseline
    val navLabelSize: TextUnit // 9sp baseline
)

val LocalDimens = compositionLocalOf { defaultDimens() }

private fun defaultDimens() = createDimens(1.0f)

private fun createDimens(scale: Float): Dimens {
    // Clamp scale to avoid extremes
    val s = scale.coerceIn(0.75f, 1.35f)
    return Dimens(
        scaleFactor = s,

        spaceXSmall  = (3 * s).dp,
        spaceSmall   = (6 * s).dp,
        spaceMedium  = (12 * s).dp,
        spaceLarge   = (16 * s).dp,
        spaceXLarge  = (20 * s).dp,
        spaceXXLarge = (28 * s).dp,

        screenPadding = (16 * s).dp,

        iconSmall  = (14 * s).dp,
        iconMedium = (18 * s).dp,
        iconLarge  = (22 * s).dp,
        iconXLarge = (28 * s).dp,
        avatarSmall = (34 * s).dp,
        avatarLarge = (78 * s).dp,
        buttonHeight = (44 * s).dp,
        cardRadius = (14 * s).dp,

        fontCaption  = (9 * s).sp,
        fontSmall    = (11 * s).sp,
        fontBody     = (13 * s).sp,
        fontButton   = (14 * s).sp,
        fontTitle    = (20 * s).sp,
        fontHeadline = (24 * s).sp,
        fontHero     = (30 * s).sp,

        weekDayLabelSize  = (7 * s).sp,
        weekDayNumberSize = (13 * s).sp,

        statsValueSize = (18 * s).sp,

        navIconSize  = (18 * s).dp,
        navLabelSize = (9 * s).sp
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
