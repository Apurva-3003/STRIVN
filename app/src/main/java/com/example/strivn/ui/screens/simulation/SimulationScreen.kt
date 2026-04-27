package com.example.strivn.ui.screens.simulation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.strivn.ui.theme.StrivnDefaults

@Composable
fun SimulationScreen(
    modifier: Modifier = Modifier,
    viewModel: SimulationViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()
    val canSimulate = state.metrics != null && state.recommendation != null
    val simulationsEnabled = !state.hasRunLoggedToday
    val canRunSimulations = canSimulate && simulationsEnabled

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (!state.hasCheckInToday && !state.hasRunLoggedToday) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = StrivnDefaults.secondaryCardColors(),
                elevation = StrivnDefaults.cardElevation(),
            ) {
                Text(
                    text = "Complete your daily check-in before you can log a run from simulation.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(18.dp),
                )
            }
        }

        if (state.hasRunLoggedToday) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = StrivnDefaults.secondaryCardColors(),
                elevation = StrivnDefaults.cardElevation(),
            ) {
                Text(
                    text = "You’ve already logged a run today. Simulation and logging another run are disabled until tomorrow.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(18.dp),
                )
            }
        }

        RecommendedRunSimulationCard(
            recommendation = state.recommendation,
            onSimulateClick = { viewModel.simulateRecommendedRun() },
            preview = state.recommendedPreview,
            simulateEnabled = canRunSimulations,
        )

        CustomRunSimulationCard(
            customDistance = state.customDistance,
            customDuration = state.customDuration,
            customRpe = state.customRpe,
            onDistanceChange = viewModel::updateCustomDistance,
            onDurationChange = viewModel::updateCustomDuration,
            onRpeChange = viewModel::updateCustomRpe,
            onSimulateClick = { viewModel.simulateCustomRun() },
            preview = state.customPreview,
            simulateEnabled = state.metrics != null && simulationsEnabled,
        )

        SkipRunSimulationCard(
            onSimulateClick = { viewModel.simulateSkipRun() },
            preview = state.skipPreview,
            simulateEnabled = state.metrics != null && simulationsEnabled,
        )

        Spacer(Modifier.height(8.dp))
    }

    if (state.showImpactPopup && state.simulationResult != null) {
        SimulationImpactDialog(
            result = state.simulationResult!!,
            onDismissRequest = { viewModel.closeImpactPopup() },
            onLogRun = { viewModel.logRunFromSimulation() },
            showLogRunButton = state.impactShowsLogRun,
            logRunEnabled = !state.hasRunLoggedToday,
        )
    }

    state.userMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissUserMessage() },
            title = { Text("Check-in required") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissUserMessage() }) {
                    Text("OK")
                }
            },
        )
    }
}
