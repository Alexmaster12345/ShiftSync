package com.example.shiftsync.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiftsync.*
import com.example.shiftsync.ui.theme.*
import java.text.DateFormatSymbols
import java.util.*

@Composable
fun ManualEntryScreen(settings: AppSettings, onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val cal = remember { Calendar.getInstance() }
    var dateMillis by remember { mutableLongStateOf(cal.timeInMillis) }
    var shiftType by remember { mutableStateOf(ShiftType.REGULAR) }
    var startHour by remember { mutableIntStateOf(9) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(17) }
    var endMinute by remember { mutableIntStateOf(0) }
    var breakMinutes by remember { mutableIntStateOf(30) }
    var notes by remember { mutableStateOf("") }
    val currentDate = Calendar.getInstance().apply { timeInMillis = dateMillis }
    val duration = (((endHour * 60 + endMinute) - (startHour * 60 + startMinute)).takeIf { it > 0 } ?: (8 * 60)).toLong()
    val hourlyRate = PayrollCalculator.hourlyRate(settings)
    val pay = PayrollCalculator.estimatePay(duration, breakMinutes, hourlyRate, shiftType, settings.overtimeEnabled, (settings.overtimeDailyThresholdHours * 60).toLong(), settings.overtimeMultiplier, settings.workDayHours, settings.salaryAmount)
    val monthNames = DateFormatSymbols().months.toList()
    val grid = remember(currentDate.get(Calendar.MONTH), currentDate.get(Calendar.YEAR)) {
        val tmp = Calendar.getInstance().apply { set(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), 1) }
        val offset = (tmp.get(Calendar.DAY_OF_WEEK) + 5) % 7
        MutableList<Int?>(offset) { null }.apply { repeat(tmp.getActualMaximum(Calendar.DAY_OF_MONTH)) { add(it + 1) } }
    }
    fun save() {
        val entry = ShiftEntry(
            startedAtMillis = Calendar.getInstance().apply { timeInMillis = dateMillis; set(Calendar.HOUR_OF_DAY, if (shiftType == ShiftType.VACATION) 0 else startHour); set(Calendar.MINUTE, if (shiftType == ShiftType.VACATION) 0 else startMinute) }.timeInMillis,
            shiftType = shiftType,
            durationMinutes = if (shiftType == ShiftType.VACATION) (settings.workDayHours * 60).toLong() else duration,
            unpaidBreakMinutes = if (shiftType == ShiftType.VACATION) 0 else breakMinutes,
            hourlyRate = hourlyRate,
            estimatedPay = pay,
            notes = notes
        )
        saveEntries(prefs, listOf(entry) + loadEntries(prefs)); onBack()
    }
    Scaffold(containerColor = LightBackground, topBar = { Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { TextButton(onClick = onBack) { Text("Cancel", color = ShiftBlue) }; Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { Text("Add Manual Entry", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppText) }; Spacer(Modifier.width(64.dp)) } }, bottomBar = { Box(Modifier.fillMaxWidth().background(LightBackground).padding(20.dp)) { Button(onClick = ::save, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(30.dp), colors = ButtonDefaults.buttonColors(containerColor = ShiftBlue)) { Text("Save Entry", color = Color.White, fontWeight = FontWeight.Bold) } } }) { padding ->
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AppCard { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { IconButton({ currentDate.add(Calendar.MONTH, -1); dateMillis = currentDate.timeInMillis }) { Icon(Icons.Default.ChevronLeft, null, tint = ShiftBlue) }; Text("${monthNames[currentDate.get(Calendar.MONTH)]} ${currentDate.get(Calendar.YEAR)}", fontWeight = FontWeight.Bold); IconButton({ currentDate.add(Calendar.MONTH, 1); dateMillis = currentDate.timeInMillis }) { Icon(Icons.Default.ChevronRight, null, tint = ShiftBlue) } }; Row(Modifier.fillMaxWidth()) { listOf("M","T","W","T","F","S","S").forEach { Text(it, modifier = Modifier.weight(1f), color = TextSecondary, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center) } }; repeat((grid.size + 6) / 7) { row -> Row(Modifier.fillMaxWidth()) { repeat(7) { col -> val day = grid.getOrNull(row * 7 + col); Box(Modifier.weight(1f).aspectRatio(1f).clip(CircleShape).background(if (day == currentDate.get(Calendar.DAY_OF_MONTH)) if (shiftType == ShiftType.VACATION) GreenAccent else ShiftBlue else Color.Transparent).clickable(enabled = day != null) { if (day != null) { currentDate.set(Calendar.DAY_OF_MONTH, day); dateMillis = currentDate.timeInMillis } }, contentAlignment = Alignment.Center) { Text(day?.toString().orEmpty(), color = if (day == currentDate.get(Calendar.DAY_OF_MONTH)) Color.White else AppText) } } } } }
            AppCard { SimpleRow(Icons.Default.Label, ShiftBlue, "Type", shiftType.label, trailing = { Icon(Icons.Default.KeyboardArrowDown, null, tint = ShiftBlue) }, onClick = { shiftType = if (shiftType == ShiftType.REGULAR) ShiftType.VACATION else ShiftType.REGULAR }) }
            if (shiftType == ShiftType.REGULAR) {
                AppCard { Text("SHIFT HOURS", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp); Spacer(Modifier.height(12.dp)); Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { TimePill("Start", "%02d:%02d".format(startHour, startMinute), Modifier.weight(1f)) { TimePickerDialog(context, { _, h, m -> startHour = h; startMinute = m }, startHour, startMinute, true).show() }; TimePill("End", "%02d:%02d".format(endHour, endMinute), Modifier.weight(1f)) { TimePickerDialog(context, { _, h, m -> endHour = h; endMinute = m }, endHour, endMinute, true).show() } }; Spacer(Modifier.height(14.dp)); Text("🕐 Duration: ${formatDuration(duration)}", fontWeight = FontWeight.SemiBold, color = AppText) }
                AppCard { Text("DETAILS", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp); Spacer(Modifier.height(12.dp)); Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("🪙 Unpaid Break", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold); Stepper("${breakMinutes} min", { breakMinutes = (breakMinutes - 15).coerceAtLeast(0) }, { breakMinutes += 15 }) } }
                Text("NOTES (OPTIONAL)", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                OutlinedTextField(value = notes, onValueChange = { notes = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("e.g. Forgot to clock in") }, shape = RoundedCornerShape(18.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = CardBackground, focusedContainerColor = CardBackground, unfocusedBorderColor = BorderColor, focusedBorderColor = ShiftBlue))
            } else {
                AppCard { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("☀ ${formatDate(dateMillis)}", color = GreenAccent, fontWeight = FontWeight.Bold); Text("1 day", color = AppText, fontWeight = FontWeight.SemiBold) } }
                AppCard { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("💲 Estimated pay", fontWeight = FontWeight.SemiBold); Text(formatCurrency(pay, settings.currencySymbol), color = GreenAccent, fontWeight = FontWeight.Bold) } }
                Text("NOTES (OPTIONAL)", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                OutlinedTextField(value = notes, onValueChange = { notes = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("e.g. Annual leave, doctor visit...") }, shape = RoundedCornerShape(18.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = CardBackground, focusedContainerColor = CardBackground, unfocusedBorderColor = BorderColor, focusedBorderColor = ShiftBlue))
            }
        }
    }
}

@Composable private fun TimePill(title: String, value: String, modifier: Modifier, onClick: () -> Unit) = Surface(modifier = modifier.clickable(onClick = onClick), shape = RoundedCornerShape(18.dp), color = CardBackgroundAlt) { Column(Modifier.padding(14.dp)) { Text(title, color = TextSecondary, fontSize = 12.sp); Text(value, fontWeight = FontWeight.Bold, color = AppText) } }
