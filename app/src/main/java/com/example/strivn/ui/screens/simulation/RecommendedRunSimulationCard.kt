package com.example.strivn.ui.screens.simulation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.strivn.core.engines.Recommendation
import com.example.strivn.ui.theme.StrivnDefaults

/**
 * @param recommendation When null, detail rows show a loading placeholder.
 * @param onSimulateClick Wire to [SimulationViewModel.simulateRecommendedRun].
 */
@Composable
fun RecommendedRunSimulationCard(
    recommendation: Recommendation?,
    onSimulateClick: () -> Unit,
    preview: com.example.strivn.core.models.SimulationResult? = null,
    modifier: Modifier = Modifier,
    simulateEnabled: Boolean = recommendation != null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = StrivnDefaults.primaryCardColors(),
        elevation = StrivnDefaults.cardElevation(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Simulate Today's Recommended Run",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )

            if (recommendation != null) {
                SimulationDetailRow(label = "Focus", value = recommendation.focus)
                SimulationDetailRow(
                    label = "Distance",
                    value = "%.2f km".format(recommendation.distanceKm),
                )
                SimulationDetailRow(
                    label = "Duration",
                    value = "${recommendation.durationMin} min",
                )
                SimulationDetailRow(
                    label = "RPE",
                    value = "${recommendation.rpe}/10",
                )
                if (preview != null) {
                    Text(
                        text = "Impact: Fitness ${fmt(preview.fitnessChange)} • Fatigue ${fmt(preview.fatigueChange)} • Capacity ${fmt(preview.capacityChange)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Text(
                    text = "Loading recommendation…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Button(
                onClick = onSimulateClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = simulateEnabled,
                colors = StrivnDefaults.primaryButtonColors(),
            ) {
                Text("Simulate Impact")
            }
        }
    }
}

private fun fmt(delta: Int): String = if (delta > 0) "+$delta" else "$delta"

@Composable
private fun SimulationDetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
