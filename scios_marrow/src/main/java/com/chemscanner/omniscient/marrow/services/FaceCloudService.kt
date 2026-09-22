package com.chemscanner.omniscient.marrow.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SOVEREIGN FACE CLOUD SERVICE v1.0
 * Descărcă fețele și corpurile jucătorilor reali în format HD pentru randarea volumetrică AR.
 */
@Singleton
class FaceCloudService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: OkHttpClient
) {
    private val cacheDir = File(context.cacheDir, "player_faces")

    init {
        if (!cacheDir.exists()) cacheDir.mkdirs()
    }

    /**
     * Obține imaginea reală a unui jucător (ex: "Mbappe", "Olaru").
     */
    suspend fun getPlayerFace(playerName: String): Bitmap? = withContext(Dispatchers.IO) {
        val fileName = "${playerName.lowercase().replace(" ", "_")}.png"
        val cacheFile = File(cacheDir, fileName)

        if (cacheFile.exists()) {
            return@withContext BitmapFactory.decodeFile(cacheFile.absolutePath)
        }

        // ATENȚIE: "api.sovereign-football.io" este un domeniu placeholder, nu un backend real,
        // deployat. Până nu există un endpoint real (propriu sau al unui furnizor sportiv licențiat)
        // acest apel va eșua mereu (host inexistent) și va returna null, ceea ce e comportamentul
        // corect - NU trebuie înlocuit cu un bitmap hardcodat "de rezervă" care s-ar putea confunda
        // cu o poză reală descărcată. Serviciul nu e apelat momentan de nicio ecran/ViewModel din
        // proiect (cod neconectat) - de folosit doar după ce se configurează un backend real.
        val url = "https://api.sovereign-football.io/v1/faces/$fileName"

        try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bytes = response.body?.bytes() ?: return@withContext null
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    
                    // Salvăm în cache pentru performanță
                    FileOutputStream(cacheFile).use { out ->
                        out.write(bytes)
                    }
                    return@withContext bitmap
                } else {
                    Timber.w("FaceCloudService: HTTP ${response.code} pentru $playerName - endpoint indisponibil, nu s-a găsit o față reală.")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Eroare la descărcarea feței pentru $playerName - fără backend configurat, se returnează null în loc de o imagine falsă.")
        }
        
        return@withContext null
    }
}
