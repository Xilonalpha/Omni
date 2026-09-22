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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.ui.viewmodels.DNASequence
import com.chemscanner.omniscient.ui.viewmodels.GenomicArchitectViewModel
import com.chemscanner.omniscient.marrow.utils.LanguageManager // FIXED IMPORT
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.*

@AndroidEntryPoint
class GenomicArchitectActivity : AppCompatActivity() {

    private val viewModel: GenomicArchitectViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GenomicArchitectScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenomicArchitectScreen(
    viewModel: GenomicArchitectViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "dna")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing)), label = "rotation"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("GENOMIC ARCHITECT (CRISPR)", color = Color(0xFFFF4081), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black))
                        Text("AVAILABLE ENERGY: ${"%.1f".format(uiState.availableEnergy)} NE", color = Color.Cyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
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
            
            // 1. INTERACTIVE DNA VISUALIZER
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF020508))
                    .border(1.dp, Color(0xFFFF4081).copy(0.2f), RoundedCornerShape(24.dp))
            ) {
                DNAVisualizer(rotation, uiState.dnaSequences)
                
                Column(modifier = Modifier.align(Alignment.TopStart).padding(24.dp)) {
                    Text("SYNTHESIS STATUS:", color = Color(0xFFFF4081), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Text(uiState.incubationMessage, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            // 2. CRISPR CONTROL PANEL
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                color = Color.Black.copy(alpha = 0.9f),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF4081).copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    LazyColumn(modifier = Modifier.height(120.dp)) {
                        itemsIndexed(uiState.dnaSequences) { index, gene ->
                            GeneItem(
                                gene = gene,
                                isSelected = uiState.selectedGeneIndex == index,
                                onClick = { viewModel.selectGene(index) }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    if (uiState.analysisResult != null) {
                        Surface(color = Color.White.copy(0.05f), shape = RoundedCornerShape(12.dp)) {
                            Text(
                                text = uiState.analysisResult!!,
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp).heightIn(max = 80.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { viewModel.incubateWithLastMolecule() },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !uiState.isAnalyzing
                        ) {
                            Icon(Icons.Default.Add, null)
                            Text("INCUBATE", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { viewModel.applyCrispr() },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081), contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            enabled = uiState.selectedGeneIndex != -1 && !uiState.isCrisprActive
                        ) {
                            Icon(Icons.Default.ContentCut, null)
                            Text("CRISPR", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DNAVisualizer(rotation: Float, sequences: List<DNASequence>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val centerX = w / 2
        val centerY = h / 4
        
        val amplitude = 120f
        val frequency = 0.05f
        val step = 25f
        
        for (i in 0..25) {
            val y = i * step
            val angle = (y * frequency) + Math.toRadians(rotation.toDouble()).toFloat()
            
            // Pulse based on stability
            // CRITICAL FIX: Added check to prevent divide by zero arithmetic exception
            val stability = if (sequences.isNotEmpty()) {
                sequences.getOrNull(i % sequences.size)?.stabilityLevel ?: 0.5f
            } else {
                0.5f
            }
            val pulse = 1f + (sin(rotation * 0.1f) * 0.1f * (1f - stability))

            val x1 = centerX + sin(angle) * amplitude * pulse
            val x2 = centerX + sin(angle + PI.toFloat()) * amplitude * pulse
            
            val z1 = cos(angle)
            val z2 = cos(angle + PI.toFloat())
            
            drawLine(
                color = Color.White.copy(alpha = 0.1f),
                start = Offset(x1, y + centerY),
                end = Offset(x2, y + centerY),
                strokeWidth = 2f
            )
            
            drawCircle(
                color = if (z1 > 0) Color(0xFFFF4081) else Color(0xFFC2185B),
                radius = (8f + z1 * 4f) * stability,
                center = Offset(x1, y + centerY),
                alpha = (0.5f + z1 * 0.5f) * stability
            )
            drawCircle(
                color = if (z2 > 0) Color(0xFF00E5FF) else Color(0xFF00B8D4),
                radius = (8f + z2 * 4f) * stability,
                center = Offset(x2, y + centerY),
                alpha = (0.5f + z2 * 0.5f) * stability
            )
        }
    }
}

@Composable
fun GeneItem(gene: DNASequence, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onClick() },
        color = if (isSelected) Color(0xFFFF4081).copy(0.1f) else Color.White.copy(0.05f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFFFF4081) else Color.Transparent)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(gene.id, color = Color(0xFFFF4081), fontWeight = FontWeight.Bold, fontSize = 10.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(gene.geneFunction, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                LinearProgressIndicator(
                    progress = { gene.stabilityLevel },
                    modifier = Modifier.width(60.dp).height(2.dp),
                    color = Color.Green,
                    trackColor = Color.Gray.copy(0.2f)
                )
            }
            if (gene.isModified) {
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.Check, null, tint = Color.Green, modifier = Modifier.size(16.dp))
            }
        }
    }
}
