package com.chemscanner.omniscient.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Grain
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
import com.chemscanner.omniscient.ui.viewmodels.DarkMatterViewModel
import com.chemscanner.omniscient.ui.viewmodels.GravitationalAnomaly
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.*

@AndroidEntryPoint
class DarkMatterActivity : AppCompatActivity() {

    private val viewModel: DarkMatterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DarkMatterScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarkMatterScreen(
    viewModel: DarkMatterViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "dark_matter")
    val flux by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing)), label = "flux"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DARK MATTER RESONATOR", color = Color(0xFF9C27B0), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black))
                        Text("RESONANCE FREQUENCY: ${"%.1f".format(uiState.resonanceFrequency)} Hz", color = Color.Cyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
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
                    .background(Color(0xFF020005))
                    .border(1.dp, Color(0xFF9C27B0).copy(0.2f), RoundedCornerShape(24.dp))
            ) {
                DarkMatterFieldCanvas(uiState.anomalies, flux)
                
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("ANOMALY DETECTION LOG:", color = Color(0xFF9C27B0), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Text(uiState.detectionLog, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                color = Color.Black.copy(alpha = 0.9f),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF9C27B0).copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Grain, null, tint = Color(0xFF9C27B0), modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("GRAVITATIONAL FLUX SENSORS", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                    
                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = { if (uiState.isScanning) viewModel.stopScan() else viewModel.initiateResonance() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isScanning) Color.Red else Color(0xFF9C27B0),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (uiState.isScanning) "ABORT RESONANCE" else "INITIATE SCAN", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
fun DarkMatterFieldCanvas(anomalies: List<GravitationalAnomaly>, flux: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        anomalies.forEachIndexed { index, anomaly ->
            // Use RA/DEC as relative coordinates (scaled to view)
            val canvasX = ((anomaly.ra % 360) / 360f).toFloat() * w
            val canvasY = (((anomaly.dec + 90) % 180) / 180f).toFloat() * h
            val center = Offset(canvasX, canvasY)
            val magnitude = anomaly.magnitude
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF9C27B0).copy(alpha = 0.2f), Color.Transparent),
                    center = center,
                    radius = 300f * magnitude
                ),
                radius = 300f * magnitude,
                center = center
            )
            
            val path = androidx.compose.ui.graphics.Path()
            path.moveTo(center.x, center.y)
            for (i in 0..360 step 45) {
                val rad = Math.toRadians(i.toDouble())
                val endX = center.x + cos(rad).toFloat() * 200f * sin(flux * PI.toFloat() + index).absoluteValue
                val endY = center.y + sin(rad).toDouble().toFloat() * 200f * sin(flux * PI.toFloat() + index).absoluteValue
                path.quadraticTo(
                    center.x + (endX - center.x) / 2 + 50f * sin(flux * 2 * PI.toFloat()),
                    center.y + (endY - center.y) / 2 + 50f * cos(flux * 2 * PI.toFloat()),
                    endX, endY
                )
            }
            drawPath(path, Color(0xFF9C27B0).copy(alpha = 0.3f), style = Stroke(width = 1f))
        }
    }
}
