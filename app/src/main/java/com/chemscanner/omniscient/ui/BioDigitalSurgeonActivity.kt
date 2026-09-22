package com.chemscanner.omniscient.ui

import android.content.Context
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
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.ui.viewmodels.BioDigitalSurgeonViewModel
import com.chemscanner.omniscient.ui.viewmodels.NanoSurgicalBot
import com.chemscanner.omniscient.marrow.utils.LanguageManager // FIXED IMPORT
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject // ADDED INJECT
import kotlin.math.*

@AndroidEntryPoint
class BioDigitalSurgeonActivity : AppCompatActivity() {

    private val viewModel: BioDigitalSurgeonViewModel by viewModels()
    @Inject lateinit var languageManager: LanguageManager // ADDED

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BioDigitalSurgeonScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BioDigitalSurgeonScreen(
    viewModel: BioDigitalSurgeonViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val heartbeatScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "heart"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("NANO-MED CONVERGENCE", color = Color(0xFFE91E63), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black))
                        Text("SURGICAL BIOSYNC: ${(uiState.bioSyncLevel * 100).toInt()}%", color = Color.Cyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
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
            
            // 1. CELLULAR SCANNER FIELD (REPAIR INTERFACE)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF050102))
                    .border(1.dp, Color(0xFFE91E63).copy(0.2f), RoundedCornerShape(24.dp))
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            viewModel.deployNanobots(offset)
                        }
                    }
            ) {
                // Blood Stream Visualizer
                BloodStreamCanvas(heartbeatScale)

                // Render Pathogens (Targets)
                uiState.pathogens.forEach { pathogen ->
                    PathogenGraphic(pathogen)
                }

                // Render NanoBots
                uiState.nanobots.forEach { bot ->
                    NanoBotGraphic(bot)
                }

                // Surgical HUD Telemetry
                Column(modifier = Modifier.padding(24.dp)) {
                    MedicalTelemetryRow("CELL INTEGRITY", "${(uiState.cellIntegrity * 100).toInt()}%", if(uiState.cellIntegrity > 0.9) Color.Green else Color.Yellow)
                    MedicalTelemetryRow("PATHOGEN LOAD", "${uiState.pathogens.size} UNITS", Color.Red)
                    MedicalTelemetryRow("NANOBOT STATUS", if(uiState.isOperationActive) "DEPLOYED" else "READY", Color.Cyan)
                }
            }

            // 2. SURGICAL CONTROL PANEL
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                color = Color.Black.copy(alpha = 0.9f),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE91E63).copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MedicalServices, null, tint = Color(0xFFE91E63), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("ANA ASLAN 2.0 - SURGICAL ADVISOR", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                    
                    Spacer(Modifier.height(16.dp))

                    if (uiState.aiMedicalReport != null) {
                        Surface(
                            color = Color.White.copy(0.05f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(bottom = 16.dp).heightIn(max = 120.dp)
                        ) {
                            Text(
                                text = uiState.aiMedicalReport!!,
                                color = Color.LightGray,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        Text(
                            "Tap on the cellular field to deploy nanobots and neutralize pathogens. Surgical precision required.",
                            color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    LinearProgressIndicator(
                        progress = { uiState.cellIntegrity },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = Color(0xFFE91E63),
                        trackColor = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun BloodStreamCanvas(pulse: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Red Blood Cells Background (Blurred)
        for (i in 0..15) {
            drawCircle(
                color = Color(0xFFB71C1C).copy(alpha = 0.1f),
                radius = (40f + i * 5f) * pulse,
                center = Offset(size.width * (i * 0.17f % 1f), size.height * (i * 0.83f % 1f))
            )
        }
    }
}

@Composable
fun PathogenGraphic(pathogen: com.chemscanner.omniscient.ui.viewmodels.Pathogen) {
    Box(
        modifier = Modifier
            .offset(x = pathogen.position.x.dp / 3, y = pathogen.position.y.dp / 3)
            .size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxSize().blur(8.dp).background(Color.Red.copy(0.3f), CircleShape)
        )
        Icon(
            if(pathogen.type == "VIRUS") Icons.Default.Coronavirus else Icons.Default.Warning,
            null, tint = Color.Red, modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun NanoBotGraphic(bot: NanoSurgicalBot) {
    Box(
        modifier = Modifier
            .offset(x = bot.position.x.dp / 3, y = bot.position.y.dp / 3)
            .size(20.dp)
            .background(Color.Cyan, CircleShape)
            .border(1.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(4.dp).background(Color.White, CircleShape))
    }
}

@Composable
fun MedicalTelemetryRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("$label: ", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
}
