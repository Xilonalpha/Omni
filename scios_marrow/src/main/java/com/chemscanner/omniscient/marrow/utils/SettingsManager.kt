package com.chemscanner.omniscient.marrow.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE ARCHITECT'S SETTINGS: v2.0.
 * Manages local preferences and the Sovereign Integration status.
 */
@Singleton
class SettingsManager @Inject constructor(@ApplicationContext context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("OmniscientPrefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_SAVE_SCANS_AUTOMATICALLY = "save_scans_automatically"
        const val KEY_SOVEREIGN_DISCLAIMER_ACCEPTED = "sovereign_disclaimer_accepted"
    }

    fun setSaveScansAutomatically(shouldSave: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_SAVE_SCANS_AUTOMATICALLY, shouldSave).apply()
    }

    fun shouldSaveScansAutomatically(): Boolean {
        return sharedPreferences.getBoolean(KEY_SAVE_SCANS_AUTOMATICALLY, true)
    }

    // --- SOVEREIGN PROTOCOL ---
    
    fun setDisclaimerAccepted(accepted: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_SOVEREIGN_DISCLAIMER_ACCEPTED, accepted).apply()
    }

    fun isDisclaimerAccepted(): Boolean {
        return sharedPreferences.getBoolean(KEY_SOVEREIGN_DISCLAIMER_ACCEPTED, false)
    }
}
