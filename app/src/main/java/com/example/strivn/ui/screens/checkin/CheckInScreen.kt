package com.example.strivn.ui.screens.checkin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.strivn.data.models.DailyCheckIn
import com.example.strivn.data.repository.InMemoryCheckInStore
import com.example.strivn.data.repository.displayDateForToday
import com.example.strivn.data.repository.todayString
import com.example.strivn.navigation.Routes
import com.example.strivn.network.toUserVisibleMessage
import com.example.strivn.ui.screens.home.HomeViewModel
import com.example.strivn.ui.theme.StrivnDefaults
import kotlin.math.roundToInt

@Composable
fun CheckInScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController? = null,
) {
    // Same HomeViewModel instance as HomeScreen (HOME route) so check-in updates shared state.
    val homeViewModel: HomeViewModel = if (navController != null) {
        viewModel(
            viewModelStoreOwner = navController.getBackStackEntry(Routes.HOME),
        )
    } else {
        viewModel()
    }

    val todayCheckIn = InMemoryCheckInStore.getCheckInForToday()
    val hasCheckInForToday = InMemoryCheckInStore.hasCheckInForToday()
    val displayDate = displayDateForToday()

    // Section 1: Recovery
    var sleepHoursText by remember(todayCheckIn) {
        mutableStateOf(todayCheckIn?.sleepHours?.toString()?.takeIf { it != "0.0" } ?: "")
    }
    var sleepQuality by remember(todayCheckIn) {
        mutableFloatStateOf((todayCheckIn?.sleepQuality ?: 3).toFloat())
    }
    var muscleSoreness by remember(todayCheckIn) {
        mutableFloatStateOf((todayCheckIn?.muscleSoreness ?: 3).toFloat())
    }

    // Section 2: Daily Feel
    var energyLevel by remember(todayCheckIn) {
        mutableFloatStateOf((todayCheckIn?.energyLevel ?: 3).toFloat())
    }
    var stressLevel by remember(todayCheckIn) {
        mutableFloatStateOf((todayCheckIn?.stressLevel ?: 3).toFloat())
    }
    var checkInSubmitError by remember { mutableStateOf<String?>(null) }

    checkInSubmitError?.let { err ->
        AlertDialog(
            onDismissRequest = { checkInSubmitError = null },
            title = { Text("Check-in failed") },
            text = { Text(err) },
            confirmButton = {
                TextButton(onClick = { checkInSubmitError = null }) {
                    Text("OK")
                }
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Current date, top right (under top bar)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Text(
                text = displayDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // ——— SECTION 1: Recovery ———
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = StrivnDefaults.primaryCardColors(),
            elevation = StrivnDefaults.cardElevation(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SectionHeader(title = "Recovery")

                OutlinedTextField(
                    value = sleepHoursText,
                    onValueChange = { sleepHoursText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Sleep (hours)") },
                    placeholder = { Text("e.g. 7.5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )

                ScaleSlider(
                    label = "Sleep Quality",
                    value = sleepQuality,
                    valueRange = 1f..5f,
                    steps = 3,
                    valueLabel = sleepQuality.roundToInt().toString(),
                    helper = "1 (poor) → 5 (excellent)",
                    onValueChange = { sleepQuality = it },
                )

                ScaleSlider(
                    label = "Muscle Soreness",
                    value = muscleSoreness,
                    valueRange = 1f..5f,
                    steps = 3,
                    valueLabel = muscleSoreness.roundToInt().toString(),
                    helper = "1 (none) → 5 (severe)",
                    onValueChange = { muscleSoreness = it },
                )
            }
        }

        // ——— SECTION 2: Daily Feel ———
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = StrivnDefaults.primaryCardColors(),
            elevation = StrivnDefaults.cardElevation(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SectionHeader(title = "Daily Feel")

                ScaleSlider(
                    label = "Energy Level",
                    value = energyLevel,
                    valueRange = 1f..5f,
                    steps = 3,
                    valueLabel = energyLevel.roundToInt().toString(),
                    helper = "1 (low) → 5 (high)",
                    onValueChange = { energyLevel = it },
                )

                ScaleSlider(
                    label = "Stress Level",
                    value = stressLevel,
                    valueRange = 1f..5f,
                    steps = 3,
                    valueLabel = stressLevel.roundToInt().toString(),
                    helper = "1 (low) → 5 (high)",
                    onValueChange = { stressLevel = it },
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                val sleepHours = sleepHoursText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0

                val checkIn = DailyCheckIn(
                    date = todayString(),
                    sleepHours = sleepHours,
                    sleepQuality = sleepQuality.roundToInt().coerceIn(1, 5),
                    muscleSoreness = muscleSoreness.roundToInt().coerceIn(1, 5),
                    energyLevel = energyLevel.roundToInt().coerceIn(1, 5),
                    stressLevel = stressLevel.roundToInt().coerceIn(1, 5),
                )
                homeViewModel.submitCheckIn(checkIn) { result ->
                    result.fold(
                        onSuccess = {
                            checkInSubmitError = null
                            // Pop back to existing HOME so we reuse the same HomeViewModel (popup state lives there).
                            val popped = navController?.popBackStack() == true
                            if (!popped) {
                                navController?.navigate(Routes.HOME) {
                                    popUpTo(Routes.HOME) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        },
                        onFailure = { e ->
                            checkInSubmitError = e.toUserVisibleMessage("Check-in failed. Please try again.")
                        },
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = StrivnDefaults.primaryButtonColors(),
        ) {
            Text(if (hasCheckInForToday) "Edit Check-In" else "Submit Check-In")
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun ScaleSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    valueLabel: String,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    helper: String? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
        )

        if (helper != null) {
            Text(
                text = helper,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
