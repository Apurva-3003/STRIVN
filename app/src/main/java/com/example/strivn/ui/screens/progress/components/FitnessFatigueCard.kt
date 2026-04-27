package com.example.strivn.ui.screens.progress.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.strivn.ui.screens.progress.FitnessFatiguePoint
import com.example.strivn.ui.theme.StrivnAccent
import com.example.strivn.ui.theme.StrivnDefaults
import com.example.strivn.ui.theme.StrivnError
import kotlin.math.max

private val chartHeight = 210.dp
private const val maxMetric = 100f

@Composable
fun FitnessFatigueCard(
    currentFitness: Int?,
    currentFatigue: Int?,
    fitnessFatigueTrend: List<FitnessFatiguePoint>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = StrivnDefaults.secondaryCardColors(),
        elevation = StrivnDefaults.cardElevation(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Fitness vs Fatigue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    MetricPill(label = "Fitness", value = currentFitness)
                }
                Column(modifier = Modifier.weight(1f)) {
                    MetricPill(label = "Fatigue", value = currentFatigue)
                }
            }
            if (fitnessFatigueTrend.isEmpty()) {
                Text(
                    text = "No metrics history for this window yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val maxY = max(
                    maxMetric,
                    fitnessFatigueTrend.maxOf { max(it.fitness, it.fatigue) }.toFloat(),
                ).coerceAtLeast(1f)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight),
                ) {
                    val pad = 28.dp.toPx()
                    val n = fitnessFatigueTrend.size
                    val denom = (n - 1).coerceAtLeast(1)
                    fun xAt(i: Int): Float =
                        pad + (size.width - 2 * pad) * i / denom
                    fun yAt(v: Int): Float =
                        size.height - pad - (size.height - 2 * pad) * (v / maxY)

                    val pathFitness = Path()
                    val pathFatigue = Path()
                    fitnessFatigueTrend.forEachIndexed { i, p ->
                        val ox = xAt(i)
                        val yf = yAt(p.fitness)
                        val yt = yAt(p.fatigue)
                        if (i == 0) {
                            pathFitness.moveTo(ox, yf)
                            pathFatigue.moveTo(ox, yt)
                        } else {
                            pathFitness.lineTo(ox, yf)
                            pathFatigue.lineTo(ox, yt)
                        }
                    }
                    val stroke = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    )
                    drawPath(pathFitness, StrivnAccent, style = stroke)
                    drawPath(pathFatigue, StrivnError, style = stroke)
                    fitnessFatigueTrend.forEachIndexed { i, p ->
                        val ox = xAt(i)
                        drawCircle(StrivnAccent, 4.dp.toPx(), Offset(ox, yAt(p.fitness)))
                        drawCircle(StrivnError, 4.dp.toPx(), Offset(ox, yAt(p.fatigue)))
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top,
                ) {
                    fitnessFatigueTrend.forEach { p ->
                        Text(
                            text = p.weekLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LegendDot(color = StrivnAccent, label = "Fitness")
                    LegendDot(color = StrivnError, label = "Fatigue")
                }
            }
        }
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: Int?,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value?.coerceIn(0, 100)?.toString() ?: "—",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun LegendDot(
    color: Color,
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Canvas(Modifier.size(10.dp)) {
            drawCircle(color = color, radius = size.minDimension / 2f)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
