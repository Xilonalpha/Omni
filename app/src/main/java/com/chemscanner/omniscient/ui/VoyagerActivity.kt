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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.marrow.services.VoyagerDirectLink
import com.chemscanner.omniscient.ui.viewmodels.VoyagerViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin

@AndroidEntryPoint
class VoyagerActivity : AppCompatActivity() {
    
    @Inject lateinit var voyagerLink: VoyagerDirectLink
    private val viewModel: VoyagerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        voyagerLink.igniteGhostLink()
        setContent {
            VoyagerScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }

    override fun onDestroy() {
        voyagerLink.severGhostLink()
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoyagerScreen(
    viewModel: VoyagerViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "voyager")
    val signalPulse by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart), label = "pulse"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("VOYAGER GHOST LINK", color = Color(0xFF00E5FF), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black))
                        Text(uiState.kernelStatus, color = Color.White.copy(0.5f), fontSize = 8.sp)
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
            
            // 1. SIGNAL PROPAGATION (Background)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height * 0.4f)
                drawCircle(Color(0xFF00E5FF).copy(0.1f), radius = 300f, center = center)
                
                // Signal waves
                for (i in 0..3) {
                    val radius = ((signalPulse + i) % 4) * 200f
                    drawCircle(
                        Color(0xFF00E5FF).copy(alpha = (1f - (radius / 800f)).coerceIn(0f, 0.2f)),
                        radius = radius,
                        center = center,
                        style = Stroke(2f)
                    )
                }
            }

            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                
                // 2. PROBE STATUS (Center)
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SettingsInputAntenna, null, tint = Color(0xFF00E5FF), modifier = Modifier.size(80.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("VOYAGER 1", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text("INTERSTELLAR MISSION", color = Color(0xFF00E5FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // 3. TELEMETRY CARDS
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TelemetryCard(
                        label = "DISTANCE",
                        value = if(uiState.voyagerSignal != null) "${"%.2f".format(uiState.voyagerSignal!!.rangeKm / 1e9)}B KM" else "24.38B KM",
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryCard(
                        label = "POWER",
                        value = if(uiState.voyagerSignal != null) "${uiState.voyagerSignal!!.signalPowerDbm} dBm" else "-160.5 dBm",
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(0.05f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.1f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Wifi, null, tint = Color.Green, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("DSN STATUS", color = Color.White.copy(0.5f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if(uiState.voyagerSignal != null) "LOCKED ON ${uiState.voyagerSignal!!.antenna}" else "PREDICTIVE GHOST LINK ACTIVE",
                                color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TelemetryCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(0.05f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, color = Color(0xFF00E5FF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
        }
    }
}
