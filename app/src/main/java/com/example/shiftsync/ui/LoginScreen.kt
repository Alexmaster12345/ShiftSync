package com.example.shiftsync.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiftsync.ui.theme.ShiftBlue
import com.example.shiftsync.ui.theme.TextMuted
import com.example.shiftsync.ui.theme.LocalDimens

// Login screen uses a LIGHT white background exactly like the mockup PNG
private val LightBg       = Color(0xFFFFFFFF)
private val LightText     = Color(0xFF111827)
private val LightSubText  = Color(0xFF6B7280)
private val LightBorder   = Color(0xFFE5E7EB)
private val LightField    = Color(0xFFF9FAFB)

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var identifier by remember { mutableStateOf("") }
    var password   by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
    ) {
        val dimens = LocalDimens.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = (28 * dimens.scaleFactor).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height((72 * dimens.scaleFactor).dp))

            // ── App icon ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size((84 * dimens.scaleFactor).dp)
                    .shadow(8.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(ShiftBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SyncAlt,
                    contentDescription = "ShiftSync",
                    tint = Color.White,
                    modifier = Modifier.size((46 * dimens.scaleFactor).dp)
                )
            }

            Spacer(Modifier.height(18.dp))

            // ── Brand name ────────────────────────────────────────────
            Text(
                text = "ShiftSync",
                color = ShiftBlue,
                fontWeight = FontWeight.ExtraBold,
                fontSize = (30 * dimens.scaleFactor).sp,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "EMPLOYEE PORTAL",
                color = LightSubText,
                fontSize = dimens.fontSmall,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height((44 * dimens.scaleFactor).dp))

            // ── Heading ───────────────────────────────────────────────
            Text(
                text = "Employee Login",
                color = LightText,
                fontWeight = FontWeight.ExtraBold,
                fontSize = (28 * dimens.scaleFactor).sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Access your shifts and work schedule",
                color = LightSubText,
                fontSize = 15.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))

            // ── Email / ID label + field ──────────────────────────────
            Text(
                text = "Email or Employee ID",
                color = LightText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = identifier,
                onValueChange = { identifier = it },
                placeholder = {
                    Text("e.g. EMP-12345", color = LightSubText, fontSize = 15.sp)
                },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = LightSubText)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = ShiftBlue,
                    unfocusedBorderColor = LightBorder,
                    focusedContainerColor   = LightBg,
                    unfocusedContainerColor = LightBg,
                    focusedTextColor   = LightText,
                    unfocusedTextColor = LightText,
                    cursorColor = ShiftBlue
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )

            Spacer(Modifier.height(18.dp))

            // ── Password label + field ────────────────────────────────
            Text(
                text = "Password",
                color = LightText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = {
                    Text("••••••••", color = LightSubText, fontSize = 15.sp)
                },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = LightSubText)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility
                                          else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = LightSubText
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = ShiftBlue,
                    unfocusedBorderColor = LightBorder,
                    focusedContainerColor   = LightBg,
                    unfocusedContainerColor = LightBg,
                    focusedTextColor   = LightText,
                    unfocusedTextColor = LightText,
                    cursorColor = ShiftBlue
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )

            // ── Forgot password ───────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {}) {
                    Text("Forgot password?", color = ShiftBlue, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(6.dp))

            // ── Sign In ───────────────────────────────────────────────
            Button(
                onClick = { onLoginSuccess(identifier.ifBlank { "User" }) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height((54 * dimens.scaleFactor).dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShiftBlue),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Sign In",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── Continue as Guest ─────────────────────────────────────
            OutlinedButton(
                onClick = { onLoginSuccess("Guest") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height((54 * dimens.scaleFactor).dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LightText),
                border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder)
            ) {
                Text(
                    text = "Continue as Guest",
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = LightText
                )
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(32.dp))

            // ── Create account footer ─────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text("New to the team?  ", color = LightSubText, fontSize = 14.sp)
                TextButton(onClick = {}, contentPadding = PaddingValues(0.dp)) {
                    Text(
                        text = "Create Account",
                        color = ShiftBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
