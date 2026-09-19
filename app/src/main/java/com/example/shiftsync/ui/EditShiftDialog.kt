package com.example.shiftsync.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.shiftsync.*
import com.example.shiftsync.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EditShiftDialog(
    entry: ShiftEntry,
    settings: AppSettings,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    onDeleted: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val isDayType = entry.shiftType == ShiftType.VACATION

    var dateMillis by remember { mutableLongStateOf(entry.startedAtMillis) }
    val startCal = remember { Calendar.getInstance().apply { timeInMillis = entry.startedAtMillis } }
    var startHour by remember { mutableIntStateOf(startCal.get(Calendar.HOUR_OF_DAY)) }
    var startMinute by remember { mutableIntStateOf(startCal.get(Calendar.MINUTE)) }
    val endCal = remember { Calendar.getInstance().apply { timeInMillis = entry.startedAtMillis + entry.durationMinutes * 60000 } }
    var endHour by remember { mutableIntStateOf(endCal.get(Calendar.HOUR_OF_DAY)) }
    var endMinute by remember { mutableIntStateOf(endCal.get(Calendar.MINUTE)) }
    var vacationDays by remember {
        mutableIntStateOf(maxOf(1, (entry.durationMinutes / (settings.workDayHours * 60).toLong().coerceAtLeast(1)).toInt()))
    }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val startMinutes = startHour * 60 + startMinute
    val endMinutes = endHour * 60 + endMinute
    val durationMinutes = when {
        isDayType -> vacationDays * (settings.workDayHours * 60).toLong()
        endMinutes > startMinutes -> (endMinutes - startMinutes).toLong()
        else -> 0L
    }
    val canSave = if (isDayType) vacationDays > 0 else durationMinutes > 0

    fun dateLabel(): String {
        val fmt = SimpleDateFormat("MMMM d", Locale.getDefault())
        val today = Calendar.getInstance()
        val d = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val isToday = today.get(Calendar.YEAR) == d.get(Calendar.YEAR) && today.get(Calendar.DAY_OF_YEAR) == d.get(Calendar.DAY_OF_YEAR)
        return if (isToday) "Today, ${fmt.format(Date(dateMillis))}" else fmt.format(Date(dateMillis))
    }

    fun save() {
        if (!canSave) return
        val hourlyRate = PayrollCalculator.hourlyRate(settings)
        val newStart = if (isDayType) {
            Calendar.getInstance().apply {
                timeInMillis = dateMillis
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        } else {
            Calendar.getInstance().apply {
                timeInMillis = dateMillis
                set(Calendar.HOUR_OF_DAY, startHour); set(Calendar.MINUTE, startMinute); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
        val pay = PayrollCalculator.estimatePay(
            durationMinutes, if (isDayType) 0 else entry.unpaidBreakMinutes, hourlyRate, entry.shiftType,
            settings.overtimeEnabled, (settings.overtimeDailyThresholdHours * 60).toLong(), settings.overtimeMultiplier,
            settings.workDayHours, settings.salaryAmount
        )
        val updated = entry.copy(
            startedAtMillis = newStart,
            durationMinutes = durationMinutes,
            unpaidBreakMinutes = if (isDayType) 0 else entry.unpaidBreakMinutes,
            hourlyRate = hourlyRate,
            estimatedPay = pay
        )
        updateEntry(prefs, entry.id, updated)
        onSaved()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), color = LightBackground) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Edit Shift", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = AppText)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            Modifier.size(40.dp).clip(CircleShape).background(RedAccent.copy(alpha = 0.15f))
                                .clickable { showDeleteConfirm = true },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Delete, "Delete Shift", tint = RedAccent, modifier = Modifier.size(16.dp)) }
                        Box(
                            Modifier.size(40.dp).clip(CircleShape).background(TextSecondary.copy(alpha = 0.15f))
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Close, "Close", tint = TextSecondary, modifier = Modifier.size(16.dp)) }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("DATE", color = TextSecondary, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
                            DatePickerDialog(context, { _, y, m, d ->
                                dateMillis = Calendar.getInstance().apply { set(y, m, d) }.timeInMillis
                            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                        },
                        color = CardBackground, shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)).background(ShiftBlue.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CalendarMonth, null, tint = ShiftBlue, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(dateLabel(), fontWeight = FontWeight.Bold, color = AppText, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, null, tint = TextSecondary)
                        }
                    }
                }

                if (isDayType) {
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CardBackground).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)).background(GreenAccent.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.WbSunny, null, tint = GreenAccent, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("Days", color = AppText, modifier = Modifier.weight(1f))
                        Stepper("$vacationDays", { if (vacationDays > 1) vacationDays-- }, { vacationDays++ })
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        EditTimeBox("START", "%02d:%02d".format(startHour, startMinute), Modifier.weight(1f)) {
                            TimePickerDialog(context, { _, h, m ->
                                startHour = h; startMinute = m
                                if (endHour * 60 + endMinute <= h * 60 + m) {
                                    val bumped = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, h); set(Calendar.MINUTE, m); add(Calendar.HOUR_OF_DAY, 1) }
                                    endHour = bumped.get(Calendar.HOUR_OF_DAY); endMinute = bumped.get(Calendar.MINUTE)
                                }
                            }, startHour, startMinute, true).show()
                        }
                        EditTimeBox("END", "%02d:%02d".format(endHour, endMinute), Modifier.weight(1f)) {
                            TimePickerDialog(context, { _, h, m -> endHour = h; endMinute = m }, endHour, endMinute, true).show()
                        }
                    }
                }

                Button(
                    onClick = ::save,
                    enabled = canSave,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ShiftBlue, disabledContainerColor = TextSecondary)
                ) {
                    Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Shift?") },
            text = { Text("This shift record will be permanently deleted. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    deleteEntry(prefs, entry.id)
                    showDeleteConfirm = false
                    onDeleted()
                }) { Text("Delete", color = RedAccent) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun EditTimeBox(label: String, value: String, modifier: Modifier, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(label, color = TextSecondary, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
        Spacer(Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
            color = CardBackground, shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                value, fontWeight = FontWeight.Bold, color = AppText, fontSize = 17.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)
            )
        }
    }
}
