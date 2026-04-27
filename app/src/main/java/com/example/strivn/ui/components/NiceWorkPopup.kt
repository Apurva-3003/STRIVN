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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.strivn.ui.theme.StrivnAccent
import com.example.strivn.ui.theme.StrivnError
import com.example.strivn.ui.theme.StrivnPrimaryCard

/**
 * Data passed from Log Run popup to Nice Work popup.
 */
data class RunImpactData(
    val fitnessChange: Int,
    val fatigueChange: Int,
    val projectedTomorrowCapacity: Int,
)

/**
 * Step 2 of Finish Run flow. Shows impact from the logged run.
 * Positive values in green (#14B8A6), fatigue increase in red (#F97373).
 */
@Composable
fun NiceWorkPopup(
    impact: RunImpactData,
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
                    text = "Nice Work!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Impact from Today",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))

                val defaultColor = MaterialTheme.colorScheme.onBackground
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ImpactRow(
                        label = "Fitness change",
                        value = formatChange(impact.fitnessChange),
                        valueColor = impactColor(impact.fitnessChange, isFitness = true, defaultColor),
                    )
                    ImpactRow(
                        label = "Fatigue change",
                        value = formatChange(impact.fatigueChange),
                        valueColor = impactColor(impact.fatigueChange, isFitness = false, defaultColor),
                    )
                    ImpactRow(
                        label = "Projected Tomorrow's Capacity",
                        value = "${impact.projectedTomorrowCapacity}%",
                        valueColor = defaultColor,
                    )
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Today's effort increased your fitness while adding manageable fatigue.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = StrivnAccent),
                ) {
                    Text("Done")
                }
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

private fun formatChange(delta: Int): String {
    return when {
        delta > 0 -> "+$delta"
        delta < 0 -> "$delta"
        else -> "0"
    }
}

private fun impactColor(delta: Int, isFitness: Boolean, defaultColor: Color): Color {
    return when {
        isFitness -> when {
            delta > 0 -> StrivnAccent
            delta < 0 -> StrivnError
            else -> defaultColor
        }
        else -> when {
            // Fatigue: increase = red, decrease = green
            delta > 0 -> StrivnError
            delta < 0 -> StrivnAccent
            else -> defaultColor
        }
    }
}
