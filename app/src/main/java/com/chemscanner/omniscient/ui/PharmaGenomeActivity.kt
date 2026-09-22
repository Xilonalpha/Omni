package com.chemscanner.omniscient.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.chemscanner.omniscient.ui.compose.screens.PharmaGenomeScreen
import com.chemscanner.omniscient.ui.viewmodels.PharmaGenomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PharmaGenomeActivity : AppCompatActivity() {
    
    private val viewModel: PharmaGenomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val pharmaState by viewModel.pharmaState.collectAsState()
            
            PharmaGenomeScreen(
                state = pharmaState,
                onSearch = { query -> 
                    viewModel.analyzeDrug(query)
                }
            )
        }
    }
}
