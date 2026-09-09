package com.example.shiftsync.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiftsync.ui.theme.AppText
import com.example.shiftsync.ui.theme.ShiftBlue
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen() {
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(Color(0xFFDCE9FF), Color(0xFFF8FBFF), Color.White)))
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            ClockFace()

            Spacer(Modifier.height(20.dp))
            RowDots()
            Spacer(Modifier.height(16.dp))
            Text("ShiftSync", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = AppText)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.width(28.dp), thickness = 1.dp, color = ShiftBlue.copy(alpha = 0.4f))
                Text(
                    "  INTELLIGENT SYNC  ",
                    color = ShiftBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
                HorizontalDivider(modifier = Modifier.width(28.dp), thickness = 1.dp, color = ShiftBlue.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
private fun ClockFace() {
    Box(
        Modifier
            .size(188.dp)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f
            val tickInset = 14.dp.toPx()
            // Tick marks at 12, 3, 6, 9 o'clock
            listOf(0.0, 90.0, 180.0, 270.0).forEach { angleDeg ->
                val angle = Math.toRadians(angleDeg - 90.0)
                val outer = Offset(center.x + (radius - 6.dp.toPx()) * cos(angle).toFloat(), center.y + (radius - 6.dp.toPx()) * sin(angle).toFloat())
                val inner = Offset(center.x + (radius - tickInset) * cos(angle).toFloat(), center.y + (radius - tickInset) * sin(angle).toFloat())
                drawLine(color = Color(0xFFC9D6E8), start = inner, end = outer, strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
            }
            // Hour hand pointing to 12
            drawLine(
                color = ShiftBlue,
                start = center,
                end = Offset(center.x, center.y - radius * 0.62f),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Center dot
            drawCircle(color = ShiftBlue, radius = 5.dp.toPx(), center = center)
        }
    }
}

@Composable
private fun RowDots() {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(ShiftBlue, ShiftBlue.copy(alpha = 0.6f), ShiftBlue.copy(alpha = 0.3f)).forEach { color ->
            Box(Modifier.size(6.dp).clip(CircleShape).background(color))
        }
    }
}
