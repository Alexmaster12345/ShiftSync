<div align="center">

# ShiftSync

**A modern, 100% offline work-hours tracker for Android**

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android&logoColor=white)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow?style=flat)](LICENSE)

</div>

---

## Overview

ShiftSync is a beautifully designed, privacy-first Android app that helps workers effortlessly track their shifts, calculate earnings in real-time, and maintain a complete work history — all with a single tap.

Whether you are a barista, nurse, warehouse worker, or freelancer, ShiftSync keeps your hours honest and your paycheck accurate. There are **no accounts, no servers, and no analytics** — every byte of data stays on your device.

---

## Features

### Guest-First Onboarding
- No sign-up required — enter an optional display name and tap **Continue as Guest**
- "Sign In with Email" is reserved for a future release (shown as *Coming Soon*)

### The Live Clock
- Prominent one-tap **Clock In / Clock Out** button on the dashboard
- Triggers a **persistent foreground notification** so you can track time without keeping the app open
- Real-time elapsed-time display, estimated pay, and weekly/monthly rollups

### Manual Entries — Regular & Vacation
- Add a **Regular** shift with custom start/end times and unpaid break duration
- Add a **Vacation** day with automatic estimated-pay calculation based on your salary settings
- Full month calendar picker for choosing the entry date

### Calendar / Schedule
- Month calendar highlighting days with logged shifts
- Day-by-day shift list with duration and completion status

### Workplace & Geofencing
- Set a workplace location from an interactive map picker
- Toggle **Auto-Geofencing** to arm arrival/departure detection (requires location permission)

### Smart Payroll Calculations
- Automatically subtracts unpaid break time
- **Configurable Overtime Rules** — daily/weekly thresholds and overtime multiplier (1.2x / 1.5x / 2x), with auto-split of shifts that exceed the daily threshold
- **Salary & Currency** settings — Dollar / Shekel / Euro, Monthly or Hourly pay type, configurable work-day hours

### Export & Backup
- **Export Reports** as CSV or PDF for a selected period (This Week / This Month / This Year / All Time), with a live preview list
- **Security & Privacy** screen — export a full JSON backup, import a backup on a new device, or clear all app data

### Notifications & Reminders
- Shift start/end alerts when clocking in/out
- Scheduled clock-in/clock-out reminders with day-of-week selection and Material3 time pickers
- Sound & vibration preferences; reminders persist across reboots via `BootReminderReceiver`

### Profile & Settings
- Personal Info (display name, email, job title, branch)
- Appearance (Dark / Light / System)
- Terms of Use and Privacy Policy screens
- Fully responsive UI that scales across all screen sizes via a shared `Dimens` system

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 (light theme) |
| Local Storage | SharedPreferences (delimited + JSON backup) |
| Background Work | Foreground Service, AlarmManager, BroadcastReceiver |
| Export | Android `PdfDocument` API, CSV + FileProvider sharing |
| Build System | Gradle (KTS) |

No network, cloud database, or third-party auth is used — the app is fully offline by design.

---

## Project Structure

```
app/src/main/java/com/example/shiftsync/
+-- MainActivity.kt                  # App entry point, navigation host, Screen enum
+-- ShiftDomain.kt                   # Models, payroll calculator, settings & persistence helpers
+-- ClockForegroundService.kt        # Live clock persistent notification service
+-- ShiftAlertHelper.kt              # Shift start/end alert notifications
+-- ShiftReminderReceiver.kt         # Scheduled clock-in/out reminders
+-- BootReminderReceiver.kt          # Re-schedules reminders after device reboot
+-- ui/
    +-- LoginScreen.kt                  # Guest onboarding
    +-- HomeScreen.kt                   # Dashboard with Clock In/Out
    +-- ManualEntryScreen.kt            # Manual entry — Regular & Vacation
    +-- CalendarScreen.kt               # Calendar / schedule overview
    +-- NotificationsScreen.kt          # Notifications panel
    +-- NotificationSettingsScreen.kt   # Arrival/departure + reminder settings
    +-- WorkplaceScreen.kt              # Workplace + map location picker
    +-- ProfileScreen.kt                # Profile hub
    +-- PersonalInfoScreen.kt           # Name, email, job title, branch
    +-- AppearanceScreen.kt             # Dark / Light / System
    +-- OvertimeRulesScreen.kt          # Overtime thresholds & multiplier
    +-- ReportAndSecurityScreens.kt     # Export Reports, Salary & Currency,
    |                                   # Security & Privacy, Terms of Use, Privacy Policy
    +-- UiComponents.kt                 # Shared cards, rows, floating bottom nav bar
    +-- theme/                          # Color palette, typography, Dimens, shapes
```

---

## Getting Started

### Prerequisites

- Android Studio (Hedgehog or later)
- Android SDK 26+

### Clone the Repository

```bash
git clone https://github.com/Alexmaster12345/ShiftSync.git
cd ShiftSync
```

### Build & Run

```bash
# Run on a connected device or emulator
./gradlew installDebug

# Build a debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test
```

Or open the project in **Android Studio** and press **Run**.

---

## Testing

Unit tests cover payroll calculation scenarios:

```bash
./gradlew test
```

Test file: `app/src/test/java/com/example/shiftsync/PayrollCalculatorTest.kt`

---

## Roadmap

- [x] Clock In/Out with persistent foreground notification
- [x] Shift types — Regular, Vacation, Overtime
- [x] Configurable overtime rules (daily/weekly threshold, multiplier)
- [x] Manual entry with month calendar picker
- [x] Calendar / schedule view with day drill-down
- [x] Workplace map picker & auto-geofencing toggle
- [x] Export Reports — CSV & PDF
- [x] JSON backup / import & clear-all-data
- [x] Salary & Currency settings (Dollar / Shekel / Euro)
- [x] Scheduled clock-in/out reminders with day-of-week selection
- [x] Responsive UI across all screen sizes
- [x] Light theme redesign with floating bottom navigation
- [ ] Full Dark theme (Appearance screen currently persists the choice; live theme switching is in progress)
- [ ] Home screen widget for quick Clock In
- [ ] Multi-workplace support

---

## Privacy

ShiftSync does not collect, transmit, or sell any personal data. There are no servers, accounts, or analytics/advertising SDKs. All shift entries, settings, and profile info are stored only in local app storage on your device — see the in-app **Privacy Policy** and **Terms of Use** screens for full details.

---

## Contributing

Contributions are welcome!

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "Add your feature"`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

---

## License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

<div align="center">
Built with Kotlin and Jetpack Compose
</div>
