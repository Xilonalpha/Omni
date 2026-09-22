package com.chemscanner.omniscient.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.chemscanner.omniscient.ui.viewmodels.PlanetScannerViewModel
import com.planetscanner.app.data.models.Planet
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlanetScannerActivity : AppCompatActivity() {

    private val viewModel: PlanetScannerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val planetName = intent.getStringExtra("PLANET_DATA")
        if (planetName != null) {
            viewModel.loadSpecificPlanet(planetName)
        }

        setContent {
            PlanetScannerScreen(viewModel, onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanetScannerScreen(viewModel: PlanetScannerViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "planet")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("PLANETARY SURFACE SCANNER", color = Color(0xFF4DB6AC), fontWeight = FontWeight.Black, fontSize = 14.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState())) {
            
            // 1. PLANET VISUALIZER (ORBITAL VIEW)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.radialGradient(listOf(Color(0xFF002B2B), Color.Black)))
                    .border(1.dp, Color(0xFF4DB6AC).copy(0.3f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                PlanetOrbitalCanvas(uiState.detectedPlanet, pulse, uiState.aiBiopsyReport != null)
                
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color(0xFF4DB6AC))
                }
            }

            Spacer(Modifier.height(24.dp))

            // 2. PLANETARY DATA PANEL
            if (uiState.detectedPlanet != null) {
                PlanetDataPanel(uiState.detectedPlanet!!)
                
                Spacer(Modifier.height(16.dp))
                
                // 3. SPECTROSCOPIC ANALYSIS CONTROL
                SpectroscopicPanel(
                    isProgress = uiState.isBiopsyInProgress,
                    progress = uiState.biopsyProgress,
                    onStart = { viewModel.runGeologicalBiopsy() }
                )
                
                if (uiState.aiBiopsyReport != null) {
                    Spacer(Modifier.height(16.dp))
                    BiosignatureReportPanel(uiState.aiBiopsyReport!!)
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // NEW: NASA TRANSMISSION BUTTON
                    Button(
                        onClick = { viewModel.transmitDataToNasa() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isTransmitting
                    ) {
                        if (uiState.isTransmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Send, null)
                            Spacer(Modifier.width(12.dp))
                            Text("TRANSMIT DISCOVERY TO NASA DSN", fontWeight = FontWeight.Black)
                        }
                    }
                    
                    if (uiState.transmissionHash != null) {
                        Text(
                            "AKASHA CONFIRMATION: ${uiState.transmissionHash}",
                            color = Color.Cyan,
                            fontSize = 8.sp,
                            modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally)
                        )
                    }
                }
            } else {
                Surface(color = Color.White.copy(0.05f), shape = RoundedCornerShape(16.dp)) {
                    Text("NO PLANET DATA SYNCED. INITIATE SCAN FROM SURVEYOR.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(24.dp))
                }
            }
        }
    }
}

@Composable
fun PlanetOrbitalCanvas(planet: Planet?, pulse: Float, showHotspots: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "hotspots")
    val hotspotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "alpha"
    )

    Canvas(modifier = Modifier.size(240.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 2.5f
        
        // Atmosphere Glow
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0xFF4DB6AC).copy(0.2f), Color.Transparent)),
            radius = radius * 1.3f * pulse,
            center = center
        )
        
        // Planet Body
        drawCircle(
            color = if(planet != null) Color(0xFF00695C) else Color.DarkGray,
            radius = radius,
            center = center
        )

        // Draw Biosignature Hotspots if analysis is complete
        if (showHotspots) {
            val points = listOf(
                Offset(center.x - 30f, center.y - 40f),
                Offset(center.x + 50f, center.y + 20f),
                Offset(center.x - 10f, center.y + 60f)
            )
            points.forEach { pos ->
                drawCircle(
                    color = Color(0xFF00E676).copy(alpha = hotspotAlpha),
                    radius = 6f,
                    center = pos
                )
                drawCircle(
                    color = Color(0xFF00E676).copy(alpha = hotspotAlpha * 0.3f),
                    radius = 12f,
                    center = pos,
                    style = Stroke(2f)
                )
            }
        }
        
        // Scanner Lidar Ring
        drawCircle(Color(0xFF4DB6AC).copy(0.4f), radius = radius, center = center, style = Stroke(1f))
    }
}

@Composable
fun PlanetDataPanel(planet: Planet) {
    Surface(
        color = Color.White.copy(0.05f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4DB6AC).copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Public, null, tint = Color(0xFF4DB6AC), modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text(planet.name.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
            
            Text(planet.summary, color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(vertical = 8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                PlanetStat("TYPE", planet.type)
                PlanetStat("GRAVITY", "${"%.2f".format(planet.gravity)} G")
                PlanetStat("TEMP", "${planet.temperature.toInt()}°C")
            }
            
            Spacer(Modifier.height(16.dp))
            Text("ATMOSPHERE COMPOSITION:", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (planet.atmosphere.isEmpty()) {
                    Text("Analysis Pending...", color = Color.Yellow, fontSize = 10.sp)
                } else {
                    planet.atmosphere.forEach { gas ->
                        Surface(color = Color(0xFF4DB6AC).copy(0.1f), shape = RoundedCornerShape(4.dp)) {
                            Text(gas, color = Color(0xFF4DB6AC), fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlanetStat(label: String, value: String) {
    Column {
        Text(label, color = Color.Gray, fontSize = 7.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun SpectroscopicPanel(isProgress: Boolean, progress: Float, onStart: () -> Unit) {
    Surface(
        color = Color(0xFF001A1A),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4DB6AC).copy(0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ATMOSPHERIC SPECTROSCOPY", color = Color(0xFF4DB6AC), fontSize = 10.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(12.dp))
            
            if (isProgress) {
                LinearProgressIndicator(progress = { progress }, color = Color(0xFF4DB6AC), trackColor = Color.White.copy(0.05f), modifier = Modifier.fillMaxWidth())
                Text("SCANNING PHOTONS... ${(progress * 100).toInt()}%", color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(top = 4.dp))
            } else {
                Button(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4DB6AC), contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Science, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("INITIATE SPECTRAL ANALYSIS", fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun BiosignatureReportPanel(aiReport: String) {
    Surface(
        color = if (aiReport.contains("BIOLOGICĂ") || aiReport.contains("biologică")) Color(0xFF1A0000) else Color(0xFF001A00),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (aiReport.contains("BIOLOGICĂ") || aiReport.contains("biologică")) Color.Red.copy(0.3f) else Color.Green.copy(0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (aiReport.contains("BIOLOGICĂ") || aiReport.contains("biologică")) Icons.Default.Warning else Icons.Default.DoneAll, 
                    null, 
                    tint = if (aiReport.contains("BIOLOGICĂ") || aiReport.contains("biologică")) Color.Red else Color.Green, 
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (aiReport.contains("BIOLOGICĂ") || aiReport.contains("biologică")) "CRITICAL BIOSIGNATURE DETECTED" else "STABLE ATMOSPHERIC SCAN", 
                    color = if (aiReport.contains("BIOLOGICĂ") || aiReport.contains("biologică")) Color.Red else Color.Green,
                    fontWeight = FontWeight.Black, 
                    fontSize = 12.sp
                )
            }
            
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color.White.copy(0.1f))
            Spacer(Modifier.height(8.dp))
            
            Text("ANA SPECTRAL ANALYSIS:", color = Color.Yellow, fontSize = 8.sp, fontWeight = FontWeight.Black)
            Text(aiReport, color = Color.LightGray, fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
        }
    }
}
