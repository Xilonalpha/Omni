package com.chemscanner.omniscient.ui

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.chemscanner.omniscient.R
import com.chemscanner.omniscient.marrow.utils.LanguageManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChemicalDetailsActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chemical_details)

        val scanId = intent.getStringExtra("SCAN_ID") ?: "N/A"
        
        val textViewChemicalName = findViewById<TextView>(R.id.textViewChemicalName)
        val textViewScanType = findViewById<TextView>(R.id.textViewScanType)
        val textViewDetails = findViewById<TextView>(R.id.textViewDetails)
        val buttonBack = findViewById<Button>(R.id.buttonBack)

        textViewChemicalName.text = "Scan ID: $scanId"
        textViewScanType.text = "Tip scanare: Identificare Marrow Core"
        textViewDetails.text = "Analiză în curs pentru ID: $scanId. Conexiunea cu Universal Law Engine este stabilă."

        buttonBack.setOnClickListener {
            finish()
        }
    }
}
