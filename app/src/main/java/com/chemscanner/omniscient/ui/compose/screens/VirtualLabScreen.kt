package com.chemscanner.omniscient.ui.compose.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.ui.viewmodels.VirtualLabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualLabScreen(
    viewModel: VirtualLabViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var reactant1 by remember { mutableStateOf("") }
    var reactant2 by remember { mutableStateOf("") }

    // Definirea culorii dinamice pentru reactor
    val reactorColor = when {
        uiState.isSimulating -> Color.Cyan
        uiState.result?.contains("Eroare", ignoreCase = true) == true -> Color.Red
        uiState.result != null -> Color(0xFF00E676)
        else -> Color.Cyan.copy(alpha = 0.6f)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "reactor")
    val liquidLevel by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.45f,
        animationSpec = infiniteRepeatable(animation = tween(2000), repeatMode = RepeatMode.Reverse), label = "liquid"
    )

    val bubbleAnim = rememberInfiniteTransition(label = "bubbles")
    val bubbleOffset by bubbleAnim.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1500, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "offset"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("REACTOR CHIMIC VIRTUAL", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White)
            )
        },
        containerColor = Color(0xFF04090B)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            Column(modifier = Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                
                // 1. REACTOR ANIMAT CU PARTICULE
                Box(modifier = Modifier.size(180.dp, 280.dp), contentAlignment = Alignment.BottomCenter) {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .border(3.dp, Color.White.copy(0.15f), RoundedCornerShape(bottomStart = 50.dp, bottomEnd = 50.dp))
                        .background(Color.White.copy(0.05f), RoundedCornerShape(bottomStart = 50.dp, bottomEnd = 50.dp))
                    )
                    
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(liquidLevel)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(bottomStart = 44.dp, bottomEnd = 44.dp))
                        .background(Brush.verticalGradient(listOf(reactorColor, reactorColor.copy(alpha = 0.4f))))
                    )

                    // Sistem de Bule (Animație Inovatoare)
                    if (uiState.isSimulating || uiState.result != null) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val count = 12
                            for (i in 0 until count) {
                                val x = size.width * (0.25f + (i.toFloat() / count) * 0.5f)
                                val yStart = size.height * 0.85f
                                val y = yStart - (size.height * 0.4f * ((bubbleOffset + (i * 0.15f)) % 1f))
                                
                                drawCircle(
                                    color = Color.White,
                                    radius = 3f,
                                    center = Offset(x, y),
                                    alpha = 0.3f * (1f - (yStart - y) / (size.height * 0.4f))
                                )
                            }
                        }
                    }
                    
                    if (uiState.isSimulating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center).size(48.dp))
                    }
                }

                Spacer(Modifier.height(32.dp))

                // 2. CONSOLA DE CONTROL
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.03f),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("ALIMENTARE REACTANȚI", color = Color.Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = reactant1,
                            onValueChange = { reactant1 = it },
                            label = { Text("Substanța A", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color.Cyan,
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = reactant2,
                            onValueChange = { reactant2 = it },
                            label = { Text("Substanța B", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color.Cyan,
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Button(
                            onClick = { viewModel.simulateReaction(reactant1, reactant2) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan, contentColor = Color.Black),
                            enabled = !uiState.isSimulating && reactant1.isNotBlank() && reactant2.isNotBlank()
                        ) {
                            Text("PORNEȘTE SINTEZA", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        }
                    }
                }

                // 3. RAPORT REZULTAT
                if (uiState.result != null) {
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, reactorColor.copy(alpha = 0.5f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Science, null, tint = reactorColor, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(text = uiState.result!!, color = Color.White, modifier = Modifier.weight(1f), fontSize = 13.sp, lineHeight = 18.sp)
                            IconButton(onClick = { viewModel.speakResult() }) {
                                Icon(Icons.Default.VolumeUp, null, tint = Color.Cyan)
                            }
                        }
                    }
                }
            }
        }
    }
}
