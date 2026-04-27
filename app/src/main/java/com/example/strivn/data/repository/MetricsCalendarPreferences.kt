package com.example.strivn.data.repository

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate

/**
 * Tracks which calendar dates have had end-of-day metrics reconciliation (skipped-run recovery).
 */
class MetricsCalendarPreferences(context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Last calendar date for which we've applied daily reconciliation on app open.
     * Null on first launch → treated as [LocalDate.now()] (no historical backfill).
     */
    var lastReconciledCalendarDate: LocalDate?
        get() = prefs.getString(KEY_LAST_RECONCILED, null)?.let { LocalDate.parse(it) }
        set(value) {
            prefs.edit().apply {
                if (value == null) remove(KEY_LAST_RECONCILED)
                else putString(KEY_LAST_RECONCILED, value.toString())
            }.apply()
        }

    companion object {
        private const val PREFS_NAME = "strivn_metrics_calendar"
        private const val KEY_LAST_RECONCILED = "last_reconciled_calendar_date"
    }
}
