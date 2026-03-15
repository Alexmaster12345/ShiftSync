package com.example.shiftsync.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.shiftsync.PayrollCalculator
import com.example.shiftsync.PREFS_NAME
import com.example.shiftsync.ShiftEntry
import com.example.shiftsync.ShiftType
import com.example.shiftsync.loadEntries
import com.example.shiftsync.saveEntries
import com.example.shiftsync.ui.theme.*
import com.example.shiftsync.ui.theme.LocalDimens
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    // Calendar state
    val today = remember { Calendar.getInstance() }
    var displayMonth by remember { mutableIntStateOf(today.get(Calendar.MONTH)) }
    var displayYear by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }
    var selectedDay by remember { mutableIntStateOf(today.get(Calendar.DAY_OF_MONTH)) }

    // Time pickers
    var startHour by remember { mutableIntStateOf(9) }
    var startMinute by remember { mutableIntStateOf(0) }
    var startAmPm by remember { mutableStateOf("AM") }
    var endHour by remember { mutableIntStateOf(5) }
    var endMinute by remember { mutableIntStateOf(0) }
    var endAmPm by remember { mutableStateOf("PM") }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    // Break
    var breakMinutes by remember { mutableIntStateOf(30) }

    // Reason
    var reason by remember { mutableStateOf("") }

    val monthNames = listOf("January","February","March","April","May","June","July","August","September","October","November","December")

    // Build calendar grid
    val calGrid = remember(displayMonth, displayYear) {
        val c = Calendar.getInstance().apply { set(displayYear, displayMonth, 1) }
        val firstDow = (c.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY + 6) % 7 // Mon=0
        val daysInMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH)
        val cells = mutableListOf<Int?>()
        repeat(firstDow) { cells.add(null) }
        for (d in 1..daysInMonth) cells.add(d)
        cells
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBg)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "Manual Hours Entry",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.width(48.dp))
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBg)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        // Build and save entry
                        val startTotalMinutes = (if (startAmPm == "AM") startHour % 12 else (startHour % 12) + 12) * 60 + startMinute
                        val endTotalMinutes = (if (endAmPm == "AM") endHour % 12 else (endHour % 12) + 12) * 60 + endMinute
                        val durationMinutes = (endTotalMinutes - startTotalMinutes).toLong().coerceAtLeast(1L)
                        val hourlyRate = prefs.getString("hourly_rate", "20.0")?.toDoubleOrNull() ?: 20.0
                        val cal = Calendar.getInstance().apply { set(displayYear, displayMonth, selectedDay, 0, 0, 0) }
                        val entry = ShiftEntry(
                            startedAtMillis = cal.timeInMillis,
                            shiftType = ShiftType.MORNING,
                            durationMinutes = durationMinutes,
                            unpaidBreakMinutes = breakMinutes,
                            hourlyRate = hourlyRate,
                            estimatedPay = PayrollCalculator.estimatePay(durationMinutes, breakMinutes, hourlyRate, ShiftType.MORNING)
                        )
                        val existing = loadEntries(prefs)
                        saveEntries(prefs, listOf(entry) + existing)
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ShiftBlue)
                ) {
                    Icon(Icons.Default.SaveAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Save Entry", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
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
            Spacer(Modifier.height(8.dp))

            // ── Calendar ──────────────────────────────────────────────
            SectionLabel("SELECT DATE")
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Month nav
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (displayMonth == 0) { displayMonth = 11; displayYear-- } else displayMonth--
                        }) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Prev", tint = ShiftBlue)
                        }
                        Text(
                            "${monthNames[displayMonth]} $displayYear",
                            color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp
                        )
                        IconButton(onClick = {
                            if (displayMonth == 11) { displayMonth = 0; displayYear++ } else displayMonth++
                        }) {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next", tint = ShiftBlue)
                        }
                    }

                    // Day-of-week headers
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("S","M","T","W","T","F","S").forEach { h ->
                            Text(
                                h,
                                modifier = Modifier.weight(1f),
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))

                    // Days grid
                    val rows = (calGrid.size + 6) / 7
                    for (row in 0 until rows) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (col in 0 until 7) {
                                val idx = row * 7 + col
                                val day = calGrid.getOrNull(idx)
                                val isSelected = day == selectedDay
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(CircleShape)
                                        .background(if (isSelected) ShiftBlue else Color.Transparent)
                                        .then(if (day != null) Modifier.clickable { selectedDay = day } else Modifier),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (day != null) {
                                        Text(
                                            day.toString(),
                                            color = if (isSelected) Color.White else TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Start / End time ──────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    SectionLabel("START TIME")
                    Spacer(Modifier.height(8.dp))
                    TimePickerButton(
                        hour = startHour, minute = startMinute, amPm = startAmPm,
                        icon = Icons.Default.AccessTime
                    ) { showStartPicker = true }
                }
                Column(modifier = Modifier.weight(1f)) {
                    SectionLabel("END TIME")
                    Spacer(Modifier.height(8.dp))
                    TimePickerButton(
                        hour = endHour, minute = endMinute, amPm = endAmPm,
                        icon = Icons.Default.AccessAlarm
                    ) { showEndPicker = true }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Break duration ────────────────────────────────────────
            SectionLabel("BREAK DURATION")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(30 to "30 min", 45 to "45 min", 60 to "1 hour").forEach { (mins, label) ->
                    val sel = breakMinutes == mins
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (sel) ShiftBlue else DarkCard)
                            .border(1.dp, if (sel) ShiftBlue else TextMuted, RoundedCornerShape(24.dp))
                            .clickable { breakMinutes = mins }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(label, color = if (sel) Color.White else TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
                // Custom chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(DarkCard)
                        .border(1.dp, TextMuted, RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text("Custom", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Reason ────────────────────────────────────────────────
            SectionLabel("REASON FOR MANUAL ENTRY")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                placeholder = { Text("e.g. Forgot to clock in, device battery died...", color = TextMuted, fontSize = 13.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShiftBlue,
                    unfocusedBorderColor = DarkCard,
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = ShiftBlue
                )
            )

            Spacer(Modifier.height(16.dp))
        }
    }

    // ── Time picker dialogs ───────────────────────────────────────────
    if (showStartPicker) {
        TimeDialog(
            initialHour = if (startAmPm == "AM") startHour % 12 else (startHour % 12) + 12,
            initialMinute = startMinute,
            onConfirm = { h, m ->
                startHour = if (h == 0 || h == 12) 12 else h % 12
                startMinute = m
                startAmPm = if (h < 12) "AM" else "PM"
                showStartPicker = false
            },
            onDismiss = { showStartPicker = false }
        )
    }
    if (showEndPicker) {
        TimeDialog(
            initialHour = if (endAmPm == "AM") endHour % 12 else (endHour % 12) + 12,
            initialMinute = endMinute,
            onConfirm = { h, m ->
                endHour = if (h == 0 || h == 12) 12 else h % 12
                endMinute = m
                endAmPm = if (h < 12) "AM" else "PM"
                showEndPicker = false
            },
            onDismiss = { showEndPicker = false }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = TextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp
    )
}

@Composable
private fun TimePickerButton(
    hour: Int, minute: Int, amPm: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                text = "%02d:%02d %s".format(hour, minute, amPm),
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = false)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        title = { Text("Select time", color = TextPrimary) },
        text = {
            TimePicker(
                state = state,
                colors = TimePickerDefaults.colors(
                    clockDialColor = DarkSurface,
                    selectorColor = ShiftBlue,
                    containerColor = DarkCard,
                    clockDialSelectedContentColor = Color.White,
                    clockDialUnselectedContentColor = TextSecondary,
                    timeSelectorSelectedContainerColor = ShiftBlue,
                    timeSelectorUnselectedContainerColor = DarkSurface,
                    timeSelectorSelectedContentColor = Color.White,
                    timeSelectorUnselectedContentColor = TextSecondary,
                    periodSelectorSelectedContainerColor = ShiftBlue,
                    periodSelectorUnselectedContainerColor = DarkSurface,
                    periodSelectorSelectedContentColor = Color.White,
                    periodSelectorUnselectedContentColor = TextSecondary,
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text("OK", color = ShiftBlue, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}







