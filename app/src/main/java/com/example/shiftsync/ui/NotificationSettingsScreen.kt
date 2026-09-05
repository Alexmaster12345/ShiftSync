package com.example.shiftsync.ui

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
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

// Calendar.DAY_OF_WEEK values, ordered Monday-first for display (1=Sun...7=Sat)
private val WEEK_DAYS = listOf(
    "2" to "M", "3" to "T", "4" to "W", "5" to "T", "6" to "F", "7" to "S", "1" to "S"
)

@Composable
fun NotificationSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var alertsEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_NOTIFY_SHIFT_START, true) || prefs.getBoolean(KEY_NOTIFY_SHIFT_END, true)) }

    var workingDays by remember { mutableStateOf(prefs.getStringSet(KEY_REMINDER_CLOCK_IN_DAYS, DEFAULT_REMINDER_DAYS) ?: DEFAULT_REMINDER_DAYS) }

    var clockInEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_REMINDER_CLOCK_IN_ENABLED, false)) }
    var clockInHour by remember { mutableIntStateOf(prefs.getInt(KEY_REMINDER_CLOCK_IN_HOUR, 9)) }
    var clockInMinute by remember { mutableIntStateOf(prefs.getInt(KEY_REMINDER_CLOCK_IN_MINUTE, 0)) }

    var clockOutEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_REMINDER_CLOCK_OUT_ENABLED, false)) }
    var clockOutHour by remember { mutableIntStateOf(prefs.getInt(KEY_REMINDER_CLOCK_OUT_HOUR, 17)) }
    var clockOutMinute by remember { mutableIntStateOf(prefs.getInt(KEY_REMINDER_CLOCK_OUT_MINUTE, 0)) }

    fun applyReminders() {
        if (clockInEnabled) {
            ShiftReminderReceiver.schedule(context, ShiftReminderReceiver.TYPE_CLOCK_IN, clockInHour, clockInMinute, workingDays)
        } else {
            ShiftReminderReceiver.cancel(context, ShiftReminderReceiver.TYPE_CLOCK_IN)
        }
        if (clockOutEnabled) {
            ShiftReminderReceiver.schedule(context, ShiftReminderReceiver.TYPE_CLOCK_OUT, clockOutHour, clockOutMinute, workingDays)
        } else {
            ShiftReminderReceiver.cancel(context, ShiftReminderReceiver.TYPE_CLOCK_OUT)
        }
    }

    fun persist() {
        prefs.edit()
            .putBoolean(KEY_NOTIFY_SHIFT_START, alertsEnabled)
            .putBoolean(KEY_NOTIFY_SHIFT_END, alertsEnabled)
            .putStringSet(KEY_REMINDER_CLOCK_IN_DAYS, workingDays)
            .putStringSet(KEY_REMINDER_CLOCK_OUT_DAYS, workingDays)
            .putBoolean(KEY_REMINDER_CLOCK_IN_ENABLED, clockInEnabled)
            .putInt(KEY_REMINDER_CLOCK_IN_HOUR, clockInHour)
            .putInt(KEY_REMINDER_CLOCK_IN_MINUTE, clockInMinute)
            .putBoolean(KEY_REMINDER_CLOCK_OUT_ENABLED, clockOutEnabled)
            .putInt(KEY_REMINDER_CLOCK_OUT_HOUR, clockOutHour)
            .putInt(KEY_REMINDER_CLOCK_OUT_MINUTE, clockOutMinute)
            .apply()
        applyReminders()
    }

    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.height(8.dp)); HeaderWithBack("Notifications", onBack)
        AppCard {
            SimpleRow(
                Icons.Default.Notifications,
                OrangeAccent,
                "Arrival & Departure Alerts",
                if (alertsEnabled) "On — notified when you arrive or leave work" else "Off",
                trailing = { Switch(checked = alertsEnabled, onCheckedChange = { alertsEnabled = it; persist() }) }
            )
        }

        Text("WORKING DAYS", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
        AppCard {
            Text("Only remind me on the days I actually work", color = TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                WEEK_DAYS.forEach { (dayValue, label) ->
                    val active = dayValue in workingDays
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (active) ShiftBlue else CardBackgroundAlt)
                            .clickable {
                                workingDays = if (active) workingDays - dayValue else workingDays + dayValue
                                persist()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = if (active) Color.White else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        Text("SHIFT REMINDERS", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
        AppCard {
            SimpleRow(
                Icons.Default.Notifications,
                ShiftBlue,
                "Remind Me to Clock In",
                if (clockInEnabled) "Daily at %02d:%02d on working days".format(clockInHour, clockInMinute) else "Off",
                trailing = { Switch(checked = clockInEnabled, onCheckedChange = { clockInEnabled = it; persist() }) },
                onClick = if (clockInEnabled) {
                    { TimePickerDialog(context, { _, h, m -> clockInHour = h; clockInMinute = m; persist() }, clockInHour, clockInMinute, true).show() }
                } else null
            )
            HorizontalDivider(color = BorderColor)
            SimpleRow(
                Icons.Default.Notifications,
                GreenAccent,
                "Remind Me to Clock Out",
                if (clockOutEnabled) "Daily at %02d:%02d on working days".format(clockOutHour, clockOutMinute) else "Off",
                trailing = { Switch(checked = clockOutEnabled, onCheckedChange = { clockOutEnabled = it; persist() }) },
                onClick = if (clockOutEnabled) {
                    { TimePickerDialog(context, { _, h, m -> clockOutHour = h; clockOutMinute = m; persist() }, clockOutHour, clockOutMinute, true).show() }
                } else null
            )
        }
        Text(
            "You will not be reminded to clock in or out on days that are not marked as working days above — for example, on your day off.",
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}
