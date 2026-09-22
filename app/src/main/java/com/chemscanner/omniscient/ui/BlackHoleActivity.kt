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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.ui.viewmodels.BlackHoleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.cos
import kotlin.math.sin

@AndroidEntryPoint
class BlackHoleActivity : AppCompatActivity() {
    private val viewModel: BlackHoleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BlackHoleScreen(viewModel, onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlackHoleScreen(viewModel: BlackHoleViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "singularity")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing)), label = "rotate"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SINGULARITY LAB", color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Text("EVENT HORIZON DYNAMICS", color = Color.Red.copy(0.7f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
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
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState())) {
            
            // 1. SCHWARZSCHILD VISUALIZER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.radialGradient(listOf(Color(0xFF1A0000), Color.Black)))
                    .border(1.dp, Color.Red.copy(0.2f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                SingularityCanvas(rotation, uiState.timeDilation.toFloat())
                
                Column(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TIME DILATION FACTOR", color = Color.Gray, fontSize = 8.sp)
                    Text("${"%.6f".format(uiState.timeDilation)}", color = Color.Cyan, fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            // 2. NASA-GRADE METRICS
            uiState.metrics?.let { metrics ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SingularityCard("HORIZON RADIUS", "${"%.2f".format(metrics.schwarzschildRadiusKm)} KM", Color.Red, Modifier.weight(1f))
                    SingularityCard("HAWKING TEMP", "${"%.2E".format(metrics.hawkingTemperatureK)} K", Color.Yellow, Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SingularityCard("ENTROPY", "${"%.2E".format(metrics.entropy)} J/K", Color.Magenta, Modifier.weight(1f))
                    SingularityCard("EVAPORATION", "${"%.2E".format(metrics.evaporationTimeYears)} YR", Color.Green, Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(24.dp))

            // 3. MASS CONTROL
            Text("STELLAR MASS ADJUSTMENT: ${uiState.currentMass.toInt()} M☉", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
            Slider(
                value = uiState.currentMass.toFloat(),
                onValueChange = { viewModel.updateMass(it.toDouble()) },
                valueRange = 1f..100f,
                colors = SliderDefaults.colors(thumbColor = Color.Red, activeTrackColor = Color.Red)
            )

            Spacer(Modifier.height(24.dp))

            // 4. AI SINGULARITY ANALYSIS (ANA ISTLA)
            Button(
                onClick = { viewModel.requestAiDeepAnalysis() },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isAiAnalyzing
            ) {
                if (uiState.isAiAnalyzing) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Psychology, null)
                    Spacer(Modifier.width(12.dp))
                    Text("ANA DEEP SINGULARITY ANALYSIS", fontWeight = FontWeight.Black)
                }
            }

            if (uiState.aiAnalysis != null) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    color = Color.White.copy(0.05f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Yellow.copy(0.3f))
                ) {
                    Text(
                        text = uiState.aiAnalysis!!,
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun SingularityCanvas(rotation: Float, dilation: Float) {
    Canvas(modifier = Modifier.size(280.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val baseRadius = size.width / 6
        
        // Dynamic Dilation Multiplier: High dilation causes the disk to "warp" or expand
        val warpFactor = (1f + (dilation / 1000f)).coerceAtMost(2f)

        // Accretion Disk (Outer) - Now using 'dilation' to affect visual intensity and orbit radius
        for (i in 1..20) {
            val angle = (rotation + i * 18) * (PI.toFloat() / 180f)
            val dist = (baseRadius * 2.5f + i * 2f) * warpFactor
            drawCircle(
                color = Color(0xFFFF4500).copy(alpha = (0.1f * warpFactor).coerceAtMost(0.5f)),
                radius = 2f * warpFactor,
                center = Offset(center.x + cos(angle) * dist, center.y + sin(angle) * dist * 0.3f)
            )
        }

        // Event Horizon Glow - Pulse with dilation
        drawCircle(
            brush = Brush.radialGradient(listOf(Color.White.copy(0.2f * warpFactor), Color.Transparent)),
            radius = baseRadius * 1.5f * warpFactor,
            center = center
        )

        // The Singularity (The Black Hole)
        drawCircle(
            color = Color.Black,
            radius = baseRadius,
            center = center
        )
        
        // Edge highlighting - intensified by dilation
        drawCircle(
            color = Color.White.copy((0.3f * warpFactor).coerceAtMost(1f)),
            radius = baseRadius,
            center = center,
            style = Stroke(1f * warpFactor)
        )
    }
}

@Composable
fun SingularityCard(label: String, value: String, accent: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(0.03f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }
}

private const val PI = 3.14159265358979323846
