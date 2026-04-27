package com.example.strivn.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.strivn.data.models.RunLog
import com.example.strivn.ui.theme.StrivnDefaults
import com.example.strivn.ui.theme.StrivnError
import java.time.format.DateTimeFormatter
import java.util.Locale

private val runDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())

/** Good direction when the metric moves up (fitness, capacity). Fatigue uses [higherIsBetter] = false. */
private val impactIncreasePositiveGreen = Color(0xFF4ADE80)

@Composable
fun RunHistoryCard(
    run: RunLog,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = StrivnDefaults.secondaryCardColors(),
        elevation = StrivnDefaults.cardElevation(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.DirectionsRun,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = run.date.format(runDateFormatter),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "%.1f km • %d min • RPE %d".format(
                            run.distanceKm,
                            run.durationMinutes,
                            run.rpe,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Run Impact",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                RunImpactRow(
                    label = "Fitness",
                    before = run.fitnessBefore,
                    after = run.fitnessAfter,
                    higherIsBetter = true,
                )
                RunImpactRow(
                    label = "Fatigue",
                    before = run.fatigueBefore,
                    after = run.fatigueAfter,
                    higherIsBetter = false,
                )
                RunImpactRow(
                    label = "Training Capacity",
                    before = run.capacityBefore,
                    after = run.capacityAfter,
                    higherIsBetter = true,
                )
            }
        }
    }
}

@Composable
private fun RunImpactRow(
    label: String,
    before: Int,
    after: Int,
    higherIsBetter: Boolean,
) {
    val neutral = MaterialTheme.colorScheme.onSurfaceVariant
    val afterColor = afterValueColor(
        before = before,
        after = after,
        higherIsBetter = higherIsBetter,
        neutral = neutral,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$before → ",
                style = MaterialTheme.typography.bodyMedium,
                color = neutral,
            )
            Text(
                text = "$after",
                style = MaterialTheme.typography.bodyMedium,
                color = afterColor,
            )
        }
    }
}

private fun afterValueColor(
    before: Int,
    after: Int,
    higherIsBetter: Boolean,
    neutral: Color,
): Color {
    if (after == before) return neutral
    val increased = after > before
    return when {
        higherIsBetter && increased -> impactIncreasePositiveGreen
        higherIsBetter && !increased -> StrivnError
        !higherIsBetter && increased -> StrivnError
        else -> impactIncreasePositiveGreen
    }
}
