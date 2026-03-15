package com.example.shiftsync.ui

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiftsync.*
import com.example.shiftsync.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    // Existing toggles
    var shiftStartEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_NOTIFY_SHIFT_START, true)) }
    var shiftEndEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_NOTIFY_SHIFT_END, true)) }
    var soundEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_NOTIFY_SOUND, true)) }
    var vibrateEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_NOTIFY_VIBRATE, true)) }

    // Clock-in reminder
    var clockInReminderOn by remember { mutableStateOf(prefs.getBoolean(KEY_REMINDER_CLOCK_IN_ENABLED, false)) }
    var clockInHour by remember { mutableIntStateOf(prefs.getInt(KEY_REMINDER_CLOCK_IN_HOUR, 9)) }
    var clockInMinute by remember { mutableIntStateOf(prefs.getInt(KEY_REMINDER_CLOCK_IN_MINUTE, 0)) }
    var showClockInPicker by remember { mutableStateOf(false) }
    var clockInDays by remember { mutableStateOf(prefs.getStringSet(KEY_REMINDER_CLOCK_IN_DAYS, DEFAULT_REMINDER_DAYS) ?: DEFAULT_REMINDER_DAYS) }

    // Clock-out reminder
    var clockOutReminderOn by remember { mutableStateOf(prefs.getBoolean(KEY_REMINDER_CLOCK_OUT_ENABLED, false)) }
    var clockOutHour by remember { mutableIntStateOf(prefs.getInt(KEY_REMINDER_CLOCK_OUT_HOUR, 17)) }
    var clockOutMinute by remember { mutableIntStateOf(prefs.getInt(KEY_REMINDER_CLOCK_OUT_MINUTE, 0)) }
    var showClockOutPicker by remember { mutableStateOf(false) }
    var clockOutDays by remember { mutableStateOf(prefs.getStringSet(KEY_REMINDER_CLOCK_OUT_DAYS, DEFAULT_REMINDER_DAYS) ?: DEFAULT_REMINDER_DAYS) }

    // ── Time Picker Dialogs ───────────────────────────────────────
    if (showClockInPicker) {
        val tpState = rememberTimePickerState(initialHour = clockInHour, initialMinute = clockInMinute)
        AlertDialog(
            onDismissRequest = { showClockInPicker = false },
            containerColor = DarkCard,
            title = { Text("Set Clock-In Reminder", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(
                        state = tpState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = DarkBg,
                            clockDialSelectedContentColor = Color.White,
                            clockDialUnselectedContentColor = TextSecondary,
                            selectorColor = ShiftBlue,
                            containerColor = DarkCard,
                            periodSelectorBorderColor = ShiftBlue,
                            periodSelectorSelectedContainerColor = ShiftBlue,
                            periodSelectorUnselectedContainerColor = DarkBg,
                            periodSelectorSelectedContentColor = Color.White,
                            periodSelectorUnselectedContentColor = TextSecondary,
                            timeSelectorSelectedContainerColor = ShiftBlue,
                            timeSelectorUnselectedContainerColor = DarkBg,
                            timeSelectorSelectedContentColor = Color.White,
                            timeSelectorUnselectedContentColor = TextSecondary
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    clockInHour = tpState.hour
                    clockInMinute = tpState.minute
                    prefs.edit()
                        .putInt(KEY_REMINDER_CLOCK_IN_HOUR, tpState.hour)
                        .putInt(KEY_REMINDER_CLOCK_IN_MINUTE, tpState.minute)
                        .apply()
                    if (clockInReminderOn) {
                        ShiftReminderReceiver.schedule(
                            context, ShiftReminderReceiver.TYPE_CLOCK_IN,
                            tpState.hour, tpState.minute, clockInDays
                        )
                    }
                    showClockInPicker = false
                }) { Text("Set", color = ShiftBlue, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showClockInPicker = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showClockOutPicker) {
        val tpState = rememberTimePickerState(initialHour = clockOutHour, initialMinute = clockOutMinute)
        AlertDialog(
            onDismissRequest = { showClockOutPicker = false },
            containerColor = DarkCard,
            title = { Text("Set Clock-Out Reminder", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(
                        state = tpState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = DarkBg,
                            clockDialSelectedContentColor = Color.White,
                            clockDialUnselectedContentColor = TextSecondary,
                            selectorColor = ShiftBlue,
                            containerColor = DarkCard,
                            periodSelectorBorderColor = ShiftBlue,
                            periodSelectorSelectedContainerColor = ShiftBlue,
                            periodSelectorUnselectedContainerColor = DarkBg,
                            periodSelectorSelectedContentColor = Color.White,
                            periodSelectorUnselectedContentColor = TextSecondary,
                            timeSelectorSelectedContainerColor = ShiftBlue,
                            timeSelectorUnselectedContainerColor = DarkBg,
                            timeSelectorSelectedContentColor = Color.White,
                            timeSelectorUnselectedContentColor = TextSecondary
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    clockOutHour = tpState.hour
                    clockOutMinute = tpState.minute
                    prefs.edit()
                        .putInt(KEY_REMINDER_CLOCK_OUT_HOUR, tpState.hour)
                        .putInt(KEY_REMINDER_CLOCK_OUT_MINUTE, tpState.minute)
                        .apply()
                    if (clockOutReminderOn) {
                        ShiftReminderReceiver.schedule(
                            context, ShiftReminderReceiver.TYPE_CLOCK_OUT,
                            tpState.hour, tpState.minute, clockOutDays
                        )
                    }
                    showClockOutPicker = false
                }) { Text("Set", color = ShiftBlue, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showClockOutPicker = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // ── Screen ────────────────────────────────────────────────────
    Scaffold(
        containerColor = DarkBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBg)
                    .padding(horizontal = 4.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                Text("Notification Settings", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.width(48.dp))
            }
        }
    ) { padding ->
        val dimens = LocalDimens.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = dimens.screenPadding)
        ) {
            Spacer(Modifier.height(12.dp))

            // ── Header card ───────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ShiftBlue.copy(alpha = 0.10f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = ShiftBlue,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Shift Alerts", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Get notified when you clock in or out, and set daily reminders.",
                            color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Shift Alerts ──────────────────────────────────────
            SectionLabel("SHIFT ALERTS")
            Spacer(Modifier.height(8.dp))

            NotifToggleRow(
                icon = Icons.Default.PlayArrow,
                iconTint = GreenAccent,
                title = "Shift Start Alert",
                subtitle = "Notify when you clock in to a shift",
                checked = shiftStartEnabled,
                onCheckedChange = {
                    shiftStartEnabled = it
                    prefs.edit().putBoolean(KEY_NOTIFY_SHIFT_START, it).apply()
                }
            )

            Spacer(Modifier.height(8.dp))

            NotifToggleRow(
                icon = Icons.Default.Close,
                iconTint = RedAccent,
                title = "Shift End Alert",
                subtitle = "Notify when you clock out of a shift",
                checked = shiftEndEnabled,
                onCheckedChange = {
                    shiftEndEnabled = it
                    prefs.edit().putBoolean(KEY_NOTIFY_SHIFT_END, it).apply()
                }
            )

            Spacer(Modifier.height(24.dp))

            // ── Scheduled Reminders ───────────────────────────────
            SectionLabel("SCHEDULED REMINDERS")
            Spacer(Modifier.height(8.dp))

            ReminderCard(
                icon = Icons.Default.AccessTime,
                iconTint = GreenAccent,
                title = "Clock-In Reminder",
                subtitle = "Reminder to start your shift",
                timeLabel = formatTime12(clockInHour, clockInMinute),
                enabled = clockInReminderOn,
                selectedDays = clockInDays,
                onToggle = { on ->
                    clockInReminderOn = on
                    prefs.edit().putBoolean(KEY_REMINDER_CLOCK_IN_ENABLED, on).apply()
                    if (on) {
                        ShiftReminderReceiver.schedule(
                            context, ShiftReminderReceiver.TYPE_CLOCK_IN,
                            clockInHour, clockInMinute, clockInDays
                        )
                    } else {
                        ShiftReminderReceiver.cancel(context, ShiftReminderReceiver.TYPE_CLOCK_IN)
                    }
                },
                onTimeClick = { showClockInPicker = true },
                onDayToggle = { dayKey ->
                    val updated = clockInDays.toMutableSet()
                    if (dayKey in updated) updated.remove(dayKey) else updated.add(dayKey)
                    clockInDays = updated
                    prefs.edit().putStringSet(KEY_REMINDER_CLOCK_IN_DAYS, updated).apply()
                    if (clockInReminderOn && updated.isNotEmpty()) {
                        ShiftReminderReceiver.schedule(
                            context, ShiftReminderReceiver.TYPE_CLOCK_IN,
                            clockInHour, clockInMinute, updated
                        )
                    }
                }
            )

            Spacer(Modifier.height(8.dp))

            ReminderCard(
                icon = Icons.Default.AccessTime,
                iconTint = OrangeAccent,
                title = "Clock-Out Reminder",
                subtitle = "Reminder to end your shift",
                timeLabel = formatTime12(clockOutHour, clockOutMinute),
                enabled = clockOutReminderOn,
                selectedDays = clockOutDays,
                onToggle = { on ->
                    clockOutReminderOn = on
                    prefs.edit().putBoolean(KEY_REMINDER_CLOCK_OUT_ENABLED, on).apply()
                    if (on) {
                        ShiftReminderReceiver.schedule(
                            context, ShiftReminderReceiver.TYPE_CLOCK_OUT,
                            clockOutHour, clockOutMinute, clockOutDays
                        )
                    } else {
                        ShiftReminderReceiver.cancel(context, ShiftReminderReceiver.TYPE_CLOCK_OUT)
                    }
                },
                onTimeClick = { showClockOutPicker = true },
                onDayToggle = { dayKey ->
                    val updated = clockOutDays.toMutableSet()
                    if (dayKey in updated) updated.remove(dayKey) else updated.add(dayKey)
                    clockOutDays = updated
                    prefs.edit().putStringSet(KEY_REMINDER_CLOCK_OUT_DAYS, updated).apply()
                    if (clockOutReminderOn && updated.isNotEmpty()) {
                        ShiftReminderReceiver.schedule(
                            context, ShiftReminderReceiver.TYPE_CLOCK_OUT,
                            clockOutHour, clockOutMinute, updated
                        )
                    }
                }
            )

            Spacer(Modifier.height(24.dp))

            // ── Alert Style ───────────────────────────────────────
            SectionLabel("ALERT STYLE")
            Spacer(Modifier.height(8.dp))

            NotifToggleRow(
                icon = Icons.Default.Star,
                iconTint = OrangeAccent,
                title = "Sound",
                subtitle = "Play a sound when alert fires",
                checked = soundEnabled,
                onCheckedChange = {
                    soundEnabled = it
                    prefs.edit().putBoolean(KEY_NOTIFY_SOUND, it).apply()
                }
            )

            Spacer(Modifier.height(8.dp))

            NotifToggleRow(
                icon = Icons.Default.Phone,
                iconTint = Color(0xFF8B5CF6),
                title = "Vibrate",
                subtitle = "Vibrate device when alert fires",
                checked = vibrateEnabled,
                onCheckedChange = {
                    vibrateEnabled = it
                    prefs.edit().putBoolean(KEY_NOTIFY_VIBRATE, it).apply()
                }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Helper composables ────────────────────────────────────────────

private fun formatTime12(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    return String.format(Locale.getDefault(), "%d:%02d %s", h, minute, amPm)
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text, color = TextSecondary, fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp
    )
}

@Composable
private fun NotifToggleRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconTint.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            }
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ShiftBlue,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = DarkCardAlt
                )
            )
        }
    }
}

@Composable
private fun ReminderCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    timeLabel: String,
    enabled: Boolean,
    selectedDays: Set<String>,
    onToggle: (Boolean) -> Unit,
    onTimeClick: () -> Unit,
    onDayToggle: (String) -> Unit
) {
    // Day labels: Calendar.DAY_OF_WEEK  1=Sun…7=Sat
    val dayLabels = listOf(
        "1" to "S", "2" to "M", "3" to "T", "4" to "W",
        "5" to "T", "6" to "F", "7" to "S"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconTint.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(2.dp))
                    Text(subtitle, color = TextSecondary, fontSize = 12.sp)
                }
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ShiftBlue,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = DarkCardAlt
                    )
                )
            }

            if (enabled) {
                Spacer(Modifier.height(10.dp))

                // ── Time row ──────────────────────────────────────
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onTimeClick),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Reminder at", color = TextSecondary, fontSize = 13.sp)
                        }
                        Text(
                            timeLabel,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── Day selector row ──────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkBg)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Repeat on", color = TextSecondary, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            dayLabels.forEach { (key, label) ->
                                val isSelected = key in selectedDays
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) iconTint else DarkCard
                                        )
                                        .clickable { onDayToggle(key) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        label,
                                        color = if (isSelected) Color.White else TextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}









