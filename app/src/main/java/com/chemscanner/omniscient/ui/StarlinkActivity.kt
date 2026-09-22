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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.marrow.services.StarlinkMeshService
import com.chemscanner.omniscient.ui.viewmodels.StarlinkViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin

@AndroidEntryPoint
class StarlinkActivity : AppCompatActivity() {
    
    @Inject lateinit var starlinkService: StarlinkMeshService
    private val viewModel: StarlinkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        starlinkService.startMeshAnalysis()
        setContent {
            StarlinkScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarlinkScreen(
    viewModel: StarlinkViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "starlink")
    val orbitRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(40000, easing = LinearEasing), RepeatMode.Restart), label = "rotate"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("STARLINK LASER MESH", color = Color(0xFF00E5FF), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black))
                        Text("SPACE SYNC: ACTIVE", color = Color.Cyan.copy(0.7f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Cyan) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFF020508)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            // 1. ADVANCED SPACE CANVAS
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                
                // Outer Glow
                drawCircle(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF0D47A1).copy(0.3f), Color.Transparent),
                        center = center,
                        radius = 600f
                    ),
                    radius = 600f,
                    center = center
                )

                // The Planet (Earth)
                drawCircle(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF1565C0), Color(0xFF010203)),
                        start = Offset(center.x - 200f, center.y - 200f),
                        end = Offset(center.x + 200f, center.y + 200f)
                    ),
                    radius = 250f,
                    center = center
                )
                
                // Atmosphere Glow
                drawCircle(
                    Color(0xFF4FC3F7).copy(0.2f),
                    radius = 260f,
                    center = center,
                    style = Stroke(4f)
                )

                // Satellite Orbits & Laser Links
                rotate(orbitRotation) {
                    val satelliteCount = 8
                    val orbitRadius = 400f
                    val satellites = mutableListOf<Offset>()

                    for (i in 0 until satelliteCount) {
                        val angle = (i * 2 * Math.PI / satelliteCount).toFloat()
                        val x = center.x + orbitRadius * cos(angle.toDouble()).toFloat()
                        val y = center.y + orbitRadius * sin(angle.toDouble()).toFloat()
                        val pos = Offset(x, y)
                        satellites.add(pos)
                        
                        // Draw Orbit Line
                        drawCircle(Color.White.copy(0.05f), radius = orbitRadius, center = center, style = Stroke(1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)))
                        
                        // Satellite Node
                        drawCircle(Color.Cyan, radius = 4f, center = pos)
                        drawCircle(Color.Cyan.copy(0.3f), radius = 8f * pulseScale, center = pos)
                    }

                    // Laser Interlinks (Lines between satellites)
                    if (uiState.mesh.laserLinkActive) {
                        for (i in 0 until satelliteCount) {
                            val next = (i + 1) % satelliteCount
                            drawLine(
                                color = Color(0xFF00E5FF).copy(0.4f),
                                start = satellites[i],
                                end = satellites[next],
                                strokeWidth = 2f
                            )
                        }
                    }
                }
            }

            // 2. CYBER HUD OVERLAY
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    CyberStat("ACTIVE NODES", "${uiState.mesh.activeNodes}", Color(0xFF00E5FF))
                    CyberStat("LATENCY", "${uiState.mesh.networkLatencyMs}ms", Color(0xFFFFD600))
                    CyberStat("SYNC LEVEL", "${(uiState.mesh.signalCoherence * 100).toInt()}%", Color(0xFF00C853))
                }

                Spacer(Modifier.weight(1f))

                // 3. TELEMETRY PANEL
                Surface(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    color = Color.Black.copy(0.6f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Brush.horizontalGradient(listOf(Color.Cyan.copy(0.5f), Color.Transparent)))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(if(uiState.mesh.laserLinkActive) Color(0xFF00E5FF).copy(0.1f) else Color.Red.copy(0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if(uiState.mesh.laserLinkActive) Icons.Default.Wifi else Icons.Default.WifiOff,
                                null, 
                                tint = if(uiState.mesh.laserLinkActive) Color(0xFF00E5FF) else Color.Red,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("COMMUNICATION PROTOCOL", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if(uiState.mesh.laserLinkActive) "LASER MESH COHERENT" else "RF BACKUP ONLY",
                                color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black
                            )
                            LinearProgressIndicator(
                                progress = { uiState.mesh.signalCoherence },
                                modifier = Modifier.padding(top = 8.dp).fillMaxWidth().height(2.dp),
                                color = Color(0xFF00E5FF),
                                trackColor = Color.White.copy(0.1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CyberStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, color = color, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        Box(modifier = Modifier.width(20.dp).height(2.dp).background(color))
    }
}
