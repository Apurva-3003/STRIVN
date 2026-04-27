package com.example.strivn.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.strivn.data.models.UserProfile
import com.example.strivn.data.repository.InMemoryBanisterStateStore
import com.example.strivn.data.repository.InMemoryUserMetricsStore
import com.example.strivn.data.repository.InMemoryUserProfileStore
import com.example.strivn.data.repository.OnboardingPreferences
import com.example.strivn.core.engines.MetricsInitializer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.strivn.ui.screens.auth.AuthViewModel
import com.example.strivn.ui.theme.StrivnAccent
import com.example.strivn.ui.theme.StrivnBackground
import com.example.strivn.ui.theme.StrivnDefaults
import com.example.strivn.ui.theme.StrivnPrimaryCard
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val GOAL_OPTIONS = listOf(
    "5K",
    "10K",
    "Half Marathon",
    "Marathon",
    "Improve Fitness",
    "Build Base",
)

private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

private val PAGE_COUNT = 9

@Composable
fun OnboardingScreen(
    authViewModel: AuthViewModel,
    needsRegistrationAtCompletion: Boolean = true,
    onNavigateToLogin: () -> Unit = {},
    onComplete: (navigateToCheckIn: Boolean) -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { OnboardingPreferences(context) }

    val authUi by authViewModel.uiState.collectAsStateWithLifecycle()
    var pendingRouteAfterAuth by remember { mutableStateOf<Boolean?>(null) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var step by remember { mutableIntStateOf(0) }
    var goal by remember { mutableStateOf("") }
    var raceDateMillis by remember { mutableStateOf<Long?>(null) }
    var weeklyMileage by remember { mutableFloatStateOf(40f) }
    var longestRun by remember { mutableFloatStateOf(20f) }
    var runningDays by remember { mutableFloatStateOf(4f) }
    var injuryStatus by remember { mutableStateOf(false) }
    var avgSleepHours by remember { mutableFloatStateOf(7f) }

    fun raceDateString(): String =
        raceDateMillis?.let { dateFormat.format(Date(it)) } ?: ""

    fun canProceed(page: Int): Boolean = when (page) {
        0 -> true
        1 -> goal.isNotBlank()
        2 -> raceDateMillis != null
        3, 4, 5, 6, 7 -> true
        8 -> if (needsRegistrationAtCompletion) {
            email.isNotBlank() && password.isNotBlank()
        } else {
            true
        }
        else -> false
    }

    fun buildProfile(): UserProfile = UserProfile(
        goal = goal,
        raceDate = raceDateString().ifBlank { dateFormat.format(Calendar.getInstance().apply { add(Calendar.MONTH, 3) }.time) },
        weeklyMileage = weeklyMileage.coerceIn(10f, 100f).toDouble(),
        longestRun = longestRun.coerceIn(5f, 50f).toDouble(),
        runningDaysPerWeek = runningDays.roundToInt().coerceIn(1, 7),
        injuryStatus = injuryStatus,
        avgSleepHours = avgSleepHours.coerceIn(4f, 12f).toDouble(),
    )

    fun finishOnboarding(profile: UserProfile) {
        prefs.completeOnboarding(profile)
        InMemoryUserProfileStore.update(profile)
        val initialMetrics = MetricsInitializer.initializeMetrics(profile)
        InMemoryUserMetricsStore.update(initialMetrics)
        InMemoryBanisterStateStore.initializeState(initialMetrics.fitness, initialMetrics.fatigue)
    }

    LaunchedEffect(authUi.awaitingProfileSaveAfterRegister, pendingRouteAfterAuth) {
        if (needsRegistrationAtCompletion &&
            authUi.awaitingProfileSaveAfterRegister &&
            pendingRouteAfterAuth != null
        ) {
            val goCheckIn = pendingRouteAfterAuth!!
            pendingRouteAfterAuth = null
            val profile = buildProfile()
            finishOnboarding(profile)
            onComplete(goCheckIn)
            authViewModel.acknowledgeSessionAfterProfileSaved()
        }
    }

    LaunchedEffect(authUi.errorMessage, authUi.isLoading) {
        if (needsRegistrationAtCompletion && !authUi.isLoading && authUi.errorMessage != null) {
            pendingRouteAfterAuth = null
        }
    }

    fun finishAndGoToCheckIn() {
        if (needsRegistrationAtCompletion) {
            pendingRouteAfterAuth = true
            authViewModel.register(email, password)
            return
        }
        val profile = buildProfile()
        finishOnboarding(profile)
        onComplete(true)
    }

    fun finishOnly() {
        if (needsRegistrationAtCompletion) {
            pendingRouteAfterAuth = false
            authViewModel.register(email, password)
            return
        }
        val profile = buildProfile()
        finishOnboarding(profile)
        onComplete(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
    ) {
        when (step) {
            0 -> key(0) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    WelcomeStep(
                        onGetStarted = { step = 1 },
                        showLoginLink = needsRegistrationAtCompletion,
                        onNavigateToLogin = onNavigateToLogin,
                    )
                }
            }
            1, 2, 3, 4, 5, 6, 7 -> key(step) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    OnboardingContentStep(
                        page = step,
                        goal = goal,
                        onGoalChange = { goal = it },
                        raceDateMillis = raceDateMillis,
                        onDateSelected = { raceDateMillis = it },
                        weeklyMileage = weeklyMileage,
                        onWeeklyMileageChange = { weeklyMileage = it },
                        longestRun = longestRun,
                        onLongestRunChange = { longestRun = it },
                        runningDays = runningDays,
                        onRunningDaysChange = { runningDays = it },
                        injuryStatus = injuryStatus,
                        onInjuryChange = { injuryStatus = it },
                        avgSleepHours = avgSleepHours,
                        onAvgSleepChange = { avgSleepHours = it },
                        canProceed = { canProceed(step) },
                        onBack = { step -= 1 },
                        onNext = { step += 1 },
                    )
                }
            }
            8 -> Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CompletionStep(
                    needsRegistration = needsRegistrationAtCompletion,
                    email = email,
                    onEmailChange = { email = it },
                    password = password,
                    onPasswordChange = { password = it },
                    isRegistering = authUi.isLoading,
                    errorMessage = authUi.errorMessage,
                    onCompleteCheckIn = { finishAndGoToCheckIn() },
                    onSkipToHome = { finishOnly() },
                )
            }
        }
    }
}

@Composable
private fun OnboardingContentStep(
    page: Int,
    goal: String,
    onGoalChange: (String) -> Unit,
    raceDateMillis: Long?,
    onDateSelected: (Long) -> Unit,
    weeklyMileage: Float,
    onWeeklyMileageChange: (Float) -> Unit,
    longestRun: Float,
    onLongestRunChange: (Float) -> Unit,
    runningDays: Float,
    onRunningDaysChange: (Float) -> Unit,
    injuryStatus: Boolean,
    onInjuryChange: (Boolean) -> Unit,
    avgSleepHours: Float,
    onAvgSleepChange: (Float) -> Unit,
    canProceed: () -> Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {
        Text(
            text = "Step $page of ${PAGE_COUNT - 2}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 20.dp),
        )
        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when (page) {
                    1 -> CardStep(compact = true) {
                        GoalStep(goal = goal, onGoalChange = onGoalChange)
                    }
                    2 -> CardStep { RaceDateStep(raceDateMillis = raceDateMillis, onDateSelected = onDateSelected) }
                    3 -> CardStep { WeeklyMileageStep(km = weeklyMileage, onKmChange = onWeeklyMileageChange) }
                    4 -> CardStep { LongestRunStep(km = longestRun, onKmChange = onLongestRunChange) }
                    5 -> CardStep { RunningDaysStep(days = runningDays, onDaysChange = onRunningDaysChange) }
                    6 -> CardStep { InjuryStep(hasInjury = injuryStatus, onInjuryChange = onInjuryChange) }
                    7 -> CardStep { AvgSleepStep(hours = avgSleepHours, onHoursChange = onAvgSleepChange) }
                    else -> {}
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Step 1: Back returns to Welcome ("Get started"). Steps 2+: Back to previous step.
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
            ) {
                Text("Back")
            }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = canProceed(),
                colors = StrivnDefaults.primaryButtonColors(),
            ) {
                Text(if (page == 7) "Next" else "Next")
            }
        }
    }
}

@Composable
private fun CardStep(
    compact: Boolean = false,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier
            .then(
                if (compact) Modifier.widthIn(max = 320.dp)
                else Modifier.fillMaxWidth()
            )
            .verticalScroll(rememberScrollState()),
        colors = StrivnDefaults.primaryCardColors(),
        elevation = StrivnDefaults.cardElevation(),
    ) {
        Column(
            modifier = Modifier
                .then(
                    if (compact) Modifier.widthIn(max = 320.dp)
                    else Modifier.fillMaxWidth()
                )
                .padding(vertical = 28.dp, horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun GoalStep(goal: String, onGoalChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "What's your main goal?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        GOAL_OPTIONS.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = goal == option,
                        onClick = { onGoalChange(option) }
                    )
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = goal == option,
                    onClick = { onGoalChange(option) },
                )
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RaceDateStep(raceDateMillis: Long?, onDateSelected: (Long) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = raceDateMillis ?: System.currentTimeMillis(),
        yearRange = Calendar.getInstance().get(Calendar.YEAR)..(Calendar.getInstance().get(Calendar.YEAR) + 2),
    )

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onDateSelected(it)
                            showPicker = false
                        }
                    },
                ) {
                    Text("OK", color = StrivnAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "When is your race?",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Tap to select from calendar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedButton(
            onClick = { showPicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = raceDateMillis?.let { dateFormat.format(Date(it)) } ?: "Select date",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun WeeklyMileageStep(km: Float, onKmChange: (Float) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Weekly mileage",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "${km.roundToInt()} km per week",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Slider(
            value = km,
            onValueChange = onKmChange,
            valueRange = 10f..100f,
            steps = 17,
        )
    }
}

@Composable
private fun LongestRunStep(km: Float, onKmChange: (Float) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Longest run (past 4 weeks)",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "${km.roundToInt()} km",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Slider(
            value = km,
            onValueChange = onKmChange,
            valueRange = 5f..50f,
            steps = 8,
        )
    }
}

@Composable
private fun RunningDaysStep(days: Float, onDaysChange: (Float) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Running days per week",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "${days.roundToInt()} days",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Slider(
            value = days,
            onValueChange = onDaysChange,
            valueRange = 1f..7f,
            steps = 5,
        )
    }
}

@Composable
private fun InjuryStep(hasInjury: Boolean, onInjuryChange: (Boolean) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Current injury status",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Are you currently managing an injury?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = if (hasInjury) "Yes, I have an injury" else "No injury",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Switch(checked = hasInjury, onCheckedChange = onInjuryChange)
        }
    }
}

@Composable
private fun AvgSleepStep(hours: Float, onHoursChange: (Float) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Average sleep per night",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "${hours} hours",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Slider(
            value = hours,
            onValueChange = onHoursChange,
            valueRange = 4f..12f,
            steps = 15,
        )
    }
}

@Composable
private fun WelcomeStep(
    onGetStarted: () -> Unit,
    showLoginLink: Boolean,
    onNavigateToLogin: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            StrivnAccent.copy(alpha = 0.08f),
                            StrivnBackground,
                            StrivnPrimaryCard.copy(alpha = 0.3f),
                            StrivnBackground,
                        ),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(Modifier.weight(1f))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .background(
                            StrivnAccent.copy(alpha = 0.2f),
                            shape = MaterialTheme.shapes.extraLarge,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.DirectionsRun,
                        contentDescription = null,
                        modifier = Modifier.padding(40.dp),
                        tint = StrivnAccent,
                    )
                }
                Text(
                    text = "STRIVN",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = TextUnit(0.08f, TextUnitType.Em),
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Your adaptive running coach",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = StrivnDefaults.primaryButtonColors(),
            ) {
                Text("Get Started")
            }
            if (showLoginLink) {
                TextButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.padding(bottom = 24.dp),
                ) {
                    Text("Already have an account? Log in")
                }
            } else {
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CompletionStep(
    needsRegistration: Boolean,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    isRegistering: Boolean,
    errorMessage: String?,
    onCompleteCheckIn: () -> Unit,
    onSkipToHome: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f),
            colors = StrivnDefaults.primaryCardColors(),
            elevation = StrivnDefaults.cardElevation(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "You're all set!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Complete your daily check-in to get personalized training recommendations based on how you feel today.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (needsRegistration) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = { Text("Email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isRegistering,
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isRegistering,
                    )
                    errorMessage?.let { msg ->
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onCompleteCheckIn,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRegistering,
            colors = StrivnDefaults.primaryButtonColors(),
        ) {
            if (isRegistering) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Complete Daily Check-In")
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onSkipToHome,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRegistering,
        ) {
            Text("Go to Home")
        }
    }
}
