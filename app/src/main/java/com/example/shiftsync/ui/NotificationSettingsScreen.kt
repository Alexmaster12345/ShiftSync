package com.example.shiftsync.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shiftsync.*
import com.example.shiftsync.ui.theme.*

@Composable
fun NotificationSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var enabled by remember { mutableStateOf(prefs.getBoolean(KEY_NOTIFY_SHIFT_START, true) || prefs.getBoolean(KEY_NOTIFY_SHIFT_END, true)) }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.height(8.dp)); HeaderWithBack("Notifications", onBack)
        AppCard { SimpleRow(Icons.Default.Notifications, OrangeAccent, "Arrival & Departure Alerts", if (enabled) "On — notified when you arrive or leave work" else "Off", trailing = { Switch(checked = enabled, onCheckedChange = { enabled = it; prefs.edit().putBoolean(KEY_NOTIFY_SHIFT_START, it).putBoolean(KEY_NOTIFY_SHIFT_END, it).apply() }) }) }
    }
}
