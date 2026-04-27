package com.example.strivn.navigation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.strivn.data.repository.InMemoryBanisterStateStore
import com.example.strivn.data.repository.InMemoryUserMetricsStore
import com.example.strivn.data.repository.InMemoryUserProfileStore
import com.example.strivn.data.repository.MetricsCalendarPreferences
import com.example.strivn.data.repository.OnboardingPreferences
import com.example.strivn.data.repository.TrainingRepositoryProvider
import com.example.strivn.logic.MetricsDayReconciliation
import com.example.strivn.ui.components.BottomNavigationBar
import com.example.strivn.ui.components.TopBar
import com.example.strivn.ui.screens.adaptai.AdaptAiScreen
import com.example.strivn.ui.screens.auth.AuthViewModel
import com.example.strivn.ui.screens.auth.LoginScreen
import com.example.strivn.ui.screens.checkin.CheckInScreen
import com.example.strivn.ui.screens.home.HomeScreen
import com.example.strivn.ui.screens.onboarding.OnboardingScreen
import com.example.strivn.ui.screens.pastruns.PastRunsScreen
import com.example.strivn.ui.screens.progress.ProgressScreen
import com.example.strivn.ui.screens.simulation.SimulationScreen
import com.example.strivn.network.AuthEventBus
import com.example.strivn.network.TokenStore

/**
 * App entry: JWT gate → optional profile onboarding → main [NavHost] (Home, etc.).
 */
@Composable
fun StrivnApp(
    openCheckInOnLaunch: MutableState<Boolean>? = null,
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val authViewModel: AuthViewModel = viewModel(viewModelStoreOwner = activity)
    val authenticated by authViewModel.isAuthenticated.collectAsStateWithLifecycle()

    val prefs = remember { OnboardingPreferences(context) }
    var navigateToCheckInAfterOnboarding by remember { mutableStateOf(false) }
    var profileSetupGate by remember { mutableIntStateOf(0) }
    val metricsCalendarPrefs = remember { MetricsCalendarPreferences(context) }

    LaunchedEffect(Unit) {
        AuthEventBus.logoutEvent.collect {
            Log.d("STRIVN_SEED", "Logout event received; clearing token and returning to login")
            TokenStore.token = null
            authViewModel.forceLogout()
            navigateToCheckInAfterOnboarding = false
            openCheckInOnLaunch?.value = false
            profileSetupGate++
        }
    }

    LaunchedEffect(authenticated, prefs.isOnboardingComplete) {
        if (authenticated && prefs.isOnboardingComplete) {
            prefs.getProfile()?.let { profile ->
                InMemoryUserProfileStore.update(profile)
            }
            Log.d("STRIVN_SEED", "StrivnNavGraph: calling ensureMetricsSeededAfterAuth()")
            val seedResult = TrainingRepositoryProvider.instance.ensureMetricsSeededAfterAuth(context)
            seedResult.fold(
                onSuccess = {
                    Log.d("STRIVN_SEED", "StrivnNavGraph: ensureMetricsSeededAfterAuth() success")
                },
                onFailure = { e ->
                    Log.e(
                        "STRIVN_SEED",
                        "StrivnNavGraph: ensureMetricsSeededAfterAuth() failed type=${e::class.java.name} message=${e.message}",
                        e,
                    )
                },
            )
            val m = InMemoryUserMetricsStore.metrics
            InMemoryBanisterStateStore.initializeState(m.fitness, m.fatigue)
            MetricsDayReconciliation.reconcileIfNeeded(metricsCalendarPrefs)
        }
    }

    key(profileSetupGate) {
        val mainNavController = rememberNavController()

        LaunchedEffect(openCheckInOnLaunch?.value, authenticated, prefs.isOnboardingComplete) {
            if (openCheckInOnLaunch?.value == true && authenticated && prefs.isOnboardingComplete) {
                mainNavController.navigate(Routes.CHECK_IN) {
                    popUpTo(Routes.HOME) { inclusive = false }
                    launchSingleTop = true
                }
                openCheckInOnLaunch?.value = false
            }
        }

        LaunchedEffect(navigateToCheckInAfterOnboarding, authenticated, prefs.isOnboardingComplete) {
            if (navigateToCheckInAfterOnboarding && authenticated && prefs.isOnboardingComplete) {
                mainNavController.navigate(Routes.CHECK_IN) {
                    popUpTo(Routes.HOME) { inclusive = false }
                    launchSingleTop = true
                }
                navigateToCheckInAfterOnboarding = false
            }
        }

        when {
            !authenticated -> {
                val authNavController = rememberNavController()
                val authStart =
                    if (prefs.isOnboardingComplete) Routes.AUTH_LOGIN else Routes.AUTH_ONBOARDING
                AuthNavHost(
                    navController = authNavController,
                    authViewModel = authViewModel,
                    startDestination = authStart,
                    onAuthOnboardingComplete = { goToCheckIn ->
                        navigateToCheckInAfterOnboarding = goToCheckIn
                    },
                )
            }
            !prefs.isOnboardingComplete -> {
                OnboardingScreen(
                    authViewModel = authViewModel,
                    needsRegistrationAtCompletion = false,
                    onNavigateToLogin = {},
                    onComplete = { navigateToCheckIn ->
                        navigateToCheckInAfterOnboarding = navigateToCheckIn
                        profileSetupGate++
                    },
                )
            }
            else -> {
                StrivnScaffold(navController = mainNavController)
            }
        }
    }
}

@Composable
private fun AuthNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    startDestination: String,
    onAuthOnboardingComplete: (navigateToCheckIn: Boolean) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable(Routes.AUTH_LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToSignUp = {
                    navController.navigate(Routes.AUTH_ONBOARDING) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(Routes.AUTH_ONBOARDING) {
            OnboardingScreen(
                authViewModel = authViewModel,
                needsRegistrationAtCompletion = true,
                onNavigateToLogin = {
                    navController.navigate(Routes.AUTH_LOGIN) {
                        popUpTo(Routes.AUTH_ONBOARDING) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onComplete = onAuthOnboardingComplete,
            )
        }
    }
}

@Composable
private fun StrivnScaffold(
    navController: NavHostController,
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val title = when {
        currentRoute == Routes.HOME -> "STRIVN – Adaptive Engine"
        currentRoute == "progress" -> "Progress"
        currentRoute == Routes.CHECK_IN -> "Daily Check-In"
        currentRoute == Routes.SIMULATION -> "Simulation"
        currentRoute == Routes.ADAPT_AI -> "Adapt AI"
        currentRoute == Routes.PAST_RUNS -> "Past Runs"
        else -> "STRIVN"
    }

    val canNavigateBack = navController.previousBackStackEntry != null
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                title = title,
                centerTitle = true,
                onBackClick = if (canNavigateBack) {
                    { navController.popBackStack() }
                } else {
                    null
                },
                actions = {
                    if (currentRoute == Routes.HOME) {
                        IconButton(
                            onClick = {
                                navController.navigate(Routes.PAST_RUNS) {
                                    launchSingleTop = true
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = "Past runs",
                            )
                        }
                    }
                },
            )
        },
        bottomBar = { BottomNavigationBar(navController = navController) },
    ) { innerPadding ->
        StrivnNavHost(
            navController = navController,
            contentPadding = innerPadding,
        )
    }
}

@Composable
private fun StrivnNavHost(
    navController: NavHostController,
    contentPadding: PaddingValues,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                navController = navController,
                modifier = Modifier.padding(contentPadding),
            )
        }
        composable("progress") {
            ProgressScreen(
                navController = navController,
                modifier = Modifier.padding(contentPadding),
            )
        }
        composable(Routes.CHECK_IN) {
            CheckInScreen(
                navController = navController,
                modifier = Modifier.padding(contentPadding),
            )
        }
        composable(Routes.SIMULATION) {
            SimulationScreen(modifier = Modifier.padding(contentPadding))
        }
        composable(Routes.ADAPT_AI) {
            AdaptAiScreen(modifier = Modifier.padding(contentPadding))
        }
        composable(Routes.PAST_RUNS) {
            PastRunsScreen(
                navController = navController,
                modifier = Modifier.padding(contentPadding),
            )
        }
    }
}
