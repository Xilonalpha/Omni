package com.chemscanner.omniscient.ui.compose.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chemscanner.omniscient.marrow.repository.DsnSignal
import com.chemscanner.omniscient.ui.viewmodels.CelestialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CelestialControlScreen(
    viewModel: CelestialViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("DEEP SPACE COMMAND", fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(Color.Black, Color(0xFF001529))))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SECTION: VERA RUBIN
            item {
                ControlSection(
                    title = "VERA RUBIN OBSERVATORY",
                    subtitle = "Transient Alert Stream (Supernovae)",
                    icon = Icons.Default.AutoGraph,
                    color = Color.Yellow,
                    isActive = uiState.isVeraRubinActive,
                    onToggle = { viewModel.toggleVeraRubin(it) }
                ) {
                    uiState.latestCosmicAlert?.let { alert ->
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Text("DETECTED: ${alert.type}", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Object ID: ${alert.objectId} | Mag: ${alert.magnitude}", color = Color.Gray, fontSize = 11.sp)
                        }
                    } ?: Text("Scanning for cosmic transients...", color = Color.DarkGray, fontSize = 11.sp)
                }
            }

            // SECTION: DSN (VOYAGER)
            item {
                ControlSection(
                    title = "VOYAGER GHOST LINK (DSN)",
                    subtitle = "Interstellar Signal Tracking",
                    icon = Icons.Default.SettingsInputAntenna,
                    color = Color.Cyan,
                    isActive = uiState.isDsnActive,
                    onToggle = { viewModel.toggleDsn(it) }
                ) {
                    if (uiState.dsnSignals.isEmpty()) {
                        Text("Searching for Voyager and deep space probes...", color = Color.DarkGray, fontSize = 11.sp)
                    } else {
                        uiState.dsnSignals.take(3).forEach { signal ->
                            DsnSignalRow(signal)
                        }
                    }
                }
            }

            // SECTION: STARLINK MESH
            item {
                ControlSection(
                    title = "STARLINK LASER MESH",
                    subtitle = "SpaceX Celestial Interlink",
                    icon = Icons.Default.Wifi,
                    color = Color.Green,
                    isActive = uiState.isStarlinkActive,
                    onToggle = { viewModel.toggleStarlink(it) }
                ) {
                    val status = uiState.starlinkStatus
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Text(
                            text = "SATELLITE: ${status.lastSatelliteName}", 
                            color = Color(0xFF00E5FF), 
                            fontWeight = FontWeight.Black, 
                            fontSize = 12.sp
                        )
                        Text(
                            text = "COORDINATES: ${status.currentCoordinates}", 
                            color = Color.White, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 11.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Active Nodes: ${status.activeNodes} | Latency: ${status.networkLatencyMs}ms", 
                            color = Color.Gray, 
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // SECTION: JWST
            item {
                ControlSection(
                    title = "JAMES WEBB (JWST)",
                    subtitle = "Infrared Deep Space Vision",
                    icon = Icons.Default.Visibility,
                    color = Color(0xFFE040FB),
                    isActive = uiState.isJwstActive,
                    onToggle = { viewModel.toggleJwst(it) }
                ) {
                    Text(
                        text = uiState.lastJwstDiscovery.take(150) + "...",
                        color = Color.White.copy(0.8f),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
            
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun ControlSection(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    isActive: Boolean,
    onToggle: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!isActive) },
        color = Color.White.copy(0.03f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp, 
            color = if(isActive) color.copy(0.4f) else Color.White.copy(0.05f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = if(isActive) color else Color.Gray, 
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    Text(subtitle, color = Color.Gray, fontSize = 10.sp)
                }
                Switch(
                    checked = isActive,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = color,
                        checkedTrackColor = color.copy(alpha = 0.3f),
                        uncheckedThumbColor = Color.DarkGray
                    )
                )
            }
            if (isActive) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(0.05f))
                content()
            }
        }
    }
}

@Composable
fun DsnSignalRow(signal: DsnSignal) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(signal.spacecraft, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("${signal.band} | ${(signal.rangeKm / 1e9).format(2)} billion km", color = Color.Gray, fontSize = 9.sp)
        }
        Text("${signal.signalPowerDbm.toInt()} dBm", color = if(signal.signalPowerDbm > -150) Color.Green else Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

fun Double.format(digits: Int) = "%.${digits}f".format(this)
