package com.chemscanner.omniscient.ui.compose.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.marrow.utils.XilonDiscovery
import com.chemscanner.omniscient.ui.viewmodels.XilonProfViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * XILON PROF MANAGER v2.0.
 * AUTHORITY: ARCHITECT XILON.
 * v2.0: Added COSMIC SEAL display for RAW data verification.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XilonProfScreen(
    viewModel: XilonProfViewModel,
    onBack: () -> Unit
) {
    val discoveries by viewModel.discoveries.collectAsState()
    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text("XILON PROF MANAGER", 
                        color = Color.Cyan, 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearHistory() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.Red.copy(alpha = 0.7f))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.9f)
                )
            )
        },
        containerColor = Color(0xFF020405)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF020405), Color(0xFF0A1015))))
                .padding(padding)
        ) {
            if (discoveries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.WorkspacePremium, 
                            null, 
                            modifier = Modifier.size(64.dp), 
                            tint = Color.Gray.copy(alpha = 0.3f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("NO SOVEREIGN DISCOVERIES RECORDED", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(discoveries) { discovery ->
                        DiscoveryCard(discovery, dateFormat, context)
                    }
                }
            }
        }
    }
}

@Composable
fun DiscoveryCard(discovery: XilonDiscovery, dateFormat: SimpleDateFormat, context: Context) {
    val accentColor = when (discovery.importance) {
        in 4..5 -> Color.Red
        3 -> Color.Yellow
        else -> Color.Cyan
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.03f),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    discovery.module,
                    color = accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.weight(1f))
                Text(
                    dateFormat.format(Date(discovery.timestamp)),
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    discovery.content,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(end = 40.dp)
                )
                
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Xilon Discovery", discovery.content)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.align(Alignment.TopEnd).size(32.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = Color.Gray.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // COSMIC SEAL SECTION: Displaying RAW Context Data
            if (!discovery.cosmicContext.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = Color.Cyan.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.Cyan.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.WorkspacePremium,
                            null,
                            tint = Color.Cyan.copy(alpha = 0.6f),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "COSMIC SEAL: ${discovery.cosmicContext}",
                            color = Color.Cyan.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { index ->
                    Icon(
                        Icons.Default.WorkspacePremium,
                        null,
                        modifier = Modifier.size(12.dp),
                        tint = if (index < discovery.importance) accentColor else Color.Gray.copy(alpha = 0.2f)
                    )
                    Spacer(Modifier.width(2.dp))
                }
            }
        }
    }
}
