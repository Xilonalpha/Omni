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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
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
import com.chemscanner.omniscient.ui.viewmodels.BioSonicViewModel
import com.chemscanner.omniscient.ui.viewmodels.SoundWave
import com.chemscanner.omniscient.marrow.utils.LanguageManager // FIXED IMPORT
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.sin

@AndroidEntryPoint
class BioSonicActivity : AppCompatActivity() {

    private val viewModel: BioSonicViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BioSonicScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BioSonicScreen(
    viewModel: BioSonicViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var moleculeInput by remember { mutableStateOf("DNA Helix") }
    
    val infiniteTransition = rememberInfiniteTransition(label = "sonic")
    val waveAnim by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 6.28f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)), label = "wave"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("BIO-SONIC SYMPHONY", color = Color(0xFF00BCD4), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            // 1. SONIC WAVE VISUALIZER
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF000508))
                    .border(1.dp, Color(0xFF00BCD4).copy(0.3f), RoundedCornerShape(24.dp))
            ) {
                SonicWaveCanvas(uiState.activeWaves, waveAnim)
                
                // HUD
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("HARMONY LEVEL", color = Color(0xFF00BCD4), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Text("${(uiState.harmonyLevel * 100).toInt()}% SYNC", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            // 2. CONTROL HUB
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                color = Color.Black.copy(alpha = 0.9f),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00BCD4).copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    OutlinedTextField(
                        value = moleculeInput,
                        onValueChange = { moleculeInput = it },
                        label = { Text("Enter Molecule for Sonification", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00BCD4),
                            unfocusedBorderColor = Color.DarkGray,
                            focusedLabelColor = Color(0xFF00BCD4),
                            unfocusedLabelColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(Modifier.height(16.dp))

                    if (uiState.aiSonicReport != null) {
                        Text(uiState.aiSonicReport!!, color = Color.LightGray, fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(bottom = 16.dp))
                    }

                    Button(
                        onClick = { viewModel.synthesizeMoleculeSound(moleculeInput) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BCD4)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isSynthesizing
                    ) {
                        if (uiState.isSynthesizing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.MusicNote, null)
                            Spacer(Modifier.width(12.dp))
                            Text("SYNTHESIZE SONIC SIGNATURE", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SonicWaveCanvas(waves: List<SoundWave>, offset: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        if (waves.isEmpty()) {
            // Draw idle pulse
            drawCircle(Color(0xFF00BCD4).copy(0.1f), radius = 200f * sin(offset).coerceAtLeast(0.1f), center = Offset(w/2, h/2))
        }

        waves.forEachIndexed { index, wave ->
            val path = androidx.compose.ui.graphics.Path()
            val centerY = h * (0.3f + index * 0.1f)
            path.moveTo(0f, centerY)
            
            for (x in 0..w.toInt() step 5) {
                val y = centerY + sin(x * 0.02f + offset + wave.phase) * (100f * wave.amplitude)
                path.lineTo(x.toFloat(), y)
            }
            
            drawPath(path, Color(0xFF00BCD4).copy(alpha = 0.5f), style = Stroke(width = 2f))
        }
    }
}
