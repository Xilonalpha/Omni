package com.chemscanner.omniscient.ui.viewmodels

import android.app.Application
import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.utils.LanguageManager
import com.chemscanner.omniscient.marrow.utils.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * SETTINGS VIEW MODEL v16.1 (ULTIMATE INTEGRITY).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Manage system preferences, backups, and the AI Matrix Keys.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val globalKnowledge: GlobalKnowledgeRepository,
    val languageManager: LanguageManager,
    val settingsManager: SettingsManager
) : AndroidViewModel(application) {

    val availableLanguages = mapOf(
        "English" to "en", "Română" to "ro", "Italiano" to "it",
        "العربية" to "ar", "中文" to "zh", "Português" to "pt", "Español" to "es"
    )

    fun onLanguageSelected(languageCode: String) {
        if (languageManager.getLanguage() != languageCode) {
            languageManager.setLanguage(languageCode)
        }
    }

    /**
     * SAVE CUSTOM KEY:
     * Injects the user-provided API key into the Sovereign Matrix.
     */
    fun saveCustomKey(provider: String, key: String, index: Int) {
        val keyName = if (provider == "GEMINI") "GEMINI_$index" else provider
        globalKnowledge.updateCustomKey(keyName, key)
        Toast.makeText(application, "Cheia $keyName a fost asimilată în matrice.", Toast.LENGTH_SHORT).show()
    }

    fun onSaveScansToggled(shouldSave: Boolean) { 
        settingsManager.setSaveScansAutomatically(shouldSave) 
    }

    fun shouldSaveScans(): Boolean { 
        return settingsManager.shouldSaveScansAutomatically() 
    }

    fun getCurrentLanguageName(): String {
        val currentCode = languageManager.getLanguage()
        return availableLanguages.entries.find { it.value == currentCode }?.key ?: "English"
    }

    /**
     * SOVEREIGN BACKUP (100% RESTORED):
     * Copies the internal database to public Documents folder for portability.
     */
    fun exportDatabase() {
        try {
            val dbFile = application.getDatabasePath("omniscient_database")
            if (dbFile.exists()) {
                val backupDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SCI-OS_Backups")
                if (!backupDir.exists()) backupDir.mkdirs()
                val backupFile = File(backupDir, "akasha_ledger_v9.db")
                
                FileInputStream(dbFile).use { input -> 
                    FileOutputStream(backupFile).use { output -> 
                        input.copyTo(output) 
                    } 
                }
                Toast.makeText(application, "Arhiva Akasha exportată în Documents/SCI-OS_Backups", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) { 
            Toast.makeText(application, "Eroare export: ${e.message}", Toast.LENGTH_SHORT).show() 
        }
    }

    /**
     * SOVEREIGN RESTORE (100% RESTORED):
     * Copies a backup from public storage back to app internal storage.
     */
    fun importDatabase() {
        try {
            val backupFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SCI-OS_Backups/akasha_ledger_v9.db")
            if (backupFile.exists()) {
                val dbFile = application.getDatabasePath("omniscient_database")
                FileInputStream(backupFile).use { input -> 
                    FileOutputStream(dbFile).use { output -> 
                        input.copyTo(output) 
                    } 
                }
                Toast.makeText(application, "Restaurare reușită. Repornește aplicația pentru sincronizare.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(application, "Nu s-a găsit niciun backup.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) { 
            Toast.makeText(application, "Eroare import: ${e.message}", Toast.LENGTH_SHORT).show() 
        }
    }
}
