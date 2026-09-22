package com.chemscanner.omniscient.marrow.services

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE SOVEREIGN LICENSE GUARD v2.0.
 * NOW WITH CRYPTOGRAPHIC CLIENT VALIDATION.
 */
@Singleton
class MarrowIntegrityGuard @Inject constructor(
    @ApplicationContext private val context: Context,
    private val licenseManager: MarrowLicenseManager
) {
    private val PREFS_NAME = "MARROW_AUTH_CELL"
    private val KEY_ACTIVATED = "is_perpetual_active"
    
    private val GENESIS_TIMESTAMP = 1739190000000L 
    private val EXPIRATION_PERIOD = 30L * 24 * 60 * 60 * 1000 

    /**
     * Check if the module is authorized via time OR unique client key.
     */
    fun isMarrowAuthorized(): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isPerpetual = prefs.getBoolean(KEY_ACTIVATED, false)
        
        if (isPerpetual) return true

        val currentTime = System.currentTimeMillis()
        val expirationTime = GENESIS_TIMESTAMP + EXPIRATION_PERIOD
        
        return currentTime < expirationTime
    }

    /**
     * UNIQUE CLIENT ACTIVATION:
     * Only works if the key matches the client name via SHA-256 Marrow logic.
     */
    fun activateForClient(clientName: String, uniqueKey: String): Boolean {
        if (licenseManager.verifyLicense(clientName, uniqueKey)) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_ACTIVATED, true).apply()
            return true
        }
        return false
    }

    fun getLockoutCode(): String {
        return "MARROW_LOCKOUT_UNAUTHORIZED_USE"
    }
}
