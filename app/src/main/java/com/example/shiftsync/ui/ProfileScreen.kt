
package com.example.shiftsync.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiftsync.ui.theme.*

@Composable
fun ProfileScreen(onBack: () -> Unit) {
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
                Text("Profile", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = ShiftBlue)
                }
            }
        },
        bottomBar = { BottomNavBar(selected = 4) { onBack() } }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Avatar ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(ShiftBlue, ShiftBlueDark))),
                contentAlignment = Alignment.Center
            ) {
                Text("A", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold)
            }

            Spacer(Modifier.height(14.dp))

            Text("Alex Johnson", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Senior Barista", color = TextSecondary, fontSize = 14.sp)

            Spacer(Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ShiftBlue.copy(alpha = 0.15f)
            ) {
                Text(
                    "EMP-12345",
                    color = ShiftBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Stats row ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileStatCard(Modifier.weight(1f), "32.5", "Hrs This Week")
                ProfileStatCard(Modifier.weight(1f), "$780", "Est. Earnings")
                ProfileStatCard(Modifier.weight(1f), "94%", "Attendance")
            }

            Spacer(Modifier.height(28.dp))

            // ── Settings sections ─────────────────────────────────────
            SectionHeader("ACCOUNT")
            ProfileRow(Icons.Default.Person, "Personal Info", ShiftBlue)
            ProfileRow(Icons.Default.AccountBox, "Job Details", ShiftBlue)
            ProfileRow(Icons.Default.Lock, "Change Password", ShiftBlue)

            Spacer(Modifier.height(16.dp))
            SectionHeader("PREFERENCES")
            ProfileRow(Icons.Default.Notifications, "Notification Settings", OrangeAccent)
            ProfileRow(Icons.Default.Star, "Appearance", OrangeAccent)
            ProfileRow(Icons.Default.Settings, "Language", OrangeAccent)

            Spacer(Modifier.height(16.dp))
            SectionHeader("SUPPORT")
            ProfileRow(Icons.Default.Notifications, "Help & FAQ", TextSecondary)
            ProfileRow(Icons.Default.Notifications, "Privacy Policy", TextSecondary)
            ProfileRow(Icons.Default.Info, "About ShiftSync", TextSecondary)

            Spacer(Modifier.height(24.dp))

            // ── Sign Out ──────────────────────────────────────────────
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedAccent.copy(alpha = 0.15f)),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = RedAccent, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Sign Out", color = RedAccent, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileStatCard(modifier: Modifier, value: String, label: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(3.dp))
            Text(label, color = TextSecondary, fontSize = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text, color = TextSecondary, fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@Composable
private fun ProfileRow(icon: ImageVector, label: String, iconTint: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(12.dp),
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
                    .size(36.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(label, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
        }
    }
}









