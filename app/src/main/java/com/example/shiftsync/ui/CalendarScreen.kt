package com.example.shiftsync.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiftsync.*
import com.example.shiftsync.ui.theme.*
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(onNavigate: (NavItem) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val entries = remember { loadEntries(prefs) }
    val today = Calendar.getInstance()
    var month by remember { mutableIntStateOf(today.get(Calendar.MONTH)) }
    var year by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }
    var selectedDay by remember { mutableIntStateOf(today.get(Calendar.DAY_OF_MONTH)) }
    val monthNames = DateFormatSymbols().months.toList()
    val monthEntries = entriesForMonth(entries, year, month)
    val selectedEntries = monthEntries.filter { Calendar.getInstance().apply { timeInMillis = it.startedAtMillis }.get(Calendar.DAY_OF_MONTH) == selectedDay }
    val grid = remember(month, year) {
        val cal = Calendar.getInstance().apply { set(year, month, 1) }
        val offset = cal.get(Calendar.DAY_OF_WEEK) - 1
        val days = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        MutableList<Int?>(offset) { null }.apply { repeat(days) { add(it + 1) } }
    }

    AppScaffold(bottomNav = NavItem.Calendar, onNavigate = onNavigate) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, top = padding.calculateTopPadding() + 16.dp, end = 20.dp, bottom = padding.calculateBottomPadding() + 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Schedule", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 30.sp, color = AppText)
            }
            item {
                AppCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (month == 0) { month = 11; year-- } else month-- }) { Icon(Icons.Default.ChevronLeft, null, tint = ShiftBlue) }
                        Text("${monthNames[month]} $year", fontWeight = FontWeight.Bold)
                        IconButton(onClick = { if (month == 11) { month = 0; year++ } else month++ }) { Icon(Icons.Default.ChevronRight, null, tint = ShiftBlue) }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth()) { listOf("S", "M", "T", "W", "T", "F", "S").forEach { Text(it, modifier = Modifier.weight(1f), color = TextSecondary, textAlign = TextAlign.Center) } }
                    repeat((grid.size + 6) / 7) { row ->
                        Row(Modifier.fillMaxWidth()) {
                            repeat(7) { col ->
                                val day = grid.getOrNull(row * 7 + col)
                                val hasShift = day != null && monthEntries.any { entry ->
                                    Calendar.getInstance().apply { timeInMillis = entry.startedAtMillis }.get(Calendar.DAY_OF_MONTH) == day
                                }
                                Box(
                                    Modifier.weight(1f).aspectRatio(1f).clip(CircleShape)
                                        .background(if (day == selectedDay) ShiftBlue else if (hasShift) ShiftBlue.copy(.15f) else Color.Transparent)
                                        .clickable(enabled = day != null) { if (day != null) selectedDay = day },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(day?.toString().orEmpty(), color = if (day == selectedDay) Color.White else AppText, fontWeight = if (day == selectedDay || hasShift) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("SHIFTS FOR ${monthNames[month].uppercase()}", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("${monthEntries.size} SHIFT • ${formatDuration(monthEntries.sumOf { it.durationMinutes }).uppercase()}", color = TextSecondary, fontSize = 12.sp)
                }
            }
            items(if (selectedEntries.isEmpty()) monthEntries.take(1) else selectedEntries) { entry ->
                AppCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = ShiftBlue.copy(.12f), shape = RoundedCornerShape(16.dp)) {
                            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(SimpleDateFormat("MMM", Locale.getDefault()).format(Date(entry.startedAtMillis)).uppercase(), color = ShiftBlue, fontSize = 11.sp)
                                Text(SimpleDateFormat("d", Locale.getDefault()).format(Date(entry.startedAtMillis)), color = AppText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(entry.shiftType.label, fontWeight = FontWeight.Bold)
                            Text(if (entry.shiftType == ShiftType.VACATION) "Paid day off" else "${formatTime12(entry.startedAtMillis)} - ${formatTime12(entry.startedAtMillis + entry.durationMinutes * 60000)}", color = TextSecondary, fontSize = 13.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(formatDuration(entry.durationMinutes), fontWeight = FontWeight.Bold)
                            Text("COMPLETED", color = if (entry.shiftType == ShiftType.VACATION) GreenAccent else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
