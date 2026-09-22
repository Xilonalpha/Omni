package com.chemscanner.omniscient.ui.compose.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chemscanner.omniscient.marrow.data.models.ScanHistory // FIXED IMPORT
import com.chemscanner.omniscient.ui.HistoryDetailActivity
import com.chemscanner.omniscient.ui.viewmodels.HistoryUiState
import com.chemscanner.omniscient.ui.viewmodels.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ARHIVA AKASHA", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black, titleContentColor = Color.White)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is HistoryUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.Cyan)
                }
                is HistoryUiState.Success -> {
                    if (state.scans.isEmpty()) {
                        EmptyHistoryPlaceholder()
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.scans) { scan ->
                                HistoryItemCard(scan) {
                                    val intent = Intent(context, HistoryDetailActivity::class.java).apply {
                                        putExtra("EXTRA_SCAN_ID", scan.id.toString()) // FIXED: using String ID
                                    }
                                    context.startActivity(intent)
                                }
                            }
                        }
                    }
                }
                is HistoryUiState.Error -> {
                    Text(text = state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(scan: ScanHistory, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(scan.chemicalName.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(scan.scanType, color = Color.Cyan, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
            Text(
                SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(scan.scanDate)),
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun EmptyHistoryPlaceholder() {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.History, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text("Arhiva este goală. Începe o scanare!", color = Color.Gray)
    }
}
