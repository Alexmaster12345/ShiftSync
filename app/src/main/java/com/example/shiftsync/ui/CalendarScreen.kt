package com.example.shiftsync.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiftsync.ui.theme.*
import com.example.shiftsync.ui.theme.LocalDimens
import java.util.*

@Composable
fun CalendarScreen(onBack: () -> Unit) {
    val today = remember { Calendar.getInstance() }
    var displayMonth by remember { mutableIntStateOf(today.get(Calendar.MONTH)) }
    var displayYear  by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }
    var selectedDay  by remember { mutableIntStateOf(today.get(Calendar.DAY_OF_MONTH)) }

    val monthNames = listOf(
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    )

    val calGrid = remember(displayMonth, displayYear) {
        val c = Calendar.getInstance().apply { set(displayYear, displayMonth, 1) }
        // Sun=0 offset so grid starts on Sunday
        val firstDow = c.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
        val daysInMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH)
        val cells = mutableListOf<Int?>()
        repeat(firstDow) { cells.add(null) }
        for (d in 1..daysInMonth) cells.add(d)
        cells
    }

    // Fake shift dots for demo — days that have a shift
    val shiftDays = setOf(3, 5, 8, 10, 12, 15, 17, 19, 22, 24, 26)

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
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "Schedule",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.width(48.dp))
            }
        },
        bottomBar = { BottomNavBar(selected = 1) { onBack() } }
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

            // ── Month card ────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // Month navigation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (displayMonth == 0) { displayMonth = 11; displayYear-- }
                            else displayMonth--
                        }) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Prev", tint = ShiftBlue)
                        }
                        Text(
                            "${monthNames[displayMonth]} $displayYear",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        IconButton(onClick = {
                            if (displayMonth == 11) { displayMonth = 0; displayYear++ }
                            else displayMonth++
                        }) {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next", tint = ShiftBlue)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Day-of-week headers
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("S","M","T","W","T","F","S").forEach { h ->
                            Text(
                                h,
                                modifier = Modifier.weight(1f),
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Calendar grid
                    val rows = (calGrid.size + 6) / 7
                    for (row in 0 until rows) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (col in 0 until 7) {
                                val idx = row * 7 + col
                                val day = calGrid.getOrNull(idx)
                                val isSelected = day == selectedDay &&
                                        displayMonth == today.get(Calendar.MONTH) &&
                                        displayYear == today.get(Calendar.YEAR)
                                val isToday = day == today.get(Calendar.DAY_OF_MONTH) &&
                                        displayMonth == today.get(Calendar.MONTH) &&
                                        displayYear == today.get(Calendar.YEAR)
                                val hasShift = day != null && shiftDays.contains(day)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> ShiftBlue
                                                else -> Color.Transparent
                                            }
                                        )
                                        .then(
                                            if (isToday && !isSelected)
                                                Modifier.border(1.dp, ShiftBlue, CircleShape)
                                            else Modifier
                                        )
                                        .then(if (day != null) Modifier.clickable { selectedDay = day } else Modifier),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (day != null) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                day.toString(),
                                                color = if (isSelected) Color.White else TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                            )
                                            if (hasShift) {
                                                Spacer(Modifier.height(1.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) Color.White else ShiftBlue)
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

            Spacer(Modifier.height(24.dp))

            // ── Shifts for selected day ───────────────────────────────
            Text(
                "Shifts on ${monthNames[displayMonth]} $selectedDay",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(10.dp))

            if (shiftDays.contains(selectedDay)) {
                ShiftDetailCard(
                    time = "09:00 AM – 05:00 PM",
                    role = "Senior Barista",
                    location = "Main Branch",
                    color = ShiftBlue
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkCard)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No shifts scheduled", color = TextMuted, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ShiftDetailCard(time: String, role: String, location: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
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
                    .width(4.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Spacer(Modifier.width(14.dp))
            Column {
                Text(time, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
                Text("$role  •  $location", color = TextSecondary, fontSize = 13.sp)
            }
        }
    }
}


