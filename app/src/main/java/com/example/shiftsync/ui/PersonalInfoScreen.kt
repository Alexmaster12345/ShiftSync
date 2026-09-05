package com.example.shiftsync.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun PersonalInfoScreen(settings: AppSettings, onBack: () -> Unit, onSaved: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var name by remember { mutableStateOf(settings.displayName) }
    var email by remember { mutableStateOf(settings.email) }
    var title by remember { mutableStateOf(settings.jobTitle) }
    var branch by remember { mutableStateOf(settings.branch) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(8.dp)); HeaderWithBack("Personal Info", onBack)
        Box(Modifier.size(86.dp).clip(CircleShape).background(ShiftBlue), contentAlignment = Alignment.Center) { Text(name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 34.sp) }
        AppCard(Modifier.fillMaxWidth()) { FieldRow(Icons.Default.Person, "Display Name", name) { name = it }; HorizontalDivider(color = BorderColor); FieldRow(Icons.Default.Email, "Email", email) { email = it }; HorizontalDivider(color = BorderColor); FieldRow(Icons.Default.Work, "Job Title", title) { title = it }; HorizontalDivider(color = BorderColor); FieldRow(Icons.Default.Business, "Branch", branch) { branch = it } }
        Button(onClick = { prefs.edit().putString(KEY_DISPLAY_NAME, name).putString(KEY_EMAIL, email).putString(KEY_JOB_TITLE, title).putString(KEY_BRANCH, branch).apply(); onSaved(); onBack() }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = androidx.compose.foundation.shape.RoundedCornerShape(30.dp), colors = ButtonDefaults.buttonColors(containerColor = ShiftBlue)) { Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold) }
    }
}

@Composable private fun FieldRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, onValueChange: (String) -> Unit) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = ShiftBlue); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(label, color = TextSecondary, fontSize = 12.sp); OutlinedTextField(value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(), placeholder = { Text(if (label == "Branch") "e.g. Main Branch" else "") }, colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = CardBackground, unfocusedContainerColor = CardBackground, focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent)) } } }
