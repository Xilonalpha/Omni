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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.ui.viewmodels.MicroVerseViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.cos
import kotlin.math.sin

@AndroidEntryPoint
class MicroVerseActivity : AppCompatActivity() {

    private val viewModel: MicroVerseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MicroVerseScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicroVerseScreen(
    viewModel: MicroVerseViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "micro")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "pulse"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MICROVERSE EXPLORER", color = Color(0xFF1DE9B6), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black))
                        Text("SCALE: NANOMETRIC 10^-9", color = Color.White.copy(0.5f), fontSize = 8.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleThermalVision() }) {
                        Icon(Icons.Default.Waves, null, tint = if (uiState.isThermalVisionActive) Color.Red else Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black.copy(0.8f))
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    viewModel.scanPoint(offset.x, offset.y)
                }
            }
        ) {
            
            // 1. BIOLOGICAL BACKGROUND (Cellular soup)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val color = if (uiState.isThermalVisionActive) Color.Red.copy(0.1f) else Color(0xFF1DE9B6).copy(0.05f)
                drawCircle(color, radius = 400f * pulse, center = center)
                
                // Draw some "organelles"
                for (i in 0..10) {
                    val angle = (i * 36f) * (Math.PI / 180f).toFloat()
                    val x = center.x + cos(angle) * 200f
                    val y = center.y + sin(angle) * 200f
                    drawCircle(color.copy(0.2f), radius = 30f, center = Offset(x, y))
                }
            }

            // 2. NANOBOTS LAYER
            uiState.activeNanobots.forEach { bot ->
                Box(
                    modifier = Modifier
                        .offset(bot.x.dp, bot.y.dp)
                        .size(8.dp)
                        .background(Color.Cyan, CircleShape)
                        .border(1.dp, Color.White, CircleShape)
                )
            }

            // 3. HUD - SCANNER OVERLAY
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val pos = uiState.probePosition
                    drawLine(Color.Cyan.copy(0.5f), Offset(pos.x - 20, pos.y), Offset(pos.x + 20, pos.y), 1f)
                    drawLine(Color.Cyan.copy(0.5f), Offset(pos.x, pos.y - 20), Offset(pos.x, pos.y + 20), 1f)
                    drawCircle(Color.Cyan.copy(0.2f), radius = 40f, center = pos, style = Stroke(1f))
                }
            }

            // 4. CONTROL PANEL (Bottom)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
            ) {
                // AI Analysis Box (SCROLLABLE)
                if (uiState.aiCellAnalysis != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                            .padding(bottom = 16.dp),
                        color = Color.Black.copy(0.7f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1DE9B6).copy(0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())) {
                            Text("ANA CELL ANALYSIS:", color = Color(0xFF1DE9B6), fontSize = 9.sp, fontWeight = FontWeight.Black)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = uiState.aiCellAnalysis!!,
                                color = Color.White,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MicroControlButton(
                        icon = Icons.Default.PrecisionManufacturing,
                        label = "NANOBOTS",
                        color = Color.Cyan,
                        onClick = { viewModel.deployNanobots(500f, 1000f) }
                    )
                    
                    MicroControlButton(
                        icon = Icons.Default.Vaccines,
                        label = "INJECT",
                        color = Color.Magenta,
                        onClick = { viewModel.injectAntibiotic() },
                        enabled = !uiState.isInjectingAntibiotic
                    )
                    
                    MicroControlButton(
                        icon = Icons.Default.Hub,
                        label = "ANALYSIS",
                        color = Color(0xFF1DE9B6),
                        onClick = { viewModel.analyzeCellWithAi() },
                        loading = uiState.isAiAnalyzing
                    )
                }
            }
            
            if (uiState.isInjectingAntibiotic) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.6f)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color.Magenta)
                        Text("INJECTING COMPOUND...", color = Color.Magenta, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MicroControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = onClick,
            enabled = enabled && !loading,
            modifier = Modifier.size(64.dp).border(1.dp, color.copy(0.5f), CircleShape),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = color.copy(0.2f)),
            contentPadding = PaddingValues(0.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = color, strokeWidth = 2.dp)
            } else {
                Icon(icon, null, tint = color)
            }
        }
        Text(label, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
    }
}
