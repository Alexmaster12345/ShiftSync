package com.example.shiftsync.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.*
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
    var selected by remember { mutableStateOf(settings.appearance) }
    var use24HourClock by remember { mutableStateOf(settings.use24HourClock) }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.height(8.dp)); HeaderWithBack("Appearance", onBack)
        AppCard { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { SegmentedOption("Dark", selected == AppearanceMode.DARK, { selected = AppearanceMode.DARK; prefs.edit().putString(KEY_APPEARANCE, selected.prefValue).apply(); onSaved() }, Modifier.weight(1f), Icons.Default.Brightness4); SegmentedOption("Light", selected == AppearanceMode.LIGHT, { selected = AppearanceMode.LIGHT; prefs.edit().putString(KEY_APPEARANCE, selected.prefValue).apply(); onSaved() }, Modifier.weight(1f), Icons.Default.Brightness5); SegmentedOption("System", selected == AppearanceMode.SYSTEM, { selected = AppearanceMode.SYSTEM; prefs.edit().putString(KEY_APPEARANCE, selected.prefValue).apply(); onSaved() }, Modifier.weight(1f), Icons.Default.Contrast) } }
        AppCard {
            SimpleRow(Icons.Default.Schedule, ShiftBlue, "Time Format", "Used for shift times on Home, Calendar, and Export")
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SegmentedOption("12-Hour (AM/PM)", !use24HourClock, {
                    use24HourClock = false
                    prefs.edit().putBoolean(KEY_USE_24_HOUR_CLOCK, false).apply()
                    onSaved()
                }, Modifier.weight(1f))
                SegmentedOption("24-Hour", use24HourClock, {
                    use24HourClock = true
                    prefs.edit().putBoolean(KEY_USE_24_HOUR_CLOCK, true).apply()
                    onSaved()
                }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Text("Native time pickers follow your device's own 12/24-hour setting.", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
