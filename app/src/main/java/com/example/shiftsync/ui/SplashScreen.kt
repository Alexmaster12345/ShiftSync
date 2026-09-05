package com.example.shiftsync.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.shiftsync.ui.theme.ShiftBlue
import com.example.shiftsync.ui.theme.ShiftBlueDark

/**
 * Animated splash screen shown while the app cold-starts, mirroring the iOS
 * SplashView in ContentView.swift: a white background with a blue gradient
 * clock icon that springs in, gently pulses, and has its hands sweep around
 * the dial, then fades out to reveal the real app content.
 */
@Composable
fun SplashScreen() {
    val scale = remember { Animatable(0.6f) }
    val opacity = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow))
    }
    LaunchedEffect(Unit) {
        opacity.animateTo(1f, animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow))
    }

    val infinite = rememberInfiniteTransition(label = "splash")
    val pulse by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val minuteRotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(3000, easing = LinearEasing)),
        label = "minuteHand"
    )
    val hourRotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(24000, easing = LinearEasing)),
        label = "hourHand"
    )

    Box(Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(120.dp)
                .graphicsLayer {
                    val s = scale.value * pulse
                    scaleX = s
                    scaleY = s
                    alpha = opacity.value
                }
                .shadow(20.dp, CircleShape, ambientColor = ShiftBlue.copy(alpha = 0.4f), spotColor = ShiftBlue.copy(alpha = 0.4f))
                .background(Brush.linearGradient(listOf(ShiftBlue, ShiftBlueDark)), CircleShape)
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)

                // Tick marks
                for (i in 0 until 12) {
                    rotate(degrees = i * 30f, pivot = center) {
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.55f),
                            topLeft = Offset(center.x - 1.dp.toPx(), 8.dp.toPx()),
                            size = Size(2.dp.toPx(), 6.dp.toPx()),
                            cornerRadius = CornerRadius(50f)
                        )
                    }
                }

                // Hour hand
                rotate(degrees = hourRotation, pivot = center) {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.9f),
                        topLeft = Offset(center.x - 2.dp.toPx(), center.y - 30.dp.toPx()),
                        size = Size(4.dp.toPx(), 20.dp.toPx()),
                        cornerRadius = CornerRadius(50f)
                    )
                }

                // Minute hand
                rotate(degrees = minuteRotation, pivot = center) {
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(center.x - 1.5.dp.toPx(), center.y - 38.dp.toPx()),
                        size = Size(3.dp.toPx(), 32.dp.toPx()),
                        cornerRadius = CornerRadius(50f)
                    )
                }

                // Center hub
                drawCircle(color = Color.White, radius = 6.5.dp.toPx(), center = center)
                drawCircle(color = ShiftBlue, radius = 5.dp.toPx(), center = center)
            }
        }
    }
}
