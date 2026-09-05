package com.example.shiftsync

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.shiftsync.ui.*
import com.example.shiftsync.ui.theme.ShiftSyncTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ShiftSyncRoot() }
    }
}

enum class Screen {
    LOGIN, HOME, NOTIFICATIONS, MANUAL_ENTRY, CALENDAR, WORKPLACE, MAP_PICKER, PROFILE,
    APPEARANCE, NOTIFICATION_SETTINGS, PERSONAL_INFO, OVERTIME_RULES, EXPORT_REPORTS,
    SECURITY_PRIVACY, SALARY_CURRENCY, TERMS_OF_USE, PRIVACY_POLICY
}

@Composable
private fun ShiftSyncRoot() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var settings by remember { mutableStateOf(loadSettings(prefs)) }
    var screen by remember { mutableStateOf(if (prefs.contains(KEY_DISPLAY_NAME)) Screen.HOME else Screen.LOGIN) }
    var salaryCurrencyOrigin by remember { mutableStateOf(Screen.PROFILE) }
    var showSplash by remember { mutableStateOf(true) }
    val refresh: () -> Unit = { settings = loadSettings(prefs) }

    // Mirrors the iOS ContentView's 2.5s splash hold before fading into the real content.
    LaunchedEffect(Unit) {
        delay(2500)
        showSplash = false
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        prefs.edit().putBoolean(KEY_AUTO_GEOFENCING, granted).apply(); refresh()
    }
    LaunchedEffect(screen) {
        if (screen == Screen.HOME && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    ShiftSyncTheme(settings.appearance) {
        Box {
        when (screen) {
            Screen.LOGIN -> LoginScreen { name ->
                prefs.edit().putString(KEY_DISPLAY_NAME, name).apply(); refresh(); screen = Screen.HOME
            }
            Screen.HOME -> HomeScreen(settings, { screen = Screen.MANUAL_ENTRY }, { screen = Screen.NOTIFICATIONS }) {
                when (it) {
                    NavItem.Home -> screen = Screen.HOME
                    NavItem.Calendar -> screen = Screen.CALENDAR
                    NavItem.Add -> screen = Screen.MANUAL_ENTRY
                    NavItem.Workplace -> screen = Screen.WORKPLACE
                    NavItem.Profile -> screen = Screen.PROFILE
                }
            }
            Screen.NOTIFICATIONS -> NotificationsScreen(onBack = { screen = Screen.HOME })
            Screen.MANUAL_ENTRY -> ManualEntryScreen(settings, onBack = { screen = Screen.HOME; refresh() })
            Screen.CALENDAR -> CalendarScreen(onNavigate = {
                screen = when (it) {
                    NavItem.Home -> Screen.HOME; NavItem.Calendar -> Screen.CALENDAR; NavItem.Add -> Screen.MANUAL_ENTRY; NavItem.Workplace -> Screen.WORKPLACE; NavItem.Profile -> Screen.PROFILE
                }
            })
            Screen.WORKPLACE -> WorkplaceScreen(settings, onOpenMap = { screen = Screen.MAP_PICKER }, onToggleGeofencing = { enable ->
                if (!enable) {
                    prefs.edit().putBoolean(KEY_AUTO_GEOFENCING, false).apply(); refresh()
                } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    prefs.edit().putBoolean(KEY_AUTO_GEOFENCING, true).apply(); refresh()
                } else {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }, onNavigate = {
                screen = when (it) {
                    NavItem.Home -> Screen.HOME; NavItem.Calendar -> Screen.CALENDAR; NavItem.Add -> Screen.MANUAL_ENTRY; NavItem.Workplace -> Screen.WORKPLACE; NavItem.Profile -> Screen.PROFILE
                }
            })
            Screen.MAP_PICKER -> MapPickerScreen(onBack = { screen = Screen.WORKPLACE }, onSaved = { refresh(); screen = Screen.WORKPLACE })
            Screen.PROFILE -> ProfileScreen(settings, onNavigate = {
                screen = when (it) {
                    NavItem.Home -> Screen.HOME; NavItem.Calendar -> Screen.CALENDAR; NavItem.Add -> Screen.MANUAL_ENTRY; NavItem.Workplace -> Screen.WORKPLACE; NavItem.Profile -> Screen.PROFILE
                }
            }, onOpen = { salaryCurrencyOrigin = Screen.PROFILE; screen = it }, onSignOut = {
                prefs.edit().remove(KEY_DISPLAY_NAME).apply(); refresh(); screen = Screen.LOGIN
            })
            Screen.APPEARANCE -> AppearanceScreen(settings, onBack = { refresh(); screen = Screen.PROFILE }, onSaved = refresh)
            Screen.NOTIFICATION_SETTINGS -> NotificationSettingsScreen(onBack = { refresh(); screen = Screen.PROFILE })
            Screen.PERSONAL_INFO -> PersonalInfoScreen(settings, onBack = { refresh(); screen = Screen.PROFILE }, onSaved = refresh)
            Screen.OVERTIME_RULES -> OvertimeRulesScreen(settings, onBack = { refresh(); screen = Screen.PROFILE }, onSaved = refresh)
            Screen.EXPORT_REPORTS -> ExportReportsScreen(settings, onBack = { screen = Screen.PROFILE })
            Screen.SECURITY_PRIVACY -> SecurityPrivacyScreen(
                onBack = { screen = Screen.PROFILE },
                onSalaryCurrency = { salaryCurrencyOrigin = Screen.SECURITY_PRIVACY; screen = Screen.SALARY_CURRENCY },
                onCleared = { refresh(); screen = Screen.LOGIN }
            )
            Screen.SALARY_CURRENCY -> SalaryCurrencyScreen(settings, onBack = { refresh(); screen = salaryCurrencyOrigin }, onSaved = refresh)
            Screen.TERMS_OF_USE -> TermsOfUseScreen(onBack = { screen = Screen.PROFILE })
            Screen.PRIVACY_POLICY -> PrivacyPolicyScreen(onBack = { screen = Screen.PROFILE })
        }
        AnimatedVisibility(visible = showSplash, exit = fadeOut(androidx.compose.animation.core.tween(500))) {
            SplashScreen()
        }
        }
    }
}
