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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.ui.viewmodels.HighVelocityLabViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.cos
import kotlin.math.sin

@AndroidEntryPoint
class HighVelocityLabActivity : AppCompatActivity() {

    private val viewModel: HighVelocityLabViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HighVelocityLabScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighVelocityLabScreen(
    viewModel: HighVelocityLabViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "alpha"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("QUANTUM VELOCITY LAB", color = Color.Cyan, style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black))
                        Text(uiState.statusReport, color = Color.White.copy(0.5f), fontSize = 8.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black.copy(0.7f))
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            // 1. SPACE BACKGROUND (Stars)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val speedFactor = (uiState.vehicle.velocity / 1000f).coerceIn(1f, 10f)
                for (i in 0..50) {
                    val x = (i * 12345.67f) % size.width
                    // FIXED: Corrected math and distance reference
                    val y = (i * 98765.43f + (uiState.distanceTraveledLy.toFloat() * 1000000f)) % size.height
                    drawCircle(Color.White, radius = 1.5f, center = Offset(x, y), alpha = starAlpha)
                }
            }

            // 2. HUD - FLIGHT DATA
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    HudStat("VELOCITY", "${"%.2f".format(uiState.vehicle.velocity)} km/s", Color.Cyan)
                    HudStat("LORENTZ (γ)", "%.4f".format(uiState.vehicle.lorentzFactor), Color.Magenta)
                }
                
                Spacer(Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    HudStat("DISTANCE", "${"%.6f".format(uiState.distanceTraveledLy)} LY", Color.Green)
                    HudStat("STRESS", "${(uiState.inertialStress * 100).toInt()}%", if (uiState.inertialStress > 0.7f) Color.Red else Color.Yellow)
                }

                Spacer(Modifier.weight(1f))

                // 3. FLIGHT CONTROLS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.applySteering(-5f) },
                        modifier = Modifier.size(64.dp).background(Color.White.copy(0.1f), CircleShape)
                    ) { Icon(Icons.AutoMirrored.Filled.RotateLeft, null, tint = Color.White) }

                    Button(
                        onClick = { viewModel.toggleEngine() },
                        modifier = Modifier.size(100.dp).border(2.dp, if (uiState.isEngineActive) Color.Red else Color.Cyan, CircleShape),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isEngineActive) Color.Red.copy(0.3f) else Color.Cyan.copy(0.3f)
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(if (uiState.isEngineActive) Icons.Default.Stop else Icons.Default.RocketLaunch, null)
                            Text(if (uiState.isEngineActive) "HALT" else "IGNITE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(
                        onClick = { viewModel.applySteering(5f) },
                        modifier = Modifier.size(64.dp).background(Color.White.copy(0.1f), CircleShape)
                    ) { Icon(Icons.AutoMirrored.Filled.RotateRight, null, tint = Color.White) }
                }
                
                Spacer(Modifier.height(40.dp))
            }
            
            // 4. VEHICLE RENDER (Center)
            Box(modifier = Modifier.align(Alignment.Center).size(120.dp), contentAlignment = Alignment.Center) {
                if (uiState.isEngineActive) {
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f, targetValue = 1.5f,
                        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "warp"
                    )
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            brush = Brush.radialGradient(listOf(Color.Cyan.copy(0.4f), Color.Transparent)),
                            radius = (size.width / 2) * scale
                        )
                    }
                }
                
                Icon(
                    Icons.Default.Navigation, 
                    contentDescription = null, 
                    modifier = Modifier.size(48.dp), 
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun HudStat(label: String, value: String, color: Color) {
    Column {
        Text(label, color = color.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
    }
}
