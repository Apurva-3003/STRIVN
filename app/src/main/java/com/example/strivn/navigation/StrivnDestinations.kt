package com.example.strivn.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class StrivnDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Home : StrivnDestination(
        route = Routes.HOME,
        label = "Home",
        icon = Icons.Filled.Home,
    )

    data object Progress : StrivnDestination(
        route = Routes.PROGRESS,
        label = "Progress",
        icon = Icons.Filled.ShowChart,
    )

    data object CheckIn : StrivnDestination(
        route = Routes.CHECK_IN,
        label = "Check-In",
        icon = Icons.Filled.CheckCircle,
    )

    data object Simulation : StrivnDestination(
        route = Routes.SIMULATION,
        label = "Simulate",
        icon = Icons.Filled.PlayCircle,
    )

    data object AdaptAi : StrivnDestination(
        route = Routes.ADAPT_AI,
        label = "Adapt AI",
        icon = Icons.Filled.AutoAwesome,
    )
}

val bottomNavDestinations: List<StrivnDestination> = listOf(
    StrivnDestination.Home,
    StrivnDestination.Progress,
    StrivnDestination.CheckIn,
    StrivnDestination.Simulation,
    StrivnDestination.AdaptAi,
)

