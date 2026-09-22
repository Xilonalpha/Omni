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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.ui.viewmodels.NeuroPhysViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NeuroPhysActivity : AppCompatActivity() {

    private val viewModel: NeuroPhysViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NeuroPhysScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuroPhysScreen(
    viewModel: NeuroPhysViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "neuro")
    val brainPulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "pulse"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("NEUROPHYS LINK", color = Color(0xFFFF5252), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black))
                        Text("INTERFACE BCI: ACTIVE", color = Color.White.copy(0.5f), fontSize = 8.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black.copy(0.9f))
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            // 1. BRAIN WAVE VISUALIZER (Background)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val waveHeight = 100f
                val points = 20
                val brainWaves = uiState.brainWaveState
                
                // Alpha Waves (Focus)
                if (brainWaves != null) {
                    val alphaAlpha = brainWaves.alpha.coerceIn(0.1f, 1f)
                    for (i in 0 until points) {
                        val x = (size.width / points) * i
                        val y = size.height / 2 + (Math.sin(i.toDouble() * 0.5 + System.currentTimeMillis() * 0.005).toFloat() * waveHeight * brainWaves.alpha)
                        drawCircle(Color.Cyan.copy(alpha = alphaAlpha * 0.3f), radius = 5f, center = Offset(x, y))
                    }
                    
                    // Beta Waves (Active Processing)
                    val betaAlpha = brainWaves.beta.coerceIn(0.1f, 1f)
                    for (i in 0 until points) {
                        val x = (size.width / points) * i
                        val y = size.height / 2 + (Math.cos(i.toDouble() * 0.8 + System.currentTimeMillis() * 0.01).toFloat() * waveHeight * 0.5f * brainWaves.beta)
                        drawCircle(Color.Red.copy(alpha = betaAlpha * 0.3f), radius = 3f, center = Offset(x, y))
                    }
                }
            }

            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                
                // 2. SCENARIO SELECTOR
                Text("NEURAL SCENARIOS", color = Color.White.copy(0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.scenarios) { scenario ->
                        val isSelected = uiState.currentScenario?.id == scenario.id
                        Surface(
                            onClick = { viewModel.selectScenario(scenario) },
                            color = if (isSelected) Color(0xFFFF5252).copy(0.2f) else Color.White.copy(0.05f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFFFF5252) else Color.White.copy(0.2f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp).width(120.dp)) {
                                Text(scenario.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(scenario.id.replace("_", " "), color = Color.White.copy(0.5f), fontSize = 8.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // 3. MAIN INTERACTION AREA
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    when (uiState.currentScenario?.id) {
                        "telekinetic_ball" -> {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .scale(brainPulse)
                                    .offset(uiState.objectPositionX.dp, uiState.objectPositionY.dp)
                                    .background(
                                        Brush.radialGradient(listOf(Color.Cyan, Color.Transparent)),
                                        CircleShape
                                    )
                                    .border(2.dp, if (uiState.isMentalControlActive) Color.White else Color.Transparent, CircleShape)
                            )
                        }
                        "electromagnetic_pulse" -> {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { uiState.mentalEnergyCharge },
                                    modifier = Modifier.size(200.dp),
                                    color = Color.Red,
                                    trackColor = Color.Red.copy(0.1f),
                                    strokeWidth = 8.dp
                                )
                                Icon(
                                    Icons.Default.Bolt,
                                    null, 
                                    tint = if (uiState.mentalEnergyCharge > 0.8f) Color.White else Color.Red,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                        else -> {
                            Icon(Icons.Default.Psychology, null, tint = Color.White.copy(0.2f), modifier = Modifier.size(120.dp))
                        }
                    }
                }

                // 4. NEURO METRICS HUD (Bottom)
                Surface(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    color = Color.Black.copy(0.7f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.1f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NeuroMetric("ALPHA (FOCUS)", uiState.brainWaveState?.alpha ?: 0f, Color.Cyan)
                        HorizontalDivider(modifier = Modifier.fillMaxHeight().width(1.dp), color = Color.White.copy(0.1f))
                        NeuroMetric("BETA (LOGIC)", uiState.brainWaveState?.beta ?: 0f, Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun NeuroMetric(label: String, value: Float, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = color.copy(0.7f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { value },
            modifier = Modifier.width(100.dp).height(4.dp).clip(CircleShape),
            color = color,
            trackColor = color.copy(0.2f)
        )
        Text("${(value * 100).toInt()}%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}
