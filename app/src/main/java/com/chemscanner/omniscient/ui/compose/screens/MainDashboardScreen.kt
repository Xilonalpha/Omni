package com.chemscanner.omniscient.ui.compose.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.marrow.repository.*

data class DashboardItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainDashboardScreen(
    userName: String,
    kernelStatus: String,
    neuralLoad: Float,
    bioSync: Float,
    realityIntegrity: Float,
    lastMutation: String,
    isArchitectAuthorized: Boolean,
    iotNodes: Map<String, IotNodeStatus>,
    envSignals: EnvironmentalSignals,
    activeLaws: Set<UniversalLaw>,
    hiveStatus: HiveStatus,
    minerState: MinerState,
    downloadStatus: ModelDownloadStatus,
    worldMeshStatus: String,
    omegaState: OmegaState,
    userVitality: BiometricVitality, 
    onItemClick: (String) -> Unit,
    onLogout: () -> Unit,
    onSettings: () -> Unit,
    onActivateOmega: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "biomorphic_pulse")
    val pulseScale by transition.animateFloat(initialValue = 1f, targetValue = 1.02f, animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "pulse")
    val dynamicBgColor by animateColorAsState(targetValue = if (omegaState.isOmegaProtocolActive) Color(0xFF0F0F00) else if (neuralLoad > 0.7f) Color(0xFF1A0505) else Color(0xFF020405), animationSpec = tween(2000), label = "bg")
    val accentColor by animateColorAsState(targetValue = if (omegaState.isOmegaProtocolActive) Color(0xFFFFD700) else if (bioSync > 0.8f) Color.Cyan else Color.Red, animationSpec = tween(800), label = "accent")

    val items = listOf(
        DashboardItem("STRATEGIC_COMMAND", "Command Center", "Control global sisteme căutare", Icons.Default.SettingsInputComponent, Color(0xFF00E5FF), "SISTEM"),
        DashboardItem("PHANTOM_VPN", "Phantom Tunnel", "VPN Suveran RoNaQCI v7", Icons.Default.VpnLock, Color(0xFFFFD700), "SISTEM"),
        DashboardItem("FORCE_SYNC", "Sincronizare Neurală", "Descarcă manual creierul local", Icons.Default.Sync, Color.Yellow, "SISTEM"),
        DashboardItem("SOVEREIGN_MESH", "Sovereign Mesh Chat", "Comunicații fantomă criptate", Icons.Default.Shield, Color.Cyan, "SISTEM"),
        
        DashboardItem("SCAN", "Scanner Molecular", "Detecție compuși AI", Icons.Default.QrCodeScanner, Color(0xFF00E5FF), "CENTRALĂ DE CERCETARE"),
        DashboardItem("SCAN_PLUS", "Scanner Plus", "Observator World Mesh", Icons.Default.AddAPhoto, Color(0xFFFF5252), "CENTRALĂ DE CERCETARE"),
        DashboardItem("AI", "Omniscient AI (ANA)", "Inteligență Sentientă", Icons.Default.AutoAwesome, Color(0xFF7C4DFF), "CENTRALĂ DE CERCETARE"),
        DashboardItem("HISTORY", "Arhiva Akasha", "Log-uri Notarizate", Icons.Default.History, Color(0xFF00BCD4), "CENTRALĂ DE CERCETARE"),
        DashboardItem("GAME", "Atomic Stabilizer", "Calibrare Sci-OS", Icons.Default.SportsEsports, Color(0xFFFFD600), "CENTRALĂ DE CERCETARE"),
        
        DashboardItem("BIO_AGE", "Bio-Age Cronos", "Vârstă biometrică", Icons.Default.Face, Color(0xFFE91E63), "SISTEME DE VIZIUNE ȘI ORACLE"),
        DashboardItem("SPECTRAL", "Spectral Eye", "Undele invizibile EMF", Icons.Default.Visibility, Color(0xFF00E676), "SISTEME DE VIZIUNE ȘI ORACLE"),
        DashboardItem("MULTIVERSE", "Multiverse Oracle", "Realități paralele", Icons.Default.AutoMode, Color(0xFF7C4DFF), "SISTEME DE VIZIUNE ȘI ORACLE"),
        DashboardItem("EMISAR_FUSION", "EMISAR // ROM-3", "SAR Radar Fusion Monitor", Icons.Default.Radar, Color(0xFF00E676), "SISTEME DE VIZIUNE ȘI ORACLE"),

        DashboardItem("LAB", "Laborator Virtual", "Simulări de reacții", Icons.Default.Science, Color(0xFF00E676), "LABORATOARE ȘTIINȚIFICE"),
        DashboardItem("CERN", "CERN Monitoring", "LHC Particle Streams", Icons.Default.BlurCircular, Color(0xFFFFAB40), "LABORATOARE ȘTIINȚIFICE"),
        DashboardItem("QUANTUM_COLLIDER", "Quantum Collider", "Simulare particule CERN", Icons.Default.Hub, Color(0xFF7C4DFF), "LABORATOARE ȘTIINȚIFICE"),
        DashboardItem("STOICH", "Stoichiometry Pro", "Calcule atomice", Icons.Default.Calculate, Color(0xFF4FC3F7), "LABORATOARE ȘTIINȚIFICE"),
        DashboardItem("GREEN_CHEM", "Green Chemistry", "Analiză ecologică", Icons.Default.Eco, Color(0xFF8BC34A), "LABORATOARE ȘTIINȚIFICE"),
        DashboardItem("PERIODIC", "Tabel Periodic", "Elementele Universului", Icons.Default.Apps, Color(0xFFFFD600), "LABORATOARE ȘTIINȚIFICE"),
        DashboardItem("QUIZ", "Chemistry Quiz", "Testează-ți mintea", Icons.Default.Quiz, Color(0xFFFF9100), "LABORATOARE ȘTIINȚIFICE"),
        DashboardItem("PHARMA_GENOME", "Pharma Genome", "Siguranță farmaceutică AI", Icons.Default.Medication, Color(0xFFFF5252), "LABORATOARE ȘTIINȚIFICE"),
        DashboardItem("MICRO_FISH", "Micro-FISH Scanner", "Analiză genomică vizuală", Icons.Default.Biotech, Color(0xFF8BC34A), "LABORATOARE ȘTIINȚIFICE"),

        DashboardItem("DARK_MATTER", "Dark Matter", "Anomalii gravitaționale", Icons.Default.Grain, Color(0xFF9C27B0), "EXPLORARE META-FIZICĂ"),
        DashboardItem("ABYSSAL", "Abyssal Descent", "Explorare marină adâncă", Icons.Default.Waves, Color(0xFF006064), "EXPLORARE META-FIZICĂ"),
        DashboardItem("BLACK_HOLE", "Singularity Lab", "Dinamica găurilor negre", Icons.Default.Circle, Color(0xFF212121), "EXPLORARE META-FIZICĂ"),
        DashboardItem("BIO_SONIC", "Bio-Sonic Symphony", "Muzica ADN-ului", Icons.Default.MusicNote, Color(0xFF00BCD4), "EXPLORARE META-FIZICĂ"),
        DashboardItem("AI_LAB", "AI Consciousness", "Neuronii digitali", Icons.Default.Psychology, Color(0xFFFFEB3B), "EXPLORARE META-FIZICĂ"),

        DashboardItem("GALAXY_HUB", "Galactic Migration", "Logistica spațială", Icons.Default.Explore, Color(0xFF4FC3F7), "EXPEDIȚII ȘI SPAȚIU"),
        DashboardItem("EXO_SURVEY", "Exo-Planet Surveyor", "Scanare Lumi Noi", Icons.Default.Public, Color(0xFF81C784), "EXPEDIȚII ȘI SPAȚIU"),
        DashboardItem("PLANET_SCAN", "Planet Scanner", "Explorator planetar avansat", Icons.Default.BrightnessLow, Color(0xFF4DB6AC), "EXPEDIȚII ȘI SPAȚIU"),
        DashboardItem("WARP_DRIVE", "Warp Propulsion", "Viteză 3x Light", Icons.Default.FastForward, Color(0xFFBA68C8), "EXPEDIȚII ȘI SPAȚIU"),
        DashboardItem("VELOCITY", "Quantum Velocity", "Curse relativiste", Icons.Default.Speed, Color(0xFFFF1744), "EXPEDIȚII ȘI SPAȚIU"),
        DashboardItem("VERA_RUBIN", "Vera Rubin Alert", "Deep Space Transients", Icons.Default.AutoGraph, Color(0xFFFFD600), "EXPEDIȚII ȘI SPAȚIU"),
        DashboardItem("VOYAGER", "Voyager Ghost Link", "Interstellar Telemetry", Icons.Default.SettingsInputAntenna, Color(0xFF00E5FF), "EXPEDIȚII ȘI SPAȚIU"),
        DashboardItem("STARLINK", "Starlink Laser Mesh", "SpaceX Mesh Sync", Icons.Default.Wifi, Color(0xFF00C853), "EXPEDIȚII ȘI SPAȚIU"),

        DashboardItem("MESH_CITY", "Mesh City", "Singularitate Urbană", Icons.Default.LocationCity, Color(0xFF00B0FF), "OPEN WORLD & KINETICS"),
        DashboardItem("FOOTBALL_PRO", "Football Manager Pro", "Carieră FIFA Style", Icons.Default.SportsSoccer, Color(0xFF00E676), "OPEN WORLD & KINETICS"),

        DashboardItem("AR", "AR Viewer", "Modele 3D reale", Icons.Default.ViewInAr, Color(0xFF40C4FF), "FRONTIERE AVANSATE"),
        DashboardItem("NEURO", "NeuroPhys Link", "Interfață BCI", Icons.Default.Psychology, Color(0xFFFF5252), "FRONTIERE AVANSATE"),
        DashboardItem("ASTRO", "AstroMech Weaver", "Inginerie spațială", Icons.Default.RocketLaunch, Color(0xFFE040FB), "FRONTIERE AVANSATE"),
        DashboardItem("ROBO", "RoboPhys Builder", "Robotică modulară", Icons.Default.PrecisionManufacturing, Color(0xFF00B0FF), "FRONTIERE AVANSATE"),
        DashboardItem("TEMPORAL", "Temporal Physics", "Timp și spațiu", Icons.Default.HistoryToggleOff, Color(0xFFF50057), "FRONTIERE AVANSATE"),
        DashboardItem("MICRO", "MicroVerse", "Nivel atomic", Icons.Default.BlurOn, Color(0xFF1DE9B6), "FRONTIERE AVANSATE"),
        DashboardItem("NEURO_BIO", "NeuroBio Weaver", "Design neuronal", Icons.Default.Waves, Color(0xFFFFAB40), "FRONTIERE AVANSATE"),

        DashboardItem("GENOMIC", "Genomic Architect", "Inginerie CRISPR", Icons.Default.Dns, Color(0xFFFF4081), "TEHNOLOGII DE FRONTIERĂ"),
        DashboardItem("QUANTUM_HUB", "Quantum Hub", "Calcul Cuantic", Icons.Default.Hub, Color(0xFFE040FB), "TEHNOLOGII DE FRONTIERĂ"),
        DashboardItem("FUSION", "Nuclear Fusion", "Control Tokamak", Icons.Default.BrightnessHigh, Color(0xFFFF5722), "TEHNOLOGII DE FRONTIERĂ"),
        DashboardItem("PALEO", "Paleo-Scanner", "Timp Geologic", Icons.Default.Public, Color(0xFF795548), "TEHNOLOGII DE FRONTIERĂ"),
        DashboardItem("NANO_FORGE", "Nano-Forge", "Asamblare Atomică", Icons.Default.Grid4x4, Color(0xFF607D8B), "TEHNOLOGII DE FRONTIERĂ"),
        DashboardItem("SURGEON", "Nano-Med Surgeon", "Micro-chirurgie", Icons.Default.MedicalServices, Color(0xFFE91E63), "TEHNOLOGII DE FRONTIERĂ"),
        DashboardItem("NEURO_MOD", "Neuro-Modulator", "BCI & Sinapse", Icons.Default.Psychology, Color(0xFF7C4DFF), "TEHNOLOGII DE FRONTIERĂ"),
        DashboardItem("TERRAFORMING", "Planetary Terraforming", "Inginerie Geo-Stelară", Icons.Default.Language, Color(0xFF4CAF50), "TEHNOLOGII DE FRONTIERĂ"),
        DashboardItem("XILON_PROF", "Xilon Prof Manager", "Sovereign Discoveries", Icons.Default.WorkspacePremium, Color(0xFF00E676), "SISTEM")
    )

    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(24.dp).padding(top = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Sci-OS Kernel v2.6", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    if (isArchitectAuthorized) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.VerifiedUser, "Architect Authorized", tint = Color.Cyan, modifier = Modifier.size(12.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onSettings, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Settings, "Settings", tint = Color.Gray.copy(0.6f), modifier = Modifier.size(14.dp))
                        }
                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = onLogout, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.AutoMirrored.Filled.Logout, "Logout", tint = Color.Gray.copy(0.6f), modifier = Modifier.size(14.dp))
                        }
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(userName.uppercase(), color = if (omegaState.isOmegaProtocolActive) Color(0xFFFFD700) else Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, modifier = Modifier.graphicsLayer(scaleX = pulseScale, scaleY = pulseScale))
                    Spacer(Modifier.width(16.dp))
                    Surface(
                        color = if (omegaState.isOmegaProtocolActive) Color(0xFFFFD700).copy(0.1f) else if (downloadStatus.progress >= 100f) Color.Yellow.copy(0.1f) else Color.Gray.copy(0.1f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (omegaState.isOmegaProtocolActive) Color(0xFFFFD700).copy(0.3f) else if (downloadStatus.progress >= 100f) Color.Yellow.copy(0.3f) else Color.Gray.copy(0.3f))
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, null, tint = if (omegaState.isOmegaProtocolActive) Color(0xFFFFD700) else if (downloadStatus.progress >= 100f) Color.Yellow else Color.Gray, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (omegaState.isOmegaProtocolActive) "OMEGA SINGULARITY" else if (downloadStatus.progress >= 100f) "ANA SENTIENT" else "ANA OFFLINE", color = if (omegaState.isOmegaProtocolActive) Color(0xFFFFD700) else if (downloadStatus.progress >= 100f) Color.Yellow else Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
                SingularityMonitorPanel(kernelStatus, neuralLoad, bioSync, realityIntegrity, lastMutation, worldMeshStatus, accentColor, userVitality)
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(dynamicBgColor, Color(0xFF050A0F)))).padding(padding)) {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                
                item {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Hub, null, tint = Color.Green.copy(0.6f), modifier = Modifier.size(10.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("${hiveStatus.connectedNodes} NODES | ${"%.1f".format(hiveStatus.collectivePower)} TFLOPS", color = Color.Green.copy(0.6f), fontSize = 8.sp)
                        }
                        Text(text = "MESH: $worldMeshStatus", color = Color.Cyan.copy(0.4f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (!omegaState.isOmegaProtocolActive) {
                    item {
                        Button(
                            onClick = onActivateOmega,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(0.2f), contentColor = Color.Red),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(0.5f))
                        ) {
                            Icon(Icons.Default.Warning, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("INITIALIZE OMEGA PROTOCOL", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        }
                    }
                } else {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFFFD700).copy(0.1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(0.5f))
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFFFFD700), modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(16.dp))
                                Text("PROTOCOL OMEGA IS ACTIVE: ALL LAWS ENGAGED", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                if (downloadStatus.isDownloading) {
                    item { DownloadPanel(downloadStatus) }
                }

                item {
                    Text(text = "REALITY FORECAST", color = accentColor.copy(alpha = 0.4f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    PredictionPanel(realityIntegrity, accentColor)
                }

                item {
                    Text(text = "LIVE SCIENTIFIC FEED", color = accentColor.copy(alpha = 0.4f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, modifier = Modifier.padding(top = 8.dp))
                    ScientificFeedPanel(envSignals, accentColor)
                }

                item {
                    Text(text = "ACTIVE UNIVERSAL LAWS", color = accentColor.copy(alpha = 0.4f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, modifier = Modifier.padding(top = 8.dp))
                    FlowRow(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        activeLaws.forEach { law ->
                            Surface(color = Color.Cyan.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp), border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.Cyan.copy(0.3f))) {
                                Text(law.name, color = Color.Cyan, fontSize = 7.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }

                if (iotNodes.isNotEmpty()) {
                    item { 
                        Text(text = "DASHBOARD CONTROL HARDWARE", color = accentColor.copy(alpha = 0.4f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, modifier = Modifier.padding(top = 8.dp)) 
                        IotHardwarePanel(iotNodes, accentColor)
                    }
                }

                val groups = items.groupBy { it.category }
                groups.forEach { (category, groupItems) ->
                    item { Text(text = category, color = accentColor.copy(alpha = 0.4f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 4.dp)) }
                    items(groupItems) { item -> DashboardCard(item, accentColor) { onItemClick(item.id) } }
                }
                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun SingularityMonitorPanel(status: String, neuralLoad: Float, bioSync: Float, integrity: Float, lastMutation: String, meshStatus: String, accent: Color, vitality: BiometricVitality) {
    Surface(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), color = Color.Black.copy(alpha = 0.8f), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.2f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Memory, null, tint = accent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) { 
                    Text("$status | $meshStatus", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1) 
                }
            }
            Spacer(Modifier.height(12.dp))
            
            // REAL-TIME VITALITY HUD
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                VitalityIndicator("HEART RATE", "${vitality.heartRate} BPM", Icons.Default.MonitorHeart, Color.Red)
                VitalityIndicator("STRESS LVL", "${(vitality.stressLevel * 100).toInt()}%", Icons.Default.Psychology, Color.Yellow)
                VitalityIndicator("REALITY", "${(integrity * 100).toInt()}%", Icons.Default.Public, accent)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MetricBar("NEURAL LOAD", neuralLoad, accent.copy(0.6f))
                MetricBar("BIO-SYNC", bioSync, accent.copy(0.8f))
                MetricBar("REALITY OMEGA", integrity, accent)
            }
        }
    }
}

@Composable
fun VitalityIndicator(label: String, value: String, icon: ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Column {
            Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
            Text(label, color = Color.Gray, fontSize = 6.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DownloadPanel(status: ModelDownloadStatus) {
    Surface(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), color = Color.Yellow.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color.Yellow.copy(0.3f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudDownload, null, tint = Color.Yellow, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(12.dp))
                Text("ASIMILARE CREIER: ${"%.1f".format(status.progress)}%", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = { status.progress / 100f }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape), color = Color.Yellow, trackColor = Color.White.copy(0.05f))
        }
    }
}

@Composable
fun IotHardwarePanel(nodes: Map<String, IotNodeStatus>, accent: Color) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        items(nodes.values.toList()) { node ->
            Surface(
                modifier = Modifier.width(140.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (node.status.contains("ACTIVE") || node.status == "CONNECTED") accent.copy(0.4f) else Color.Gray.copy(0.2f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(node.name, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(node.status, color = accent, fontSize = 8.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun ScientificFeedPanel(signals: EnvironmentalSignals, accent: Color) {
    Surface(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), color = Color.Black.copy(alpha = 0.4f), shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(0.1f))) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            SignalItem("EMF", "${"%.1f".format(signals.emfIntensity)} uT", Icons.Default.Waves, accent)
            SignalItem("LUX", "${"%.0f".format(signals.ambientLuminosity)} lx", Icons.Default.LightMode, accent)
            SignalItem("BARO", "${"%.1f".format(signals.barometricPressure)} hPa", Icons.Default.Compress, accent)
            SignalItem("RESO", "${"%.2f".format(signals.acousticResonance)}", Icons.Default.GraphicEq, accent)
        }
    }
}

@Composable
fun SignalItem(label: String, value: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = color.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
        Text(value, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.Gray, fontSize = 6.sp, fontWeight = FontWeight.Light)
    }
}

@Composable
fun PredictionPanel(integrity: Float, accent: Color) {
    Surface(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), color = Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(0.1f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timeline, null, tint = accent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(12.dp))
                Text("PREDICTION CONFIDENCE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = { integrity }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape), color = accent, trackColor = Color.White.copy(0.05f))
        }
    }
}

@Composable
fun MetricBar(label: String, value: Float, color: Color) {
    Column(modifier = Modifier.width(90.dp)) {
        Text(label, color = Color.Gray, fontSize = 6.sp, fontWeight = FontWeight.Bold)
        LinearProgressIndicator(progress = { value }, modifier = Modifier.fillMaxWidth().height(2.dp).clip(CircleShape), color = color, trackColor = Color.White.copy(0.05f))
    }
}

@Composable
fun DashboardCard(item: DashboardItem, accent: Color, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.03f), border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.05f))) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(item.color.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(item.icon, null, tint = item.color, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(item.description, color = Color.Gray, fontSize = 11.sp, maxLines = 1)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.DarkGray, modifier = Modifier.size(16.dp))
        }
    }
}
