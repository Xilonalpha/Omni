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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.ui.viewmodels.NeuroModulatorViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NeuroModulatorActivity : AppCompatActivity() {

    private val viewModel: NeuroModulatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NeuroModulatorScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuroModulatorScreen(
    viewModel: NeuroModulatorViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "neuro")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "pulse"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("NEURO-MODULATOR", color = Color(0xFF7C4DFF), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black))
                        Text("BIO-COHERENCE: ${(uiState.biometricCoherence * 100).toInt()}%", color = Color.White.copy(0.5f), fontSize = 8.sp)
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
            
            // 1. NEURAL NETWORK VISUALIZER (Background)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val color = Color(0xFF7C4DFF).copy(alpha = 0.1f)
                drawCircle(color, radius = 300f * pulse, center = center)
                
                // Draw some neural connections
                for (i in 0..15) {
                    val start = Offset((i * 1234.56f) % size.width, (i * 987.65f) % size.height)
                    val end = center
                    drawLine(color.copy(0.2f), start, end, 1f)
                    drawCircle(color.copy(0.5f), radius = 4f, center = start)
                }
            }

            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                
                // 2. TRANSMITTER SLIDERS
                TransmitterControl(
                    label = "DOPAMINE (REWARD)",
                    value = uiState.transmitters.dopamine,
                    color = Color(0xFFFFD54F),
                    onValueChange = { viewModel.updateDopamine(it) }
                )
                
                Spacer(Modifier.height(16.dp))
                
                TransmitterControl(
                    label = "SEROTONIN (STABILITY)",
                    value = uiState.transmitters.serotonin,
                    color = Color(0xFF4FC3F7),
                    onValueChange = { viewModel.updateSerotonin(it) }
                )

                Spacer(Modifier.height(32.dp))

                // 3. EFFICIENCY GAUGE (Visual Center)
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { uiState.neuralEfficiency },
                            modifier = Modifier.size(180.dp),
                            color = Color(0xFF7C4DFF),
                            trackColor = Color(0xFF7C4DFF).copy(0.1f),
                            strokeWidth = 6.dp
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("NEURAL", color = Color.White.copy(0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("${(uiState.neuralEfficiency * 100).toInt()}%", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                            Text("EFFICIENCY", color = Color.White.copy(0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 4. AI NEURO-REPORT
                if (uiState.aiNeuroReport != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 120.dp).padding(bottom = 16.dp),
                        color = Color.Black.copy(0.7f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C4DFF).copy(0.5f))
                    ) {
                        Text(
                            text = uiState.aiNeuroReport!!,
                            color = Color.White,
                            modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState()),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // 5. OPTIMIZE ACTION
                if (!uiState.isMateriaSufficient) {
                    Text("RESOURCES REQUIRED: SCAN AMINO ACIDS", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp))
                }

                Button(
                    onClick = { viewModel.runNeuralOptimization() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                    enabled = !uiState.isModulating
                ) {
                    if (uiState.isModulating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoGraph, null)
                            Spacer(Modifier.width(8.dp))
                            Text("RUN NEURAL OPTIMIZATION", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransmitterControl(label: String, value: Float, color: Color, onValueChange: (Float) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = color, fontSize = 9.sp, fontWeight = FontWeight.Black)
            Text("${(value * 100).toInt()}%", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color, inactiveTrackColor = color.copy(0.2f))
        )
    }
}
