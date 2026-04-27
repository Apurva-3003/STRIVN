package com.example.strivn.logic

import com.example.strivn.core.engines.TrainingModel
import com.example.strivn.data.repository.InMemoryUserMetricsStore
import com.example.strivn.data.repository.MetricsCalendarPreferences
import com.example.strivn.data.repository.RunHistoryStore
import java.time.LocalDate

/**
 * If the user did not log a run on calendar day D, treat that as a rest/skip day: apply passive
 * recovery when we first open the app on a later day, so metrics move forward consistently.
 *
 * Walks from [MetricsCalendarPreferences.lastReconciledCalendarDate] (exclusive of stale “today”
 * processing) through yesterday; for each day with no [com.example.strivn.data.models.RunLog],
 * applies [TrainingModel.predictPassiveRecoveryDay] once.
 */
object MetricsDayReconciliation {

    fun reconcileIfNeeded(prefs: MetricsCalendarPreferences) {
        val today = LocalDate.now()
        var last = prefs.lastReconciledCalendarDate ?: today
        if (last.isAfter(today)) last = today

        var metrics = InMemoryUserMetricsStore.metrics
        var changed = false
        var d = last
        while (d.isBefore(today)) {
            val hadRun = RunHistoryStore.runsFlow.value.any { run -> run.date == d }
            if (!hadRun) {
                metrics = TrainingModel.predictPassiveRecoveryDay(metrics)
                changed = true
            }
            d = d.plusDays(1)
        }

        if (changed) {
            InMemoryUserMetricsStore.update(metrics)
        }
        prefs.lastReconciledCalendarDate = today
    }
}
