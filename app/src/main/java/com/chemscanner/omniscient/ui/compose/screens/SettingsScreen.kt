package com.chemscanner.omniscient.ui.compose.screens

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chemscanner.omniscient.ui.MainActivity
import com.chemscanner.omniscient.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val context = LocalContext.current as Activity

    var showLanguageMenu by remember { mutableStateOf(false) }
    var showAiKeyDialog by remember { mutableStateOf<String?>(null) }
    var saveScans by remember { mutableStateOf(viewModel.shouldSaveScans()) }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF04090B), Color(0xFF0D1B1E))
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "CENTRU CONTROL", 
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF04090B)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().background(backgroundGradient).padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // --- MATRICEA AI (NEW SUPREME SECTION) ---
                Text(
                    "MATRICEA DE INTELIGENȚĂ", 
                    color = Color.Magenta.copy(alpha = 0.6f), 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                SettingsCard {
                    SettingsClickItem(
                        icon = Icons.Default.AutoAwesome,
                        title = "Google Gemini Pool",
                        value = "3 Keys Active",
                        onClick = { showAiKeyDialog = "GEMINI" }
                    )
                    Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsClickItem(
                        icon = Icons.Default.Memory,
                        title = "OpenAI Matrix",
                        value = "GPT-4o Ready",
                        onClick = { showAiKeyDialog = "OPENAI" }
                    )
                    Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsClickItem(
                        icon = Icons.Default.Psychology,
                        title = "Grok-X.AI Portal",
                        value = "Grok-2 Ready",
                        onClick = { showAiKeyDialog = "GROK" }
                    )
                }

                Text(
                    "PREFERINȚE SISTEM", 
                    color = Color.Cyan.copy(alpha = 0.5f), 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                SettingsCard {
                    SettingsToggleItem(
                        icon = Icons.Default.History,
                        title = "Salvare Automată",
                        subtitle = "Salvează scanările în arhivă",
                        checked = saveScans,
                        onCheckedChange = {
                            saveScans = it
                            viewModel.onSaveScansToggled(it)
                        }
                    )
                    
                    Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsClickItem(
                        icon = Icons.Default.Language,
                        title = "Limbă Aplicație",
                        value = viewModel.getCurrentLanguageName(),
                        onClick = { showLanguageMenu = true }
                    )
                }

                Text(
                    "SECURITATE ȘI ARHIVĂ", 
                    color = Color.Yellow.copy(alpha = 0.5f), 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                SettingsCard {
                    SettingsClickItem(
                        icon = Icons.Default.CloudUpload,
                        title = "Export Arhiva Akasha",
                        value = "Manual Backup",
                        onClick = { viewModel.exportDatabase() }
                    )
                    
                    Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsClickItem(
                        icon = Icons.Default.CloudDownload,
                        title = "Import Arhiva Akasha",
                        value = "Restore Data",
                        onClick = { viewModel.importDatabase() }
                    )
                }

                Text(
                    "INFORMAȚII", 
                    color = Color.Cyan.copy(alpha = 0.5f), 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                SettingsCard {
                    SettingsClickItem(
                        icon = Icons.Default.Info,
                        title = "Versiune Software",
                        value = "v16.1-OMEGA",
                        onClick = {}
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
                
                Text(
                    "Omniscient Intelligence Engine",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = Color.DarkGray,
                    fontSize = 10.sp
                )
            }

            // DIALOG PENTRU INTRODUCERE CHEI
            if (showAiKeyDialog != null) {
                AiKeyDialog(
                    provider = showAiKeyDialog!!,
                    onDismiss = { showAiKeyDialog = null },
                    onSave = { key, index -> 
                        viewModel.saveCustomKey(showAiKeyDialog!!, key, index)
                        showAiKeyDialog = null
                    }
                )
            }

            if (showLanguageMenu) {
                AlertDialog(
                    onDismissRequest = { showLanguageMenu = false },
                    title = { Text("Selectează Limba") },
                    text = {
                        Column {
                            viewModel.availableLanguages.forEach { (name, code) ->
                                Text(
                                    text = name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showLanguageMenu = false
                                            viewModel.onLanguageSelected(code)
                                            val intent = Intent(context, MainActivity::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            context.startActivity(intent)
                                        }
                                        .padding(16.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {}
                )
            }
        }
    }
}

@Composable
fun AiKeyDialog(provider: String, onDismiss: () -> Unit, onSave: (String, Int) -> Unit) {
    var key1 by remember { mutableStateOf("") }
    var key2 by remember { mutableStateOf("") }
    var key3 by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0D1B1E),
        title = { Text("Configurare $provider", color = Color.Cyan, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (provider == "GEMINI") {
                    OutlinedTextField(value = key1, onValueChange = { key1 = it }, label = { Text("Gemini Alpha Key") }, textStyle = LocalTextStyle.current.copy(color = Color.White))
                    OutlinedTextField(value = key2, onValueChange = { key2 = it }, label = { Text("Gemini Beta Key") }, textStyle = LocalTextStyle.current.copy(color = Color.White))
                    OutlinedTextField(value = key3, onValueChange = { key3 = it }, label = { Text("Gemini Gamma Key") }, textStyle = LocalTextStyle.current.copy(color = Color.White))
                } else {
                    OutlinedTextField(value = key1, onValueChange = { key1 = it }, label = { Text("$provider Master Key") }, textStyle = LocalTextStyle.current.copy(color = Color.White))
                }
                Text("Cheile sunt criptate local în nucleul Marrow.", fontSize = 10.sp, color = Color.Gray)
            }
        },
        confirmButton = {
            TextButton(onClick = { 
                if (provider == "GEMINI") {
                    if (key1.isNotBlank()) onSave(key1, 1)
                    if (key2.isNotBlank()) onSave(key2, 2)
                    if (key3.isNotBlank()) onSave(key3, 3)
                } else if (key1.isNotBlank()) {
                    onSave(key1, 1)
                }
                onDismiss()
            }) { Text("Sincronizează", color = Color.Cyan, fontWeight = FontWeight.Bold) }
        }
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        content = { Column(content = content) }
    )
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Cyan, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = Color.Cyan,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
fun SettingsClickItem(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.clickable { onClick() }.padding(16.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Cyan, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(value, color = Color.Cyan.copy(alpha = 0.7f), fontSize = 14.sp)
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.DarkGray)
    }
}
