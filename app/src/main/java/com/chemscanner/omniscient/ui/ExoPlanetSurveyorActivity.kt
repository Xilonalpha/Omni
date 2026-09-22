package com.chemscanner.omniscient.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.ui.viewmodels.ExoPlanet
import com.chemscanner.omniscient.ui.viewmodels.ExoPlanetSurveyorViewModel
import com.chemscanner.omniscient.marrow.services.TextToSpeechService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import java.util.Locale

@AndroidEntryPoint
class ExoPlanetSurveyorActivity : AppCompatActivity() {

    private val viewModel: ExoPlanetSurveyorViewModel by viewModels()
    @Inject lateinit var ttsService: TextToSpeechService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExoPlanetSurveyorScreen(
                viewModel = viewModel,
                onBack = { finish() },
                onLand = { planet ->
                    ttsService.speak("Traiectorie calculată pentru ${planet.name}. Inițiez secvența de coborâre atmosferică.")
                    val intent = Intent(this, PlanetScannerActivity::class.java).apply {
                        putExtra("PLANET_DATA", planet.name)
                    }
                    startActivity(intent)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExoPlanetSurveyorScreen(
    viewModel: ExoPlanetSurveyorViewModel,
    onBack: () -> Unit,
    onLand: (ExoPlanet) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("SOVEREIGN EXO-SURVEYOR", color = Color(0xFF81C784), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                },
                actions = {
                    // NEW: SYNC BUTTON
                    IconButton(
                        onClick = { viewModel.syncWithNasa() },
                        enabled = !uiState.isSyncing && !uiState.isScanning
                    ) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Cyan, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.CloudSync, "Sync NASA", tint = Color.Cyan)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF010502)).border(1.dp, Color(0xFF81C784).copy(0.3f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                RadarCanvas(uiState.isScanning || uiState.isSyncing, uiState.scanProgress)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (uiState.isScanning) {
                        Text("SYNCING WITH GLOBAL TELESCOPE NETWORK...", color = Color(0xFF81C784), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    } else if (uiState.isSyncing) {
                        Text("DOWNLOADING NASA CORE DATABASE...", color = Color.Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("TELESCOPE LIVE FEED - AGGREGATED DATA", color = Color.Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(uiState.detectedPlanets) { planet ->
                    SovereignPlanetCard(planet, onLand) 
                }
            }

            if (uiState.aiAnalysis != null) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    color = Color.White.copy(0.05f), 
                    shape = RoundedCornerShape(12.dp), 
                    modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())) {
                        Text("ANA CELESTIAL ANALYSIS", color = Color.Yellow, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(4.dp))
                        Text(text = uiState.aiAnalysis!!, color = Color.LightGray, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.startDeepSpaceScan() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isScanning && !uiState.isSyncing
            ) {
                if (uiState.isScanning) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Rocket, null)
                    Spacer(Modifier.width(12.dp))
                    Text("SCAN DEEP SPACE FOR CANDIDATES", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun SovereignPlanetCard(planet: ExoPlanet, onLand: (ExoPlanet) -> Unit) {
    Surface(
        color = Color.White.copy(0.03f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Public, null, tint = Color(0xFF81C784), modifier = Modifier.size(28.dp).padding(top=4.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(planet.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text(planet.telescopeSource, color = Color.Cyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("HOST STAR: ${planet.hostStar} | TYPE: ${planet.type}", color = Color.Gray, fontSize = 10.sp)
                }
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text("${(planet.habitability * 100).toInt()}% HAB", color = if(planet.habitability > 0.6) Color.Green else Color.Yellow, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    Text("RA: ${"%.2f".format(planet.ra)}", color = Color.Gray, fontSize = 8.sp)
                    Text("DEC: ${"%.2f".format(planet.dec)}", color = Color.Gray, fontSize = 8.sp)
                }
            }
            
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(0.05f))
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                PlanetMetric("SIZE (R_Earth)", "${planet.physics.radius}")
                PlanetMetric("TEMP (Est.)", "${planet.physics.temperature.toInt()}°C")
                PlanetMetric("SOURCE", planet.telescopeSource.split(" ")[0])
            }
            
            Spacer(Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = { onLand(planet) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Cyan),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Cyan.copy(0.5f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.RocketLaunch, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("INITIATE ATMOSPHERIC PROBE", fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun PlanetMetric(label: String, value: String) {
    Column {
        Text(label, color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Black)
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RadarCanvas(isScanning: Boolean, progress: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing)), label = "angle"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = size.minDimension / 2 - 20f
        drawCircle(Color(0xFF81C784).copy(0.1f), radius = radius, style = Stroke(1f))
        drawCircle(Color(0xFF81C784).copy(0.1f), radius = radius * 0.6f, style = Stroke(1f))
        
        if (isScanning) {
            drawCircle(
                color = Color(0xFF81C784).copy(0.2f),
                radius = radius * (if(progress > 0) progress else 0.5f),
                style = Stroke(2f)
            )

            drawArc(
                brush = androidx.compose.ui.graphics.Brush.sweepGradient(0f to Color(0xFF81C784).copy(alpha = 0.5f), 0.2f to Color.Transparent),
                startAngle = angle, sweepAngle = 90f, useCenter = true,
                size = androidx.compose.ui.geometry.Size(size.minDimension - 40f, size.minDimension - 40f),
                topLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius)
            )
        }
    }
}
