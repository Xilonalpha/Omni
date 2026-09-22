package com.chemscanner.omniscient.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.ui.viewmodels.SpectralEyeViewModel
import com.chemscanner.omniscient.ui.viewmodels.SpectralWave
import com.chemscanner.omniscient.marrow.utils.LanguageManager // FIXED IMPORT
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.*

@AndroidEntryPoint
class SpectralEyeActivity : AppCompatActivity() {

    private val viewModel: SpectralEyeViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpectralEyeScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpectralEyeScreen(
    viewModel: SpectralEyeViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "spectrum")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "offset"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SPECTRAL EYE VISUALIZER", color = Color(0xFF00E676), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black))
                        Text("EMF SENSORS: ACTIVE", color = Color.Cyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            // 1. SPECTRAL FIELD (The Viral Visual)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF010502))
                    .border(1.dp, Color(0xFF00E676).copy(0.2f), RoundedCornerShape(24.dp))
            ) {
                SpectralCanvas(uiState.waves, waveOffset)
                
                // Sensor Data HUD
                Column(modifier = Modifier.padding(24.dp)) {
                    SpectralTelemetryRow("ACTIVE NODES", "${uiState.activeSensors}", Color.Cyan)
                    SpectralTelemetryRow("ANOMALIES", "${uiState.detectedAnomalies}", Color.Red)
                    SpectralTelemetryRow("FREQUENCY", "2.4GHz - 5.8GHz", Color(0xFF00E676))
                }
            }

            // 2. INFORMATION PANEL (Bottom)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                color = Color.Black.copy(alpha = 0.9f),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Waves, null, tint = Color(0xFF00E676), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("ELECTROMAGNETIC SPECTRUM ANALYSIS", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        "Visualizing invisible data streams (WiFi, Cellular, Cosmic). The patterns represent energy density and packet frequency in your immediate vicinity.",
                        color = Color.LightGray, fontSize = 13.sp, lineHeight = 18.sp
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Wave detail list
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.waves.take(3).forEach { wave ->
                            Surface(
                                color = Color(wave.color).copy(0.1f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(wave.color).copy(0.5f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(wave.type, color = Color(wave.color), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpectralCanvas(waves: List<SpectralWave>, offset: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        waves.forEachIndexed { index, wave ->
            val color = Color(wave.color)
            val centerY = h * (0.3f + index * 0.15f)
            
            // Draw flowing wave
            val path = androidx.compose.ui.graphics.Path()
            path.moveTo(0f, centerY)
            
            for (x in 0..w.toInt() step 10) {
                val y = centerY + sin(x * 0.01f + offset + index) * (50f * wave.intensity)
                path.lineTo(x.toFloat(), y)
            }
            
            drawPath(
                path = path,
                color = color.copy(alpha = 0.4f),
                style = Stroke(width = 2f)
            )
            
            // Particle effect on waves
            for (i in 0..10) {
                val px = (offset * 100 + i * 100) % w
                val py = centerY + sin(px * 0.01f + offset + index) * (50f * wave.intensity)
                drawCircle(color, radius = 3f, center = Offset(px, py), alpha = 0.6f)
            }
        }
    }
}

@Composable
fun SpectralTelemetryRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("$label: ", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
}
