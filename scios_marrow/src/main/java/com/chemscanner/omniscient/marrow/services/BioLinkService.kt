package com.chemscanner.omniscient.marrow.services

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.chemscanner.omniscient.marrow.repository.BiometricVitality
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE ULTRA-LINK PROTOCOL: v5.3.
 * v5.3: FIXED constructor for BiometricVitality.
 */
@Singleton
class BioLinkService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val globalKnowledge: GlobalKnowledgeRepository
) {
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        manager.adapter
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var scanJob: Job? = null

    init {
        if (bluetoothAdapter?.isEnabled == true) {
            checkAndStartBioLink()
        }
    }

    private fun checkAndStartBioLink() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_SCAN
        } else {
            Manifest.permission.ACCESS_FINE_LOCATION
        }

        val hasPermission = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        
        if (hasPermission) {
            startPeriodicUltraScan()
        }
    }

    private fun startPeriodicUltraScan() {
        scanJob?.cancel()
        scanJob = scope.launch {
            while (isActive) {
                startUltraSniffing()
                delay(60000) 
                stopUltraSniffing()
                delay(300000) 
            }
        }
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val deviceName = result.device.name ?: ""
            if (deviceName.contains("Ultra", ignoreCase = true) || deviceName.contains("Watch", ignoreCase = true)) {
                val rawData = result.scanRecord?.bytes
                val heartRate = extractHeartRateFromPacket(rawData)
                if (heartRate > 0) {
                    updateXilonVitality(heartRate)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startUltraSniffing() {
        try {
            bluetoothAdapter?.bluetoothLeScanner?.startScan(scanCallback)
            scope.launch {
                delay(15000)
                stopUltraSniffing()
            }
        } catch (e: Exception) {
            Timber.e(e, "Sniffing failed")
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopUltraSniffing() {
        try { bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback) } catch (_: Exception) {}
    }

    private fun extractHeartRateFromPacket(data: ByteArray?): Int {
        if (data == null || data.size < 20) return 0
        return data[18].toInt() and 0xFF
    }

    private fun updateXilonVitality(hr: Int) {
        scope.launch(Dispatchers.Main) {
            val stress = if (hr > 100) 0.8f else 0.2f
            // REPARAT: Folosim constructorul corect din MarrowDataClasses.kt
            globalKnowledge.updateUserVitality(BiometricVitality(heartRate = hr, stressLevel = stress, symbioticAlignment = 1.0f))
            globalKnowledge.logEvent("BIO_LINK", "Vitalitate: $hr BPM", 2)
        }
    }

    fun shutdown() {
        scanJob?.cancel()
        stopUltraSniffing()
        scope.cancel()
    }
}
