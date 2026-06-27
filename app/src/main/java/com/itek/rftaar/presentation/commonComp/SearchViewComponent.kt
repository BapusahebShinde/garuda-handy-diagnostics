package com.itek.rftaar.presentation.commonComp

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.presentation.viewmodel.ReaderViewModel
import com.itek.rftaar.ui.theme.Green
import com.itek.rftaar.ui.theme.Orange
import com.itek.rftaar.ui.theme.RedColor
import com.itek.rftaar.ui.theme.SearchYellow
import com.itek.rftaar.ui.theme.TabColor
import com.itek.rftaar.utils.CommonUtils.chkNull
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin


fun trianglePath(
    center: Offset,
    angle: Float,
    size: Float = 18f
): Path {
    val path = Path()

    val tip = Offset(
        center.x + size * cos(angle),
        center.y + size * sin(angle)
    )

    val left = Offset(
        center.x + size * cos(angle + Math.PI.toFloat() * 0.75f),
        center.y + size * sin(angle + Math.PI.toFloat() * 0.75f)
    )

    val right = Offset(
        center.x + size * cos(angle - Math.PI.toFloat() * 0.75f),
        center.y + size * sin(angle - Math.PI.toFloat() * 0.75f)
    )

    path.moveTo(tip.x, tip.y)
    path.lineTo(left.x, left.y)
    path.lineTo(right.x, right.y)
    path.close()

    return path
}



fun drawGaugeNeedle(
    drawScope: DrawScope,
    center: Offset,
    angleRad: Float,
    gaugeRadius: Float,
    value: Int,
    color: Color = Color.Black
) = with(drawScope) {

    /** 🔵 Correct hub size */
    val hubRadius = gaugeRadius * 0.16f

    /** 🔺 Needle proportions */
    val needleLength = gaugeRadius * 0.95f
    val needleBaseWidth = hubRadius * 0.9f

    /** Needle start slightly INSIDE circle (for connection) */
    val start = Offset(
        center.x + (hubRadius * 0.85f) * cos(angleRad),
        center.y + (hubRadius * 0.85f) * sin(angleRad)
    )

    /** Needle tip */
    val tip = Offset(
        center.x + needleLength * cos(angleRad),
        center.y + needleLength * sin(angleRad)
    )

    /** Perpendicular for width */
    val perpendicular = angleRad + Math.PI.toFloat() / 2f

    val left = Offset(
        start.x + needleBaseWidth * cos(perpendicular),
        start.y + needleBaseWidth * sin(perpendicular)
    )

    val right = Offset(
        start.x - needleBaseWidth * cos(perpendicular),
        start.y - needleBaseWidth * sin(perpendicular)
    )

    /** Needle shape */
    val needlePath = Path().apply {
        moveTo(left.x, left.y)
        lineTo(tip.x, tip.y)
        lineTo(right.x, right.y)
        close()
    }

    /** Draw needle FIRST */
    drawPath(
        path = needlePath,
        color = color
    )

    /** Draw hub ON TOP */
    drawCircle(
        color = color,
        radius = hubRadius,
        center = center
    )

    /** Value text */
    drawContext.canvas.nativeCanvas.drawText(
        value.toString(),
        center.x,
        center.y + hubRadius * 0.35f,
        android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = hubRadius * 1.1f
            isFakeBoldText = true
        }
    )
}


@Composable
fun DirectionGauge(
    value: Float,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    startAngle: Float = 180f,
    sweepAngle: Float = 180f
) {
    Canvas(modifier = modifier) {

        val progress =
            (value - valueRange.start) /
                    (valueRange.endInclusive - valueRange.start)

        val angle = startAngle + progress * sweepAngle
        val angleRad = Math.toRadians(angle.toDouble()).toFloat()

        val center = Offset(size.width / 2, size.height / 2)
        val gaugeRadius = size.minDimension * 0.45f

        /** arcs */
        drawArc(
            color = Color.LightGray,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(gaugeRadius * 0.08f, cap = StrokeCap.Round),
            size = Size(gaugeRadius * 2, gaugeRadius * 2),
            topLeft = Offset(center.x - gaugeRadius, center.y - gaugeRadius)
        )

        drawArc(
            color = Color.Green,
            startAngle = startAngle,
            sweepAngle = sweepAngle * progress,
            useCenter = false,
            style = Stroke(gaugeRadius * 0.08f, cap = StrokeCap.Round),
            size = Size(gaugeRadius * 2, gaugeRadius * 2),
            topLeft = Offset(center.x - gaugeRadius, center.y - gaugeRadius)
        )

        /** needle */
        drawGaugeNeedle(
            drawScope = this,
            center = center,
            angleRad = angleRad,
            gaugeRadius = gaugeRadius,
            value = value.toInt(),
            color = Color.Black
        )

        /** triangle direction marker */
        drawTriangleIndicator(
            center = center,
            angleRad = angleRad,
            radius = gaugeRadius * 1.1f
        )
    }
}

fun DrawScope.drawTriangleIndicator(
    center: Offset,
    angleRad: Float,
    radius: Float,
    color: Color = Color(0xFF00C853)
) {
    val size = radius * 0.08f
    val perpendicular = angleRad + Math.PI.toFloat() / 2f

    val base = Offset(
        center.x + radius * cos(angleRad),
        center.y + radius * sin(angleRad)
    )

    val tip = Offset(
        base.x + size * cos(angleRad),
        base.y + size * sin(angleRad)
    )

    val left = Offset(
        base.x + size * cos(perpendicular),
        base.y + size * sin(perpendicular)
    )

    val right = Offset(
        base.x - size * cos(perpendicular),
        base.y - size * sin(perpendicular)
    )

    drawPath(
        Path().apply {
            moveTo(tip.x, tip.y)
            lineTo(left.x, left.y)
            lineTo(right.x, right.y)
            close()
        },
        color
    )
}


/**fun DrawScope.drawProximityNeedle(
    center: Offset,
    angleRad: Float,
    radius: Float,
    value: Int,
    color: Color = Color.Black
) {
    val hubRadius = radius * 0.12f
    val needleLength = radius * 0.9f
    val baseWidth = hubRadius * 0.9f

    val start = Offset(
        center.x + hubRadius * 0.8f * cos(angleRad),
        center.y + hubRadius * 0.8f * sin(angleRad)
    )

    val tip = Offset(
        center.x + needleLength * cos(angleRad),
        center.y + needleLength * sin(angleRad)
    )

    val perpendicular = angleRad + Math.PI.toFloat() / 2f

    val left = Offset(
        start.x + baseWidth * cos(perpendicular),
        start.y + baseWidth * sin(perpendicular)
    )

    val right = Offset(
        start.x - baseWidth * cos(perpendicular),
        start.y - baseWidth * sin(perpendicular)
    )

    val needlePath = Path().apply {
        moveTo(left.x, left.y)
        lineTo(tip.x, tip.y)
        lineTo(right.x, right.y)
        close()
    }

    // shadow
    drawPath(needlePath, color.copy(alpha = 0.25f))

    // needle
    drawPath(needlePath, color)

    // hub
    drawCircle(color, hubRadius, center)

    // value
    drawContext.canvas.nativeCanvas.drawText(
        value.toString(),
        center.x,
        center.y + hubRadius * 0.35f,
        Paint().apply {
            textAlign = Paint.Align.CENTER
            textSize = hubRadius * 1.1f
            this.color = android.graphics.Color.WHITE
            isFakeBoldText = true
        }
    )
}*/

fun DrawScope.drawProximityNeedle(
    center: Offset,
    angleRad: Float,
    radius: Float,
    value: Int,
    color: Color = Color.Black
) {

    val hubRadius = radius * 0.18f
    val needleLength = radius * 0.95f
    val baseWidth = hubRadius * 0.28f

    val tip = Offset(
        center.x + needleLength * cos(angleRad),
        center.y + needleLength * sin(angleRad)
    )

    val perpendicular = angleRad + Math.PI.toFloat() / 2f

    val left = Offset(
        center.x + baseWidth * cos(perpendicular),
        center.y + baseWidth * sin(perpendicular)
    )

    val right = Offset(
        center.x - baseWidth * cos(perpendicular),
        center.y - baseWidth * sin(perpendicular)
    )

    val path = Path().apply {
        moveTo(left.x, left.y)
        lineTo(tip.x, tip.y)
        lineTo(right.x, right.y)
        close()
    }

    drawPath(path, color)

    drawCircle(color, hubRadius, center)

    drawContext.canvas.nativeCanvas.drawText(
        value.toString(),
        center.x,
        center.y + hubRadius * 0.35f,
        Paint().apply {
            textAlign = Paint.Align.CENTER
            textSize = hubRadius * 1.0f
            this.color = android.graphics.Color.WHITE
            isFakeBoldText = true
        }
    )
}


@Composable
fun SmoothDirectionGauge(
    deviceValue: Float,
    modifier: Modifier = Modifier
) {
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(deviceValue) {
        animatedValue.animateTo(
            targetValue = deviceValue.coerceIn(0f, 100f),
            animationSpec = tween(
                durationMillis = 250,
                easing = LinearOutSlowInEasing
            )
        )
    }

    DirectionGauge(
        value = animatedValue.value,
        modifier = modifier
    )
}

@Composable
fun DirectionProximityGauge(
    readerViewModel: ReaderViewModel,
    modifier: Modifier = Modifier,
    contentWidth: Dp = 350.dp
) {

    val serPercent =  readerViewModel.searchPercentage().observeAsState()
    val serAngle =  readerViewModel.searchAngle.observeAsState()
    LogUtils.showLog("serAngle",""+serAngle.value)
    val rawAngle = chkNull(serAngle.value, 0f)
    val percent = serPercent.value ?: 0f
    val angleFromSensor = serAngle.value ?: 0f

// Reset angle if percent is zero
    val targetAngle = if (percent <= 0f) 0f else angleFromSensor
    LogUtils.showLog("targetAngle", "DirectionProximityGauge: $targetAngle")

    val animatedProximity = animateFloatAsState(
        targetValue = serPercent.value ?: 0f,
        animationSpec = tween(600),
        label = ""
    )

    val animatedTriangleAngle = animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = tween(400),
        label = "triangleSmooth"
    )


    Canvas(
        modifier = modifier.size(
            width = contentWidth,
            height = contentWidth / 2
        )
    ) {

        val startAngle = 135f
        val sweepAngle = 270f

        val center = Offset(size.width / 2, size.height)

        val outerRadius = size.width / 2
        val bandThickness = outerRadius * 0.22f
        val innerRadius = outerRadius - bandThickness

        val safeProximity = animatedProximity.value.coerceIn(0f, 100f)
        val progressFraction = safeProximity / 100f

        /* ------------------ DYNAMIC COLOR ------------------ */
        val progressColor = when {
            safeProximity >= 90f -> Green
            safeProximity >= 66f -> SearchYellow
            safeProximity >= 33f -> Orange
            safeProximity > 0f -> RedColor
            else -> TabColor
        }

        /* ------------------------------------------------ */
        /* Filled arc band                                  */
        /* ------------------------------------------------ */
        fun drawBand(color: Color, sweep: Float) {
            val path = Path().apply {

                arcTo(
                    rect = Rect(center = center, radius = outerRadius),
                    startAngleDegrees = startAngle,
                    sweepAngleDegrees = sweep,
                    forceMoveTo = true
                )

                arcTo(
                    rect = Rect(center = center, radius = innerRadius),
                    startAngleDegrees = startAngle + sweep,
                    sweepAngleDegrees = -sweep,
                    forceMoveTo = false
                )

                close()
            }

            drawPath(path, color)
        }

        /* background arc */
        drawBand(Color(0xFFE6E6E6), sweepAngle)

        /* progress arc */
        drawBand(progressColor, sweepAngle * progressFraction)

        /* inner glow */
        drawCircle(
            brush = Brush.radialGradient(
                listOf(progressColor.copy(alpha = 0.35f), Color.Transparent),
                center,
                innerRadius
            ),
            radius = innerRadius,
            center = center
        )

        val needleAngleRad = Math.toRadians((startAngle + sweepAngle * progressFraction).toDouble()).toFloat()

        val isShowDirection = readerViewModel.isSensorAvailable() && chkNull(serPercent.value,0f) > 33.0f
        LogUtils.showLog("isShowDirection",""+isShowDirection)
        val angle  = if(chkNull(serPercent.value,0f)>0f) 0f else chkNull(serAngle.value,0f)
        val triangleAngleRad = Math.toRadians((animatedTriangleAngle.value - 90f).toDouble()).toFloat()

        /* ------------------------------------------------ */
        /* Direction triangle                               */
        /* ------------------------------------------------ */

        val gap = 20.dp.toPx()

        val tipRadius = outerRadius + gap

        val triHeight = bandThickness * 0.7f
        val triWidth = bandThickness * 0.55f

        val tip = Offset(
            center.x + tipRadius * cos(triangleAngleRad),
            center.y + tipRadius * sin(triangleAngleRad)
        )

        val baseCenter = Offset(
            center.x + (tipRadius - triHeight) * cos(triangleAngleRad),
            center.y + (tipRadius - triHeight) * sin(triangleAngleRad)
        )

        val perpendicular = triangleAngleRad + Math.PI.toFloat() / 2f

        val left = Offset(
            baseCenter.x + triWidth * cos(perpendicular),
            baseCenter.y + triWidth * sin(perpendicular)
        )

        val right = Offset(
            baseCenter.x - triWidth * cos(perpendicular),
            baseCenter.y - triWidth * sin(perpendicular)
        )

        val triangle = Path().apply {
            moveTo(tip.x, tip.y)
            lineTo(left.x, left.y)
            lineTo(right.x, right.y)
            close()
        }

        /* triangle color = progress color */
        if (isShowDirection) drawPath(triangle, progressColor)

        /* needle */
        drawProximityNeedle(
            center = center,
            angleRad = needleAngleRad,
            radius = innerRadius,
            value = safeProximity.toInt()
        )
    }
}



@Preview(showBackground = true)
@Composable
fun SmoothDirectionGaugePreview() {
    SmoothDirectionGauge(
        deviceValue = 65f, // 👈 sample preview value
        modifier = Modifier.size(280.dp)
    )
}


@Preview(showBackground = true)
@Composable
fun AnimatedGaugePreview() {
    var value = remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            value.value = (value.value + 10f) % 100f
            delay(500)
        }
    }

    SmoothDirectionGauge(
        deviceValue = value.value,
        modifier = Modifier.size(280.dp)
    )
}





/*@Preview
@Composable
fun view(){
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        var proximity = remember { mutableFloatStateOf(0f) }
        var angle = remember { mutableStateOf(0f) }


        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {

            TextField(
                value = proximity.value.toString(),   // Float → String

                onValueChange = { value ->
                    proximity.value = value.toFloatOrNull() ?: 0f   // String → Float safely
                },

                readOnly = false,

                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {},

                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            TextField(
                value = angle.value.toString(),   // Float → String

                onValueChange = { value ->
                    angle.value = value.toFloatOrNull() ?: 0f   // String → Float safely
                },

                readOnly = false,

                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {},

                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )



            DirectionProximityGauge(
                //directionAngle = 45f,   // direction to go
                proximityValue = proximity.value,   // how close
                modifier = Modifier.size(280.dp),
                targetDirection = angle.value
            )
            Spacer(modifier = Modifier.padding(30.dp))


        }
    }

}*/

/*@Preview
@Composable
fun GaugeScreen() {
    var direction = remember { mutableFloatStateOf(0f) }
    var proximity = remember { mutableFloatStateOf(0f) }

    // Simulate device movement
    LaunchedEffect(Unit) {
        while (true) {
            direction.value = (direction.value + 5f) % 180f
            proximity.value = (proximity.value + 2f) % 100f
            delay(200)
        }
    }
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

        DirectionProximityGauge(
            //  directionAngle = direction.value,
            proximityValue = proximity.value,
            modifier = Modifier.size(280.dp),
            targetDirection = 300.toFloat()
        )
    }

}*/

