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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.chemscanner.omniscient.ui.viewmodels.RoboPhysViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RoboPhysActivity : AppCompatActivity() {

    private val viewModel: RoboPhysViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RoboPhysScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoboPhysScreen(
    viewModel: RoboPhysViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "robot")
    val gridAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f, targetValue = 0.15f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "grid"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ROBOPHYS BUILDER", color = Color(0xFF00B0FF), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black))
                        Text("KINETIC STABILITY: ${if(uiState.isStabilityValid) "OPTIMAL" else "WARNING"}", color = if(uiState.isStabilityValid) Color.Green.copy(0.7f) else Color.Red, fontSize = 8.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearDesign() }) {
                        Icon(Icons.Default.DeleteSweep, null, tint = Color.White.copy(0.6f))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black.copy(0.9f))
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            // 1. ENGINEERING GRID (Background)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 40.dp.toPx()
                for (i in 0..(size.width / step).toInt()) {
                    drawLine(Color.Cyan.copy(alpha = gridAlpha), start = androidx.compose.ui.geometry.Offset(i * step, 0f), end = androidx.compose.ui.geometry.Offset(i * step, size.height))
                }
                for (i in 0..(size.height / step).toInt()) {
                    drawLine(Color.Cyan.copy(alpha = gridAlpha), start = androidx.compose.ui.geometry.Offset(0f, i * step), end = androidx.compose.ui.geometry.Offset(size.width, i * step))
                }
            }

            Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState)) {
                
                // 2. CHALLENGE SELECTOR
                Text("MISSION OBJECTIVE", color = Color.White.copy(0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.challenges) { challenge ->
                        val isSelected = uiState.currentChallenge?.id == challenge.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectChallenge(challenge) },
                            label = { Text(challenge.title, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00B0FF),
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                // 3. ASSEMBLY AREA
                Box(
                    modifier = Modifier
                        .height(240.dp)
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(16.dp))
                        .background(Color.White.copy(0.02f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.robotDesign.isEmpty()) {
                        Text("DRAG COMPONENTS HERE", color = Color.White.copy(0.2f), fontWeight = FontWeight.Black)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            uiState.robotDesign.forEach { component ->
                                Surface(
                                    modifier = Modifier.padding(2.dp).width(120.dp),
                                    color = Color(0xFF00B0FF).copy(0.2f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00B0FF).copy(0.5f))
                                ) {
                                    Text(component.name, color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(4.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                }
                            }
                        }
                    }
                    
                    if (!uiState.isStabilityValid) {
                        Surface(
                            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                            color = Color.Red.copy(0.8f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("UNSTABLE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 4. COMPONENT DRAWER
                Text("AVAILABLE COMPONENTS", color = Color.White.copy(0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.availableComponents) { component ->
                        Surface(
                            modifier = Modifier
                                .width(100.dp)
                                .clickable { viewModel.addComponentToRobot(component) },
                            color = Color.White.copy(0.05f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.1f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    when(component.type.name) {
                                        "WHEEL" -> Icons.Default.SettingsInputComponent
                                        "SENSOR" -> Icons.Default.Visibility
                                        "ARM" -> Icons.Default.Hardware
                                        else -> Icons.Default.Extension
                                    },
                                    null, tint = Color(0xFF00B0FF), modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(component.name, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                Text("${component.mass}kg", color = Color.White.copy(0.5f), fontSize = 8.sp)
                            }
                        }
                    }
                }

                // AI Result Drawer (SCROLLABLE)
                if (uiState.aiOptimizationSuggestion != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 180.dp).padding(bottom = 12.dp),
                        color = Color(0xFF00B0FF).copy(0.1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00B0FF).copy(0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())) {
                            Text("ANA KINETIC Sugestion:", color = Color(0xFF00B0FF), fontSize = 9.sp, fontWeight = FontWeight.Black)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = uiState.aiOptimizationSuggestion!!,
                                color = Color.White,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // 5. AI OPTIMIZER BUTTON
                Button(
                    onClick = { viewModel.optimizeDesignWithAi() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B0FF)),
                    enabled = uiState.robotDesign.isNotEmpty() && !uiState.isAiOptimizing
                ) {
                    if (uiState.isAiOptimizing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoFixHigh, null, tint = Color.Black)
                            Spacer(Modifier.width(8.dp))
                            Text("RUN KINETIC AI OPTIMIZER", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
