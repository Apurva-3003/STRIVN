package com.example.strivn.ui.screens.simulation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.strivn.core.models.SimulationResult
import com.example.strivn.ui.theme.StrivnAccent
import com.example.strivn.ui.theme.StrivnError

/**
 * Uses [SimulationResult.wheelFitnessChange], [wheelFatigueChange], [wheelCapacityChange] so labels
 * match Home metric wheels (all gauges use stored 0–100 as arc %).
 */
@Composable
fun SimulationImpactDialog(
    result: SimulationResult,
    onDismissRequest: () -> Unit,
    onLogRun: () -> Unit,
    modifier: Modifier = Modifier,
    /** Hidden for skip/rest preview (Close only). */
    showLogRunButton: Boolean = true,
    logRunEnabled: Boolean = true,
) {
    val capacityColor = when {
        result.wheelCapacityChange > 0 -> StrivnAccent
        result.wheelCapacityChange < 0 -> StrivnError
        else -> MaterialTheme.colorScheme.onSurface
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = {
            Text(
                text = "Projected Impact for Tomorrow",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MetricBeforeAfterRow(
                    label = "Fitness",
                    before = result.beforeFitness,
                    after = result.predictedFitness,
                    delta = result.wheelFitnessChange,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                MetricBeforeAfterRow(
                    label = "Fatigue",
                    before = result.beforeFatigue,
                    after = result.predictedFatigue,
                    delta = result.wheelFatigueChange,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                MetricBeforeAfterRow(
                    label = "Training Capacity (DTC)",
                    before = result.beforeCapacity,
                    after = result.predictedCapacity,
                    delta = result.wheelCapacityChange,
                    color = capacityColor,
                )
            }
        },
        confirmButton = {
            if (showLogRunButton) {
                TextButton(
                    onClick = onLogRun,
                    enabled = logRunEnabled,
                ) {
                    Text("Log Run")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Close")
            }
        },
    )
}

@Composable
private fun MetricBeforeAfterRow(
    label: String,
    before: Int,
    after: Int,
    delta: Int,
    color: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = color,
        )
        Text(
            text = "${before.coerceIn(0, 100)} → ${after.coerceIn(0, 100)}  (${formatSigned(delta)})",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = color,
        )
    }
}

private fun formatSigned(delta: Int): String =
    if (delta > 0) "+$delta" else "$delta"
