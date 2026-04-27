package com.example.strivn.logic

/**
 * Placeholder weather provider for Indoor/Outdoor recommendations.
 * In production, integrate with a real API (OpenWeather, WeatherKit, etc.).
 */
object WeatherProvider {

    /** Returns "Outdoor" or "Indoor" based on conditions. */
    fun getEnvironment(): String {
        // Placeholder: always Outdoor. Replace with real API:
        // val conditions = fetchWeather(lat, lon)
        // if (conditions.isRain || conditions.isSnow || conditions.tempC < -10 || conditions.tempC > 35) return "Indoor"
        return "Outdoor"
    }

    /** For testing: force Indoor when needed. */
    fun getEnvironment(forceIndoor: Boolean): String = if (forceIndoor) "Indoor" else getEnvironment()
}
