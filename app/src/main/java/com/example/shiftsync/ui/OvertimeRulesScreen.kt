package com.example.shiftsync.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiftsync.*
import com.example.shiftsync.ui.theme.*

@Composable
fun OvertimeRulesScreen(settings: AppSettings, onBack: () -> Unit, onSaved: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var enabled by remember { mutableStateOf(settings.overtimeEnabled) }
    var daily by remember { mutableDoubleStateOf(settings.overtimeDailyThresholdHours) }
    var weekly by remember { mutableDoubleStateOf(settings.overtimeWeeklyThresholdHours) }
    var multiplier by remember { mutableDoubleStateOf(settings.overtimeMultiplier) }
    fun persist() { prefs.edit().putBoolean(KEY_OVERTIME_ENABLED, enabled).putFloat(KEY_OVERTIME_DAILY_THRESHOLD_HOURS, daily.toFloat()).putFloat(KEY_OVERTIME_WEEKLY_THRESHOLD_HOURS, weekly.toFloat()).putFloat(KEY_OVERTIME_MULTIPLIER, multiplier.toFloat()).apply(); onSaved() }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.height(8.dp)); HeaderWithBack("Overtime Rules", onBack)
        AppCard { SimpleRow(Icons.Default.TrendingUp, OrangeAccent, "Overtime Rules", if (enabled) "Auto-splits shifts when threshold is exceeded" else "Off", trailing = { Switch(checked = enabled, onCheckedChange = { enabled = it; persist() }) }) }
        if (enabled) {
            AppCard { SimpleRow(Icons.Default.Schedule, ShiftBlue, "Daily Threshold", "Shifts longer than this get split", trailing = { Stepper("${daily}h", { daily = (daily - 0.5).coerceAtLeast(1.0); persist() }, { daily += 0.5; persist() }) }); HorizontalDivider(color = BorderColor); SimpleRow(Icons.Default.DateRange, GreenAccent, "Weekly Threshold", "Used to flag weeks with excess hours", trailing = { Stepper("${weekly.toInt()}h", { weekly = (weekly - 1).coerceAtLeast(1.0); persist() }, { weekly += 1; persist() }, GreenAccent) }) }
            AppCard { Text("Overtime Multiplier", fontWeight = FontWeight.Bold); Text("Pay rate for overtime hours", color = TextSecondary, fontSize = 13.sp); Spacer(Modifier.height(12.dp)); Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { listOf(1.2, 1.5, 2.0).forEach { value -> SegmentedOption("${value}×", multiplier == value, { multiplier = value; persist() }, Modifier.weight(1f), selectedColor = GreenAccent) } } }
            AppCard { Text("ℹ️ When you clock out after ${daily}h, ShiftSync automatically splits your shift: the first ${daily}h at regular pay and the rest at ${multiplier}× pay.", color = ShiftBlue, lineHeight = 20.sp) }
        }
    }
}
