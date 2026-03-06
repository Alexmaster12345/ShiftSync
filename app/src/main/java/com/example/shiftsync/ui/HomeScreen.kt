package com.example.shiftsync.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.shiftsync.ClockForegroundService
import com.example.shiftsync.KEY_ACTIVE_START_MILLIS
import com.example.shiftsync.NO_ACTIVE_SHIFT
import com.example.shiftsync.PREFS_NAME
import com.example.shiftsync.ShiftEntry
import com.example.shiftsync.formatCurrency
import com.example.shiftsync.formatDuration
import com.example.shiftsync.loadEntries
import com.example.shiftsync.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    onAddManualEntry: () -> Unit,
    onCalendarView: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var activeStartMillis by remember {
        mutableStateOf(prefs.getLong(KEY_ACTIVE_START_MILLIS, NO_ACTIVE_SHIFT).takeIf { it > 0L })
    }
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(activeStartMillis) {
        while (activeStartMillis != null) {
            nowMillis = System.currentTimeMillis()
            delay(1_000)
        }
    }

    val isClocked = activeStartMillis != null
    val elapsedMinutes = if (isClocked) ((nowMillis - activeStartMillis!!) / 60_000L).coerceAtLeast(0L) else 0L

    val dayFormatter = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).apply { isLenient = false }
    val todayLabel = dayFormatter.format(Date()).uppercase()

    // Week days strip – full 7 days Sun→Sat
    val cal = Calendar.getInstance()
    val todayDayOfMonth  = cal.get(Calendar.DAY_OF_MONTH)
    val todayDayOfWeek   = cal.get(Calendar.DAY_OF_WEEK)          // 1=Sun … 7=Sat
    val dayNames = listOf("SUN","MON","TUE","WED","THU","FRI","SAT")
    val startOfWeek = cal.clone() as Calendar
    startOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)        // anchor to Sunday
    val weekDays = (0..6).map { i ->
        val c = startOfWeek.clone() as Calendar
        c.add(Calendar.DAY_OF_MONTH, i)
        Triple(dayNames[i], c.get(Calendar.DAY_OF_MONTH), i + 1)  // name, day#, dow (1=Sun)
    }

    // Default selection = today's index (0=Sun…6=Sat)
    var selectedWeekIdx by remember { mutableIntStateOf(todayDayOfWeek - 1) }

    // Saved shift entries – reload whenever manual entry screen is popped
    val savedEntries = remember { mutableStateOf(loadEntries(prefs)) }
    // Also refresh on composition
    LaunchedEffect(Unit) { savedEntries.value = loadEntries(prefs) }

    // Shifts for the currently selected week day
    val selectedCalDay = weekDays[selectedWeekIdx]   // Triple(name, day#, dow)
    val shiftsForSelectedDay = remember(selectedWeekIdx, savedEntries.value) {
        val dayOfMonth = selectedCalDay.second
        savedEntries.value.filter { entry ->
            val c = Calendar.getInstance().apply { timeInMillis = entry.startedAtMillis }
            c.get(Calendar.DAY_OF_MONTH) == dayOfMonth &&
            c.get(Calendar.DAY_OF_WEEK) == selectedCalDay.third
        }
    }

    // Static upcoming shifts (scheduled, not yet worked) – always shown for THU/FRI demo
    val upcomingSchedule = mapOf(
        5 to listOf(Pair("12:00 PM – 08:00 PM", "Floor Manager • Service Dept.")),
        6 to listOf(Pair("08:00 AM – 04:00 PM", "Senior Barista • Coffee Bar"))
    ) // dow index: 5=FRI(index), 6=SAT(index) — adjust to week position

    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            BottomNavBar(selected = 0, onNavigate = { index ->
                when (index) {
                    1 -> onCalendarView()
                    4 -> onProfile()
                }
            })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            // ── Top bar ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = todayLabel,
                        color = ShiftBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Hello, Alex",
                        color = TextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(DarkCard)
                            .clickable { onNotifications() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = TextPrimary, modifier = Modifier.size(20.dp))
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(RedAccent)
                                .align(Alignment.TopEnd)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(DarkCard)
                            .clickable { onProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = TextSecondary, modifier = Modifier.size(22.dp))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Stats cards ──────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatsCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.AccessTime,
                    label = "THIS WEEK",
                    value = if (isClocked) formatDuration(elapsedMinutes) else "32.5 hrs",
                    sub = null,
                    progress = 0.78f
                )
                StatsCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.AttachMoney,
                    label = "EST. EARNINGS",
                    value = "$780",
                    valueSuffix = ".00",
                    sub = "+12% from last week",
                    subColor = GreenAccent,
                    progress = null
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Current shift / clock-in card ────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(listOf(ShiftBlue, ShiftBlueDark))
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CURRENT SHIFT",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (isClocked) "ACTIVE" else "READY",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = if (isClocked) formatDuration(elapsedMinutes) else "09:00 - 17:00",
                        color = Color.White,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Main Branch • Senior Barista",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Clock In / Out button
                    Button(
                        onClick = {
                            val startMillis = System.currentTimeMillis()
                            if (!isClocked) {
                                activeStartMillis = startMillis
                                prefs.edit().putLong(KEY_ACTIVE_START_MILLIS, startMillis).apply()
                                context.startClockService(ClockForegroundService.ACTION_START)
                            } else {
                                activeStartMillis = null
                                prefs.edit().remove(KEY_ACTIVE_START_MILLIS).apply()
                                context.startClockService(ClockForegroundService.ACTION_STOP)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Icon(
                            imageVector = if (isClocked) Icons.Default.StopCircle else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = ShiftBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isClocked) "Clock Out" else "Clock In Now",
                            color = ShiftBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Manual entry banner ───────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(OrangeAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.EditNote, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Missed a shift log?", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Manually add your worked hours for any date.", color = TextSecondary, fontSize = 12.sp)
                    }
                }
                OutlinedButton(
                    onClick = onAddManualEntry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TextMuted),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add Hours Manually", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── My Week strip ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("MY WEEK", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                TextButton(onClick = { onCalendarView() }) {
                    Text("Calendar View", color = ShiftBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                weekDays.forEachIndexed { idx, (name, day, dow) ->
                    val isToday    = day == todayDayOfMonth && dow == todayDayOfWeek
                    val isSelected = idx == selectedWeekIdx
                    val bgColor = when {
                        isSelected && isToday  -> ShiftBlue
                        isSelected             -> ShiftBlueLight
                        else                   -> DarkCard
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(bgColor)
                            .clickable { selectedWeekIdx = idx }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                name,
                                color = if (isSelected) Color.White else TextSecondary,
                                fontSize = 8.sp, fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                day.toString(),
                                color = if (isSelected) Color.White else TextPrimary,
                                fontSize = 15.sp, fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Day detail: shifts worked on selected day ─────────────
            val monthAbbr = SimpleDateFormat("MMM", Locale.getDefault())
                .format(Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, selectedCalDay.third)
                    set(Calendar.DAY_OF_MONTH, selectedCalDay.second)
                }.time)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${selectedCalDay.first}, $monthAbbr ${selectedCalDay.second}".uppercase(),
                    color = TextSecondary, fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp
                )
                TextButton(onClick = {}) {
                    Text("See All", color = ShiftBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(8.dp))

            if (shiftsForSelectedDay.isNotEmpty()) {
                // Show actual worked shifts saved by user
                shiftsForSelectedDay.forEach { entry ->
                    WorkedShiftCard(entry = entry)
                    Spacer(Modifier.height(8.dp))
                }
            } else {
                // No logged shifts — show empty state
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.EventBusy,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "No shifts logged for this day",
                            color = TextMuted, fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(4.dp))
                        TextButton(onClick = onAddManualEntry) {
                            Text("+ Add Hours Manually", color = ShiftBlue, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Helper composables ────────────────────────────────────────────────────────

@Composable
private fun StatsCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    valueSuffix: String? = null,
    sub: String? = null,
    subColor: Color = TextSecondary,
    progress: Float? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(label, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                if (valueSuffix != null) Text(valueSuffix, color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(bottom = 3.dp))
            }
            if (progress != null) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = ShiftBlue,
                    trackColor = DarkBg
                )
            }
            if (sub != null) {
                Spacer(Modifier.height(4.dp))
                Text(sub, color = subColor, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun UpcomingShiftRow(dayAbbr: String, dayNum: String, time: String, role: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurface),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(dayAbbr, color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                    Text(dayNum, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(time, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(role, color = TextSecondary, fontSize = 12.sp)
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TextMuted)
        }
    }
}

@Composable
fun BottomNavBar(selected: Int, onNavigate: (Int) -> Unit = {}) {
    NavigationBar(containerColor = DarkSurface, tonalElevation = 0.dp) {
        val items = listOf(
            Pair(Icons.Default.Home, "Home"),
            Pair(Icons.Default.CalendarToday, "Schedule"),
            Pair(Icons.Default.SyncAlt, "Trade"),
            Pair(Icons.Default.MonetizationOn, "Earnings"),
            Pair(Icons.Default.Person, "Profile")
        )
        items.forEachIndexed { index, (icon, label) ->
            NavigationBarItem(
                selected = index == selected,
                onClick = { onNavigate(index) },
                icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp)) },
                label = { Text(label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ShiftBlue,
                    selectedTextColor = ShiftBlue,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun WorkedShiftCard(entry: com.example.shiftsync.ShiftEntry) {
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val startTime = timeFormatter.format(java.util.Date(entry.startedAtMillis))
    val endMillis = entry.startedAtMillis + entry.durationMinutes * 60_000L
    val endTime = timeFormatter.format(java.util.Date(endMillis))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(54.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ShiftBlue)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "$startTime – $endTime",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "${entry.shiftType.label}  •  ${formatDuration(entry.durationMinutes)}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                if (entry.unpaidBreakMinutes > 0) {
                    Text(
                        "Break: ${entry.unpaidBreakMinutes} min",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
            // Pay chip
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = GreenAccent.copy(alpha = 0.12f)
            ) {
                Text(
                    formatCurrency(entry.estimatedPay),
                    color = GreenAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

private fun Context.startClockService(action: String) {
    val i = Intent(this, ClockForegroundService::class.java).apply { this.action = action }
    ContextCompat.startForegroundService(this, i)
}





















