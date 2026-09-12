package com.example.shiftsync.ui

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import kotlinx.coroutines.delay

private val WEEK_DAYS = listOf(
    "2" to "M", "3" to "T", "4" to "W", "5" to "T", "6" to "F", "7" to "S", "1" to "S"
)

private enum class DayAssignment { OFFICE, HOME, OFF }

@Composable
fun NotificationSettingsScreen(settings: AppSettings, onBack: () -> Unit, onSaved: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var alertsEnabled by remember { mutableStateOf(settings.arrivalDepartureAlerts) }
    var workFromHomeEnabled by remember { mutableStateOf(settings.workFromHomeEnabled) }
    var officeDays by remember { mutableStateOf(settings.officeDays) }
    var homeDays by remember { mutableStateOf(settings.homeDays) }
    var clockInEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_REMINDER_CLOCK_IN_ENABLED, settings.workFromHomeEnabled)) }
    var clockInHour by remember { mutableIntStateOf(prefs.getInt(KEY_REMINDER_CLOCK_IN_HOUR, 9)) }
    var clockInMinute by remember { mutableIntStateOf(prefs.getInt(KEY_REMINDER_CLOCK_IN_MINUTE, 0)) }
    var clockOutEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_REMINDER_CLOCK_OUT_ENABLED, settings.workFromHomeEnabled)) }
    var clockOutHour by remember { mutableIntStateOf(prefs.getInt(KEY_REMINDER_CLOCK_OUT_HOUR, 17)) }
    var clockOutMinute by remember { mutableIntStateOf(prefs.getInt(KEY_REMINDER_CLOCK_OUT_MINUTE, 0)) }
    var saved by remember { mutableStateOf(false) }

    fun assignment(dayValue: String): DayAssignment = when {
        dayValue in officeDays -> DayAssignment.OFFICE
        dayValue in homeDays -> DayAssignment.HOME
        else -> DayAssignment.OFF
    }

    fun cycleDay(dayValue: String) {
        when (assignment(dayValue)) {
            DayAssignment.OFF -> officeDays = officeDays + dayValue
            DayAssignment.OFFICE -> {
                officeDays = officeDays - dayValue
                if (workFromHomeEnabled) homeDays = homeDays + dayValue
            }
            DayAssignment.HOME -> homeDays = homeDays - dayValue
        }
    }

    fun applyReminders() {
        if (workFromHomeEnabled && clockInEnabled && homeDays.isNotEmpty()) {
            ShiftReminderReceiver.schedule(context, ShiftReminderReceiver.TYPE_CLOCK_IN, clockInHour, clockInMinute, homeDays)
        } else {
            ShiftReminderReceiver.cancel(context, ShiftReminderReceiver.TYPE_CLOCK_IN)
        }
        if (workFromHomeEnabled && clockOutEnabled && homeDays.isNotEmpty()) {
            ShiftReminderReceiver.schedule(context, ShiftReminderReceiver.TYPE_CLOCK_OUT, clockOutHour, clockOutMinute, homeDays)
        } else {
            ShiftReminderReceiver.cancel(context, ShiftReminderReceiver.TYPE_CLOCK_OUT)
        }
    }

    fun persist() {
        prefs.edit()
            .putBoolean(KEY_NOTIFY_SHIFT_START, alertsEnabled)
            .putBoolean(KEY_NOTIFY_SHIFT_END, alertsEnabled)
            .putBoolean(KEY_WORK_FROM_HOME_ENABLED, workFromHomeEnabled)
            .putStringSet(KEY_OFFICE_DAYS, officeDays)
            .putStringSet(KEY_HOME_DAYS, homeDays)
            .putStringSet(KEY_REMINDER_CLOCK_IN_DAYS, homeDays)
            .putStringSet(KEY_REMINDER_CLOCK_OUT_DAYS, homeDays)
            .putBoolean(KEY_REMINDER_CLOCK_IN_ENABLED, clockInEnabled)
            .putInt(KEY_REMINDER_CLOCK_IN_HOUR, clockInHour)
            .putInt(KEY_REMINDER_CLOCK_IN_MINUTE, clockInMinute)
            .putBoolean(KEY_REMINDER_CLOCK_OUT_ENABLED, clockOutEnabled)
            .putInt(KEY_REMINDER_CLOCK_OUT_HOUR, clockOutHour)
            .putInt(KEY_REMINDER_CLOCK_OUT_MINUTE, clockOutMinute)
            .apply()
        applyReminders()
        onSaved()
    }

    LaunchedEffect(saved) {
        if (saved) {
            delay(1000)
            saved = false
        }
    }

    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.height(8.dp))
        HeaderWithBack("Notifications", onBack)
        AppCard {
            SimpleRow(
                Icons.Default.Notifications,
                OrangeAccent,
                "Arrival & Departure Alerts",
                if (alertsEnabled) "On — notified when you arrive or leave work" else "Off",
                trailing = { Switch(checked = alertsEnabled, onCheckedChange = { alertsEnabled = it }) }
            )
        }

        AppCard {
            SimpleRow(
                Icons.Default.Home,
                ShiftBlue,
                "Work From Home",
                if (workFromHomeEnabled) "On — reminded to clock in/out at set times" else "Off",
                trailing = {
                    Switch(
                        checked = workFromHomeEnabled,
                        onCheckedChange = {
                            workFromHomeEnabled = it
                            if (!it) homeDays = emptySet()
                        }
                    )
                }
            )
            if (workFromHomeEnabled) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = BorderColor)
                Spacer(Modifier.height(12.dp))
                SimpleRow(
                    Icons.Default.Schedule,
                    OrangeAccent,
                    "Clock In Time",
                    "${formatShiftTime(timeMillis(clockInHour, clockInMinute), settings.use24HourClock)} on Home days",
                    trailing = { Switch(checked = clockInEnabled, onCheckedChange = { clockInEnabled = it }) },
                    onClick = if (clockInEnabled) {
                        { TimePickerDialog(context, { _, h, m -> clockInHour = h; clockInMinute = m }, clockInHour, clockInMinute, settings.use24HourClock).show() }
                    } else null
                )
                HorizontalDivider(color = BorderColor)
                SimpleRow(
                    Icons.Default.Schedule,
                    GreenAccent,
                    "Clock Out Time",
                    "${formatShiftTime(timeMillis(clockOutHour, clockOutMinute), settings.use24HourClock)} on Home days",
                    trailing = { Switch(checked = clockOutEnabled, onCheckedChange = { clockOutEnabled = it }) },
                    onClick = if (clockOutEnabled) {
                        { TimePickerDialog(context, { _, h, m -> clockOutHour = h; clockOutMinute = m }, clockOutHour, clockOutMinute, settings.use24HourClock).show() }
                    } else null
                )
                Spacer(Modifier.height(12.dp))
                Text("Doesn't need a workplace location — reminders fire at these times on your Home days below.", color = TextSecondary, fontSize = 12.sp)
            }
        }

        Text("WORK SCHEDULE", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
        AppCard {
            Text(if (workFromHomeEnabled) "Tap a day to cycle Office → Home → Off" else "Tap a day to cycle Office → Off", color = TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                WEEK_DAYS.forEach { (dayValue, label) ->
                    val state = assignment(dayValue)
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                when (state) {
                                    DayAssignment.OFFICE -> ShiftBlue
                                    DayAssignment.HOME -> GreenAccent
                                    DayAssignment.OFF -> CardBackgroundAlt
                                }
                            )
                            .clickable { cycleDay(dayValue) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = if (state == DayAssignment.OFF) TextSecondary else Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LegendDot(ShiftBlue, "Office", Modifier.weight(1f))
                LegendDot(GreenAccent, "Home", Modifier.weight(1f))
                LegendDot(CardBackgroundAlt, "Off", Modifier.weight(1f), bordered = true)
            }
            if (!workFromHomeEnabled) {
                Spacer(Modifier.height(12.dp))
                Text("Turn on Work From Home above to assign days as Home.", color = TextMuted, fontSize = 11.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text("Office: ${daysLabel(officeDays)}", color = TextSecondary, fontSize = 11.sp)
            Text("Home: ${daysLabel(homeDays)}", color = TextSecondary, fontSize = 11.sp)
        }

        SaveChangesButton(saved = saved) {
            persist()
            saved = true
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String, modifier: Modifier = Modifier, bordered: Boolean = false) {
    Row(modifier, horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        if (bordered) {
            Spacer(Modifier.width(1.dp))
        }
        Spacer(Modifier.width(6.dp))
        Text(label, color = TextSecondary, fontSize = 11.sp)
    }
}

private fun timeMillis(hour: Int, minute: Int): Long {
    val calendar = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, hour)
        set(java.util.Calendar.MINUTE, minute)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}
