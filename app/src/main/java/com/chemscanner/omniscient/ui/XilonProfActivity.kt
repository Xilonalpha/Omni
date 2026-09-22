package com.chemscanner.omniscient.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.chemscanner.omniscient.ui.compose.screens.XilonProfScreen
import com.chemscanner.omniscient.ui.viewmodels.XilonProfViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class XilonProfActivity : AppCompatActivity() {

    private val viewModel: XilonProfViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            XilonProfScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}
