package com.example.strivn.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.strivn.data.models.DailyCheckIn
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

internal fun todayString(): String = dateFormat.format(Date())

/** Today's date in the system default zone (for [com.example.strivn.data.models.RunLog]). */
fun todayLocalDate(): LocalDate = LocalDate.now()

/** Returns days until race date (yyyy-MM-dd), or null if invalid/past. */
fun daysUntilRace(raceDate: String): Int? {
    return try {
        val race = dateFormat.parse(raceDate) ?: return null
        val today = Date()
        if (race.before(today)) return null
        ((race.time - today.time) / (24 * 60 * 60 * 1000)).toInt()
    } catch (_: Exception) { null }
}

private val displayDateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

/** Formatted date for UI display (e.g. "Monday, March 17"). */
fun displayDateForToday(): String = displayDateFormat.format(Date())

/**
 * In-memory store for daily check-ins. One check-in per day.
 * (No database yet.)
 */
object InMemoryCheckInStore {
    private const val PREFS_NAME = "strivn_checkin"
    private const val KEY_LAST_CHECKIN_DATE = "last_checkin_date"
    private const val KEY_CHECKIN_OPENED_DATE = "checkin_opened_date"

    @Volatile
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /** Formatted date for UI display (e.g. "Monday, March 17"). */
    fun getDisplayDateForToday(): String = displayDateForToday()

    private var _checkIns by mutableStateOf<Map<String, DailyCheckIn>>(emptyMap())
        private set

    /** Returns the check-in for today if one exists. */
    fun getCheckInForToday(): DailyCheckIn? = _checkIns[todayString()]

    /** Returns true if a check-in exists for today. */
    fun hasCheckInForToday(): Boolean {
        val today = todayString()
        val stored = prefs?.getString(KEY_LAST_CHECKIN_DATE, null)
        if (stored != null) return stored == today
        return getCheckInForToday() != null
    }

    /** Returns true if user already opened the check-in screen today (used to hide the Home banner). */
    fun hasOpenedCheckInToday(): Boolean {
        val today = todayString()
        return prefs?.getString(KEY_CHECKIN_OPENED_DATE, null) == today
    }

    /** Marks that the user opened check-in today (even if they didn't submit yet). */
    fun markOpenedCheckInToday() {
        val today = todayString()
        prefs?.edit()
            ?.putString(KEY_CHECKIN_OPENED_DATE, today)
            ?.apply()
    }

    /** Saves or updates the check-in for the given date. Replaces any existing one for that date. */
    fun saveOrUpdate(checkIn: DailyCheckIn) {
        _checkIns = _checkIns + (checkIn.date to checkIn)
        prefs?.edit()
            ?.putString(KEY_LAST_CHECKIN_DATE, checkIn.date)
            ?.apply()
    }
}
