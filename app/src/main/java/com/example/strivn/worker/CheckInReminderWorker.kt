package com.example.strivn.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * WorkManager Worker that shows the daily check-in reminder notification.
 * Scheduled to run every morning (e.g., 8 AM).
 */
class CheckInReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        CheckInNotificationHelper.showCheckInReminder(applicationContext)
        return Result.success()
    }
}
