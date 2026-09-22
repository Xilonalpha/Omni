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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.chemscanner.omniscient.ui.viewmodels.NanoForgeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NanoForgeActivity : AppCompatActivity() {

    private val viewModel: NanoForgeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NanoForgeScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NanoForgeScreen(
    viewModel: NanoForgeViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "forge")
    val gridAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f, targetValue = 0.15f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "grid"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("NANO FORGE", color = Color(0xFF00E5FF), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black))
                        Text("STRUCTURAL STABILITY: ${(uiState.stabilityIndex * 100).toInt()}%", color = Color.White.copy(0.5f), fontSize = 8.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearForge() }) {
                        Icon(Icons.Default.DeleteSweep, null, tint = Color.White.copy(0.6f))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black.copy(0.9f))
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            // 1. ATOMIC GRID (Background)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 40.dp.toPx()
                for (i in 0..(size.width / step).toInt()) {
                    drawLine(Color.Cyan.copy(alpha = gridAlpha), start = Offset(i * step, 0f), end = Offset(i * step, size.height))
                }
                for (i in 0..(size.height / step).toInt()) {
                    drawLine(Color.Cyan.copy(alpha = gridAlpha), start = Offset(0f, i * step), end = Offset(size.width, i * step))
                }
            }

            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                
                // 2. MATERIAL SELECTOR
                Text("SELECT ATOM TYPE", color = Color.White.copy(0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.availableMaterials.toList()) { material ->
                        val isSelected = uiState.selectedAtomType == material
                        Surface(
                            onClick = { viewModel.selectAtomType(material) },
                            color = if (isSelected) Color(0xFF00E5FF).copy(0.2f) else Color.White.copy(0.05f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF00E5FF) else Color.White.copy(0.2f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp).width(80.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Adjust, null, tint = if(isSelected) Color(0xFF00E5FF) else Color.White, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.height(4.dp))
                                Text(material, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 3. FORGE CANVAS (Assembly Area)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(24.dp))
                        .background(Color.White.copy(0.02f))
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                viewModel.addAtom(offset)
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        uiState.atoms.forEach { atom ->
                            drawCircle(
                                color = Color(atom.color),
                                radius = 12f * atom.stability,
                                center = atom.position
                            )
                            if (atom.stability < 1f) {
                                drawCircle(
                                    color = Color.Red.copy(alpha = 1f - atom.stability),
                                    radius = 15f,
                                    center = atom.position,
                                    style = Stroke(2f)
                                )
                            }
                        }
                    }
                    
                    if (uiState.atoms.isEmpty()) {
                        Text("TAP TO PLACE ATOMS", color = Color.White.copy(0.2f), modifier = Modifier.align(Alignment.Center), fontWeight = FontWeight.Black)
                    }
                }

                // 4. AI REPORT AREA
                if (uiState.aiMaterialReport != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 120.dp).padding(bottom = 16.dp),
                        color = Color.Black.copy(0.7f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                            Text("NANOTECHNOLOGY ANALYSIS", color = Color(0xFF00E5FF), fontSize = 9.sp, fontWeight = FontWeight.Black)
                            Spacer(Modifier.height(4.dp))
                            Text(uiState.aiMaterialReport!!, color = Color.White, fontSize = 11.sp, lineHeight = 16.sp)
                        }
                    }
                }

                // 5. STRESS TEST ACTION
                Button(
                    onClick = { viewModel.runStressTest() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    enabled = uiState.atoms.isNotEmpty() && !uiState.isTesting
                ) {
                    if (uiState.isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Science, null, tint = Color.Black)
                            Spacer(Modifier.width(8.dp))
                            Text("RUN MOLECULAR STRESS TEST", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
            
            // Critical Vibration Warning
            if (uiState.currentGForce > 1.0f) {
                Surface(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp),
                    color = Color.Red.copy(0.8f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("STRUCTURAL INSTABILITY DETECTED", color = Color.White, modifier = Modifier.padding(8.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
