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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.ui.viewmodels.NeuroBioViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.cos
import kotlin.math.sin

@AndroidEntryPoint
class NeuroBioActivity : AppCompatActivity() {

    private val viewModel: NeuroBioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NeuroBioScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuroBioScreen(
    viewModel: NeuroBioViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "weaver")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "pulse"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("NEURO-BIO WEAVER", color = Color(0xFFFFAB40), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black))
                        Text("NEURAL ENERGY: ${uiState.availableEnergy.toInt()} mJ", color = Color.White.copy(0.5f), fontSize = 8.sp)
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
            
            // 1. SYNAPTIC WEB (Background)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val color = Color(0xFFFFAB40).copy(alpha = 0.1f)
                val center = Offset(size.width / 2, size.height / 2)
                
                for (i in 0..12) {
                    val angle = (i * 30f) * (Math.PI / 180f).toFloat()
                    val start = Offset(
                        center.x + cos(angle) * 100f,
                        center.y + sin(angle) * 100f
                    )
                    val end = Offset(
                        center.x + cos(angle) * 400f * pulseScale,
                        center.y + sin(angle) * 400f * pulseScale
                    )
                    drawLine(color.copy(0.3f), start, end, 2f)
                    drawCircle(color.copy(0.6f), radius = 6f, center = end)
                }
                drawCircle(color, radius = 120f, center = center, style = Stroke(2f))
            }

            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                
                // 2. NEURON MODEL SELECTOR
                Text("SELECT NEURON TYPE", color = Color.White.copy(0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.availableNeurons) { neuron ->
                        val isSelected = uiState.currentNeuron?.id == neuron.id
                        Surface(
                            onClick = { viewModel.selectNeuron(neuron) },
                            color = if (isSelected) Color(0xFFFFAB40).copy(0.2f) else Color.White.copy(0.05f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFFFFAB40) else Color.White.copy(0.2f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp).width(110.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Adjust, null, tint = if(isSelected) Color(0xFFFFAB40) else Color.White, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.height(8.dp))
                                Text(neuron.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(neuron.type, color = Color.White.copy(0.5f), fontSize = 8.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 3. WEAVING CONSOLE (Input)
                var requestText by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = requestText,
                    onValueChange = { requestText = it },
                    label = { Text("Describe Neural Path (e.g. Cognitive Shield)", color = Color.White.copy(0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFAB40),
                        unfocusedBorderColor = Color.White.copy(0.2f),
                        cursorColor = Color(0xFFFFAB40),
                        focusedLabelColor = Color(0xFFFFAB40),
                        unfocusedLabelColor = Color.White.copy(0.5f),
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
                )

                Spacer(Modifier.height(16.dp))

                // 4. GENERATED ARCHITECTURE
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (uiState.aiNeuralDesign != null) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Color.Black.copy(0.6f),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFAB40).copy(0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Architecture, null, tint = Color(0xFFFFAB40), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("SYNAPTIC BLUEPRINT", color = Color(0xFFFFAB40), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(uiState.aiNeuralDesign!!, color = Color.White, fontSize = 12.sp, lineHeight = 18.sp)
                            }
                        }
                    } else if (uiState.isGenerating) {
                        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFFFFAB40))
                            Spacer(Modifier.height(16.dp))
                            Text("WEAVING SINAPSES...", color = Color(0xFFFFAB40), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Icon(Icons.Default.Hub, null, tint = Color.White.copy(0.05f), modifier = Modifier.size(150.dp).align(Alignment.Center))
                    }
                }

                Spacer(Modifier.height(24.dp))

                // 5. ACTION BUTTONS
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { viewModel.generateNeuralNetwork(requestText) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFAB40)),
                        enabled = !uiState.isGenerating && requestText.isNotBlank()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, null, tint = Color.Black)
                            Spacer(Modifier.width(8.dp))
                            Text("WEAVE NETWORK", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                    
                    if (uiState.isBioLinkReady) {
                        IconButton(
                            onClick = { viewModel.exportCurrentDesign { } },
                            modifier = Modifier.size(56.dp).background(Color.White.copy(0.1f), RoundedCornerShape(16.dp))
                        ) {
                            Icon(Icons.Default.PictureAsPdf, null, tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}
