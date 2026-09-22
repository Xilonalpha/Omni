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
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.ui.viewmodels.QuantumHubViewModel
import com.chemscanner.omniscient.ui.viewmodels.Qubit
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.cos
import kotlin.math.sin

@AndroidEntryPoint
class QuantumHubActivity : AppCompatActivity() {

    private val viewModel: QuantumHubViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuantumHubScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuantumHubScreen(
    viewModel: QuantumHubViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "quantum")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart), label = "rotate"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("QUANTUM HUB", color = Color(0xFFE040FB), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black))
                        Text("COHERENCE: ${(uiState.coherenceIntegrity * 100).toInt()}%", color = Color.White.copy(0.5f), fontSize = 8.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                actions = {
                    IconButton(onClick = { viewModel.resetProcessor() }) {
                        Icon(Icons.Default.Refresh, null, tint = Color.White.copy(0.6f))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black.copy(0.9f))
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                
                // 2. QUBIT DISPLAY (Grid)
                Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                    FlowRow(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.Center
                    ) {
                        uiState.qubits.forEach { qubit ->
                            QubitDot(qubit)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // 3. CENTRAL SINGULARITY
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(200.dp).rotate(rotation)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val brush = Brush.sweepGradient(
                                listOf(Color(0xFFE040FB), Color.Transparent, Color(0xFFE040FB))
                            )
                            drawCircle(brush, radius = size.width / 2, style = Stroke(width = 2f))
                        }
                    }
                    
                    if (uiState.isComputing) {
                        CircularProgressIndicator(color = Color(0xFFE040FB), modifier = Modifier.size(64.dp))
                    } else {
                        Icon(Icons.Default.Hub, null, tint = Color.White.copy(0.2f), modifier = Modifier.size(80.dp))
                    }
                }

                // 4. MULTIVERSAL ANALYSIS REPORT
                if (uiState.multiversalAnalysis != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).padding(bottom = 16.dp),
                        color = Color.Black.copy(0.7f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE040FB).copy(0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                            Text("DETERMINISTIC INFERENCE REPORT", color = Color(0xFFE040FB), fontSize = 9.sp, fontWeight = FontWeight.Black)
                            Text("LINKED TO: ${uiState.activeDiscoveryLink}", color = Color.Cyan, fontSize = 8.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(uiState.multiversalAnalysis!!, color = Color.White, fontSize = 11.sp, lineHeight = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QubitDot(qubit: Qubit) {
    val isCollapsed = qubit.label == "COLLAPSED_DATA"
    val color = if (isCollapsed) (if (qubit.state1 > 0.5) Color.Cyan else Color.Magenta) else Color.Gray.copy(0.3f)
    
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(color.copy(0.1f))
            .border(1.dp, color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isCollapsed) (if (qubit.state1 > 0.5) "1" else "0") else "?",
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black
        )
    }
}
