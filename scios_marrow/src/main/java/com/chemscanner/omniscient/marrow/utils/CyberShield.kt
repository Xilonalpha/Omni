package com.chemscanner.omniscient.marrow.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import com.chemscanner.omniscient.marrow.data.dao.ChemicalDao
import com.chemscanner.omniscient.marrow.data.dao.ScanHistoryDao
import com.chemscanner.omniscient.marrow.data.dao.CachedReactionDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CyberShield @Inject constructor(
    private val chemicalDao: ChemicalDao,
    private val scanHistoryDao: ScanHistoryDao,
    private val cachedReactionDao: CachedReactionDao
) {

    private var dynamicKey: String = ""
    private val shieldScope = CoroutineScope(Dispatchers.IO)
    private val _vulnerabilityCries = MutableSharedFlow<String>()
    val vulnerabilityCries = _vulnerabilityCries.asSharedFlow()

    fun initialize(context: Context) {
        dynamicKey = getSignatureHash(context)
        
        val debugger = detectDebugger()
        val emulator = detectEmulator()
        val rooted = isRooted()

        if (debugger || emulator || rooted) {
            val threatType = when {
                debugger -> "DEBUGGER_ATTACHED"
                emulator -> "EMULATOR_DETECTION"
                rooted -> "ROOT_ACCESS_VIOLATION"
                else -> "UNKNOWN_THREAT"
            }
            System.setProperty("sci_os_integrity", "compromised")
            shieldScope.launch {
                _vulnerabilityCries.emit(threatType)
            }
            executeEmergencyPurge()
        }
    }

    private fun executeEmergencyPurge() {
        shieldScope.launch {
            try {
                chemicalDao.deleteAll()
                scanHistoryDao.deleteAll()
                cachedReactionDao.deleteAll()
            } catch (e: Exception) {}
        }
    }

    fun decrypt(encryptedData: String): String {
        if (!isSystemSecure()) return "INTEGRITY_FAULT"
        return try {
            val keyBytes = dynamicKey.take(16).toByteArray()
            val decoded = Base64.decode(encryptedData, Base64.DEFAULT)
            val output = ByteArray(decoded.size)
            for (i in decoded.indices) {
                output[i] = (decoded[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
            }
            String(output)
        } catch (e: Exception) {
            "DATA_INTEGRITY_ERROR"
        }
    }

    private fun detectDebugger(): Boolean = android.os.Debug.isDebuggerConnected()

    private fun detectEmulator(): Boolean {
        val buildDetails = android.os.Build.FINGERPRINT
        return buildDetails.startsWith("generic") || buildDetails.contains("vbox") || buildDetails.contains("sdk")
    }

    private fun isRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
            "/system/bin/failsafe/su", "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }

    @Suppress("DEPRECATION")
    private fun getSignatureHash(context: Context): String {
        return try {
            val packageName = context.packageName
            val digest = MessageDigest.getInstance("SHA-256")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                val signingInfo = packageInfo.signingInfo
                if (signingInfo != null) {
                    if (signingInfo.hasMultipleSigners()) {
                        val signers = signingInfo.apkContentsSigners
                        if (signers != null && signers.isNotEmpty()) {
                            digest.update(signers[0].toByteArray())
                        }
                    } else {
                        val history = signingInfo.signingCertificateHistory
                        if (history != null && history.isNotEmpty()) {
                            digest.update(history[0].toByteArray())
                        }
                    }
                }
            } else {
                val packageInfo = context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNATURES
                )
                val signatures = packageInfo.signatures
                if (signatures != null && signatures.isNotEmpty()) {
                    digest.update(signatures[0].toByteArray())
                }
            }
            Base64.encodeToString(digest.digest(), Base64.DEFAULT).trim()
        } catch (e: PackageManager.NameNotFoundException) {
            "PACKAGE_NOT_FOUND"
        } catch (e: Exception) {
            "SIGNATURE_UNKNOWN"
        }
    }

    fun isSystemSecure(): Boolean = System.getProperty("sci_os_integrity") != "compromised"
}
