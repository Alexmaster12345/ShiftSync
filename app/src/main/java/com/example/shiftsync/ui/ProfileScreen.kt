package com.example.shiftsync.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiftsync.*
import com.example.shiftsync.ui.theme.*

@Composable
fun ProfileScreen(settings: AppSettings, onNavigate: (NavItem) -> Unit, onOpen: (Screen) -> Unit, onSignOut: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var vacationDaysPerYear by remember { mutableIntStateOf(settings.vacationDaysPerYear) }
    val vacationDaysUsed = remember(vacationDaysPerYear) { vacationDaysUsedThisYear(loadEntries(prefs)) }
    var showSignOutConfirm by remember { mutableStateOf(false) }

    AppScaffold(bottomNav = NavItem.Profile, onNavigate = onNavigate) { padding ->
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding).padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AppCard(Modifier.fillMaxWidth()) { Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) { Box(Modifier.size(84.dp).clip(CircleShape).background(ShiftBlue), contentAlignment = Alignment.Center) { Text(settings.displayName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 34.sp) }; Spacer(Modifier.height(14.dp)); Text(settings.displayName, fontWeight = FontWeight.Bold, fontSize = 26.sp); Text(settings.jobTitle, color = TextSecondary) } }
            SettingsGroup("APP SETTINGS", listOf(Triple(Icons.Default.DarkMode, "Appearance", Screen.APPEARANCE), Triple(Icons.Default.Notifications, "Notifications", Screen.NOTIFICATION_SETTINGS), Triple(Icons.Default.Person, "Personal Info", Screen.PERSONAL_INFO)), ShiftBlue, onOpen)
            SettingsGroup("WORK RULES", listOf(Triple(Icons.Default.Schedule, "Overtime Rules", Screen.OVERTIME_RULES), Triple(Icons.Default.Description, "Export Reports", Screen.EXPORT_REPORTS)), GreenAccent, onOpen)
            SettingsGroup("SECURITY & PRIVACY", listOf(Triple(Icons.Default.Shield, "Security & Privacy", Screen.SECURITY_PRIVACY)), OrangeAccent, onOpen)
            SettingsGroup("HELP", listOf(Triple(Icons.Default.MenuBook, "How to Use ShiftSync", Screen.HOW_TO_USE)), ShiftBlue, onOpen)
            SettingsGroup("LEGAL", listOf(Triple(Icons.Default.Gavel, "Terms of Use", Screen.TERMS_OF_USE), Triple(Icons.Default.Policy, "Privacy Policy", Screen.PRIVACY_POLICY)), TextSecondary, onOpen)

            SectionTitle("PAY SETTINGS")
            AppCard {
                SimpleRow(
                    Icons.Default.Work,
                    GreenAccent,
                    "Salary & Currency",
                    "${formatCurrency(settings.salaryAmount, settings.currencySymbol)}/${if (settings.paymentType == PaymentType.MONTHLY) "MO" else "HR"} • ${settings.currencySymbol} ${currencyName(settings.currencySymbol)} • ${settings.workDayHours}H DAY",
                    trailing = { Icon(Icons.Default.KeyboardArrowRight, null, tint = TextMuted) },
                    onClick = { onOpen(Screen.SALARY_CURRENCY) }
                )
            }

            SectionTitle("VACATION")
            AppCard {
                SimpleRow(
                    Icons.Default.BeachAccess,
                    GreenAccent,
                    "Days per Year",
                    "$vacationDaysUsed used of $vacationDaysPerYear",
                    trailing = {
                        Stepper(
                            "$vacationDaysPerYear",
                            onMinus = { vacationDaysPerYear = (vacationDaysPerYear - 1).coerceAtLeast(vacationDaysUsed); prefs.edit().putInt(KEY_VACATION_DAYS_PER_YEAR, vacationDaysPerYear).apply() },
                            onPlus = { vacationDaysPerYear += 1; prefs.edit().putInt(KEY_VACATION_DAYS_PER_YEAR, vacationDaysPerYear).apply() }
                        )
                    }
                )
                Spacer(Modifier.height(12.dp))
                val progress = if (vacationDaysPerYear > 0) (vacationDaysUsed.toFloat() / vacationDaysPerYear.toFloat()).coerceIn(0f, 1f) else 0f
                Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(BorderColor)) {
                    Box(Modifier.fillMaxWidth(progress).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(GreenAccent))
                }
            }

            SectionTitle("ACCOUNT")
            AppCard {
                SimpleRow(
                    Icons.Filled.Logout,
                    RedAccent,
                    "Sign Out",
                    onClick = { showSignOutConfirm = true }
                )
            }

            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ShiftSync v1.0", color = TextMuted, fontSize = 13.sp)
                Text("© 2026 ShiftSync. All rights reserved.", color = TextMuted, fontSize = 12.sp)
            }
        }
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = { TextButton(onClick = { showSignOutConfirm = false; onSignOut() }) { Text("Sign Out", color = RedAccent) } },
            dismissButton = { TextButton(onClick = { showSignOutConfirm = false }) { Text("Cancel") } }
        )
    }
}

private fun currencyName(symbol: String): String = when (symbol) {
    "$" -> "Dollar"
    "₪" -> "Shekel"
    "€" -> "Euro"
    else -> "Euro"
}

@Composable private fun SettingsGroup(title: String, rows: List<Triple<androidx.compose.ui.graphics.vector.ImageVector, String, Screen>>, tint: Color, onOpen: (Screen) -> Unit) { Text(title, color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp); AppCard { rows.forEachIndexed { index, row -> SimpleRow(row.first, tint, row.second, onClick = { onOpen(row.third) }, trailing = { Icon(Icons.Default.KeyboardArrowRight, null, tint = TextMuted) }); if (index != rows.lastIndex) HorizontalDivider(color = BorderColor) } } }
