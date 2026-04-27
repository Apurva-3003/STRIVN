package com.example.strivn

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.example.strivn.data.repository.InMemoryCheckInStore
import com.example.strivn.navigation.StrivnApp
import com.example.strivn.network.TokenStore
import com.example.strivn.ui.theme.STRIVNTheme
import com.example.strivn.worker.CheckInReminderScheduler

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_OPEN_CHECK_IN = "open_check_in"
    }

    private val openCheckInState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenStore.init(applicationContext)
        InMemoryCheckInStore.init(applicationContext)
        enableEdgeToEdge()

        // Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
        }

        // Schedule daily check-in reminder
        CheckInReminderScheduler.schedule(this)

        openCheckInState.value = intent?.getBooleanExtra(EXTRA_OPEN_CHECK_IN, false) ?: false
        intent?.removeExtra(EXTRA_OPEN_CHECK_IN)

        setContent {
            STRIVNTheme {
                StrivnApp(openCheckInOnLaunch = openCheckInState)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            setIntent(it)
            if (it.getBooleanExtra(EXTRA_OPEN_CHECK_IN, false)) {
                openCheckInState.value = true
                it.removeExtra(EXTRA_OPEN_CHECK_IN)
            }
        }
    }
}