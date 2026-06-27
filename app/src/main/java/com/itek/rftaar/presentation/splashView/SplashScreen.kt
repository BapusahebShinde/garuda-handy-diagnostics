package com.itek.rftaar.presentation.splashView

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.ui.theme.Yellow
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedSplashScreen(onFinished: () -> Unit) {

    val circleCount = remember { mutableStateOf(0) }
    val startRotation = remember { mutableStateOf(false) }
    val showTransitionCircle = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        circleCount.value = 1
        delay(80)

        circleCount.value = 2
        delay(80)

        circleCount.value = 3

        // Start rotation almost immediately
        delay(80)
        startRotation.value = true

        // Transition overlaps rotation (important!)
        delay(180)
        showTransitionCircle.value = true

        delay(200)
        onFinished()
    }

    val transitionScale = animateFloatAsState(
        targetValue = if (showTransitionCircle.value) 50f else 0f,
        animationSpec = tween(
            durationMillis = 220,
            easing = FastOutSlowInEasing
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Yellow),
        contentAlignment = Alignment.Center
    ) {

        if (showTransitionCircle.value) {

            val circleSize: Dp = 33.dp
            val radius: Dp = circleSize   // force Dp type

            val density = LocalDensity.current
            val rad = Math.toRadians(0.0)

            val radiusPx = with(density) { radius.toPx() }

            val offsetX = radiusPx * cos(rad).toFloat()
            val offsetY = radiusPx * sin(rad).toFloat()

            Box(
                modifier = Modifier
                    .size(33.dp)
                    .graphicsLayer {
                        translationX = offsetX
                        translationY = offsetY
                        scaleX = transitionScale.value
                        scaleY = transitionScale.value
                    }
                    .background(WhiteColor, CircleShape)
            )
        }

        TriangleCircles(circleCount.value, startRotation.value)
    }
}



@Composable
fun TriangleCircles(circleCount: Int, rotate: Boolean) {

    val rotation = animateFloatAsState(
        targetValue = if (rotate) 360f * 1.5f else 0f,
        animationSpec = tween(
            durationMillis = 900,
            easing = FastOutSlowInEasing
        )
    )

    val circleSize = 33.dp
    val circleRadius = circleSize

    Box(
        modifier = Modifier
            .size(120.dp)
            .graphicsLayer {
                rotationZ = rotation.value
            },
        contentAlignment = Alignment.Center
    ) {

        if (circleCount >= 1)
            CircleAtAngle(0f, circleRadius, circleSize,circleCount)

        if (circleCount >= 2)
            CircleAtAngle(120f, circleRadius, circleSize, circleCount)

        if (circleCount >= 3)
            CircleAtAngle(240f, circleRadius, circleSize, circleCount)
    }
}



@Composable
fun CircleAtAngle(angle: Float, radius: Dp, size: Dp, circleCount: Int) {
    val alpha = animateFloatAsState(
        targetValue = if (circleCount >= 1) 1f else 0f,
        animationSpec = tween(150)
    )
    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                val rad = Math.toRadians(angle.toDouble())
                translationX = (radius.toPx() * cos(rad)).toFloat()
                translationY = (radius.toPx() * sin(rad)).toFloat()
                this.alpha = alpha.value
            }
            .background(WhiteColor, CircleShape)
    )
}


