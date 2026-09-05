package com.example.shiftsync.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiftsync.AppSettings
import com.example.shiftsync.ui.theme.*

@Composable
fun WorkplaceScreen(settings: AppSettings, onOpenMap: () -> Unit, onNavigate: (NavItem) -> Unit) {
    AppScaffold(bottomNav = NavItem.Workplace, onNavigate = onNavigate) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Workplace", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 30.sp, color = AppText)
            AppCard { SimpleRow(Icons.Default.LocationSearching, TextSecondary, if (settings.workplaceSet) settings.workplaceLabel else "No workplace set", if (settings.workplaceSet) "Tap below to update your workplace location" else "Tap below to set your workplace location", onClick = onOpenMap); HorizontalDivider(color = BorderColor); Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { SimpleRow(Icons.Default.WifiTethering, ShiftBlue, "Auto-Geofencing", if (settings.workplaceSet) "Enabled when you arrive or leave work" else "Needs location permission", trailing = { Switch(checked = settings.autoGeofencingEnabled, onCheckedChange = null) }) } }
            Button(onClick = onOpenMap, modifier = Modifier.fillMaxWidth().height(56.dp), shape = androidx.compose.foundation.shape.RoundedCornerShape(30.dp), colors = ButtonDefaults.buttonColors(containerColor = ShiftBlue)) { Icon(Icons.Default.Map, null, tint = androidx.compose.ui.graphics.Color.White); Spacer(Modifier.width(8.dp)); Text("Set Workplace on Map", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold) }
        }
    }
}
