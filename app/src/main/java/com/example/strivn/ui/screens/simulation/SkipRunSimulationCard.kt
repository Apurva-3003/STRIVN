package com.example.strivn.ui.screens.simulation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.strivn.ui.theme.StrivnDefaults

/**
 * @param onSimulateClick Wire to [SimulationViewModel.simulateSkipRun].
 */
@Composable
fun SkipRunSimulationCard(
    onSimulateClick: () -> Unit,
    preview: com.example.strivn.core.models.SimulationResult? = null,
    modifier: Modifier = Modifier,
    simulateEnabled: Boolean = true,
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
                text = "Simulate Skipping Today's Run",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "See how recovery would impact your fatigue, fitness, and tomorrow's training capacity.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (preview != null) {
                Text(
                    text = "Impact: Fitness ${fmt(preview.fitnessChange)} • Fatigue ${fmt(preview.fatigueChange)} • Capacity ${fmt(preview.capacityChange)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(
                onClick = onSimulateClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = simulateEnabled,
                colors = StrivnDefaults.secondaryButtonColors(),
            ) {
                Text("Simulate Recovery Day")
            }
        }
    }
}

private fun fmt(delta: Int): String = if (delta > 0) "+$delta" else "$delta"
