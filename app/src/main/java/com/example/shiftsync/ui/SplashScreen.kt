package com.example.shiftsync.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiftsync.ui.theme.AppText
import com.example.shiftsync.ui.theme.ShiftBlue

@Composable
fun SplashScreen() {
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(Color(0xFFDCE9FF), Color(0xFFF8FBFF), Color.White)))
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(24.dp))
            Box(
                Modifier
                    .size(210.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.72f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF9FBFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(ShiftBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccessTime, null, tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                    Box(
                        Modifier
                            .size(116.dp)
                            .background(Color.Transparent),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            Modifier
                                .width(4.dp)
                                .height(52.dp)
                                .offset(y = (-58).dp)
                                .background(ShiftBlue, RoundedCornerShape(2.dp))
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            RowDots()
            Spacer(Modifier.height(18.dp))
            Text("ShiftSync", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = AppText)
            Spacer(Modifier.height(12.dp))
            Text(
                "INTELLIGENT SYNC",
                color = ShiftBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
        }
    }
}

@Composable
private fun RowDots() {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(Color(0xFF4A8BFF), Color(0xFF5DA0FF), Color(0xFF9CC5FF)).forEach { color ->
            Box(Modifier.size(6.dp).clip(CircleShape).background(color))
        }
    }
}
