package com.chemscanner.omniscient.ui.compose.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chemscanner.omniscient.ui.viewmodels.CernViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CernMonitoringScreen(
    viewModel: CernViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val infiniteTransition = rememberInfiniteTransition(label = "lhc_pulse")
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "pulse"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("CERN LHC MONITOR", fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(Color.Black, Color(0xFF0A0515))))
                .padding(16.dp)
        ) {
            // LHC Status Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(0.05f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if(uiState.isStableBeams) Color.Green.copy(0.4f) else Color.Yellow.copy(0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isStableBeams) Color.Green else Color.Yellow)
                                .graphicsLayer(alpha = pulseAlpha)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isStableBeams) "BEAMS STABLE" else "ADJUSTING...",
                            color = if (uiState.isStableBeams) Color.Green else Color.Yellow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(uiState.lhcStatus, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Metrics Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("BEAM ENERGY", "${"%.2f".format(uiState.beamEnergy)} TeV", Icons.Default.Bolt, Color.Cyan, Modifier.weight(1f))
                MetricCard("LUMINOSITY", "${uiState.luminosity} Hz/ub", Icons.Default.BrightnessHigh, Color.Magenta, Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            // Controls
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.toggleMonitoring(!uiState.isMonitoring) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if(uiState.isMonitoring) Color.Red.copy(0.2f) else Color.Green.copy(0.2f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(if(uiState.isMonitoring) Icons.Default.Stop else Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if(uiState.isMonitoring) "STOP LINK" else "START LHC LINK")
                }

                Button(
                    onClick = { viewModel.runManualAnalysis() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue.copy(0.2f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Science, null)
                    Spacer(Modifier.width(8.dp))
                    Text("ANALYZE")
                }
            }

            Spacer(Modifier.height(24.dp))

            // Events List
            Text("DATA STREAM", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.events) { event ->
                    EventCard(event.description, event.importance, active = uiState.isMonitoring)
                }
            }
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(0.03f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, color.copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text(label, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EventCard(description: String, importance: Int, active: Boolean = true) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(if (active) 0.05f else 0.02f),
        shape = RoundedCornerShape(8.dp),
        border = if (active && importance >= 4) BorderStroke(0.5.dp, Color.Red.copy(0.3f)) else null
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(if (active) 6.dp else 4.dp)
                    .clip(CircleShape)
                    .background(if (importance >= 4) Color.Red else Color.Cyan)
                    .graphicsLayer(alpha = if (active) 1f else 0.5f)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                description, 
                color = if (active) Color.White else Color.White.copy(0.4f), 
                fontSize = 11.sp,
                fontWeight = if (active && importance >= 4) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
