package com.example.strivn.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.strivn.data.models.UserMetrics
import com.example.strivn.ui.theme.StrivnAccent
import com.example.strivn.ui.theme.StrivnError
import com.example.strivn.ui.theme.StrivnWarning
import kotlin.math.roundToInt

@Immutable
enum class MetricWheelSize(
    val wheelSize: Dp,
    val strokeWidth: Dp,
) {
    Large(wheelSize = 112.dp, strokeWidth = 12.dp),
    Medium(wheelSize = 88.dp, strokeWidth = 10.dp),
}

/**
 * Wheel fill is [percentage] in 0..100 (arc sweep). Pass values derived from [UserMetrics] via
 * [metricWheelPercentages] (fitness, fatigue, sleep, capacity each use stored 0–100 as 0–100%).
 *
 * - **Daily capacity**: pass [capacityForColor] = raw 0–100 capacity to apply green/yellow/red thresholds.
 * - **Fitness, Sleep**: omit [capacityForColor]; higher = better (green when high).
 * - **Fatigue**: pass [invertedScale] = true; lower = better (green when low, red when high).
 */
@Composable
fun MetricWheel(
    label: String,
    percentage: Float,
    modifier: Modifier = Modifier,
    size: MetricWheelSize = MetricWheelSize.Medium,
    invertedScale: Boolean = false,
    /** When set, arc color uses capacity thresholds (>70 green, 40–70 yellow, &lt;40 red). */
    capacityForColor: Int? = null,
) {
    val clamped = percentage.coerceIn(0f, 100f)
    val effectivePercentage = if (invertedScale) 100f - clamped else clamped
    val targetProgressColor = when {
        capacityForColor != null -> capacityThresholdColor(capacityForColor)
        else -> metricColorForPercentage(effectivePercentage)
    }
    val progressColor by animateColorAsState(
        targetValue = targetProgressColor,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "metricWheelColor",
    )
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    val centerTextColor = MaterialTheme.colorScheme.onBackground
    val labelTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val animatedPercentage = remember { Animatable(0f) }

    LaunchedEffect(clamped) {
        animatedPercentage.animateTo(
            targetValue = clamped,
            animationSpec = tween(
                durationMillis = 900,
                easing = FastOutSlowInEasing,
            ),
        )
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(size.wheelSize),
            contentAlignment = Alignment.Center,
        ) {
            // Rotate so progress starts at 12 o'clock instead of 3 o'clock.
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(1f)
                    .rotate(-90f),
            ) {
                val stroke = Stroke(
                    width = size.strokeWidth.toPx(),
                    cap = StrokeCap.Round,
                )

                // Track
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = stroke,
                )

                // Progress
                drawArc(
                    color = progressColor,
                    startAngle = 0f,
                    sweepAngle = 360f * (animatedPercentage.value / 100f),
                    useCenter = false,
                    style = stroke,
                )
            }

            Text(
                text = "${animatedPercentage.value.roundToInt()}%",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = centerTextColor,
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = labelTextColor,
        )
    }
}

private fun metricColorForPercentage(percentage: Float): Color {
    // High = green, medium = orange, low = red
    // For inverted (e.g. Fatigue): caller passes (100 - p), so low fatigue -> high effective -> green
    return when {
        percentage >= 70f -> StrivnAccent
        percentage >= 40f -> StrivnWarning
        else -> StrivnError
    }
}

/**
 * Center label / arc % for fatigue (same value shown on Home wheels). Stored 0–100 maps 1:1 to wheel %.
 */
fun fatigueWheelDisplayPercent(storedFatigue: Int): Int =
    storedFatigue.coerceIn(0, 100)

/** Same as [fatigueWheelDisplayPercent] for fitness. */
fun fitnessWheelDisplayPercent(storedFitness: Int): Int =
    storedFitness.coerceIn(0, 100)

/** Arc fill 0..100 from [UserMetrics]; all four metrics use stored 0–100 as wheel 0–100%. */
fun metricWheelPercentages(metrics: UserMetrics): MetricWheelPercents = MetricWheelPercents(
    dailyTrainingCapacity = (metrics.dailyCapacity * 100f / 100f).coerceIn(0f, 100f),
    fatigue = (metrics.fatigue * 100f / 100f).coerceIn(0f, 100f),
    fitness = (metrics.fitness * 100f / 100f).coerceIn(0f, 100f),
    sleep = (metrics.sleep * 100f / 100f).coerceIn(0f, 100f),
)

data class MetricWheelPercents(
    val dailyTrainingCapacity: Float,
    val fatigue: Float,
    val fitness: Float,
    val sleep: Float,
)

/** Green / yellow / red for daily training capacity (0–100). */
private fun capacityThresholdColor(capacity: Int): Color {
    val c = capacity.coerceIn(0, 100)
    return when {
        c > 70 -> StrivnAccent
        c >= 40 -> StrivnWarning
        else -> StrivnError
    }
}

