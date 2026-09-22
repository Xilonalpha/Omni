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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.ui.viewmodels.TemporalPhysicsViewModel
import com.chemscanner.omniscient.marrow.utils.LanguageManager // FIXED IMPORT
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.*

@AndroidEntryPoint
class TemporalPhysicsActivity : AppCompatActivity() {

    private val viewModel: TemporalPhysicsViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemporalPhysicsScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemporalPhysicsScreen(
    viewModel: TemporalPhysicsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var earthYearsInput by remember { mutableStateOf("10") }
    
    val infiniteTransition = rememberInfiniteTransition(label = "time")
    
    val warpAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "warp"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CHRONOS-DILATION CONSOLE", color = Color.Magenta, style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black))
                        Text("TEMPORAL ACCELERATOR ACTIVE", color = Color.Cyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } },
                actions = {
                    IconButton(onClick = { viewModel.explainRelativityWithAi("Time Dilation") }) {
                        Icon(Icons.Default.AutoAwesome, null, tint = Color.Magenta)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black, titleContentColor = Color.White)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val velocity = uiState.currentVelocityFraction.toFloat()
                
                for (i in 1..8) {
                    val radius = (80 * i).dp.toPx() * (1f + velocity * 2f)
                    drawCircle(
                        color = Color.Magenta.copy(alpha = 0.15f * warpAlpha),
                        radius = radius,
                        center = center,
                        style = Stroke(width = (2f + velocity * 10f))
                    )
                }

                if (velocity > 0.5f) {
                    for (angle in 0..360 step 15) {
                        val rad = Math.toRadians(angle.toDouble())
                        val start = Offset(
                            center.x + cos(rad).toFloat() * 100f,
                            center.y + sin(rad).toFloat() * 100f
                        )
                        val end = Offset(
                            center.x + cos(rad).toFloat() * 1000f,
                            center.y + sin(rad).toFloat() * 1000f
                        )
                        drawLine(
                            brush = Brush.linearGradient(listOf(Color.Transparent, Color.Cyan.copy(alpha = 0.4f))),
                            start = start,
                            end = end,
                            strokeWidth = 1f
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.width(280.dp),
                    color = Color.Black.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Magenta.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CURRENT VELOCITY (V)", color = Color.Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text("${"%.4f".format(uiState.currentVelocityFraction * 100)}% C", 
                            color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                        
                        LinearProgressIndicator(
                            progress = { uiState.currentVelocityFraction.toFloat() },
                            modifier = Modifier.fillMaxWidth().height(4.dp).padding(top = 8.dp),
                            color = Color.Magenta,
                            trackColor = Color.DarkGray
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HourglassEmpty, null, tint = Color.Magenta, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("TWIN PARADOX SIMULATOR", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = 1.sp)
                        }
                        
                        Spacer(Modifier.height(20.dp))
                        
                        OutlinedTextField(
                            value = earthYearsInput,
                            onValueChange = { earthYearsInput = it },
                            placeholder = { Text("Enter years on Earth...", color = Color.DarkGray) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Bold),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Magenta,
                                unfocusedBorderColor = Color.DarkGray,
                                cursorColor = Color.Magenta
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Button(
                            onClick = { viewModel.calculateTwinParadox(earthYearsInput.toDoubleOrNull() ?: 10.0) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) { 
                            Text("EXECUTE CHRONO-CALCULATION", fontWeight = FontWeight.Black) 
                        }

                        if (uiState.twinParadoxResult != null) {
                            Spacer(Modifier.height(24.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                TechResultItem("TRAVELER AGE", "${"%.2f".format(uiState.twinParadoxResult!!.travelerYears)}", "YEARS", Color.Cyan)
                                HorizontalDivider(modifier = Modifier.width(1.dp).height(40.dp), color = Color.White.copy(0.1f))
                                TechResultItem("TIME GAP", "+${"%.2f".format(uiState.twinParadoxResult!!.ageDifference)}", "DILATED", Color.Magenta)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Cyan.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("LORENTZ FACTOR OVERRIDE (γ)", color = Color.Cyan, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                        Slider(
                            value = uiState.currentVelocityFraction.toFloat(),
                            onValueChange = { viewModel.updateVelocity(it.toDouble()) },
                            valueRange = 0f..0.9999f,
                            colors = SliderDefaults.colors(thumbColor = Color.Cyan, activeTrackColor = Color.Cyan)
                        )
                        
                        if (uiState.aiExplanation != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                uiState.aiExplanation!!, 
                                color = Color.White, 
                                fontSize = 14.sp, 
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TechResultItem(label: String, value: String, unit: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = color, fontSize = 8.sp, fontWeight = FontWeight.Black)
        Text(value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Text(unit, color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}
