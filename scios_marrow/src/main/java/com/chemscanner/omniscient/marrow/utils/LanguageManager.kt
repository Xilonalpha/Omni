package com.chemscanner.omniscient.marrow.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE SOVEREIGN LANGUAGE MANAGER v2.2.
 * FIXED: Package name misalignment causing KSP resolution errors.
 */
@Singleton
class LanguageManager @Inject constructor(@ApplicationContext context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "language_prefs"
        private const val KEY_LANGUAGE = "selected_language"
        const val DEFAULT_LANGUAGE = "en"
    }

    fun setLanguage(language: String) {
        prefs.edit().putString(KEY_LANGUAGE, language).apply()
    }

    fun getLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    fun updateContext(baseContext: Context): Context {
        val language = getLanguage()
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(baseContext.resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = android.os.LocaleList(locale)
            config.setLocales(localeList)
        } else {
            @Suppress("DEPRECATION")
            config.setLocale(locale)
        }
        
        return try {
            baseContext.createConfigurationContext(config)
        } catch (e: Exception) {
            baseContext
        }
    }
}
