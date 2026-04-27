package com.example.strivn.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * JWT storage backed by EncryptedSharedPreferences so the token survives restarts.
 *
 * Call [init] once at app startup (e.g. in MainActivity) with applicationContext.
 */
object TokenStore {

    private const val TAG = "STRIVN_AUTH"
    private const val PREFS_NAME = "strivn_secure_prefs"
    private const val KEY_JWT = "jwt_token"

    @Volatile
    private var prefs: SharedPreferences? = null

    @Volatile
    private var inMemoryFallback: String? = null

    fun init(context: Context) {
        if (prefs != null) return
        try {
            val appContext = context.applicationContext
            val masterKey = MasterKey.Builder(appContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            prefs = EncryptedSharedPreferences.create(
                appContext,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
            Log.d(TAG, "TokenStore initialized (EncryptedSharedPreferences)")
        } catch (t: Throwable) {
            // If crypto fails (device / keystore issues), fall back to in-memory so the app can still run.
            prefs = null
            Log.e(TAG, "TokenStore init failed; falling back to in-memory. ${t::class.java.name}: ${t.message}", t)
        }
    }

    var token: String?
        get() {
            val p = prefs
            return if (p != null) {
                p.getString(KEY_JWT, null)
            } else {
                inMemoryFallback
            }
        }
        set(value) {
            val trimmed = value?.trim()?.takeIf { it.isNotEmpty() }
            inMemoryFallback = trimmed
            val p = prefs ?: return
            try {
                p.edit().putString(KEY_JWT, trimmed).apply()
            } catch (t: Throwable) {
                Log.e(TAG, "TokenStore write failed; keeping in-memory only. ${t::class.java.name}: ${t.message}", t)
            }
        }
}
