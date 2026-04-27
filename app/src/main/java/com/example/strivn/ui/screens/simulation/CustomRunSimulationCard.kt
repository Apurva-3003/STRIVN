package com.example.strivn.ui.screens.simulation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.strivn.ui.theme.StrivnDefaults

/**
 * @param onSimulateClick Wire to [SimulationViewModel.simulateCustomRun].
 */
@Composable
fun CustomRunSimulationCard(
    customDistance: String,
    customDuration: String,
    customRpe: String,
    onDistanceChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onRpeChange: (String) -> Unit,
    onSimulateClick: () -> Unit,
    preview: com.example.strivn.core.models.SimulationResult? = null,
    modifier: Modifier = Modifier,
    simulateEnabled: Boolean = true,
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
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Simulate Custom Run",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Enter distance, duration, and RPE to preview impact.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = customDistance,
                onValueChange = onDistanceChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Distance (km)") },
                singleLine = true,
            )
            OutlinedTextField(
                value = customDuration,
                onValueChange = onDurationChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Duration (minutes)") },
                singleLine = true,
            )
            OutlinedTextField(
                value = customRpe,
                onValueChange = onRpeChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("RPE (1–10)") },
                singleLine = true,
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
                colors = StrivnDefaults.primaryButtonColors(),
            ) {
                Text("Simulate Custom Run")
            }
        }
    }
}

private fun fmt(delta: Int): String = if (delta > 0) "+$delta" else "$delta"
