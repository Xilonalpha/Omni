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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.ui.viewmodels.TerraformingViewModel
import com.chemscanner.omniscient.marrow.utils.LanguageManager // FIXED IMPORT
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.*

@AndroidEntryPoint
class TerraformingActivity : AppCompatActivity() {

    private val viewModel: TerraformingViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TerraformingScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerraformingScreen(
    viewModel: TerraformingViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "planet")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(25000, easing = LinearEasing)), label = "rotation"
    )
    val atmosphereGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Reverse), label = "glow"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PLANETARY TERRAFORMING", color = Color(0xFF4CAF50), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black))
                        Text("HABITABILITY: ${(uiState.habitabilityIndex * 100).toInt()}%", color = Color.Cyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            // 1. CINEMATIC PLANETARY VISUALIZER
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF010205))
                    .border(1.dp, Color(0xFF4CAF50).copy(0.15f), RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Background Stars Layer
                StarFieldCanvas()

                PlanetCanvas(uiState, rotation, atmosphereGlow)
                
                // Telemetry Overlay (Sleeker design)
                Column(modifier = Modifier.align(Alignment.TopStart).padding(32.dp)) {
                    TerraTelemetryRow("TEMP", "${uiState.averageTemperature.toInt()}°C", if(uiState.averageTemperature > 0) Color.Green else Color.Red)
                    TerraTelemetryRow("OXYGEN", "${(uiState.atmosphere.oxygen * 100).toInt()}%", Color.Cyan)
                    TerraTelemetryRow("BIOMASS", "${(uiState.vegetationProgress * 100).toInt()}%", Color(0xFF4CAF50))
                }
            }

            // 2. ENGINEERING CONTROLS
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                color = Color.Black.copy(alpha = 0.95f),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Public, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("GEO-STELAR ENGINEERING TERMINAL", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                    
                    Spacer(Modifier.height(16.dp))

                    if (uiState.aiPlanetaryReport != null) {
                        Surface(
                            color = Color.White.copy(0.05f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(bottom = 16.dp).heightIn(max = 80.dp)
                        ) {
                            Text(
                                text = uiState.aiPlanetaryReport!!,
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { viewModel.triggerFusionIgnition() },
                            modifier = Modifier.weight(1f).height(55.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                            shape = RoundedCornerShape(14.dp),
                            enabled = !uiState.isProcessing
                        ) {
                            Text("FUSION HEAT", fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                        
                        Button(
                            onClick = { viewModel.deployGreenAlgae() },
                            modifier = Modifier.weight(1f).height(55.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(14.dp),
                            enabled = !uiState.isProcessing
                        ) {
                            Text("BIO-DEPLOY", fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }

                        IconButton(
                            onClick = { viewModel.requestAiPlanetaryAnalysis() },
                            modifier = Modifier.background(Color.DarkGray.copy(0.5f), RoundedCornerShape(14.dp)).size(55.dp)
                        ) {
                            Icon(Icons.Default.Insights, null, tint = Color.Cyan)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlanetCanvas(uiState: com.chemscanner.omniscient.ui.viewmodels.TerraformingUiState, rotation: Float, glow: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2
        val cy = size.height / 2
        val radius = size.minDimension * 0.35f // DYNAMIC RADIUS: No more stashing
        
        // Atmosphere Outer Glow
        drawCircle(
            brush = Brush.radialGradient(
                0.8f to Color.Cyan.copy(alpha = glow * uiState.atmosphere.oxygen),
                1.0f to Color.Transparent,
                center = Offset(cx, cy),
                radius = radius * 1.3f
            ),
            radius = radius * 1.3f,
            center = Offset(cx, cy)
        )

        // Planet Body with Texture
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Lerp(Color(0xFF5D4037), Color(0xFF1B5E20), uiState.vegetationProgress),
                    Color.Lerp(Color(0xFF3E2723), Color(0xFF0D330F), uiState.vegetationProgress)
                ),
                start = Offset(cx - radius, cy - radius),
                end = Offset(cx + radius, cy + radius)
            ),
            radius = radius,
            center = Offset(cx, cy)
        )

        // Atmosphere Ring
        drawCircle(
            color = Color.Cyan.copy(alpha = (uiState.atmosphere.oxygen * 0.5f).coerceIn(0.1f, 0.8f)),
            radius = radius + 2f,
            center = Offset(cx, cy),
            style = Stroke(width = 2f)
        )

        // Procedural Continents (Rotating)
        for (i in 0..12) {
            val angle = Math.toRadians((rotation + i * 30).toDouble())
            val px = cx + cos(angle).toFloat() * (radius * 0.5f)
            val py = cy + sin(angle).toFloat() * (radius * 0.5f)
            
            drawCircle(
                color = if(uiState.habitabilityIndex > 0.4) Color.White.copy(0.2f) else Color.Black.copy(0.1f),
                radius = radius * 0.2f,
                center = Offset(px, py),
                alpha = 0.3f
            )
        }
    }
}

@Composable
fun StarFieldCanvas() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val random = java.util.Random(42)
        for (i in 0..100) {
            drawCircle(
                color = Color.White.copy(alpha = random.nextFloat() * 0.5f),
                radius = random.nextFloat() * 2f,
                center = Offset(random.nextFloat() * size.width, random.nextFloat() * size.height)
            )
        }
    }
}

@Composable
fun TerraTelemetryRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("$label: ", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 11.sp, fontWeight = FontWeight.Black)
    }
}

private fun Color.Companion.Lerp(start: Color, stop: Color, fraction: Float): Color {
    return Color(
        red = start.red + (stop.red - start.red) * fraction,
        green = start.green + (stop.green - start.green) * fraction,
        blue = start.blue + (stop.blue - start.blue) * fraction,
        alpha = start.alpha + (stop.alpha - start.alpha) * fraction
    )
}
