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
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.chemscanner.omniscient.ui.viewmodels.PaleoScannerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaleoScannerActivity : AppCompatActivity() {

    private val viewModel: PaleoScannerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PaleoScannerScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaleoScannerScreen(
    viewModel: PaleoScannerViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "paleo")
    val portalRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Restart), label = "rotate"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PALEO-SCANNER", color = Color(0xFF795548), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black))
                        Text("TEMPORAL ANCHOR: ${uiState.calibrationSource ?: "SCANNING..."}", color = Color.White.copy(0.5f), fontSize = 8.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black.copy(0.8f))
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            // 1. GEOLOGICAL STRATA BACKGROUND
            Canvas(modifier = Modifier.fillMaxSize()) {
                val eraCount = uiState.eraData.size
                if (eraCount > 0) {
                    val stripeHeight = size.height / eraCount
                    uiState.eraData.forEachIndexed { index, _ ->
                        val color = when(index % 3) {
                            0 -> Color(0xFF3E2723)
                            1 -> Color(0xFF4E342E)
                            else -> Color(0xFF5D4037)
                        }
                        drawRect(
                            color = color.copy(alpha = if(index == uiState.currentEraIndex) 0.4f else 0.1f),
                            topLeft = Offset(0f, index * stripeHeight),
                            size = androidx.compose.ui.geometry.Size(size.width, stripeHeight)
                        )
                    }
                }
            }

            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                
                // 2. ERA SELECTOR (Timeline)
                Text("TIMELINE DEPTH (Ma)", color = Color.White.copy(0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(uiState.eraData) { index, era ->
                        val isSelected = uiState.currentEraIndex == index
                        Surface(
                            onClick = { viewModel.changeEra(index) },
                            color = if (isSelected) Color(0xFF795548) else Color.White.copy(0.05f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color.White else Color.White.copy(0.2f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp).width(100.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${era.ageMillions}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                Text(era.name, color = Color.White.copy(0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 3. TEMPORAL PORTAL (Visual Center)
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Portal Rings
                    Box(modifier = Modifier.size(280.dp).rotate(portalRotation)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                brush = Brush.sweepGradient(listOf(Color(0xFF795548), Color.Transparent, Color(0xFF795548))),
                                style = Stroke(width = 4f),
                                radius = size.width / 2
                            )
                        }
                    }
                    
                    if (uiState.isScanning) {
                        CircularProgressIndicator(color = Color(0xFF795548), modifier = Modifier.size(64.dp))
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.HistoryEdu, null, tint = Color.White.copy(0.3f), modifier = Modifier.size(80.dp))
                            Text(uiState.atmosphericViability, color = if(uiState.atmosphericViability.contains("VIABILĂ")) Color.Green else Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 4. ARCHAEOLOGICAL REPORT (AI Content)
                if (uiState.aiHistoricalReport != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 150.dp).padding(bottom = 16.dp),
                        color = Color.Black.copy(0.7f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF795548).copy(0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                            Text("PALEONTOLOGICAL ANALYSIS", color = Color(0xFF795548), fontSize = 9.sp, fontWeight = FontWeight.Black)
                            Spacer(Modifier.height(4.dp))
                            Text(uiState.aiHistoricalReport!!, color = Color.White, fontSize = 12.sp, lineHeight = 18.sp)
                        }
                    }
                }

                // 5. SCAN ACTION
                Button(
                    onClick = { viewModel.startDeepScan() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF795548)),
                    enabled = !uiState.isScanning
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TravelExplore, null)
                        Spacer(Modifier.width(8.dp))
                        Text("INITIATE TEMPORAL PROBE", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
