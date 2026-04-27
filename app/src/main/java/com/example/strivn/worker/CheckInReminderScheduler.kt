package com.example.strivn.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Schedules the daily check-in reminder notification.
 * Uses WorkManager for reliable background execution.
 */
object CheckInReminderScheduler {

    private const val WORK_NAME = "check_in_reminder"
    private const val REMINDER_HOUR = 8
    private const val REMINDER_MINUTE = 0

    /**
     * Schedules a daily reminder at approximately 8 AM.
     * WorkManager runs every 24 hours; initial delay aligns first run with next 8 AM.
     */
    fun schedule(context: Context) {
        val initialDelayMinutes = minutesUntilNextReminderTime()

        val request = PeriodicWorkRequestBuilder<CheckInReminderWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
        ).setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    private fun minutesUntilNextReminderTime(): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, REMINDER_HOUR)
            set(Calendar.MINUTE, REMINDER_MINUTE)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (now.after(target)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return ((target.timeInMillis - now.timeInMillis) / (1000 * 60)).toLong()
    }
}
