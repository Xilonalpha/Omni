package com.chemscanner.omniscient.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.chemscanner.omniscient.ui.compose.screens.ScannerScreen
import com.chemscanner.omniscient.ui.viewmodels.ScannerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScannerActivity : AppCompatActivity() {

    private val viewModel: ScannerViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            setupScannerContent()
        } else {
            Toast.makeText(this, "Permisiunea pentru cameră este necesară pentru scanare", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                setupScannerContent()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun setupScannerContent() {
        setContent {
            ScannerScreen(
                viewModel = viewModel,
                onScanResult = { scanId ->
                    val intent = android.content.Intent(this, ChemicalDetailsActivity::class.java).apply {
                        putExtra("SCAN_ID", scanId)
                    }
                    startActivity(intent)
                    finish()
                },
                onBack = { finish() }
            )
        }
    }
}
