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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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
import com.chemscanner.omniscient.ui.viewmodels.ParticleGameViewModel
import com.chemscanner.omniscient.ui.viewmodels.ParticleType
import com.chemscanner.omniscient.marrow.utils.LanguageManager // FIXED IMPORT
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AtomicStabilizerActivity : AppCompatActivity() {

    private val viewModel: ParticleGameViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AtomicStabilizerScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtomicStabilizerScreen(
    viewModel: ParticleGameViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ATOMIC STABILIZER", color = Color.Cyan, style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // GAME FIELD
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF020508))
                    .border(1.dp, Color.Cyan.copy(0.2f), RoundedCornerShape(24.dp))
            ) {
                if (!uiState.gameActive) {
                    Button(
                        onClick = { viewModel.startGame() },
                        modifier = Modifier.align(Alignment.Center),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(Modifier.width(8.dp))
                        Text("START STABILIZATION", fontWeight = FontWeight.Black)
                    }
                }

                // Render Particles
                uiState.particles.forEach { particle ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        val color = when (particle.type) {
                            ParticleType.STABLE -> Color.Cyan
                            ParticleType.UNSTABLE -> Color.Red
                            ParticleType.BONUS -> Color.Yellow
                        }
                        
                        Box(
                            modifier = Modifier
                                .offset(
                                    x = (particle.x * 300).dp, // Simplified scaling
                                    y = (particle.y * 500).dp
                                )
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color.copy(0.2f))
                                .border(1.dp, color, CircleShape)
                                .clickable { viewModel.onParticleClick(particle.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when(particle.type) {
                                    ParticleType.STABLE -> "H"
                                    ParticleType.UNSTABLE -> "U"
                                    ParticleType.BONUS -> "Au"
                                },
                                color = color,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // HUD
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GameStat("SCORE", "${uiState.score}")
                GameStat("MULTIPLIER", "x${"%.1f".format(uiState.multiplier)}")
            }
        }
    }
}

@Composable
fun GameStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
    }
}
