package com.example.shiftsync.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiftsync.ui.theme.*

@Composable
fun HowToUseScreen(onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        HeaderWithBack("How to Use ShiftSync", onBack)
        HelpCard(Icons.Default.AccessTime, ShiftBlue, "Clock In / Out", "Tap Clock In on the Home screen to start a shift, and Clock Out when you're done. Pay is calculated live using your rate in Salary & Currency.")
        HelpCard(Icons.Default.EditCalendar, GreenAccent, "Manual Entry & Day Types", "Tap the + button to log a shift after the fact, or record paid day off time when you need to add it manually.")
        HelpCard(Icons.Default.Payments, GreenAccent, "Pay & Overtime", "Set your hourly or monthly rate, currency, and work day hours in Profile → Salary & Currency. Turn on Overtime Rules to automatically split shifts into regular + overtime pay once your daily threshold is exceeded.")
        HelpCard(Icons.Default.CalendarMonth, OrangeAccent, "Calendar & Export", "The Calendar tab shows every logged shift by day. Profile → Export Reports lets you generate a CSV or PDF report to share or file.")
        HelpCard(Icons.Default.LocationOn, RedAccent, "Workplace Alerts", "Set your workplace on the map from the Workplace tab to keep your workplace details up to date. Android currently stores the location for future alert features.")
        HelpCard(Icons.Default.Home, ShiftBlue, "Work From Home", "Profile → Notifications → Work From Home lets you set fixed Clock In / Clock Out times on your Home days without needing a workplace location.")
        HelpCard(Icons.Default.CloudUpload, GreenAccent, "Backup Your Data", "Profile → Security & Privacy → Export Backup saves all your shift records to a JSON file. Import it on another device to pick up right where you left off.")
    }
}

@Composable
private fun HelpCard(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, title: String, body: String) {
    AppCard {
        androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, tint = tint)
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, color = AppText)
                Spacer(Modifier.height(4.dp))
                Text(body, color = TextSecondary, fontSize = 13.sp, lineHeight = 20.sp)
            }
        }
    }
}
