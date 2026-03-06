<div align="center">

# ShiftSync

**A modern work-hours tracker for Android**

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android&logoColor=white)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=flat&logo=firebase&logoColor=black)](https://firebase.google.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow?style=flat)](LICENSE)

</div>

---

## Overview

ShiftSync is a beautifully designed Android app that helps workers effortlessly track their shifts, calculate earnings in real-time, and maintain a complete work history — all with a single tap.

Whether you are a barista, nurse, warehouse worker, or freelancer, ShiftSync keeps your hours honest and your paycheck accurate.

---

## Features

### Secure Authentication
- Sign up / Sign in with **Email & Password** or **Google Sign-In**
- Powered by **Firebase Authentication**
- Personal data is private and accessible across multiple devices

### The Live Clock
- Prominent one-tap **Clock In** button on the dashboard
- Triggers a **persistent foreground notification** so you can track time without keeping the app open
- Real-time elapsed time display

### Dynamic Timesheet Table

A clean, scannable timesheet built with **Jetpack Compose**:

| Column | Description |
|---|---|
| Date | Day the shift occurred |
| Shift Type | Morning / Night / Overtime |
| Duration | Auto-calculated from clock in/out |
| Estimated Pay | Based on your hourly rate |

### Smart Calculations
- Automatically **subtracts unpaid break time**
- Applies **1.5x Time-and-a-Half** multipliers for overtime hours
- Weekly summary with total hours and total estimated earnings

### Weekly View
- 7-day week grid showing worked days at a glance
- Tap any day to see detailed shift entries for that day
- Visual indicators for days with logged hours

### Profile & Settings
- Personal info and job details
- Configurable hourly rate
- Notification preferences
- Appearance customization

### Cloud Persistence *(optional)*
- **Firestore** integration keeps your history safe even if you lose your phone
- **Offline Mode** — data syncs automatically when you are back online

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM |
| Local Storage | SharedPreferences / Room |
| Authentication | Firebase Auth |
| Cloud Database | Cloud Firestore |
| Background Service | Android Foreground Service |
| Navigation | Jetpack Navigation Compose |
| Build System | Gradle (KTS) |

---

## Project Structure

```
app/src/main/java/com/example/shiftsync/
+-- MainActivity.kt                  # App entry point & Compose navigation host
+-- ShiftDomain.kt                   # Models, payroll calculator, persistence helpers
+-- ClockForegroundService.kt        # Live clock persistent notification service
+-- ui/
    +-- HomeScreen.kt                # Main dashboard with Clock In/Out
    +-- ManualEntryScreen.kt         # Manual hours entry form
    +-- CalendarScreen.kt            # Calendar / weekly overview
    +-- NotificationsScreen.kt       # Notifications screen
    +-- ProfileScreen.kt             # User profile & settings
    +-- LoginScreen.kt               # Authentication screen
    +-- theme/                       # Color palette, typography, shapes
```

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- Android SDK 26+
- A Firebase project (for Auth and Firestore features)

### Clone the Repository

```bash
git clone https://github.com/Alexmaster12345/ShiftSync.git
cd ShiftSync
```

### Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com/) and create a new project
2. Add an Android app with package name `com.example.shiftsync`
3. Download `google-services.json` and place it in the `app/` folder
4. Enable **Authentication** (Email/Password + Google) in the Firebase console
5. Enable **Firestore Database** in the Firebase console

### Build & Run

```bash
# Run on a connected device or emulator
./gradlew installDebug

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
- [x] Shift types — Morning, Night, Overtime
- [x] Automatic break subtraction and overtime calculations
- [x] Dynamic timesheet table
- [x] Weekly 7-day view with day drill-down
- [x] Manual hours entry
- [x] Profile screen with settings
- [ ] Firebase Authentication (Email + Google)
- [ ] Room database for offline-first storage
- [ ] Firestore cloud sync with conflict handling
- [ ] CSV / PDF export for payroll
- [ ] Home screen widget for quick Clock In
- [ ] Dark / Light theme toggle

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