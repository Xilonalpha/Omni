package com.chemscanner.omniscient.marrow.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SOVEREIGN USER SETTINGS REPOSITORY v2.0
 * Migrated to MARROW for SDK independence.
 */
@Singleton
class UserSettingsRepository @Inject constructor(@ApplicationContext context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

    fun getAlergies(): Set<String> {
        return prefs.getStringSet(KEY_ALLERGIES, emptySet()) ?: emptySet()
    }

    fun addAllergy(allergy: String) {
        val currentAllergies = getAlergies().toMutableSet()
        currentAllergies.add(allergy)
        prefs.edit().putStringSet(KEY_ALLERGIES, currentAllergies).apply()
    }

    fun removeAllergy(allergy: String) {
        val currentAllergies = getAlergies().toMutableSet()
        if (currentAllergies.remove(allergy)) {
            prefs.edit().putStringSet(KEY_ALLERGIES, currentAllergies).apply()
        }
    }

    companion object {
        private const val PREFS_FILENAME = "health_profile_prefs"
        private const val KEY_ALLERGIES = "key_allergies"
    }
}
