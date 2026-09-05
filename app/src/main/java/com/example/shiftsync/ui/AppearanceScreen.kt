package com.example.shiftsync.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.shiftsync.*
import com.example.shiftsync.ui.theme.*

@Composable
fun AppearanceScreen(settings: AppSettings, onBack: () -> Unit, onSaved: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var selected by remember { mutableStateOf(settings.appearance) }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.height(8.dp)); HeaderWithBack("Appearance", onBack)
        AppCard { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { SegmentedOption("Dark", selected == AppearanceMode.DARK, { selected = AppearanceMode.DARK; prefs.edit().putString(KEY_APPEARANCE, selected.prefValue).apply(); onSaved() }, Modifier.weight(1f), Icons.Default.Brightness4); SegmentedOption("Light", selected == AppearanceMode.LIGHT, { selected = AppearanceMode.LIGHT; prefs.edit().putString(KEY_APPEARANCE, selected.prefValue).apply(); onSaved() }, Modifier.weight(1f), Icons.Default.Brightness5); SegmentedOption("System", selected == AppearanceMode.SYSTEM, { selected = AppearanceMode.SYSTEM; prefs.edit().putString(KEY_APPEARANCE, selected.prefValue).apply(); onSaved() }, Modifier.weight(1f), Icons.Default.Contrast) } }
        AppCard { Text("🌙 Dark - Optimised for low-light environments.", fontSize = 14.sp); Spacer(Modifier.height(12.dp)); Text("☀️ Light - Classic bright interface.", fontSize = 14.sp, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(12.dp)); Text("◐ System - Follows your device's appearance setting automatically.", fontSize = 14.sp) }
    }
}
