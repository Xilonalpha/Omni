package com.chemscanner.omniscient.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.ui.viewmodels.SovereignEmisarViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EmisarActivity : AppCompatActivity() {
    private val viewModel: SovereignEmisarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EmisarScreen(viewModel, onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmisarScreen(viewModel: SovereignEmisarViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "sweep"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("EMISAR // ROM-3 FUSION", fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black, titleContentColor = Color.Green)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            
            // 1. RADAR VISUALIZER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF001A00))
                    .border(1.dp, Color.Green.copy(0.3f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                RadarCanvas(sweepAngle, uiState.isScanning)
                
                Column(modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
                    Text("SAT: ${uiState.satelliteName}", color = Color.Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("ALT: ${uiState.orbitAltitudeKm} KM", color = Color.White, fontSize = 9.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            // 2. TELEMETRY DATA
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EmisarTelemetryCard("VELOCITY", "${"%.2f".format(uiState.velocityKms)} KM/S", Modifier.weight(1f))
                EmisarTelemetryCard("COHERENCE", "${(uiState.sarCoherence * 100).toInt()}%", Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            // 3. ANOMALY FEED
            Text("SAR TERRAIN ANALYSIS", color = Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Black)
            Surface(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 8.dp),
                color = Color.White.copy(0.05f),
                shape = RoundedCornerShape(16.dp)
            ) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    if (uiState.terrainAnomalies.isEmpty()) {
                        item { Text("No active scan data. Initialize fusion link.", color = Color.Gray, fontSize = 11.sp) }
                    }
                    items(uiState.terrainAnomalies) { anomaly ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(Icons.Default.Radar, null, tint = Color.Red, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(anomaly, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 4. ACTION BUTTON
            Button(
                onClick = { viewModel.initiateSarScan() },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isScanning
            ) {
                if (uiState.isScanning) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.WifiTethering, null)
                    Spacer(Modifier.width(12.dp))
                    Text("INITIATE FUSION SCAN", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun RadarCanvas(sweepAngle: Float, isScanning: Boolean) {
    Canvas(modifier = Modifier.size(200.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 2

        // Circles
        drawCircle(Color.Green.copy(0.1f), radius = radius, style = Stroke(1f))
        drawCircle(Color.Green.copy(0.1f), radius = radius * 0.6f, style = Stroke(1f))
        drawCircle(Color.Green.copy(0.1f), radius = radius * 0.3f, style = Stroke(1f))

        // Sweep
        drawArc(
            brush = Brush.sweepGradient(listOf(Color.Transparent, Color.Green.copy(0.5f))),
            startAngle = sweepAngle - 30f,
            sweepAngle = 30f,
            useCenter = true,
            size = size
        )

        // Scanning "Blips"
        if (isScanning) {
            for (i in 0..3) {
                drawCircle(
                    Color.Red.copy(0.6f),
                    radius = 4f,
                    center = Offset(
                        center.x + (radius * 0.7f * kotlin.math.cos(i * 1.2)).toFloat(),
                        center.y + (radius * 0.7f * kotlin.math.sin(i * 1.2)).toFloat()
                    )
                )
            }
        }
    }
}

@Composable
fun EmisarTelemetryCard(label: String, value: String, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(0.03f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Green.copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
        }
    }
}
