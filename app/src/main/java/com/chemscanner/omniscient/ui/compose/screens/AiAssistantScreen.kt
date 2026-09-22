package com.chemscanner.omniscient.ui.compose.screens

import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chemscanner.omniscient.marrow.data.models.ChatMessage
import com.chemscanner.omniscient.ui.viewmodels.AiAssistantViewModel
import com.chemscanner.omniscient.ui.viewmodels.AiModel

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AiAssistantScreen(
    viewModel: AiAssistantViewModel,
    onBack: () -> Unit,
    onNavigateToHealthProfile: () -> Unit,
    onNavigateToProductScanner: () -> Unit,
    onNavigateToExperiments: () -> Unit,
    onNavigateToProblemSolver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messages = uiState.messages
    var userInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    
    val sheetState = rememberModalBottomSheetState()
    var showToolsSheet by remember { mutableStateOf(false) }
    var showDesignDialog by remember { mutableStateOf(false) }
    var showIngredientsDialog by remember { mutableStateOf(false) }
    var showModelSelector by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val cursor = context.contentResolver.query(it, null, null, null, null)
            val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor?.moveToFirst()
            val fileName = nameIndex?.let { i -> cursor.getString(i) } ?: "file"
            cursor?.close()
            viewModel.onFileSelected(it, fileName)
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MeshGradientBackground()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("ANA", color = Color.Cyan, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp))
                                Spacer(Modifier.width(8.dp))
                                AiOrbIndicator(uiState.isThinking || uiState.isLiveS2SActive)
                            }
                            Text(
                                text = "CORE: ${uiState.selectedModel.name}",
                                color = if(uiState.isOfflineModeActive) Color.Red else Color.Cyan.copy(0.6f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.Close, null, tint = Color.White) } },
                    actions = {
                        IconButton(onClick = { showModelSelector = true }) { 
                            Icon(Icons.Default.SettingsSuggest, null, tint = Color.Cyan) 
                        }
                        IconButton(onClick = { viewModel.clearChat() }) { 
                            Icon(Icons.Default.DeleteSweep, null, tint = Color.White.copy(0.6f)) 
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 20.dp)
                ) {
                    if (messages.size <= 1) {
                        item {
                            SampleQuestionsRow(questions = viewModel.suggestQuestions()) { 
                                userInput = it
                                viewModel.sendMessage(it)
                            }
                        }
                    }

                    items(messages) { message ->
                        ModernChatMessageItem(message = message, onSpeak = { viewModel.speakMessage(message) })
                    }
                    
                    if (uiState.isThinking) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.CenterStart) {
                                LinearProgressIndicator(modifier = Modifier.width(60.dp).height(2.dp), color = Color.Cyan, trackColor = Color.Transparent)
                            }
                        }
                    }

                    if (uiState.isLiveS2SActive) {
                        item {
                            S2SWaveVisualizer()
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        
                        // Scanned Molecule Analysis
                        AnimatedVisibility(
                            visible = uiState.scannedMoleculeName != null,
                            enter = slideInVertically { it } + fadeIn(),
                            exit = slideOutVertically { it } + fadeOut()
                        ) {
                            Column {
                                Text(
                                    "ANALIZĂ SMART: ${uiState.scannedMoleculeName}",
                                    color = Color.Cyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    item { ContextChip("Sustenabilitate", Icons.Default.Eco) { viewModel.generateSustainabilityReport(uiState.scannedMoleculeName!!) } }
                                    item { ContextChip("Patente", Icons.Default.Gavel) { viewModel.analyzePatents(uiState.scannedMoleculeName!!) } }
                                    item { ContextChip("Proprietăți", Icons.Default.QueryStats) { viewModel.predictProperties(uiState.scannedMoleculeName!!) } }
                                    item { ContextChip("Siguranță", Icons.Default.Shield) { viewModel.getHealthAndSafetyInfo(uiState.scannedMoleculeName!!) } }
                                    item { ContextChip("Rețea", Icons.Default.Hub) { viewModel.generateChemicalNetworkReport(uiState.scannedMoleculeName!!) } }
                                }
                            }
                        }

                        // Selected File Preview
                        AnimatedVisibility(visible = uiState.selectedFileUri != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .background(Color.Cyan.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color.Cyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AttachFile, contentDescription = "File Attached", tint = Color.Cyan)
                                Spacer(Modifier.width(8.dp))
                                Text(uiState.selectedFileName ?: "Fișier selectat", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = { viewModel.onFileSelected(Uri.EMPTY, "") },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close, 
                                        contentDescription = "Remove File", 
                                        tint = Color.Gray
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                .border(1.dp, if (uiState.isLiveS2SActive) Color(0xFFFFD700) else Color.Cyan.copy(alpha = 0.3f), CircleShape)
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { showToolsSheet = true },
                                modifier = Modifier.background(Color.Cyan.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(Icons.Default.Add, null, tint = Color.Cyan)
                            }

                            TextField(
                                value = userInput,
                                onValueChange = { userInput = it },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Medium),
                                placeholder = { Text(if (uiState.isLiveS2SActive) "ANA te ascultă live..." else "Întreabă orice (fără limite)...", color = Color.Gray, fontSize = 14.sp) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = Color.Cyan
                                ),
                                enabled = !uiState.isLiveS2SActive
                            )

                            // NEURAL LIVE LINK BUTTON (2026 S2S)
                            IconButton(
                                onClick = { viewModel.toggleLiveS2S() },
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(36.dp)
                                    .background(if (uiState.isLiveS2SActive) Color(0xFFFFD700) else Color.White.copy(0.1f), CircleShape)
                                    .border(1.dp, if (uiState.isLiveS2SActive) Color.White else Color.Transparent, CircleShape)
                            ) {
                                val s2sScale by animateFloatAsState(if (uiState.isLiveS2SActive) 1.2f else 1f, label = "s2s")
                                Icon(
                                    if (uiState.isLiveS2SActive) Icons.Default.Mic else Icons.Default.MicNone, 
                                    null, 
                                    tint = if (uiState.isLiveS2SActive) Color.Black else Color.White,
                                    modifier = Modifier.size(18.dp).graphicsLayer(scaleX = s2sScale, scaleY = s2sScale)
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .clickable(enabled = !uiState.isLiveS2SActive) {
                                        if (userInput.isNotBlank() || uiState.selectedFileUri != null) {
                                            viewModel.sendMessage(userInput)
                                            userInput = ""
                                        }
                                    },
                                color = if (uiState.isLiveS2SActive) Color.Gray else Color.Cyan
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(20.dp), tint = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showToolsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showToolsSheet = false },
                sheetState = sheetState,
                containerColor = Color(0xFF0D1B1E)
            ) {
                Column(modifier = Modifier.padding(bottom = 32.dp)) {
                    ListItem(
                        headlineContent = { Text("Încarcă fișier pentru analiză", color = Color.White) },
                        leadingContent = { Icon(Icons.Default.UploadFile, null, tint = Color.Cyan) },
                        modifier = Modifier.clickable { 
                            showToolsSheet = false
                            filePickerLauncher.launch("*/*") 
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    // NEW: Analiză Vizuală direct din scule
                    ListItem(
                        headlineContent = { Text("Analiză Vizuală (Experimental)", color = Color.White) },
                        leadingContent = { Icon(Icons.Default.CameraAlt, null, tint = Color.Cyan) },
                        modifier = Modifier.clickable { 
                            showToolsSheet = false
                            // Aici am putea activa camera, momentan lăsăm link-ul către funcție
                            viewModel.analyzeVisualEnvironment(Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888))
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    ToolsMenu { tool ->
                        showToolsSheet = false
                        when(tool) {
                            "SCAN" -> onNavigateToProductScanner()
                            "TUTOR" -> onNavigateToProblemSolver()
                            "DESIGN" -> showDesignDialog = true
                            "INGREDIENTS" -> showIngredientsDialog = true
                            "LAB" -> onNavigateToExperiments()
                            "PROFILE" -> onNavigateToHealthProfile()
                        }
                    }
                }
            }
        }

        if (showModelSelector) {
            ModalBottomSheet(
                onDismissRequest = { showModelSelector = false },
                containerColor = Color(0xFF050C0D)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("SELECTARE MOTOR NEURAL", color = Color.Cyan, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Spacer(Modifier.height(20.dp))
                    AiModel.entries.forEach { model ->
                        val isSelected = uiState.selectedModel == model
                        ListItem(
                            headlineContent = { Text(model.name, color = if(isSelected) Color.Cyan else Color.White, fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingContent = { 
                                Icon(
                                    if(model == AiModel.GEMMA_LOCAL) Icons.Default.Storage else Icons.Default.Cloud, 
                                    null, 
                                    tint = if(isSelected) Color.Cyan else Color.Gray
                                ) 
                            },
                            trailingContent = { if(isSelected) Icon(Icons.Default.Check, null, tint = Color.Cyan) },
                            modifier = Modifier.clickable { 
                                viewModel.selectModel(model)
                                showModelSelector = false
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }

        if (showDesignDialog) {
            ManualInputDialog("Design Molecular", "Descrie cerințele tale de design...", onConfirm = { viewModel.getDesignSuggestions(it); showDesignDialog = false }, onDismiss = { showDesignDialog = false })
        }

        if (showIngredientsDialog) {
            ManualInputDialog("Analiză Ingrediente", "Introdu lista de ingrediente...", onConfirm = { viewModel.analyzeIngredients(it); showIngredientsDialog = false }, onDismiss = { showIngredientsDialog = false })
        }
    }
}

@Composable
fun S2SWaveVisualizer() {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveScale by infiniteTransition.animateFloat(initialValue = 0.8f, targetValue = 1.2f, animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "wave")
    
    Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxWidth(0.6f).height(40.dp)) {
            val centerY = size.height / 2
            val spacing = size.width / 10
            for (i in 0..10) {
                val x = i * spacing
                val height = (20f + (i * 2)) * waveScale
                drawLine(
                    color = Color(0xFFFFD700).copy(alpha = 0.6f),
                    start = Offset(x, centerY - height),
                    end = Offset(x, centerY + height),
                    strokeWidth = 4f
                )
            }
        }
        Text("LEGĂTURĂ NEURALĂ ACTIVĂ", color = Color(0xFFFFD700), fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun MeshGradientBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    val xOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(animation = tween(30000, easing = LinearEasing), repeatMode = RepeatMode.Reverse), label = "x"
    )

    Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFF04090B))) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.12f), Color.Transparent),
                center = Offset(xOffset, 200f),
                radius = 1200f
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF7C4DFF).copy(alpha = 0.08f), Color.Transparent),
                center = Offset(size.width - xOffset, size.height - 300f),
                radius = 1500f
            )
        )
    }
}

@Composable
fun AiOrbIndicator(isThinking: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = if (isThinking) 1.8f else 1.2f,
        animationSpec = infiniteRepeatable(animation = tween(if (isThinking) 600 else 1200, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = if (isThinking) 1f else 0.4f,
        animationSpec = infiniteRepeatable(animation = tween(if (isThinking) 600 else 1200, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "alpha"
    )

    Box(contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(12.dp).graphicsLayer(scaleX = scale, scaleY = scale).background(if (isThinking) Color(0xFFFFD700) else Color.Cyan, CircleShape).alpha(alpha))
        Box(modifier = Modifier.size(6.dp).background(if (isThinking) Color(0xFFFFD700) else Color.Cyan, CircleShape))
    }
}

@Composable
fun ContextChip(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Cyan.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.Cyan, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(text = label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ModernChatMessageItem(message: ChatMessage, onSpeak: () -> Unit) {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val clipboardManager = LocalClipboardManager.current
    
    val bubbleColor = if (isUser) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
    
    Box(modifier = Modifier.fillMaxWidth().combinedClickable(onLongClick = { clipboardManager.setText(AnnotatedString(message.content)) }, onClick = {}), contentAlignment = alignment) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = if (isUser) 20.dp else 4.dp, bottomEnd = if (isUser) 4.dp else 20.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, if (isUser) Color.Cyan.copy(0.3f) else Color.White.copy(0.1f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (!isUser) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null, tint = Color.Cyan, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("ANA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Cyan)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = onSpeak, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
                Text(text = message.content, color = Color.White, fontSize = 15.sp, lineHeight = 22.sp)
            }
        }
    }
}

@Composable
fun ToolsMenu(onToolClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(24.dp)) {
        Text("LABORATOR INTELIGENT", color = Color.Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ToolItem(Icons.Default.QrCodeScanner, "Scaner", Color(0xFF00E5FF)) { onToolClick("SCAN") }
            ToolItem(Icons.Default.Calculate, "Tutor", Color(0xFFFFD600)) { onToolClick("TUTOR") }
            ToolItem(Icons.Default.Draw, "Design", Color(0xFFE040FB)) { onToolClick("DESIGN") }
        }
        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ToolItem(Icons.AutoMirrored.Filled.FactCheck, "Ingrediente", Color(0xFF00E676)) { onToolClick("INGREDIENTS") }
            ToolItem(Icons.Default.Science, "Laborator", Color(0xFFFF5252)) { onToolClick("LAB") }
            ToolItem(Icons.Default.Badge, "Profil", Color.White) { onToolClick("PROFILE") }
        }
    }
}

@Composable
fun ToolItem(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)).border(1.dp, color.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualInputDialog(title: String, label: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(label, color = Color.Gray) },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                textStyle = TextStyle(color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Cyan,
                    unfocusedBorderColor = Color.DarkGray,
                    cursorColor = Color.Cyan
                ),
                shape = RoundedCornerShape(16.dp)
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(text) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan, contentColor = Color.Black)) {
                Text("Procesează")
            }
        },
        containerColor = Color(0xFF0D1B1E)
    )
}

@Composable
fun SampleQuestionsRow(questions: List<String>, onClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text("ÎNCEARCĂ SĂ ÎNTREBI:", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(questions) { q ->
                Surface(
                    modifier = Modifier.clickable { onClick(q) },
                    color = Color.White.copy(0.05f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.1f))
                ) {
                    Text(q, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
                }
            }
        }
    }
}
