package com.chemscanner.omniscient.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.chemscanner.omniscient.marrow.utils.LanguageManager // FIXED
import com.chemscanner.omniscient.ui.compose.screens.GreenChemistryScreen
import com.chemscanner.omniscient.ui.viewmodels.GreenChemistryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GreenChemistryActivity : AppCompatActivity() {

    private val viewModel: GreenChemistryViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GreenChemistryScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}
