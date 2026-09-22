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


import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Radar

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.marrow.services.VeraRubinAlertService
import com.chemscanner.omniscient.ui.viewmodels.VeraRubinViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VeraRubinActivity : AppCompatActivity() {
    
    @Inject lateinit var veraRubinService: VeraRubinAlertService
    private val viewModel: VeraRubinViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        veraRubinService.startAlertStream()
        setContent {
            VeraRubinScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VeraRubinScreen(
    viewModel: VeraRubinViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "vera")
    val scanY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart), label = "scan"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("VERA RUBIN OBSERVATORY", color = Color(0xFFFFD600), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black))
                        Text("FINK BROKER: LIVE STREAM", color = Color.Green, fontSize = 8.sp)
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
            
            // 1. STAR FIELD (Deep Space)
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(Color.Black)
                for (i in 0..100) {
                    drawCircle(Color.White, radius = 0.8f, center = Offset(size.width * (i * 0.13f % 1f), size.height * (i * 0.77f % 1f)), alpha = 0.5f)
                }
            }

            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                
                // 2. TELESCOPE VIEW (Center)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(24.dp))
                        .background(Color.White.copy(0.02f)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp))) {
                        // Scan line
                        drawLine(
                            Color(0xFFFFD600).copy(0.3f),
                            Offset(0f, size.height * scanY),
                            Offset(size.width, size.height * scanY),
                            strokeWidth = 2f
                        )
                        
                        // Current Alert Marker
                        uiState.latestAlert?.let { alert ->
                            val x = (alert.ra % 360).toFloat() / 360f * size.width
                            val y = (alert.dec + 90).toFloat() / 180f * size.height
                            drawCircle(Color.Red, radius = 10f, center = Offset(x, y))
                            drawCircle(Color.Red.copy(0.3f), radius = 25f, center = Offset(x, y), style = Stroke(1f))
                        }
                    }
                    
                    if (uiState.latestAlert == null) {
                        Text("WAITING FOR TRANSIENT EVENTS...", color = Color.White.copy(0.2f), fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // 3. TELEMETRY PANEL
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(0.05f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("LATEST DETECTION", color = Color(0xFFFFD600), fontSize = 10.sp, fontWeight = FontWeight.Black)
                            
                            // MANUAL SCAN BUTTON
                            Button(
                                onClick = { viewModel.triggerManualScan() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600).copy(alpha = 0.2f), contentColor = Color(0xFFFFD600)),
                                modifier = Modifier.height(30.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                enabled = !uiState.isScanning
                            ) {
                                if (uiState.isScanning) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color(0xFFFFD600), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Radar, null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("MANUAL SCAN", fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        
                        uiState.latestAlert?.let { alert ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                InfoItem("OBJECT ID", alert.objectId)
                                InfoItem("TYPE", alert.type)
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                InfoItem("RA", "%.4f".format(alert.ra))
                                InfoItem("DEC", "%.4f".format(alert.dec))
                                InfoItem("MAG", "%.2f".format(alert.magnitude))
                            }
                        } ?: Text("Scanning the southern sky for supernovae...", color = Color.White.copy(0.5f), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column {
        Text(label, color = Color.White.copy(0.4f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}
