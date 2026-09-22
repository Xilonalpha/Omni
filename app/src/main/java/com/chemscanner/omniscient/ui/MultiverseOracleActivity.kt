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
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.ui.viewmodels.MultiverseOracleViewModel
import com.chemscanner.omniscient.ui.viewmodels.TimelineBranch
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.*

@AndroidEntryPoint
class MultiverseOracleActivity : AppCompatActivity() {

    private val viewModel: MultiverseOracleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MultiverseOracleScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiverseOracleScreen(
    viewModel: MultiverseOracleViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "quantum")
    val coherenceAnim by infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "coherence"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MULTIVERSE ORACLE", color = Color(0xFF7C4DFF), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black))
                        Text("QUANTUM COHERENCE: ${(uiState.quantumCoherence * 100).toInt()}%", color = Color.Cyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
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
                    .background(Color(0xFF020105))
                    .border(1.dp, Color(0xFF7C4DFF).copy(0.2f), RoundedCornerShape(24.dp))
            ) {
                // FIXED: Using a temporary progress simulation for UI or adding it to ViewModel if missing
                TimelineCanvas(uiState.activeTimelines, if(uiState.isCalculating) 0.5f else 1.0f, coherenceAnim)
                
                if (!uiState.isCalculating && uiState.activeTimelines.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.AutoMode, null, tint = Color(0xFF7C4DFF).copy(0.5f), modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(uiState.statusMessage, color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                color = Color.Black.copy(alpha = 0.95f),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("EXISTENTIAL DECISION NODE", color = Color.Cyan, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Spacer(Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = uiState.userDecision,
                        onValueChange = { viewModel.onDecisionInput(it) },
                        placeholder = { Text("What if I chose to...", color = Color.DarkGray) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Bold),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7C4DFF),
                            unfocusedBorderColor = Color.DarkGray,
                            cursorColor = Color(0xFF7C4DFF),
                            focusedLabelColor = Color(0xFF7C4DFF),
                            unfocusedLabelColor = Color.DarkGray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isCalculating
                    )

                    if (uiState.aiOracleReport != null) {
                        Surface(
                            color = Color.White.copy(0.05f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            LazyColumn(modifier = Modifier.padding(12.dp).heightIn(max = 150.dp)) {
                                item {
                                    Text(
                                        text = uiState.aiOracleReport!!,
                                        color = Color.LightGray,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.executeMultiverseScan() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = uiState.userDecision.isNotBlank() && !uiState.isCalculating
                    ) {
                        if (uiState.isCalculating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.AutoFixHigh, null)
                            Spacer(Modifier.width(12.dp))
                            Text("SCAN PARALLEL REALITIES", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineCanvas(timelines: List<TimelineBranch>, progress: Float, coherence: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val centerX = w * 0.2f
        val centerY = h * 0.4f
        
        drawCircle(Color.White, radius = 6f, center = Offset(centerX, centerY))
        
        if (progress > 0) {
            timelines.forEachIndexed { index, branch ->
                val branchColor = Color(branch.color)
                val targetY = centerY + (index - 1) * (h * 0.25f)
                val endX = centerX + (w * 0.6f * progress)
                
                val path = Path()
                path.moveTo(centerX, centerY)
                
                val controlX = centerX + (w * 0.3f)
                path.quadraticTo(controlX, targetY, endX, targetY)
                
                drawPath(
                    path = path,
                    color = branchColor.copy(alpha = 0.6f * coherence),
                    style = Stroke(width = 4f * branch.probability)
                )
                
                if (progress >= 1.0f) {
                    drawCircle(
                        brush = Brush.radialGradient(listOf(branchColor, Color.Transparent)),
                        radius = 40f * branch.probability * coherence,
                        center = Offset(endX, targetY),
                        alpha = 0.4f
                    )
                    drawCircle(Color.White, radius = 4f, center = Offset(endX, targetY))
                }
            }
        }
    }
}
