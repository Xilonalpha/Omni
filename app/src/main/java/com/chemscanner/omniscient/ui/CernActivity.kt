package com.chemscanner.omniscient.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.chemscanner.omniscient.ui.compose.screens.CernMonitoringScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CernActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CernMonitoringScreen(
                onBack = { finish() }
            )
        }
    }
}
