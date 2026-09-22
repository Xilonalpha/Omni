package com.chemscanner.omniscient.ui.compose.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.marrow.data.models.BlockchainEntry
import com.chemscanner.omniscient.ui.viewmodels.OmniscientNeuralLedgerViewModel
import com.chemscanner.omniscient.ui.viewmodels.CrystalNode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmniscientNeuralLedgerScreen(
    viewModel: OmniscientNeuralLedgerViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val infiniteTransition = rememberInfiniteTransition(label = "ledger")
    
    val bgRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(60000, easing = LinearEasing)), label = "bg"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("NEURAL LEDGER (AKASHA)", color = Color.Cyan, style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black.copy(0.8f))
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // FUNDAL DINAMIC (Pentru a evita ecranul negru)
            Canvas(modifier = Modifier.fillMaxSize().graphicsLayer(rotationZ = bgRotation)) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF001A1A), Color.Black),
                        center = Offset(size.width / 2, size.height / 2),
                        radius = size.minDimension
                    )
                )
            }

            // REȚEAUA DE CRISTALE (Datele Notarizate)
            if (uiState.crystals.isEmpty() && !uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CloudOff, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Text("AKASHA ESTE MOMENTAN VIDĂ", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            uiState.crystals.forEach { node ->
                AkashaCrystal(node) { viewModel.selectCrystal(node) }
            }

            // DETALII BLOC SELECTAT
            AnimatedVisibility(
                visible = uiState.selectedBlock != null,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                uiState.selectedBlock?.let { block ->
                    BlockDetailsPanel(block) { viewModel.selectCrystal(CrystalNode(block)) /* Close logic via re-select or specific nulling */ }
                }
            }
            
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.Cyan)
            }
        }
    }
}

@Composable
fun AkashaCrystal(node: CrystalNode, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "pulse"
    )

    Box(
        modifier = Modifier
            .offset(node.position.x.dp / 5, node.position.y.dp / 5) // Ajustat pentru densitate ecran
            .size((40 * node.scale).dp)
            .graphicsLayer(rotationZ = node.rotation, scaleX = pulse, scaleY = pulse)
            .background(
                Brush.linearGradient(listOf(Color.Cyan.copy(0.4f), Color(0xFF004D40))),
                RoundedCornerShape(4.dp)
            )
            .border(1.dp, Color.Cyan.copy(0.6f), RoundedCornerShape(4.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(node.block.module.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
    }
}

@Composable
fun BlockDetailsPanel(block: BlockchainEntry, onClose: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        color = Color(0xFF001515),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Cyan.copy(0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("AKASHA RECORD", color = Color.Cyan, fontWeight = FontWeight.Black, fontSize = 10.sp)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onClose, modifier = Modifier.size(20.dp)) { Icon(Icons.Default.Close, null, tint = Color.Gray) }
            }
            Spacer(Modifier.height(12.dp))
            Text("MODULE: ${block.module}", color = Color.White, fontWeight = FontWeight.Bold)
            Text("DATA: ${block.dataPayload}", color = Color.LightGray, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Text("HASH: ${block.blockHash.take(16)}...", color = Color.Cyan.copy(0.5f), fontSize = 9.sp)
            Text("PREV: ${block.previousHash.take(16)}...", color = Color.Gray, fontSize = 8.sp)
        }
    }
}
