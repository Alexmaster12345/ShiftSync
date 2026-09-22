package com.example.shiftsync

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.shiftsync.ui.theme.ShiftBlue

// Opt-in app lock (Security & Privacy). Gates the whole app behind a fingerprint,
// face unlock, or device PIN/pattern/password, mirroring iOS's Face ID/passcode gate.
object AppLockManager {
    var isUnlocked by mutableStateOf(true)
        private set

    // Called when the app leaves the foreground, so the next return requires re-authentication.
    fun lock(enabled: Boolean) {
        if (enabled) isUnlocked = false
    }

    fun authenticate(activity: FragmentActivity) {
        if (isUnlocked) return
        val allowedAuthenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        val canAuthenticate = BiometricManager.from(activity).canAuthenticate(allowedAuthenticators)
        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            // No fingerprint/face/PIN configured on the device at all — don't strand the user
            // behind a lock screen they have no way to open.
            isUnlocked = true
            return
        }
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    isUnlocked = true
                }
            }
        )
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock ShiftSync")
            .setSubtitle("Unlock to view your shifts and earnings")
            .setAllowedAuthenticators(allowedAuthenticators)
            .build()
        prompt.authenticate(promptInfo)
    }
}

@Composable
fun AppLockOverlay(onUnlock: () -> Unit) {
    LaunchedEffect(Unit) { onUnlock() }
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = ShiftBlue, modifier = Modifier.size(52.dp))
        Spacer(Modifier.height(20.dp))
        Text("ShiftSync is Locked", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onUnlock) { Text("Unlock") }
    }
}
