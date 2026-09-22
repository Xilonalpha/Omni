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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.ui.viewmodels.WarpDriveViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WarpDriveActivity : AppCompatActivity() {
    private val viewModel: WarpDriveViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WarpDriveScreen(viewModel, onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarpDriveScreen(viewModel: WarpDriveViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "warp_field")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("WARP PROPULSION SYSTEM", color = Color(0xFFBA68C8), fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
            
            // 1. ALCUBIERRE BUBBLE VISUALIZER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF0D001A), Color.Black)))
                    .border(1.dp, Color(0xFFBA68C8).copy(0.3f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                WarpBubbleCanvas(uiState.isFieldActive, uiState.warpFactor, pulse)
                
                if (uiState.isFieldActive) {
                    Text(
                        "WARP FACTOR: ${"%.1f".format(uiState.warpFactor)}",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        modifier = Modifier.graphicsLayer(scaleX = pulse, scaleY = pulse)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // 2. FTL METRICS
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WarpMetricCard("VELOCITY", "${"%.2f".format(uiState.velocityMultipleC)} c", Color.Cyan, Modifier.weight(1f))
                WarpMetricCard("STABILITY", "${(uiState.bubbleStability * 100).toInt()}%", Color.Green, Modifier.weight(1f))
                WarpMetricCard("ANTIMATTER", "${(uiState.antimatterLevel * 100).toInt()}%", Color.Yellow, Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            // 3. NAVIGATION CONTROL
            Text("DESTINATION: ${uiState.destinationSector.uppercase()}", color = Color(0xFFBA68C8), fontSize = 12.sp, fontWeight = FontWeight.Black)
            Slider(
                value = uiState.warpFactor,
                onValueChange = { viewModel.setWarpFactor(it) },
                valueRange = 0f..9.9f,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.isFieldActive,
                colors = SliderDefaults.colors(thumbColor = Color(0xFFBA68C8), activeTrackColor = Color(0xFFBA68C8))
            )

            Spacer(Modifier.height(24.dp))

            // 4. ACTION BUTTON
            Button(
                onClick = { viewModel.toggleWarpField() },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(uiState.isFieldActive) Color.Red.copy(0.8f) else Color(0xFFBA68C8),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(if(uiState.isFieldActive) Icons.Default.PowerSettingsNew else Icons.Default.FlashOn, null)
                Spacer(Modifier.width(12.dp))
                Text(if(uiState.isFieldActive) "COLLAPSE WARP FIELD" else "ENGAGE FTL DRIVE", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun WarpBubbleCanvas(isActive: Boolean, factor: Float, pulse: Float) {
    Canvas(modifier = Modifier.size(250.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 3

        if (isActive) {
            // Space-Time Distorsion Rings
            for (i in 1..5) {
                drawCircle(
                    color = Color(0xFFBA68C8).copy(alpha = 0.2f / i),
                    radius = radius * (1f + (i * 0.2f * factor / 5f)) * pulse,
                    style = Stroke(2f)
                )
            }
            
            // Core Singularity
            drawCircle(
                brush = Brush.radialGradient(listOf(Color.White, Color(0xFFBA68C8), Color.Transparent)),
                radius = radius * 0.4f,
                center = center
            )
        } else {
            drawCircle(Color.Gray.copy(0.1f), radius = radius, style = Stroke(1f))
        }
    }
}

@Composable
fun WarpMetricCard(label: String, value: String, accent: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(0.03f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = accent.copy(0.6f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
        }
    }
}
