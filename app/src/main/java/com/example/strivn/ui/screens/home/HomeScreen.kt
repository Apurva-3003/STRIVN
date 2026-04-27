package com.example.strivn.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.strivn.navigation.Routes
import com.example.strivn.ui.components.CheckInImpactPopup
import com.example.strivn.ui.components.LogRunPopup
import com.example.strivn.ui.components.NiceWorkPopup
import com.example.strivn.ui.theme.StrivnAccent
import com.example.strivn.ui.theme.StrivnDefaults
import com.example.strivn.ui.theme.StrivnError
import com.example.strivn.ui.theme.StrivnWarning
import com.example.strivn.data.repository.InMemoryCheckInStore

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController? = null,
    viewModel: HomeViewModel = if (navController != null) {
        viewModel(viewModelStoreOwner = navController.getBackStackEntry(Routes.HOME))
    } else {
        viewModel()
    },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.showLogRunPopup) {
        LogRunPopup(
            prefillDistanceKm = state.recommendation.distanceKm,
            prefillDurationMin = state.recommendation.durationMin,
            prefillRpe = state.recommendation.rpe,
            onLogRun = { distanceKm, durationMin, effortRpe ->
                viewModel.logRun(distanceKm, durationMin, effortRpe)
            },
            onCancel = { viewModel.closeLogRunPopup() },
        )
    }

    if (state.showCheckInImpactPopup && state.checkInImpactData != null) {
        CheckInImpactPopup(
            impact = state.checkInImpactData!!,
            onDone = { viewModel.closeCheckInImpactPopup() },
        )
    } else if (state.showNiceWorkPopup && state.impactDataForNiceWork != null) {
        NiceWorkPopup(
            impact = state.impactDataForNiceWork!!,
            onDone = { viewModel.closeNiceWorkPopup() },
        )
    }

    state.userMessage?.let { message ->
        val title = if (message == HomeViewModel.LOG_RUN_REQUIRES_CHECK_IN) {
            "Check-in required"
        } else {
            "Couldn’t complete action"
        }
        AlertDialog(
            onDismissRequest = { viewModel.dismissUserMessage() },
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissUserMessage() }) {
                    Text("OK")
                }
            },
        )
    }

    state.syncError?.let { err ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissSyncError() },
            title = { Text("Couldn’t sync") },
            text = { Text(err) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissSyncError() }) {
                    Text("OK")
                }
            },
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            if (!state.hasCheckInToday && !state.hasOpenedCheckInToday) {
                CheckInReminderBanner(
                    onCompleteCheckIn = {
                        InMemoryCheckInStore.markOpenedCheckInToday()
                        navController?.navigate(Routes.CHECK_IN) {
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                )
            }

            TrainingStatusSection(
                metrics = state.metrics,
                modifier = Modifier.fillMaxWidth(),
            )

            ObservationCard(
                readinessState = state.observation.state,
                explanation = state.observation.explanation,
                readinessColor = state.observation.color,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp),
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                shape = MaterialTheme.shapes.medium,
                colors = StrivnDefaults.primaryCardColors(),
                elevation = StrivnDefaults.cardElevation(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                ) {
                    Text(
                        text = state.recommendation.type,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )

                    Spacer(Modifier.padding(top = 14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(
                            modifier = Modifier.size(96.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DirectionsRun,
                                contentDescription = "Workout type",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(56.dp),
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            DetailRow(label = "Focus", value = state.recommendation.focus)
                            DetailRow(label = "Environment", value = state.recommendation.environment)
                            DetailRow(label = "Goal RPE", value = "${state.recommendation.rpe}/10")
                            DetailRow(label = "Distance", value = "%.2f km".format(state.recommendation.distanceKm))
                            DetailRow(label = "Duration", value = "${state.recommendation.durationMin} min")
                        }
                    }

                    Spacer(Modifier.padding(top = 16.dp))

                    val injuryRisk = (state.recommendation.injuryRisk.coerceIn(0, 100) / 100f)
                    val riskColor = injuryRiskColor(injuryRisk)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "Injury Risk",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        LinearProgressIndicator(
                            progress = { injuryRisk.coerceIn(0f, 1f) },
                            modifier = Modifier.weight(1f),
                            color = riskColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        )
                    }

                    Spacer(Modifier.padding(top = 16.dp))

                    Button(
                        onClick = { viewModel.openLogRunPopup() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.hasRunLoggedToday,
                        colors = StrivnDefaults.primaryButtonColors(),
                    ) {
                        Text("Finish Run")
                    }
                }
            }

            SimulationPromoCard(
                onOpenSimulation = {
                    navController?.navigate(Routes.SIMULATION) {
                        launchSingleTop = true
                    }
                },
                openSimulationEnabled = !state.hasRunLoggedToday,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
            )
        }
    }
}

@Composable
private fun SimulationPromoCard(
    onOpenSimulation: () -> Unit,
    openSimulationEnabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp),
                )
                Text(
                    text = "Training simulation",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                text = "Preview how a run—or a rest day—could affect your fitness, fatigue, and tomorrow’s training capacity before you commit. You can also open this anytime from Simulate in the bottom bar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!openSimulationEnabled) {
                Text(
                    text = "After you log today’s run, simulation is unavailable until tomorrow.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(
                onClick = onOpenSimulation,
                modifier = Modifier.fillMaxWidth(),
                enabled = openSimulationEnabled,
                colors = StrivnDefaults.primaryButtonColors(),
            ) {
                Text("Open simulation")
            }
        }
    }
}

@Composable
private fun ObservationCard(
    readinessState: String,
    explanation: String,
    readinessColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = StrivnDefaults.primaryCardColors(),
        elevation = StrivnDefaults.cardElevation(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = readinessState,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(readinessColor, shape = MaterialTheme.shapes.extraLarge),
                )
            }

            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun CheckInReminderBanner(
    onCompleteCheckIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = StrivnAccent.copy(alpha = 0.15f),
        ),
        border = BorderStroke(1.dp, StrivnAccent.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Complete your check-in for more accurate recommendations",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Button(
                onClick = onCompleteCheckIn,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = StrivnAccent),
            ) {
                Text("Complete Check-In")
            }
        }
    }
}

private fun injuryRiskColor(risk01: Float): Color {
    return when {
        risk01 < 0.34f -> StrivnAccent
        risk01 < 0.67f -> StrivnWarning
        else -> StrivnError
    }
}
