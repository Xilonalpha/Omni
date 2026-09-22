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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.ui.viewmodels.GalaxyMigrationViewModel
import com.chemscanner.omniscient.marrow.utils.LanguageManager // FIXED IMPORT
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GalaxyMigrationActivity : AppCompatActivity() {

    private val viewModel: GalaxyMigrationViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GalaxyMigrationScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalaxyMigrationScreen(
    viewModel: GalaxyMigrationViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("GALACTIC MIGRATION HUB", color = Color(0xFF4FC3F7), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            
            // FLEET OVERVIEW
            Surface(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                color = Color.White.copy(0.05f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4FC3F7).copy(0.3f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(Color(0xFF4FC3F7).copy(0.1f), radius = 300f, style = Stroke(1f))
                        drawCircle(Color(0xFF4FC3F7).copy(0.05f), radius = 150f, style = Stroke(1f))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOTAL COLONISTS", color = Color.Gray, fontSize = 10.sp)
                        Text("${uiState.totalColonists}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                        Text("SYSTEMS SYNC: ${(uiState.systemsConnectivity * 100).toInt()}%", color = Color.Green, fontSize = 10.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("ACTIVE COLONY SHIPS", color = Color.Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(uiState.activeShips) { ship ->
                    ShipCard(ship)
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { viewModel.initiateCoordination() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FC3F7)),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isCoordinating
            ) {
                if (uiState.isCoordinating) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Sync, null)
                    Spacer(Modifier.width(12.dp))
                    Text("COORDINATE FLEET", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun ShipCard(ship: com.chemscanner.omniscient.ui.viewmodels.ColonyShip) {
    Surface(
        color = Color.White.copy(0.02f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(ship.name, color = Color.White, fontWeight = FontWeight.Bold)
                Text(ship.status, color = Color.Cyan, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
            Text("DESTINATION: ${ship.destination}", color = Color.Gray, fontSize = 11.sp)
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { ship.progress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = Color(0xFF4FC3F7),
                trackColor = Color.DarkGray
            )
        }
    }
}
