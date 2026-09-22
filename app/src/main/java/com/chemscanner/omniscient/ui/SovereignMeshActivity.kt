package com.chemscanner.omniscient.ui

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.services.ShadowMeshService
import com.chemscanner.omniscient.marrow.services.QuantumIotBridge
import com.chemscanner.omniscient.marrow.services.LiFiService
import com.chemscanner.omniscient.marrow.services.HeliosSyncService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * SOVEREIGN MESH ACTIVITY v7.0 (AUTO-START MESH).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Zero-Trace Chat with automatic mesh activation.
 * v7.0: Forced ShadowMesh activation on launch to eliminate "Offline" status.
 */
@AndroidEntryPoint
class SovereignMeshActivity : ComponentActivity() {

    @Inject lateinit var shadowMesh: ShadowMeshService
    @Inject lateinit var globalKnowledge: GlobalKnowledgeRepository
    @Inject lateinit var iotBridge: QuantumIotBridge
    @Inject lateinit var lifiService: LiFiService
    @Inject lateinit var heliosSync: HeliosSyncService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ACTIVARE AUTOMATĂ: Pornește protocolul ShadowMesh la deschiderea chat-ului
        shadowMesh.activateShadowMesh("USER_INTERACTION_CHAT")

        setContent {
            SovereignMeshScreen(shadowMesh, globalKnowledge, iotBridge, lifiService, heliosSync) { finish() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Păstrăm purjarea memoriei pentru securitate, dar Mesh-ul poate rămâne activ dacă e nevoie
        shadowMesh.deactivateShadowMesh()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SovereignMeshScreen(
    shadowMesh: ShadowMeshService,
    globalKnowledge: GlobalKnowledgeRepository,
    iotBridge: QuantumIotBridge,
    lifiService: LiFiService,
    heliosSync: HeliosSyncService,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val events by globalKnowledge.events.collectAsState()
    val activeSecret by globalKnowledge.activeSovereignSecret.collectAsState()
    val isLiFiActive by lifiService.isLinkActive.collectAsState()
    val kernelStatus by globalKnowledge.kernelStatus.collectAsState()
    val meshStatus by globalKnowledge.shadowMeshStatus.collectAsState()
    
    var messageText by remember { mutableStateOf("") }
    var sovereignSecret by remember { mutableStateOf(activeSecret) }
    var transferProgress by remember { mutableStateOf(0f) }
    var heliosEnabled by remember { mutableStateOf(false) }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (sovereignSecret.isNotBlank()) {
                val packets = shadowMesh.broadcastSovereignFile(it, sovereignSecret) { progress ->
                    transferProgress = progress / 100f
                }
                packets.forEach { p -> iotBridge.publishSignal("sci_os/v6/phantom/delta", p) }
                globalKnowledge.logEvent("MESH_SEND", "Fișier trimis: ${it.lastPathSegment}", 3)
                Toast.makeText(context, "Transfer fișier inițiat...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(sovereignSecret) {
        globalKnowledge.updateSovereignSecret(sovereignSecret)
    }

    val meshMessages = events.filter { 
        it.module == "DECRYPTOR" || it.module == "MESH_SEND" || it.module == "MESH_RECEIVE" || 
        it.module == "FILE_TRANSFER" || it.module == "LIFI_SEND" || it.module == "HELIOS" || it.module == "ANR_RELAY"
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SOVEREIGN MESH V7", color = Color.Cyan, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        val displayStatus = if (meshStatus.isDarkRelayActive) "ONLINE [RONAQCI ACTIVE]" else "INITIALIZING..."
                        Text(displayStatus, color = if(meshStatus.isDarkRelayActive) Color.Green else Color.Red, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            heliosEnabled = !heliosEnabled
                            heliosSync.initiateHeliosLink(heliosEnabled) 
                        },
                        modifier = Modifier.background(if (heliosEnabled) Color(0xFFFFD600).copy(0.2f) else Color.Transparent, CircleShape)
                    ) {
                        Icon(Icons.Default.WbSunny, null, tint = if (heliosEnabled) Color(0xFFFFD600) else Color.Gray)
                    }
                    IconButton(
                        onClick = { lifiService.toggleOpticalLink(!isLiFiActive) },
                        modifier = Modifier.background(if (isLiFiActive) Color.Yellow.copy(0.2f) else Color.Transparent, CircleShape)
                    ) {
                        Icon(Icons.Default.FlashlightOn, null, tint = if (isLiFiActive) Color.Yellow else Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black.copy(0.9f))
            )
        },
        bottomBar = {
            Surface(color = Color.Black.copy(0.95f), modifier = Modifier.imePadding()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (transferProgress > 0f && transferProgress < 1f) {
                        LinearProgressIndicator(
                            progress = transferProgress,
                            modifier = Modifier.fillMaxWidth().height(2.dp).padding(bottom = 8.dp),
                            color = Color.Cyan,
                            trackColor = Color.DarkGray
                        )
                    }

                    TextField(
                        value = sovereignSecret,
                        onValueChange = { sovereignSecret = it },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        placeholder = { Text("Introdu Secretul Suveran...", color = Color.Gray, fontSize = 10.sp) },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color.Cyan, modifier = Modifier.size(16.dp)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Red.copy(0.1f),
                            unfocusedContainerColor = Color.Black,
                            cursorColor = Color.Cyan,
                            focusedTextColor = Color.Yellow
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { fileLauncher.launch("*/*") },
                            modifier = Modifier.background(Color.DarkGray.copy(0.3f), CircleShape)
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.Cyan)
                        }
                        
                        Spacer(Modifier.width(8.dp))

                        TextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Mesaj fantomă...", color = Color.Gray) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.DarkGray.copy(0.2f),
                                cursorColor = Color.Cyan,
                                focusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (messageText.isNotBlank() && sovereignSecret.isNotBlank()) {
                                    shadowMesh.sendSovereignMessage(
                                        text = messageText,
                                        secret = sovereignSecret,
                                        useLiFi = isLiFiActive,
                                        useHelios = heliosEnabled
                                    )
                                    messageText = ""
                                }
                            },
                            enabled = sovereignSecret.length >= 4,
                            modifier = Modifier.background(if (sovereignSecret.length >= 4) Color.Cyan.copy(0.2f) else Color.Gray.copy(0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.Send, null, tint = if (sovereignSecret.length >= 4) Color.Cyan else Color.Gray)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF020405), Color(0xFF0A1015)))).padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                reverseLayout = true
            ) {
                items(meshMessages.reversed()) { msg ->
                    MeshMessageBubble(msg)
                }
            }
        }
    }
}

@Composable
fun MeshMessageBubble(event: com.chemscanner.omniscient.marrow.repository.SystemEvent) {
    val isIncoming = event.module == "DECRYPTOR" || event.module == "MESH_RECEIVE" || event.module == "FILE_TRANSFER" || event.module == "ANR_RELAY"
    val isFile = event.module == "FILE_TRANSFER" || event.description.contains("Fișier trimis")
    val isLiFi = event.module == "LIFI_SEND" || event.module == "HELIOS"
    
    val bubbleColor = when {
        isFile -> Color(0xFF1B3A3B)
        isIncoming -> Color(0xFF0B1A25)
        else -> Color(0xFF1A1025)
    }
    val borderColor = when {
        isFile -> Color.Green.copy(0.6f)
        isIncoming -> Color.Cyan.copy(0.4f)
        else -> Color.Magenta.copy(0.4f)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isIncoming) Alignment.Start else Alignment.End
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (event.module == "HELIOS") "SOLAR CARRIER SIGNAL" else if (isLiFi) "OPTICAL BEAM TRANSMISSION" else if (isFile) "ENCRYPTED FILE STREAM" else if (isIncoming) "SIGNAL DECIPHERED" else "OUTGOING QUANTUM SIGNAL",
                    color = borderColor,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(4.dp))
                
                val displayData = if (event.description.contains("Payload: ")) {
                    event.description.substringAfter("Payload: ")
                } else if (event.description.contains("Deciphered: ")) {
                    event.description.substringAfter("Deciphered: ")
                } else if (event.description.contains("Broadcast: ")) {
                    event.description.substringAfter("Broadcast: ")
                } else {
                    event.description
                }
                
                Text(
                    text = displayData,
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }
    }
}
