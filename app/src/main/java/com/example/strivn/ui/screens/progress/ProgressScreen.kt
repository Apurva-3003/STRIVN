package com.example.strivn.ui.screens.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.strivn.navigation.Routes
import com.example.strivn.ui.screens.progress.components.ConsistencyCard
import com.example.strivn.ui.screens.progress.components.FitnessFatigueCard
import com.example.strivn.ui.screens.progress.components.LongRunProgressCard
import com.example.strivn.ui.screens.progress.components.WeeklyDistanceCard

@Composable
fun ProgressScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController? = null,
    viewModel: ProgressViewModel = if (navController != null) {
        viewModel(viewModelStoreOwner = navController.getBackStackEntry(Routes.PROGRESS))
    } else {
        viewModel()
    },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    state.loadError?.let { err ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissLoadError() },
            title = { Text("Couldn’t sync") },
            text = { Text(err) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissLoadError() }) {
                    Text("OK")
                }
            },
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state.isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item {
            WeeklyDistanceCard(
                weeklyDistanceThisWeekKm = state.weeklyDistanceThisWeekKm,
                weeklyDistance = state.weeklyDistance,
            )
        }
        item {
            FitnessFatigueCard(
                currentFitness = state.metrics?.fitness,
                currentFatigue = state.metrics?.fatigue,
                fitnessFatigueTrend = state.fitnessFatigueTrend,
            )
        }
        item {
            LongRunProgressCard(
                longestRunLast30DaysKm = state.longestRunLast30DaysKm,
                longRunProgression = state.longRunProgression,
            )
        }
        item {
            ConsistencyCard(consistency = state.consistency)
        }
    }
}
