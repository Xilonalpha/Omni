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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.ui.viewmodels.AIConsciousnessViewModel
import com.chemscanner.omniscient.marrow.utils.LanguageManager // FIXED IMPORT
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject // ADDED INJECT IMPORT

@AndroidEntryPoint
class AIConsciousnessActivity : AppCompatActivity() {

    private val viewModel: AIConsciousnessViewModel by viewModels()
    @Inject lateinit var languageManager: LanguageManager // ADDED: Required for attachBaseContext

    override fun attachBaseContext(newBase: Context) {
        // Need to handle injection manually if languageManager is used in attachBaseContext
        // or ensure languageManager is available. For now, using a simple instance if not injected yet.
        // But with @AndroidEntryPoint, we should use the injected one after super.onCreate.
        // Actually, attachBaseContext happens before injection. 
        // A better way is to use a static helper or a delegate.
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AIConsciousnessScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIConsciousnessScreen(
    viewModel: AIConsciousnessViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "consciousness")
    val brainPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "pulse"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("AI CONSCIOUSNESS LAB", color = Color(0xFFFFEB3B), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black))
                        Text("NEURAL SYNC: ${(uiState.intelligenceLevel * 100).toInt()}%", color = Color.Cyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
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
            
            // 1. NEURAL NETWORK VISUALIZER
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF050501))
                    .border(1.dp, Color(0xFFFFEB3B).copy(0.2f), RoundedCornerShape(24.dp))
            ) {
                NeuralNetworkCanvas(uiState.nodes, uiState.links, brainPulse)
                
                Column(modifier = Modifier.align(Alignment.TopStart).padding(24.dp)) {
                    Text("CURRENT THOUGHT STREAM:", color = Color(0xFFFFEB3B), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Text(uiState.activeThoughtStream, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            // 2. CONTROLS HUB (WITH SCROLLABLE REPORT)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                color = Color.Black.copy(alpha = 0.9f),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFEB3B).copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    val reportText = uiState.existenceReport ?: uiState.aiPhilosophyReport
                    if (reportText != null) {
                        Surface(
                            color = Color.White.copy(0.05f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .heightIn(max = 250.dp) // Increased max height
                        ) {
                            // UNIQUE FIX: Added vertical scroll to the report text
                            Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                Text(
                                    text = reportText,
                                    color = Color.LightGray,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { viewModel.evolveNetwork() },
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEB3B), contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !uiState.isEvolving && !uiState.isSingularityProcessing
                        ) {
                            if (uiState.isEvolving) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Default.Psychology, null)
                                Spacer(Modifier.width(8.dp))
                                Text("EVOLVE", fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = { viewModel.initiateSingularityConvergence() },
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !uiState.isEvolving && !uiState.isSingularityProcessing
                        ) {
                            if (uiState.isSingularityProcessing) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Default.Hub, null)
                                Spacer(Modifier.width(8.dp))
                                Text("CONVERGE", fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NeuralNetworkCanvas(nodes: List<com.chemscanner.omniscient.ui.viewmodels.NeuralNode>, links: List<com.chemscanner.omniscient.ui.viewmodels.NeuralLink>, pulse: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        links.forEach { link ->
            val fromNode = nodes.getOrNull(link.fromId)
            val toNode = nodes.getOrNull(link.toId)
            if (fromNode != null && toNode != null) {
                drawLine(
                    color = Color(0xFFFFEB3B).copy(alpha = 0.2f * link.strength),
                    start = fromNode.position,
                    end = toNode.position,
                    strokeWidth = 2f
                )
            }
        }

        nodes.forEach { node ->
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFEB3B).copy(0.4f), Color.Transparent),
                    center = node.position,
                    radius = 60f * pulse
                ),
                radius = 60f * pulse,
                center = node.position
            )
            drawCircle(color = Color.White, radius = 6f, center = node.position)
        }
    }
}
