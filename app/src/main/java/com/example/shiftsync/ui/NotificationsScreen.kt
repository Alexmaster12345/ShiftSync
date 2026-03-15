package com.example.shiftsync.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiftsync.ui.theme.*
import com.example.shiftsync.ui.theme.LocalDimens

private data class NotifItem(
    val icon: ImageVector,
    val iconBg: Color,
    val title: String,
    val body: String,
    val time: String,
    val unread: Boolean = false
)

private val sampleNotifs = listOf(
    NotifItem(Icons.Default.Schedule, ShiftBlue,
        "Shift Reminder", "Your shift starts in 30 minutes at Main Branch.", "2 min ago", true),
    NotifItem(Icons.Default.SyncAlt, Color(0xFF8B5CF6),
        "Shift Trade Request", "Jordan M. wants to swap Friday's shift with you.", "1 hr ago", true),
    NotifItem(Icons.Default.CheckCircle, GreenAccent,
        "Timesheet Approved", "Your timesheet for Oct 1–7 has been approved.", "3 hrs ago"),
    NotifItem(Icons.Default.MonetizationOn, GreenAccent,
        "Pay Processed", "Your pay for the last period has been processed.", "Yesterday"),
    NotifItem(Icons.Default.Warning, OrangeAccent,
        "Schedule Updated", "Manager updated your schedule for next week.", "2 days ago"),
    NotifItem(Icons.Default.Info, TextSecondary,
        "App Update", "ShiftSync v2.1 is available with new features.", "3 days ago"),
)

@Composable
fun NotificationsScreen(onBack: () -> Unit) {
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
                Text("Notifications", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(Modifier.weight(1f))
                // Mark all read button
                TextButton(onClick = {}) {
                    Text("Clear all", color = ShiftBlue, fontSize = 13.sp)
                }
            }
        },
        bottomBar = { BottomNavBar(selected = -1) { onBack() } }
    ) { padding ->
        val dimens = LocalDimens.current
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = dimens.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            val unread = sampleNotifs.filter { it.unread }
            val read   = sampleNotifs.filter { !it.unread }

            if (unread.isNotEmpty()) {
                item {
                    Text("NEW", color = TextSecondary, fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp,
                        modifier = Modifier.padding(vertical = 4.dp))
                }
                items(unread) { NotifCard(it) }
            }
            if (read.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text("EARLIER", color = TextSecondary, fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp,
                        modifier = Modifier.padding(vertical = 4.dp))
                }
                items(read) { NotifCard(it) }
            }
        }
    }
}

@Composable
private fun NotifCard(item: NotifItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.unread) DarkCardAlt else DarkCard
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(item.iconBg.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = null, tint = item.iconBg, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(item.time, color = TextMuted, fontSize = 11.sp)
                }
                Spacer(Modifier.height(3.dp))
                Text(item.body, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
            }
            if (item.unread) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(ShiftBlue)
                        .align(Alignment.Top)
                )
            }
        }
    }
}



