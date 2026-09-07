package com.example.shiftsync.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.shiftsync.*
import com.example.shiftsync.ui.theme.*
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun HomeScreen(settings: AppSettings, onAddManualEntry: () -> Unit, onNotifications: () -> Unit, onNavigate: (NavItem) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var entries by remember { mutableStateOf(loadEntries(prefs)) }
    LaunchedEffect(Unit) { entries = loadEntries(prefs) }
    var activeStartMillis by remember { mutableLongStateOf(prefs.getLong(KEY_ACTIVE_START_MILLIS, NO_ACTIVE_SHIFT)) }
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(activeStartMillis) { while (activeStartMillis > 0L) { delay(1000); nowMillis = System.currentTimeMillis() } }
    val elapsed = if (activeStartMillis > 0L) ((nowMillis - activeStartMillis) / 1000).coerceAtLeast(0) else 0
    val activeDurationMin = elapsed / 60
    val currency = settings.currencySymbol
    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) { in 0..11 -> "Good morning"; in 12..17 -> "Good afternoon"; else -> "Good evening" }
    val weekEntries = entriesForWeek(entries, Calendar.getInstance())
    val previousWeekEntries = entriesForWeek(entries, Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) })
    val monthEntries = entriesForMonth(entries, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH))
    val weekMinutes = weekEntries.sumOf { it.durationMinutes }
    val previousWeekMinutes = previousWeekEntries.sumOf { it.durationMinutes }
    val monthMinutes = monthEntries.sumOf { it.durationMinutes }
    val weekDiffMinutes = weekMinutes - previousWeekMinutes
    val weekDiffHours = String.format(Locale.getDefault(), "%.1f", kotlin.math.abs(weekDiffMinutes) / 60.0)
    val weekComparisonText = when {
        weekDiffMinutes > 0 -> "↑ ${weekDiffHours}h more than last week"
        weekDiffMinutes < 0 -> "↓ ${weekDiffHours}h less than last week"
        else -> "Same as last week"
    }
    val estimatedPay = PayrollCalculator.estimatePay(activeDurationMin, 0, PayrollCalculator.hourlyRate(settings), ShiftType.REGULAR, settings.overtimeEnabled, (settings.overtimeDailyThresholdHours * 60).toLong(), settings.overtimeMultiplier, settings.workDayHours, settings.salaryAmount)

    AppScaffold(bottomNav = NavItem.Home, onNavigate = onNavigate) { padding ->
        LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = padding.calculateTopPadding() + 18.dp, bottom = padding.calculateBottomPadding() + 96.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("ShiftSync", fontWeight = FontWeight.Bold, fontSize = 30.sp, color = AppText)
                        Text("$greeting, ${settings.displayName}", color = TextSecondary)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(42.dp).clip(CircleShape).background(CardBackground).clickable(onClick = onNotifications), contentAlignment = Alignment.Center) { Icon(Icons.Default.NotificationsNone, null, tint = AppText) }
                        Box(Modifier.size(42.dp).clip(CircleShape).background(ShiftBlue), contentAlignment = Alignment.Center) { Text(settings.displayName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = ShiftBlue), shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("ACTIVE SHIFT", color = Color.White.copy(.75f), fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
                            Icon(Icons.Default.Schedule, null, tint = Color.White)
                        }
                        Text(String.format("%02d:%02d:%02d", elapsed / 3600, (elapsed % 3600) / 60, elapsed % 60), color = Color.White, fontSize = 38.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MiniStatBox("Started at", if (activeStartMillis > 0L) formatTime12(activeStartMillis) else "--:--", Modifier.weight(1f))
                            MiniStatBox("Estimated Pay", formatCurrency(estimatedPay, currency), Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = {
                            if (activeStartMillis <= 0L) {
                                val start = System.currentTimeMillis(); activeStartMillis = start; prefs.edit().putLong(KEY_ACTIVE_START_MILLIS, start).apply(); context.startClockService(ClockForegroundService.ACTION_START); ShiftAlertHelper.fireShiftStartAlert(context)
                            } else {
                                val settingsNow = loadSettings(prefs)
                                val hourly = PayrollCalculator.hourlyRate(settingsNow)
                                val entry = ShiftEntry(activeStartMillis, ShiftType.REGULAR, activeDurationMin, 0, hourly, PayrollCalculator.estimatePay(activeDurationMin, 0, hourly, ShiftType.REGULAR, settingsNow.overtimeEnabled, (settingsNow.overtimeDailyThresholdHours * 60).toLong(), settingsNow.overtimeMultiplier, settingsNow.workDayHours, settingsNow.salaryAmount))
                                saveEntries(prefs, listOf(entry) + loadEntries(prefs)); prefs.edit().remove(KEY_ACTIVE_START_MILLIS).apply(); activeStartMillis = NO_ACTIVE_SHIFT; entries = loadEntries(prefs); context.startClockService(ClockForegroundService.ACTION_STOP); ShiftAlertHelper.fireShiftEndAlert(context, activeDurationMin)
                            }
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().height(54.dp)) { Text(if (activeStartMillis > 0L) "Clock Out" else "Clock In", color = ShiftBlue, fontWeight = FontWeight.Bold) }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard("This Week", Icons.Default.CalendarMonth, formatDuration(weekMinutes), weekComparisonText, GreenAccent, Modifier.weight(1f))
                    SummaryCard("This Month", Icons.Default.GridView, formatDuration(monthMinutes), "${monthEntries.size} shifts total", TextSecondary, Modifier.weight(1f))
                }
            }
            item { SectionTitle("RECENT ACTIVITY", "View All") }
            items(entries.take(10)) { entry -> RecentEntryRow(entry, currency) }
            if (entries.isEmpty()) item { AppCard { Text("No entries yet", fontWeight = FontWeight.SemiBold); Text("Use the + button to add your first shift.", color = TextSecondary) } }
        }
    }
}

private fun entriesForWeek(entries: List<ShiftEntry>, reference: Calendar): List<ShiftEntry> {
    val weekStart = reference.clone() as Calendar
    weekStart.set(Calendar.DAY_OF_WEEK, weekStart.firstDayOfWeek)
    weekStart.set(Calendar.HOUR_OF_DAY, 0)
    weekStart.set(Calendar.MINUTE, 0)
    weekStart.set(Calendar.SECOND, 0)
    weekStart.set(Calendar.MILLISECOND, 0)
    val weekEnd = weekStart.clone() as Calendar
    weekEnd.add(Calendar.DAY_OF_YEAR, 7)
    return entries.filter { it.startedAtMillis >= weekStart.timeInMillis && it.startedAtMillis < weekEnd.timeInMillis }
}

@Composable private fun MiniStatBox(label: String, value: String, modifier: Modifier) = Surface(modifier = modifier, color = Color.White.copy(.16f), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(14.dp)) { Text(label, color = Color.White.copy(.75f), fontSize = 12.sp); Text(value, color = Color.White, fontWeight = FontWeight.Bold) } }
@Composable private fun SummaryCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, note: String, noteColor: Color, modifier: Modifier) = AppCard(modifier) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = ShiftBlue); Spacer(Modifier.width(8.dp)); Text(title, fontWeight = FontWeight.SemiBold) }; Spacer(Modifier.height(12.dp)); Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(4.dp)); Text(note, color = noteColor, fontSize = 12.sp) }
@Composable private fun RecentEntryRow(entry: ShiftEntry, currency: String) = AppCard(Modifier.fillMaxWidth()) { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(40.dp).clip(CircleShape).background(if (entry.shiftType == ShiftType.VACATION) GreenAccent.copy(.14f) else ShiftBlue.copy(.14f)), contentAlignment = Alignment.Center) { Icon(if (entry.shiftType == ShiftType.NIGHT) Icons.Default.Bedtime else if (entry.shiftType == ShiftType.VACATION) Icons.Default.WbSunny else Icons.Default.Schedule, null, tint = if (entry.shiftType == ShiftType.VACATION) GreenAccent else ShiftBlue) }; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(if (Calendar.getInstance().apply { timeInMillis = entry.startedAtMillis }.let { it.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR) - 1 }) "Yesterday" else formatEntryDate(entry.startedAtMillis), fontWeight = FontWeight.SemiBold); Text(if (entry.shiftType == ShiftType.VACATION) "Paid day off" else "${formatTime12(entry.startedAtMillis)} - ${formatTime12(entry.startedAtMillis + entry.durationMinutes * 60000)}", color = TextSecondary, fontSize = 13.sp) }; Column(horizontalAlignment = Alignment.End) { Text(formatDuration(entry.durationMinutes), fontWeight = FontWeight.Bold); Text(formatCurrency(entry.estimatedPay, currency), color = GreenAccent, fontWeight = FontWeight.SemiBold) } } }
private fun Context.startClockService(action: String) { ContextCompat.startForegroundService(this, Intent(this, ClockForegroundService::class.java).apply { this.action = action }) }
