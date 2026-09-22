package com.chemscanner.omniscient.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chemscanner.omniscient.marrow.data.models.ScanHistory // FIXED IMPORT
import com.chemscanner.omniscient.databinding.ActivityPreviewBinding
import com.chemscanner.omniscient.marrow.utils.LanguageManager // FIXED IMPORT
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class PreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreviewBinding
    private var scanResult: ScanHistory? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        scanResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("SCAN_RESULT", ScanHistory::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("SCAN_RESULT")
        }

        if (scanResult == null) {
            Toast.makeText(this, "Error: Scan result not found.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        scanResult?.let {
            val imagePath = it.imagePath
            if (imagePath != null) {
                binding.previewImage.setImageURI(Uri.fromFile(File(imagePath)))
            }
            binding.chemicalNameTextView.text = it.chemicalName
        }
    }

    private fun setupClickListeners() {
        binding.arButton.setOnClickListener {
            val smiles = scanResult?.smilesNotation
            if (!smiles.isNullOrEmpty()) {
                val intent = Intent(this, ARActivity::class.java).apply {
                    putExtra("SMILES_STRING", smiles)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "SMILES notation not available for AR.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.stoichiometryButton.setOnClickListener {
            val intent = Intent(this, StoichiometryActivity::class.java).apply {
                putExtra("SCAN_RESULT", scanResult)
            }
            startActivity(intent)
        }

        binding.aiAssistantButton.setOnClickListener {
            val chemicalName = scanResult?.chemicalName
            if (chemicalName != null) {
                val intent = Intent(this, AiAssistantActivity::class.java).apply {
                    putExtra("CHEMICAL_NAME", chemicalName)
                }
                startActivity(intent)
            }
        }

        binding.closeButton.setOnClickListener {
            finish()
        }
    }
}
