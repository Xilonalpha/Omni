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
import androidx.compose.ui.draw.alpha
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
import com.chemscanner.omniscient.ui.viewmodels.FusionReactorViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.*

@AndroidEntryPoint
class FusionReactorActivity : AppCompatActivity() {

    private val viewModel: FusionReactorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FusionReactorScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FusionReactorScreen(
    viewModel: FusionReactorViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "plasma")
    val plasmaPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "pulse"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("NUCLEAR FUSION CONTROL", color = Color(0xFFFF5722), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black))
                        Text("TOKAMAK STATUS: ${uiState.reactorStatus}", color = if (uiState.magneticStability > 0.8f) Color.Green else Color.Red, fontSize = 8.sp, fontWeight = FontWeight.Bold)
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
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF080200))
                    .border(1.dp, Color(0xFFFF5722).copy(0.2f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                TokamakCanvas(uiState, plasmaPulse)
                
                Column(modifier = Modifier.align(Alignment.TopStart).padding(24.dp)) {
                    FusionTelemetryRow("TEMP", "${"%.1f".format(uiState.plasmaTemperature)}M K", Color(0xFFFF5722))
                    FusionTelemetryRow("STABILITY", "${(uiState.magneticStability * 100).toInt()}%", Color.Cyan)
                    FusionTelemetryRow("Q-FACTOR", "%.2f".format(uiState.qFactor), if (uiState.qFactor > 1.0) Color.Green else Color.Yellow)
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                color = Color.Black.copy(alpha = 0.9f),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5722).copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BrightnessHigh, null, tint = Color(0xFFFF5722), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("MAGNETIC CONTAINMENT FIELD", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                    
                    Spacer(Modifier.height(16.dp))

                    Text("TARGET FIELD INTENSITY", color = Color.Gray, fontSize = 10.sp)
                    Slider(
                        value = uiState.targetMagneticField,
                        onValueChange = { newValue -> 
                            viewModel.updateMagneticField(newValue) // REPARAT: Acum poți muta bara
                        },
                        colors = SliderDefaults.colors(thumbColor = Color(0xFFFF5722), activeTrackColor = Color(0xFFFF5722))
                    )

                    Spacer(Modifier.height(16.dp))

                    if (uiState.aiDiagnostic != null) {
                        Text(uiState.aiDiagnostic!!, color = Color.LightGray, fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.padding(bottom = 16.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { viewModel.toggleReactor() },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isReactorActive) Color.Red else Color(0xFFFF5722)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (uiState.isReactorActive) "EMERGENCY SHUTDOWN" else "IGNITE PLASMA", fontWeight = FontWeight.Black)
                        }
                        
                        IconButton(
                            onClick = { viewModel.performQuantumDiagnostic() },
                            modifier = Modifier.background(Color.DarkGray, RoundedCornerShape(12.dp)).size(50.dp)
                        ) {
                            Icon(Icons.Default.Analytics, null, tint = Color.Cyan)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TokamakCanvas(uiState: com.chemscanner.omniscient.ui.viewmodels.FusionUiState, pulse: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h * 0.4f
        
        drawCircle(
            color = Color.White.copy(alpha = 0.05f),
            radius = 350f,
            center = Offset(cx, cy),
            style = Stroke(20f)
        )
        
        for (i in 0..12) {
            val angle = (i * 30f).toDouble()
            val rad = Math.toRadians(angle)
            drawLine(
                color = Color.Cyan.copy(alpha = 0.2f * uiState.magneticStability),
                start = Offset(cx + cos(rad).toFloat() * 100f, cy + sin(rad).toFloat() * 100f),
                end = Offset(cx + cos(rad).toFloat() * 400f, cy + sin(rad).toFloat() * 400f),
                strokeWidth = 2f
            )
        }

        if (uiState.plasmaTemperature > 0) {
            val plasmaRadius = (150f * (uiState.plasmaTemperature / 150.0).toFloat()) * pulse
            val color = when {
                uiState.plasmaTemperature > 100 -> Color.White
                uiState.plasmaTemperature > 50 -> Color(0xFFFFEB3B)
                else -> Color(0xFFFF5722)
            }
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color, color.copy(alpha = 0.5f), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = plasmaRadius * 1.5f
                ),
                radius = plasmaRadius * 1.5f,
                center = Offset(cx, cy)
            )
            
            drawCircle(
                color = color,
                radius = plasmaRadius,
                center = Offset(cx, cy),
                style = Stroke(4f)
            )
        }
    }
}

@Composable
fun FusionTelemetryRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("$label: ", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}
