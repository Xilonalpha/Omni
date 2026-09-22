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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.ui.viewmodels.MeshCityViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MeshCityActivity : AppCompatActivity() {

    private val viewModel: MeshCityViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MeshCityScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeshCityScreen(
    viewModel: MeshCityViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    val gridPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "pulse"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MESH CITY SINGULARITY", color = Color(0xFF00B0FF), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black))
                        Text("SECTOR: ${uiState.currentSector}", color = Color.White.copy(0.5f), fontSize = 8.sp)
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
        BoxWithConstraints(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    viewModel.movePlayer(Offset(-dragAmount.x, -dragAmount.y))
                }
            }
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()
            val density = LocalDensity.current

            // 1. DYNAMIC MESH GRID
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 100f * uiState.worldScale
                val offsetX = (uiState.playerPosition.x % step)
                val offsetY = (uiState.playerPosition.y % step)
                
                for (x in -1..(size.width / step).toInt() + 1) {
                    val px = x * step - offsetX
                    drawLine(Color(0xFF00B0FF).copy(alpha = gridPulse), Offset(px, 0f), Offset(px, size.height), 1f)
                }
                for (y in -1..(size.height / step).toInt() + 1) {
                    val py = y * step - offsetY
                    drawLine(Color(0xFF00B0FF).copy(alpha = gridPulse), Offset(0f, py), Offset(size.width, py), 1f)
                }
            }

            // 2. CITY OBJECTS
            uiState.activeObjects.forEach { obj ->
                val relativeX = obj.position.x - uiState.playerPosition.x + widthPx / 2
                val relativeY = obj.position.y - uiState.playerPosition.y + heightPx / 2
                
                // Convert pixels to Dp for the offset
                val offsetX = with(density) { (relativeX / 3f).toDp() }
                val offsetY = with(density) { (relativeY / 3f).toDp() }

                Box(
                    modifier = Modifier
                        .offset(x = offsetX, y = offsetY)
                        .size((if(obj.type == "BUILDING") 60.dp else 24.dp) * uiState.worldScale)
                        .background(
                            if(obj.type == "BUILDING") Color(0xFF00B0FF).copy(0.2f) else Color.Yellow.copy(0.3f),
                            RoundedCornerShape(4.dp)
                        )
                        .border(1.dp, if(obj.type == "BUILDING") Color(0xFF00B0FF) else Color.Yellow, RoundedCornerShape(4.dp))
                        .clickable { viewModel.interactWithObject(obj) }
                )
            }

            // 3. PLAYER AVATAR (Center)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(20.dp)
                    .background(Color.White, CircleShape)
                    .border(2.dp, Color(0xFF00B0FF), CircleShape)
            )

            // 4. HUD
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)) {
                Text("COORDINATES: X:${uiState.playerPosition.x.toInt()} Y:${uiState.playerPosition.y.toInt()}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("ENERGY: ${uiState.availableEnergy.toInt()} mJ", color = if(uiState.isPowerActive) Color.Green else Color.Red, fontSize = 10.sp)
                
                Spacer(Modifier.height(16.dp))
                
                Slider(
                    value = uiState.worldScale,
                    onValueChange = { viewModel.modulateScale(it) },
                    valueRange = 0.01f..2.0f,
                    modifier = Modifier.width(150.dp),
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF00B0FF), activeTrackColor = Color(0xFF00B0FF))
                )
                Text("WORLD SCALE", color = Color.Gray, fontSize = 8.sp)
            }
            
            if (uiState.isInfiltrating) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.7f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00B0FF))
                }
            }
        }
    }
}
