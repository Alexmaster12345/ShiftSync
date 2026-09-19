package com.example.shiftsync.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.shiftsync.*
import com.example.shiftsync.ui.theme.*

@Composable
fun AppearanceScreen(settings: AppSettings, onBack: () -> Unit, onSaved: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    // Theme, Time Format, and Text Size are held as a local draft instead of writing straight
    // to prefs on every tap, so browsing options doesn't change the live app until Save.
    var selected by remember { mutableStateOf(settings.appearance) }
    var use24HourClock by remember { mutableStateOf(settings.use24HourClock) }
    var textSizeIndex by remember { mutableIntStateOf(settings.uiTextSizeIndex) }
    var saved by remember { mutableStateOf(false) }

    val dirty = selected != settings.appearance || use24HourClock != settings.use24HourClock || textSizeIndex != settings.uiTextSizeIndex

    fun save() {
        prefs.edit()
            .putString(KEY_APPEARANCE, selected.prefValue)
            .putBoolean(KEY_USE_24_HOUR_CLOCK, use24HourClock)
            .putInt(KEY_UI_TEXT_SIZE_INDEX, textSizeIndex)
            .apply()
        onSaved()
        saved = true
    }

    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.height(8.dp)); HeaderWithBack("Appearance", onBack)
        AppCard { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { SegmentedOption("Dark", selected == AppearanceMode.DARK, { selected = AppearanceMode.DARK }, Modifier.weight(1f), Icons.Default.Brightness4); SegmentedOption("Light", selected == AppearanceMode.LIGHT, { selected = AppearanceMode.LIGHT }, Modifier.weight(1f), Icons.Default.Brightness5); SegmentedOption("System", selected == AppearanceMode.SYSTEM, { selected = AppearanceMode.SYSTEM }, Modifier.weight(1f), Icons.Default.Contrast) } }
        AppCard {
            SimpleRow(Icons.Default.Schedule, ShiftBlue, "Time Format", "Used for shift times on Home, Calendar, and Export")
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SegmentedOption("12-Hour (AM/PM)", !use24HourClock, { use24HourClock = false }, Modifier.weight(1f))
                SegmentedOption("24-Hour", use24HourClock, { use24HourClock = true }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Text("Native time pickers follow your device's own 12/24-hour setting.", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        AppCard {
            SimpleRow(Icons.Default.FormatSize, GreenAccent, "Text Size", "Makes ShiftSync's own text bigger or smaller")
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (textSizeIndex > 0) textSizeIndex-- }, enabled = textSizeIndex > 0) {
                    Icon(Icons.Default.Remove, "Decrease text size", tint = if (textSizeIndex > 0) GreenAccent else TextMuted)
                }
                Text(
                    AppSettings.TEXT_SIZE_LABELS.getOrElse(textSizeIndex) { "Default" },
                    fontWeight = FontWeight.SemiBold, color = AppText, fontSize = 14.sp,
                    maxLines = 1, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                IconButton(onClick = { if (textSizeIndex < AppSettings.TEXT_SIZE_SCALE_STEPS.size - 1) textSizeIndex++ }, enabled = textSizeIndex < AppSettings.TEXT_SIZE_SCALE_STEPS.size - 1) {
                    Icon(Icons.Default.Add, "Increase text size", tint = if (textSizeIndex < AppSettings.TEXT_SIZE_SCALE_STEPS.size - 1) GreenAccent else TextMuted)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Independent of your device's own display-size setting — this only affects ShiftSync.",
                color = TextMuted, fontSize = 10.sp
            )
        }
        SaveChangesButton(saved = saved, enabled = dirty, onClick = ::save)
    }
}
