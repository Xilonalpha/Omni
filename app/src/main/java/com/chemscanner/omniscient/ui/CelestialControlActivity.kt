package com.chemscanner.omniscient.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.chemscanner.omniscient.ui.compose.screens.CelestialControlScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CelestialControlActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CelestialControlScreen(
                onBack = { finish() }
            )
        }
    }
}
