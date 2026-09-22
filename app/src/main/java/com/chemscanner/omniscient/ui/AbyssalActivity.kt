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
import com.chemscanner.omniscient.ui.viewmodels.AbyssalViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AbyssalActivity : AppCompatActivity() {
    private val viewModel: AbyssalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AbyssalScreen(viewModel, onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbyssalScreen(viewModel: AbyssalViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "abyssal")
    val depthOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 100f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Reverse), label = "depth"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ABYSSAL DESCENT", color = Color(0xFF00B8D4), fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Text("DEEP SEA BATHYMETRY ANALYZER", color = Color.White.copy(0.5f), fontSize = 8.sp)
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
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState())) {
            
            // 1. SONAR / BATHYMETRIC VISUALIZER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF001219), Color(0xFF000000))))
                    .border(1.dp, Color(0xFF00B8D4).copy(0.3f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                AbyssalSonarCanvas(uiState.currentDepth.toFloat(), uiState.selectedZone?.maxDepth?.toFloat() ?: 10000f, depthOffset)
                
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                    Text("CURRENT DEPTH", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text("${uiState.currentDepth.toInt()} METERS", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(Modifier.height(24.dp))

            // 2. ZONE SELECTION
            Text("SELECT EXPLORATION TRENCH", color = Color(0xFF00B8D4), fontSize = 12.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.zones.forEach { zone ->
                    val isSelected = uiState.selectedZone?.id == zone.id
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if(isSelected) Color(0xFF00B8D4) else Color.White.copy(0.05f))
                            .border(1.dp, if(isSelected) Color.Cyan else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { viewModel.selectZone(zone) },
                        color = Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(zone.name.split(" ")[0], color = if(isSelected) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // 3. REAL-TIME ENVIRONMENTAL DATA
            uiState.selectedZone?.let { zone ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AbyssalStatCard("PRESSURE", "${"%.1f".format(zone.pressure)} ATM", Color.Red, Modifier.weight(1f))
                    AbyssalStatCard("TEMP", "${zone.temperature}°C", Color.Cyan, Modifier.weight(1f))
                    AbyssalStatCard("SALINITY", "${zone.salinity} PSU", Color.Green, Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(24.dp))

            // 4. ACTION BUTTON
            Button(
                onClick = { viewModel.performAbyssalAnalysis() },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B8D4), contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isAnalyzing
            ) {
                if (uiState.isAnalyzing) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Waves, null)
                    Spacer(Modifier.width(12.dp))
                    Text("START BATHYMETRIC ANALYSIS", fontWeight = FontWeight.Black)
                }
            }

            if (uiState.abyssalIntel != null) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    color = Color.White.copy(0.05f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00B8D4).copy(0.3f))
                ) {
                    Text(
                        text = uiState.abyssalIntel!!,
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun AbyssalSonarCanvas(depth: Float, maxDepth: Float, offset: Float) {
    Canvas(modifier = Modifier.size(240.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 2.2f

        // Bathymetry Rings
        for (i in 1..4) {
            drawCircle(
                color = Color(0xFF00B8D4).copy(alpha = 0.1f * i),
                radius = radius * (i / 4f),
                style = Stroke(1f)
            )
        }

        // Depth Line
        val currentDepthRatio = (depth / maxDepth).coerceIn(0f, 1f)
        drawLine(
            color = Color.Cyan,
            start = Offset(center.x - radius, center.y + (radius * currentDepthRatio)),
            end = Offset(center.x + radius, center.y + (radius * currentDepthRatio)),
            strokeWidth = 2f
        )
        
        // Bubbles / Micro-life
        drawCircle(Color.White.copy(0.2f), radius = 2f, center = Offset(center.x - 40f, center.y - 20f + offset))
        drawCircle(Color.White.copy(0.2f), radius = 3f, center = Offset(center.x + 60f, center.y + 40f - offset))
    }
}

@Composable
fun AbyssalStatCard(label: String, value: String, accent: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(0.03f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }
}
