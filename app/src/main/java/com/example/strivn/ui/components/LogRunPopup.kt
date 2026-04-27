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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.strivn.ui.theme.StrivnAccent
import com.example.strivn.ui.theme.StrivnPrimaryCard

/**
 * Step 1 of the log-run flow. Wire [onLogRun] to `HomeViewModel.logRun` so the run is sent through the app’s
 * `TrainingRepository` / Retrofit layer and on-screen metrics come from the API response.
 */
@Composable
fun LogRunPopup(
    prefillDistanceKm: Double,
    prefillDurationMin: Int,
    prefillRpe: Int,
    onLogRun: (distanceKm: Double, durationMin: Int, effortRpe: Int) -> Unit,
    onCancel: () -> Unit,
) {
    var distanceText by remember(prefillDistanceKm, prefillDurationMin, prefillRpe) {
        mutableStateOf(
            if (prefillDistanceKm > 0) prefillDistanceKm.toString() else ""
        )
    }
    var durationText by remember(prefillDistanceKm, prefillDurationMin, prefillRpe) {
        mutableStateOf(
            if (prefillDurationMin > 0) prefillDurationMin.toString() else ""
        )
    }
    var effortRpeText by remember(prefillDistanceKm, prefillDurationMin, prefillRpe) {
        mutableStateOf(prefillRpe.coerceIn(1, 10).toString())
    }

    Dialog(onDismissRequest = onCancel) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = StrivnPrimaryCard,
                    shape = MaterialTheme.shapes.large,
                ),
        ) {
            IconButton(
                onClick = onCancel,
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
                    .padding(top = 40.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Log Your Run",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Enter your run details so Strivn can update your training",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = distanceText,
                    onValueChange = { distanceText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Distance (km)") },
                    placeholder = { Text("e.g. 5.2") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Duration (minutes)") },
                    placeholder = { Text("e.g. 30") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = effortRpeText,
                    onValueChange = { effortRpeText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Effort (RPE 1-10)") },
                    placeholder = { Text("1-10") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val distance = distanceText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
                            val duration = durationText.toIntOrNull()?.coerceAtLeast(0) ?: 0
                            val rpe = effortRpeText.toIntOrNull()?.coerceIn(1, 10) ?: 5
                            onLogRun(distance, duration, rpe)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = StrivnAccent),
                    ) {
                        Text("Log Run")
                    }
                }
            }
        }
    }
}
