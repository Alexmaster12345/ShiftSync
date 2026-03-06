package com.example.shiftsync

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.shiftsync.ui.*
import com.example.shiftsync.ui.theme.ShiftSyncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShiftSyncTheme {
                ShiftSyncApp()
            }
        }
    }
}

private enum class Screen { LOGIN, HOME, MANUAL_ENTRY, CALENDAR, NOTIFICATIONS, PROFILE }

@Composable
fun ShiftSyncApp() {
    val context = LocalContext.current
    var screen by remember { mutableStateOf(Screen.LOGIN) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    LaunchedEffect(screen) {
        if (screen == Screen.HOME &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    when (screen) {
        Screen.LOGIN -> LoginScreen(
            onLoginSuccess = { screen = Screen.HOME }
        )
        Screen.HOME -> HomeScreen(
            onAddManualEntry  = { screen = Screen.MANUAL_ENTRY },
            onCalendarView    = { screen = Screen.CALENDAR },
            onNotifications   = { screen = Screen.NOTIFICATIONS },
            onProfile         = { screen = Screen.PROFILE }
        )
        Screen.MANUAL_ENTRY -> ManualEntryScreen(
            onBack = { screen = Screen.HOME }
        )
        Screen.CALENDAR -> CalendarScreen(
            onBack = { screen = Screen.HOME }
        )
        Screen.NOTIFICATIONS -> NotificationsScreen(
            onBack = { screen = Screen.HOME }
        )
        Screen.PROFILE -> ProfileScreen(
            onBack = { screen = Screen.HOME },
            onCalendarView = { screen = Screen.CALENDAR },
            onNotifications = { screen = Screen.NOTIFICATIONS }
        )
    }
}