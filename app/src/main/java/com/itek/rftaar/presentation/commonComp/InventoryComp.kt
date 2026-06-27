package com.itek.rftaar.presentation.commonComp

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.itek.rftaar.R
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.TextSubtext
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.ui.theme.Yellow
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


@Composable
fun InventoryPulseCircle(
  totalCount: Int,
  tagCount: String,
  isInvOn: Boolean,
  lastTagCount: String?,
  showUploadSwipe: Boolean = true,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {

    val current = tagCount.toFloatOrNull() ?: 0f
    val total = lastTagCount?.toFloatOrNull()?.takeIf { it > 0 } ?: 1f
    val rawProgress = current / total
    val progress = rawProgress.coerceIn(0f, 1f)
    val percent = (rawProgress * 100).toInt()


    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800),
        label = "progress"
    )

    // ---------- PULSE ----------
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAnim"
    )

    val isTagAvailable = totalCount > 0 || current > 0f
    val isLocationSelected = lastTagCount?.toFloatOrNull()?.let { it > 0f } == true
    val showProgressRing = isLocationSelected
    val showInnerSwipe = !isInvOn && isTagAvailable && showUploadSwipe

    val bgBrush = if (isTagAvailable) {
        Brush.verticalGradient(listOf(Color.White, Color(0xFFFEF8E6)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF3F3F3), Color.White))
    }

    val borderBrush = if (isInvOn || isTagAvailable) {
        Brush.verticalGradient(listOf(Color(0xFFFFCFA2), Color(0xFFF3B100)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF3F3F3),Color(0xFFF3F3F3)))
    }

    val fillProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isInvOn, tagCount,totalCount) {
        val count = totalCount//tagCount.toIntOrNull() ?: 0

        when {
            !isInvOn && count == 0 -> fillProgress.snapTo(0f)
            count > 0 -> fillProgress.snapTo(0.25f)
        }
    }
    val swiped = fillProgress.value >= 0.25f
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val screenWidth = maxWidth

        // Dynamic sizes
        val outerSize = when {
            screenWidth < 360.dp -> screenWidth * 0.80f
            screenWidth < 600.dp -> screenWidth * 0.72f
            else -> 320.dp
        }
        val innerSize = outerSize * 0.69f
        val progressSize = outerSize * 0.70f

        val dynamicStroke = outerSize * 0.02f
        val titleFont = (outerSize.value * 0.15f).sp
        val subTitleFont = (outerSize.value * 0.08f).sp
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .padding(top = dimensionResource(R.dimen.dp_16))
                    .size(outerSize),
                contentAlignment = Alignment.Center
            ) {

                // ---------- OUTER PULSE ----------
                if (isInvOn) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val center = this.center
                        val maxRadius = size.minDimension / 2

                        repeat(3) { index ->
                            val p = (pulse.value + index * 0.35f) % 1f
                            drawCircle(
                                color = Color(0xFFF3B100).copy(alpha = 1f - p),
                                center = center,
                                radius = maxRadius * p,
                                style = Stroke(dynamicStroke.toPx())
                            )
                        }
                    }
                }

                // ---------- PROGRESS RING ----------
                if (showProgressRing) {
                    Canvas(
                        modifier = Modifier
                            .size(progressSize)
                            .zIndex(1f)
                    ) {

                        val strokeWidthPx = dynamicStroke.toPx()

                        val startAngle = -90f
                        val sweep = 360f * animatedProgress.value

                        // rings
                        drawArc(
                            color = Color.Transparent,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(strokeWidthPx)
                        )

                        drawArc(
                            color = Color(0xFFF3B100),
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(strokeWidthPx, cap = StrokeCap.Round)
                        )

                        // bubble
                        val bubbleRadius = size.minDimension * 0.08f

                        val ringRadius = size.minDimension / 2 - strokeWidthPx
                        val bubbleCenterRadius = ringRadius + bubbleRadius - 6.dp.toPx()

                        val angleRad = Math.toRadians((startAngle + sweep).toDouble())

                        val x = center.x + (bubbleCenterRadius * cos(angleRad)).toFloat()
                        val y = center.y + (bubbleCenterRadius * sin(angleRad)).toFloat()

                        drawCircle(Color.White, bubbleRadius, Offset(x, y))

                        drawCircle(
                            color = Color(0xFFF3B100),
                            radius = bubbleRadius,
                            center = Offset(x, y),
                            style = Stroke(2.dp.toPx())
                        )

                        drawContext.canvas.nativeCanvas.drawText(
                            "${percent}%",
                            x,
                            y + bubbleRadius / 3,
                            android.graphics.Paint().apply {
                                textAlign = android.graphics.Paint.Align.CENTER
                                textSize = (bubbleRadius * 0.75f)
                                isFakeBoldText = true
                                color = android.graphics.Color.BLACK
                            }
                        )
                    }
                }

                // ---------- MAIN CIRCLE ----------
                Box(
                    modifier = Modifier
                        .size(innerSize)
                        .clip(CircleShape)
                        .background(bgBrush)
                        .border(dynamicStroke, borderBrush, CircleShape),
                    contentAlignment = Alignment.Center
                ) {

                    val dragSensitivity = with(LocalDensity.current) {
                        innerSize.toPx()
                    }
                    // ---------- INNER SWIPE ----------
                    if (showInnerSwipe) {
                        Box(
                            modifier = Modifier
                                .size(innerSize)
                                .clip(CircleShape)
                                .pointerInput(Unit) {
                                    detectVerticalDragGestures(
                                        onVerticalDrag = { _, dragAmount ->
                                            scope.launch {
                                                fillProgress.snapTo(
                                                    (fillProgress.value - dragAmount / dragSensitivity)
                                                        .coerceIn(0f, 1f)
                                                )
                                            }
                                        },
                                        onDragEnd = {
                                            scope.launch {
                                                fillProgress.animateTo(
                                                    1f,
                                                    animationSpec = tween(500)
                                                )
                                                onClick()
                                            }
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {

                            // Yellow Fill (unchanged)
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .align(Alignment.BottomCenter)
                                    .graphicsLayer {
                                        scaleY = fillProgress.value
                                        transformOrigin = TransformOrigin(0.5f, 1f)
                                    }
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFFFFD54F), Color(0xFFF3B100))
                                        )
                                    )
                            )

                            val circleSize = innerSize
                            val density = LocalDensity.current

                            Icon(
                                painter = painterResource(R.drawable.property_arrow_forward),
                                contentDescription = null,
                                tint = WhiteColor,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset {
                                        with(density) {
                                            val totalPx = circleSize.toPx()

                                            val topPaddingPx = 16.dp.toPx() // 👈 adjust this value

                                            val usableTravel = (totalPx * 0.85f) - topPaddingPx

                                            val normalized = ((fillProgress.value - 0.25f) / 0.75f)
                                                .coerceIn(0f, 1f)

                                            val offsetY = -(usableTravel * normalized)
                                            IntOffset(0, offsetY.roundToInt())
                                        }
                                    }
                                    .size(innerSize * 0.18f)
                            )



                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    tagCount.ifEmpty { "0" },
                                    style = CommonTypography.current.headingH1,
                                    fontSize = titleFont
                                )

                                if (!lastTagCount.isNullOrEmpty()) {
                                    HorizontalDivider(
                                        thickness = 1.dp,
                                        color = Yellow,
                                        modifier = Modifier.padding(vertical = 9.dp)
                                    )
                                    Text(
                                        lastTagCount,
                                        style = CommonTypography.current.headingH1.copy(color = TextSubtext),
                                        fontSize = subTitleFont
                                    )
                                }
                            }
                        }
                    }

                    // ---------- CENTER CONTENT ----------
                    if (!showInnerSwipe) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                tagCount.ifEmpty { "0" },
                                style = CommonTypography.current.headingH1,
                                fontSize = titleFont
                            )

                            if (!lastTagCount.isNullOrEmpty()) {
                                HorizontalDivider(thickness = 1.dp, color = Yellow)
                                Text(
                                    lastTagCount,
                                    style = CommonTypography.current.headingH1.copy(color = TextSubtext),
                                    fontSize = subTitleFont
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
