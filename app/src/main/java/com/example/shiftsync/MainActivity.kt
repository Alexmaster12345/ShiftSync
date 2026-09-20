package com.example.shiftsync

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.content.ContextCompat
import com.example.shiftsync.ui.*
import com.example.shiftsync.ui.theme.ShiftSyncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Dismisses the system-drawn starting window (Theme.ShiftSync.Starting) as soon as
        // the first Compose frame is ready, instead of it staying up as a separate splash
        // before our own SplashScreen composable gets a chance to show.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ShiftRepository.init(applicationContext)
        setContent { ShiftSyncRoot() }
    }
}

enum class Screen {
    SPLASH, LOGIN, HOME, NOTIFICATIONS, MANUAL_ENTRY, CALENDAR, WORKPLACE, MAP_PICKER, PROFILE,
    APPEARANCE, NOTIFICATION_SETTINGS, PERSONAL_INFO, OVERTIME_RULES, EXPORT_REPORTS, HOW_TO_USE,
    SECURITY_PRIVACY, SALARY_CURRENCY, TERMS_OF_USE, PRIVACY_POLICY
}

@Composable
private fun ShiftSyncRoot() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var settings by remember { mutableStateOf(loadSettings(prefs)) }
    var screen by remember { mutableStateOf(Screen.SPLASH) }
    var salaryCurrencyOrigin by remember { mutableStateOf(Screen.PROFILE) }
    var splashVisible by remember { mutableStateOf(true) }
    val refresh: () -> Unit = { settings = loadSettings(prefs) }

    val appPermissionsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        prefs.edit().putBoolean(KEY_AUTO_GEOFENCING, granted).apply(); refresh()
    }
    LaunchedEffect(screen) {
        if (screen == Screen.HOME) {
            val missingPermissions = mutableListOf<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions += Manifest.permission.POST_NOTIFICATIONS
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions += Manifest.permission.ACCESS_FINE_LOCATION
            }
            if (missingPermissions.isNotEmpty()) {
                appPermissionsLauncher.launch(missingPermissions.toTypedArray())
            }
        }
    }

    // In-app Text Size (Profile > Appearance) overrides the density's font scale independent
    // of the device's own display-size setting — "Default" tracks the live system scale.
    val systemDensity = LocalDensity.current
    val scaledDensity = remember(settings.uiTextSizeIndex, systemDensity.density, systemDensity.fontScale) {
        Density(systemDensity.density, settings.uiTextFontScale(systemDensity.fontScale))
    }

    // The screen enum here is a flat state switch, not a real back-stack-aware navigation
    // component (Compose Navigation etc.) — without this, the system back gesture/button
    // falls through to the Activity default and exits the app from any non-Home screen,
    // instead of retracing the same steps the in-screen back arrows already take.
    val goBack: () -> Unit = {
        when (screen) {
            Screen.NOTIFICATIONS, Screen.MANUAL_ENTRY -> { screen = Screen.HOME; refresh() }
            Screen.CALENDAR, Screen.WORKPLACE, Screen.PROFILE -> screen = Screen.HOME
            Screen.MAP_PICKER -> screen = Screen.WORKPLACE
            Screen.APPEARANCE, Screen.NOTIFICATION_SETTINGS, Screen.PERSONAL_INFO,
            Screen.OVERTIME_RULES, Screen.SECURITY_PRIVACY -> { refresh(); screen = Screen.PROFILE }
            Screen.EXPORT_REPORTS, Screen.HOW_TO_USE, Screen.TERMS_OF_USE, Screen.PRIVACY_POLICY -> screen = Screen.PROFILE
            Screen.SALARY_CURRENCY -> { refresh(); screen = salaryCurrencyOrigin }
            Screen.SPLASH, Screen.LOGIN, Screen.HOME -> Unit
        }
    }
    BackHandler(enabled = !splashVisible && screen != Screen.HOME && screen != Screen.LOGIN, onBack = goBack)

    ShiftSyncTheme(settings.appearance) {
      CompositionLocalProvider(LocalDensity provides scaledDensity) {
        if (splashVisible) {
            SplashScreen()
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(1800)
                splashVisible = false
                screen = Screen.HOME
            }
        } else when (screen) {
            Screen.SPLASH -> SplashScreen()
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
            Screen.NOTIFICATION_SETTINGS -> NotificationSettingsScreen(settings, onBack = { refresh(); screen = Screen.PROFILE }, onSaved = refresh)
            Screen.PERSONAL_INFO -> PersonalInfoScreen(settings, onBack = { refresh(); screen = Screen.PROFILE }, onSaved = refresh)
            Screen.OVERTIME_RULES -> OvertimeRulesScreen(settings, onBack = { refresh(); screen = Screen.PROFILE }, onSaved = refresh)
            Screen.EXPORT_REPORTS -> ExportReportsScreen(settings, onBack = { screen = Screen.PROFILE })
            Screen.HOW_TO_USE -> HowToUseScreen(onBack = { screen = Screen.PROFILE })
            Screen.SECURITY_PRIVACY -> SecurityPrivacyScreen(
               onBack = { refresh(); screen = Screen.PROFILE },
               onCleared = { refresh(); screen = Screen.LOGIN }
            )
            Screen.SALARY_CURRENCY -> SalaryCurrencyScreen(settings, onBack = { refresh(); screen = salaryCurrencyOrigin }, onSaved = refresh)
            Screen.TERMS_OF_USE -> TermsOfUseScreen(onBack = { screen = Screen.PROFILE })
            Screen.PRIVACY_POLICY -> PrivacyPolicyScreen(onBack = { screen = Screen.PROFILE })
        }
      }
    }
}
