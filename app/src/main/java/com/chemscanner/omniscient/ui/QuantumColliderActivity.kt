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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.ui.viewmodels.QuantumColliderViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.cos
import kotlin.math.sin

@AndroidEntryPoint
class QuantumColliderActivity : AppCompatActivity() {
    private val viewModel: QuantumColliderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuantumColliderScreen(viewModel, onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantumColliderScreen(viewModel: QuantumColliderViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)), label = "rotate"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("QUANTUM COLLIDER", color = Color(0xFF7C4DFF), fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Text("CERN LIVE LINK: ${uiState.realCernStatus}", color = if(uiState.isLhcOperational) Color.Green else Color.Gray, fontSize = 8.sp)
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
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            
            // 1. COLLISION CHAMBER (Visualizer)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.radialGradient(listOf(Color(0xFF1A0033), Color.Black)))
                    .border(1.dp, Color(0xFF7C4DFF).copy(0.3f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                CollisionCanvas(rotation, uiState.isColliding)
                
                if (uiState.isColliding) {
                    Text("ACCELERATING...", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp, modifier = Modifier.blur(1.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // 2. CERN TELEMETRY & PHYSICS DATA
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PhysicsCard("LHC ENERGY", "${uiState.realLhcEnergyTev} TeV", Color.Green, Modifier.weight(1f))
                PhysicsCard("VACUUM", "${(uiState.vacuumStability * 100).toInt()}%", Color.Cyan, Modifier.weight(1f))
                PhysicsCard("COLLISION", "${"%.2f".format(uiState.collisionEnergyPev)} PeV", Color.Yellow, Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            // 3. DISCOVERY LOG (Active Feed)
            Text("PARTICLE DISCOVERY LOG", color = Color(0xFF7C4DFF), fontSize = 12.sp, fontWeight = FontWeight.Black)
            Surface(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 8.dp),
                color = Color.White.copy(0.05f),
                shape = RoundedCornerShape(16.dp)
            ) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    if (uiState.discoveredParticules.isEmpty()) {
                        item { Text("No anomalies detected in the current energy field.", color = Color.Gray, fontSize = 11.sp) }
                    }
                    items(uiState.discoveredParticules.reversed()) { particle ->
                        ParticleItem(particle)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 4. TRIGGER BUTTON
            Button(
                onClick = { viewModel.triggerCollision() },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF), contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isColliding
            ) {
                if (uiState.isColliding) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.FlashOn, null)
                    Spacer(Modifier.width(12.dp))
                    Text("TRIGGER PARTICLE COLLISION", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun CollisionCanvas(rotation: Float, isColliding: Boolean) {
    Canvas(modifier = Modifier.size(240.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 3

        // Magnetic Rings
        drawCircle(Color(0xFF7C4DFF).copy(0.2f), radius = radius, style = Stroke(2.dp.toPx()))
        drawCircle(Color(0xFF7C4DFF).copy(0.1f), radius = radius * 1.5f, style = Stroke(1.dp.toPx()))

        // Beams
        if (isColliding) {
            // Beam Offset logic: used to simulate the "pulse" or wave of particles moving through the ring
            val beamOffset = (rotation % 360) / 360f * (2 * Math.PI.toFloat())
            
            // Beam 1 (Clockwise)
            drawCircle(
                color = Color.Cyan, 
                radius = 6f, 
                center = Offset(
                    center.x + cos(rotation * 0.1f + beamOffset) * radius, 
                    center.y + sin(rotation * 0.1f + beamOffset) * radius
                )
            )
            
            // Beam 2 (Counter-Clockwise)
            drawCircle(
                color = Color.Magenta, 
                radius = 6f, 
                center = Offset(
                    center.x - cos(rotation * 0.1f + beamOffset) * radius, 
                    center.y - sin(rotation * 0.1f + beamOffset) * radius
                )
            )
            
            // Collision Glow at intersection points
            drawCircle(
                brush = Brush.radialGradient(listOf(Color.White.copy(0.4f), Color.Transparent)),
                radius = 20f,
                center = Offset(center.x + radius, center.y)
            )
        }
    }
}

@Composable
fun ParticleItem(particle: com.chemscanner.omniscient.ui.viewmodels.Particle) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth()
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if(particle.isStandardModel) Color.Green else Color.Magenta))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(particle.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("Mass: ${particle.massGev} GeV | Energy: ${particle.energyRequiredPev} PeV", color = Color.Gray, fontSize = 10.sp)
        }
        Spacer(Modifier.weight(1f))
        if (!particle.isStandardModel) {
            Icon(Icons.Default.AutoAwesome, null, tint = Color.Yellow.copy(0.6f), modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
fun PhysicsCard(label: String, value: String, accent: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(0.03f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = accent.copy(0.6f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }
}
