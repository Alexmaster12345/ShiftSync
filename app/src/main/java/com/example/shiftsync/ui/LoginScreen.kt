package com.example.shiftsync.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiftsync.ui.theme.*

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(LightBackground).padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(60.dp))
        Box(Modifier.size(90.dp).clip(CircleShape).background(ShiftBlue), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.AccessTime, null, tint = Color.White, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("ShiftSync", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = AppText)
        Spacer(Modifier.height(8.dp))
        Text("Track your hours, sync with your life.\nSimple & automatic.", color = TextSecondary, lineHeight = 22.sp)
        Spacer(Modifier.height(40.dp))
        Text("What's your name?", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.SemiBold, color = AppText)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = name, onValueChange = { name = it }, singleLine = true,
            leadingIcon = { Icon(Icons.Default.Person, null) },
            placeholder = { Text("e.g. Alex Johnson") },
            shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = CardBackground, focusedContainerColor = CardBackground, unfocusedBorderColor = BorderColor, focusedBorderColor = ShiftBlue)
        )
        Spacer(Modifier.height(8.dp))
        Text("Optional — you can update this later in your profile.", modifier = Modifier.fillMaxWidth(), color = TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))
        Button(onClick = { onLoginSuccess(name.ifBlank { "Guest" }) }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(30.dp), colors = ButtonDefaults.buttonColors(containerColor = ShiftBlue)) {
            Text("Continue as Guest", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = BorderColor); Text("  or  ", color = TextSecondary); HorizontalDivider(modifier = Modifier.weight(1f), color = BorderColor)
        }
        Spacer(Modifier.height(18.dp))
        OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(30.dp), border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(BorderColor))) {
            Icon(Icons.Default.Email, null)
            Spacer(Modifier.width(8.dp))
            Text("Sign In with Email")
            Spacer(Modifier.width(8.dp))
            Surface(shape = RoundedCornerShape(16.dp), color = ShiftBlue) { Text("Coming Soon", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
        }
    }
}
