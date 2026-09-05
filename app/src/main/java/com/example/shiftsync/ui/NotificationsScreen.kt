package com.example.shiftsync.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiftsync.ui.theme.*

@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    Scaffold(containerColor = Color.Transparent) { padding ->
        Column(Modifier.fillMaxSize().background(DarkSheet).padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("Close", color = ShiftBlue) }
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { Text("Notifications", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                Spacer(Modifier.width(56.dp))
            }
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(82.dp).clip(CircleShape).background(Color.White.copy(.08f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.NotificationsOff, null, tint = Color.White, modifier = Modifier.size(36.dp)) }
            Spacer(Modifier.height(20.dp))
            Text("No alerts yet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Spacer(Modifier.height(8.dp))
            Text("Arrival and departure alerts will appear here.", color = Color.White.copy(.65f))
            Spacer(Modifier.weight(1f))
        }
    }
}
