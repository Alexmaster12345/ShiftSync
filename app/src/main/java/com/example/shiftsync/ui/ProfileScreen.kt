package com.example.shiftsync.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiftsync.AppSettings
import com.example.shiftsync.Screen
import com.example.shiftsync.ui.theme.*

@Composable
fun ProfileScreen(settings: AppSettings, onNavigate: (NavItem) -> Unit, onOpen: (Screen) -> Unit) {
    AppScaffold(bottomNav = NavItem.Profile, onNavigate = onNavigate) { padding ->
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding).padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AppCard(Modifier.fillMaxWidth()) { Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) { Box(Modifier.size(84.dp).clip(CircleShape).background(ShiftBlue), contentAlignment = Alignment.Center) { Text(settings.displayName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 34.sp) }; Spacer(Modifier.height(14.dp)); Text(settings.displayName, fontWeight = FontWeight.Bold, fontSize = 26.sp); Text(settings.jobTitle, color = TextSecondary) } }
            SettingsGroup("APP SETTINGS", listOf(Triple(Icons.Default.DarkMode, "Appearance", Screen.APPEARANCE), Triple(Icons.Default.Notifications, "Notifications", Screen.NOTIFICATION_SETTINGS), Triple(Icons.Default.Person, "Personal Info", Screen.PERSONAL_INFO)), ShiftBlue, onOpen)
            SettingsGroup("WORK RULES", listOf(Triple(Icons.Default.Schedule, "Overtime Rules", Screen.OVERTIME_RULES), Triple(Icons.Default.Description, "Export Reports", Screen.EXPORT_REPORTS)), GreenAccent, onOpen)
            SettingsGroup("SECURITY & PRIVACY", listOf(Triple(Icons.Default.Shield, "Security & Privacy", Screen.SECURITY_PRIVACY)), OrangeAccent, onOpen)
            SettingsGroup("LEGAL", listOf(Triple(Icons.Default.Gavel, "Terms of Use", Screen.TERMS_OF_USE), Triple(Icons.Default.Policy, "Privacy Policy", Screen.PRIVACY_POLICY)), TextSecondary, onOpen)
        }
    }
}

@Composable private fun SettingsGroup(title: String, rows: List<Triple<androidx.compose.ui.graphics.vector.ImageVector, String, Screen>>, tint: Color, onOpen: (Screen) -> Unit) { Text(title, color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp); AppCard { rows.forEachIndexed { index, row -> SimpleRow(row.first, tint, row.second, onClick = { onOpen(row.third) }, trailing = { Icon(Icons.Default.KeyboardArrowRight, null, tint = TextMuted) }); if (index != rows.lastIndex) HorizontalDivider(color = BorderColor) } } }
