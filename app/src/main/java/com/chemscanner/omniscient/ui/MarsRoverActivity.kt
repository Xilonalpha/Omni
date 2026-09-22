package com.chemscanner.omniscient.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.chemscanner.omniscient.ui.compose.screens.MarsRoverScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MarsRoverActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MarsRoverScreen(
                onBack = { finish() }
            )
        }
    }
}
