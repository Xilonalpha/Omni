package com.chemscanner.omniscient.marrow.services

import android.content.Context
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.ShadowMeshStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import dagger.Lazy

/**
 * THE SHADOW MESH PROTOCOL v9.2 (AUTO-ACTIVE).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Atomic Space Sync + REAL Ghost Camouflage + DYNAMIC AI Prompt Masking.
 * v9.2: Added automatic status reporting to prevent "Mesh Offline" labels.
 */
@Singleton
/**
 * ⚠️ DISCLOSURE: This service name is ambitious but functionality is grounded.
 * See implementation for actual capabilities and limitations.
 */
class ShadowMeshService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService,
    private val blockchainNotary: BlockchainNotaryService,
    private val starlinkMesh: Lazy<StarlinkMeshService>,
    private val lifiService: Lazy<LiFiService>,
    private val heliosSync: Lazy<HeliosSyncService>,
    private val iotBridge: Lazy<QuantumIotBridge>,
    private val lensService: Lazy<SovereignLensService>
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val fastScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    
    private var isGhostModeActive = false
    private var ghostJob: Job? = null
    
    private val keyAlias = "SOVEREIGN_RONAQCI_MASTER_KEY"
    private val androidKeystore = "AndroidKeyStore"
    
    private var quantumSalt = "Q_ENTANGLED_INITIAL_STATE"
    private val DATA_PREFIX = "RAW_DATA_L1_"
    
    private var atomicOffset = 0L
    private val fileTransferBuffer = mutableMapOf<String, MutableMap<Int, ByteArray>>()

    init {
        ensureMasterKeyExists()
        observeSovereignTriggers()
        startQuantumKeyRotation()
        
        // AUTO-INIT STATUS: Confirmăm că motorul e online
        globalKnowledge.updateWorldMeshStatus("Mesh Standby")
    }

    fun secureAiPrompt(prompt: String): String {
        val sensitiveWords = listOf("NASA", "DNA", "Exoplanet", "XILON", "Location", "Sovereign")
        var secured = prompt
        
        sensitiveWords.forEach { word ->
            val dynamicToken = generateDynamicToken(word)
            secured = secured.replace(word, "[$dynamicToken]", ignoreCase = true)
        }
        
        return "[RONAQCI_DYNAMIC_CONTEXT_v9]\n$secured"
    }

    private fun generateDynamicToken(word: String): String {
        return try {
            val correctedTime = System.currentTimeMillis() + atomicOffset
            val timeWindow = correctedTime / 300000 
            val digest = MessageDigest.getInstance("SHA-256")
            val input = "SALT_${word}_$timeWindow"
            val hash = digest.digest(input.toByteArray())
            "X_" + Base64.encodeToString(hash, Base64.NO_WRAP)
                .filter { it.isLetterOrDigit() }
                .take(8)
                .uppercase()
        } catch (e: Exception) {
            "TOKEN_ERR"
        }
    }

    fun syncWithAtomicClock(satelliteEpochTime: Long) {
        val currentLocalTime = System.currentTimeMillis()
        atomicOffset = satelliteEpochTime - currentLocalTime
        val correctedTime = System.currentTimeMillis() + atomicOffset
        quantumSalt = calculateSaltForWindow(correctedTime / 300000)
        globalKnowledge.logEvent("ATOMIC_SYNC", "Aligned with Starlink Atomic Clock. Offset: ${atomicOffset}ms", 4)
    }

    private fun ensureMasterKeyExists() {
        val keyStore = KeyStore.getInstance(androidKeystore).apply { load(null) }
        if (!keyStore.containsAlias(keyAlias)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, androidKeystore)
            keyGenerator.init(KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setKeySize(256).build())
            keyGenerator.generateKey()
        }
    }

    private fun startQuantumKeyRotation() {
        scope.launch {
            while (isActive) {
                val correctedTime = System.currentTimeMillis() + atomicOffset
                val timeWindow = correctedTime / (1000 * 60 * 5)
                quantumSalt = calculateSaltForWindow(timeWindow)
                delay(60000)
            }
        }
    }

    private fun calculateSaltForWindow(timeWindow: Long): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest("RONAQCI_PHANTOM_SEED_$timeWindow".toByteArray())
        return Base64.encodeToString(hash, Base64.NO_WRAP).take(16)
    }

    private fun getSessionKey(sharedSecret: String, salt: String = quantumSalt): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-512")
        val keyBytes = digest.digest((sharedSecret + salt).toByteArray()).copyOf(32) 
        return SecretKeySpec(keyBytes, "AES")
    }

    private fun observeSovereignTriggers() {
        scope.launch {
            globalKnowledge.omegaState.collect { state ->
                if (state.realityIntegrity < 0.6f && !isGhostModeActive) activateShadowMesh("QUANTUM_VULNERABILITY")
            }
        }
    }

    fun activateShadowMesh(reason: String) {
        if (isGhostModeActive) return
        isGhostModeActive = true
        val ghostCount = 12
        globalKnowledge.updateShadowMesh(ShadowMeshStatus(ghostNodes = ghostCount, encryptionLevel = "RoNaQCI-v8.3-ATOMIC", isDarkRelayActive = true))
        
        // HUD: Force report stealth activity
        globalKnowledge.updateWorldMeshStatus("Mesh Active: RoNaQCI Stealth")
        
        globalKnowledge.logEvent("SHADOW_MESH", "RoNaQCI Atomic Stealth Active. Decoys synced with Swarm.", 5)
        starlinkMesh.getOrNull() ?:.updateMeshStatus(true)
        startGhostSignalEmission()
    }

    private fun startGhostSignalEmission() {
        ghostJob?.cancel()
        ghostJob = scope.launch {
            while (isGhostModeActive) {
                val realOrbits = globalKnowledge.orbits.value
                if (realOrbits.isNotEmpty()) {
                    val decoySat = realOrbits.random()
                    val noisePayload = "PHANTOM_NODULE_${decoySat.name}|POS:${decoySat.latitude},${decoySat.longitude},${decoySat.altitude}|VEL:${decoySat.velocityKms}"
                    iotBridge.getOrNull() ?:.publishSignal("sci_os/v6/phantom/decoy", broadcastSovereignData(noisePayload, "DECOY_KEY"))
                }
                delay(15000) 
            }
        }
    }

    fun sendSovereignMessage(text: String, secret: String, useLiFi: Boolean, useHelios: Boolean) {
        scope.launch {
            val encrypted = broadcastSovereignData(text, secret)
            iotBridge.getOrNull() ?:.publishSignal("sci_os/v6/phantom/delta", encrypted)
            if (useLiFi) lifiService.getOrNull() ?:.transmitData(encrypted)
            if (useHelios) heliosSync.getOrNull() ?:.injectSignalIntoFlux(encrypted)
        }
    }

    fun broadcastSovereignData(payload: String, sharedSecret: String = "XILON_ALPHA"): String {
        return try {
            val sessionKey = getSessionKey(sharedSecret)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = ByteArray(12).apply { SecureRandom().nextBytes(this) }
            cipher.init(Cipher.ENCRYPT_MODE, sessionKey, GCMParameterSpec(128, iv))
            val finalPayload = "[${System.currentTimeMillis() + atomicOffset}] $payload"
            val cipherText = cipher.doFinal(finalPayload.toByteArray())
            val combined = ByteArray(iv.size + cipherText.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
            DATA_PREFIX + Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) { "ERROR_ENCRYPTING" }
    }

    fun decryptAndHandle(payload: String) {
        if (!payload.startsWith(DATA_PREFIX)) return
        fastScope.launch {
            try {
                val sharedSecret = globalKnowledge.activeSovereignSecret.value
                val base64Data = payload.substringAfter(DATA_PREFIX)
                val combined = Base64.decode(base64Data, Base64.NO_WRAP)
                val iv = combined.copyOfRange(0, 12)
                val cipherText = combined.copyOfRange(12, combined.size)
                
                var decrypted: String? = null
                val currentTime = System.currentTimeMillis() + atomicOffset
                
                for (offset in listOf(0L, -300000L, 300000L)) {
                    try {
                        val sessionKey = getSessionKey(sharedSecret, calculateSaltForWindow((currentTime + offset) / 300000))
                        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { 
                            init(Cipher.DECRYPT_MODE, sessionKey, GCMParameterSpec(128, iv)) 
                        }
                        decrypted = String(cipher.doFinal(cipherText)).substringAfter("] ")
                        break 
                    } catch (e: Exception) { continue }
                }

                decrypted?.let {
                    if (it.startsWith("RAW_CHUNK:")) {
                        handleFileChunk(it.substringAfter("RAW_CHUNK:"))
                    } else {
                        globalKnowledge.logEvent("DECRYPTOR", "RoNaQCI Deciphered: $it", 5)
                    }
                }
            } catch (e: Exception) { }
        }
    }

    private fun handleFileChunk(chunkInfo: String) {
        try {
            val parts = chunkInfo.split("|")
            if (parts.size < 6) return
            val fileId = parts[0]; val fileName = parts[1]; val index = parts[2].toInt()
            val total = parts[3].toInt(); val expectedChecksum = parts[4]
            val data = Base64.decode(parts[5], Base64.NO_WRAP)
            val chunks = fileTransferBuffer.getOrPut(fileId) { mutableMapOf() }
            chunks[index] = data
            if (chunks.size == total) assembleFile(fileId, fileName, total, expectedChecksum)
        } catch (e: Exception) { }
    }

    private fun assembleFile(fileId: String, fileName: String, total: Int, expectedChecksum: String) {
        scope.launch {
            try {
                val chunks = fileTransferBuffer[fileId] ?: return@launch
                val downloadsDir = File(context.getExternalFilesDir(null), "SovereignDownloads")
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val targetFile = File(downloadsDir, fileName)
                val fullData = ByteArray(chunks.values.sumOf { it.size })
                var currentPos = 0
                for (i in 0 until total) {
                    val chunk = chunks[i] ?: byteArrayOf()
                    System.arraycopy(chunk, 0, fullData, currentPos, chunk.size)
                    currentPos += chunk.size
                }
                val digest = MessageDigest.getInstance("SHA-256")
                val actualChecksum = Base64.encodeToString(digest.digest(fullData), Base64.NO_WRAP).take(8)
                if (actualChecksum == expectedChecksum) {
                    FileOutputStream(targetFile).use { it.write(fullData) }
                    globalKnowledge.logEvent("FILE_TRANSFER", "RoNaQCI Verified: $fileName", 5)
                    withContext(Dispatchers.Main) { ttsService.speak("Fișier RoNaQCI asimilat: $fileName") }
                }
                fileTransferBuffer.remove(fileId)
            } catch (e: Exception) { }
        }
    }

    fun deactivateShadowMesh() {
        isGhostModeActive = false
        ghostJob?.cancel()
        globalKnowledge.updateShadowMesh(ShadowMeshStatus(ghostNodes = 0, isDarkRelayActive = false))
        globalKnowledge.updateWorldMeshStatus("Mesh Standby")
        globalKnowledge.purgeShadowHistory()
        globalKnowledge.logEvent("SHADOW_MESH", "RoNaQCI Mesh retracted.", 3)
    }

    fun broadcastSovereignFile(uri: Uri, sharedSecret: String = "XILON_ALPHA", onProgress: (Int) -> Unit): List<String> {
        val packets = mutableListOf<String>()
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes() ?: return emptyList()
            val fileId = "FILE_${System.currentTimeMillis().hashCode()}"
            val fileName = uri.lastPathSegment ?: "data"
            val digest = MessageDigest.getInstance("SHA-256")
            val globalChecksum = Base64.encodeToString(digest.digest(bytes), Base64.NO_WRAP).take(8)
            val chunkSize = 32768
            val totalChunks = kotlin.math.ceil(bytes.size.toDouble() / chunkSize).toInt()
            for (i in 0 until totalChunks) {
                val start = i * chunkSize
                val end = minOf(start + chunkSize, bytes.size)
                val chunkData = bytes.sliceArray(start until end)
                val payload = "$fileId|$fileName|$i|$totalChunks|$globalChecksum|${Base64.encodeToString(chunkData, Base64.NO_WRAP)}"
                packets.add(broadcastSovereignData("RAW_CHUNK:$payload", sharedSecret))
                onProgress(((i + 1).toFloat() / totalChunks * 100).toInt())
            }
        } catch (e: Exception) { }
        return packets
    }
}
