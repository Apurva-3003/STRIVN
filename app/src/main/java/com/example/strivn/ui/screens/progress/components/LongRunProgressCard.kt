package com.example.strivn.ui.screens.progress.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.strivn.ui.screens.progress.WeeklyLongRun
import com.example.strivn.ui.theme.StrivnSecondaryAction
import com.example.strivn.ui.theme.StrivnDefaults

private val chartHeight = 210.dp

@Composable
fun LongRunProgressCard(
    longestRunLast30DaysKm: Double,
    longRunProgression: List<WeeklyLongRun>,
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
                text = "Long Run Progression",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Last 30 days: %.1f km longest".format(longestRunLast30DaysKm.coerceAtLeast(0.0)),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (longRunProgression.isEmpty()) {
                Text(
                    text = "No long-run data for this window yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val maxKm = longRunProgression.maxOf { it.distanceKm }.coerceAtLeast(0.01)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight),
                ) {
                    val n = longRunProgression.size
                    val gap = size.width * 0.05f
                    val barW = (size.width - gap * (n + 1)) / n.coerceAtLeast(1)
                    val chartBottom = size.height * 0.92f
                    val chartTop = size.height * 0.08f
                    val chartH = chartBottom - chartTop
                    longRunProgression.forEachIndexed { i, w ->
                        val h = (w.distanceKm / maxKm * chartH).toFloat()
                        val x = gap + i * (barW + gap)
                        val top = chartBottom - h
                        drawRoundRect(
                            color = StrivnSecondaryAction,
                            topLeft = Offset(x, top),
                            size = Size(barW, h.coerceAtLeast(4f)),
                            cornerRadius = CornerRadius(barW * 0.12f, barW * 0.12f),
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top,
                ) {
                    longRunProgression.forEach { w ->
                        Text(
                            text = w.weekLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}
