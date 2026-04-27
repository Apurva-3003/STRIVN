package com.example.strivn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.strivn.data.models.RunRecommendation
import com.example.strivn.ui.theme.StrivnAccent
import com.example.strivn.ui.theme.StrivnError
import com.example.strivn.ui.theme.StrivnPrimaryCard

/**
 * Impact of completing the daily check-in on metrics and today's prescribed run.
 * Deltas match the metric wheels: fitness/fatigue/sleep/capacity use raw 0–100 point changes (= wheel % change).
 */
data class CheckInImpactData(
    val fitnessChange: Int,
    val fatigueChange: Int,
    val sleepChange: Int,
    val dtcChange: Int,
    val previousRecommendation: RunRecommendation,
    val newRecommendation: RunRecommendation,
)

@Composable
fun CheckInImpactPopup(
    impact: CheckInImpactData,
    onDone: () -> Unit,
) {
    Dialog(onDismissRequest = onDone) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = StrivnPrimaryCard,
                    shape = MaterialTheme.shapes.large,
                ),
        ) {
            IconButton(
                onClick = onDone,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Check-In Complete",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Impact on your training state",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))

                val defaultColor = MaterialTheme.colorScheme.onBackground
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ImpactRow(
                        label = "Fitness",
                        value = formatChange(impact.fitnessChange),
                        valueColor = impactColor(impact.fitnessChange, isFitness = true, defaultColor),
                    )
                    ImpactRow(
                        label = "Fatigue",
                        value = formatChange(impact.fatigueChange),
                        valueColor = impactColor(impact.fatigueChange, isFitness = false, defaultColor),
                    )
                    ImpactRow(
                        label = "Sleep score",
                        value = formatChange(impact.sleepChange),
                        valueColor = impactColorSleep(impact.sleepChange, defaultColor),
                    )
                    ImpactRow(
                        label = "Daily Training Capacity (DTC)",
                        value = formatChange(impact.dtcChange),
                        valueColor = impactColorDtc(impact.dtcChange, defaultColor),
                    )
                }

                Spacer(Modifier.height(20.dp))
                Text(
                    text = "Today's recommended run",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                RecommendationCompare(
                    before = impact.previousRecommendation,
                    after = impact.newRecommendation,
                )

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = StrivnAccent),
                ) {
                    Text("Got it")
                }
            }
        }
    }
}

@Composable
private fun RecommendationCompare(
    before: RunRecommendation,
    after: RunRecommendation,
) {
    val defaultColor = MaterialTheme.colorScheme.onBackground
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Before",
                style = MaterialTheme.typography.labelMedium,
                color = muted,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start,
            )
            Text(
                text = "After",
                style = MaterialTheme.typography.labelMedium,
                color = muted,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = before.type,
                    style = MaterialTheme.typography.bodyMedium,
                    color = defaultColor,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )
                Text(
                    text = "${String.format("%.1f", before.distanceKm)} km · RPE ${before.rpe}",
                    style = MaterialTheme.typography.bodySmall,
                    color = muted,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )
            }
            Text(
                text = "→",
                style = MaterialTheme.typography.titleMedium,
                color = muted,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = after.type,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StrivnAccent,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
                Text(
                    text = "${String.format("%.1f", after.distanceKm)} km · RPE ${after.rpe}",
                    style = MaterialTheme.typography.bodySmall,
                    color = muted,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

@Composable
private fun ImpactRow(
    label: String,
    value: String,
    valueColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
        )
    }
}

private fun formatChange(delta: Int): String = when {
    delta > 0 -> "+$delta"
    delta < 0 -> "$delta"
    else -> "0"
}

private fun impactColor(delta: Int, isFitness: Boolean, defaultColor: Color): Color = when {
    isFitness -> when {
        delta > 0 -> StrivnAccent
        delta < 0 -> StrivnError
        else -> defaultColor
    }
    else -> when {
        delta > 0 -> StrivnError
        delta < 0 -> StrivnAccent
        else -> defaultColor
    }
}

private fun impactColorSleep(delta: Int, defaultColor: Color): Color = when {
    delta > 0 -> StrivnAccent
    delta < 0 -> StrivnError
    else -> defaultColor
}

private fun impactColorDtc(delta: Int, defaultColor: Color): Color = when {
    delta > 0 -> StrivnAccent
    delta < 0 -> StrivnError
    else -> defaultColor
}
