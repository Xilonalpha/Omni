package com.chemscanner.omniscient.ui.compose.screens

import android.content.Intent
import android.graphics.Color
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.chemscanner.omniscient.R
import com.chemscanner.omniscient.marrow.data.models.ScanHistory // FIXED IMPORT
import com.chemscanner.omniscient.ui.MoleculeViewActivity
import com.chemscanner.omniscient.ui.viewmodels.HistoryDetailUiState
import com.chemscanner.omniscient.ui.viewmodels.HistoryDetailViewModel
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    viewModel: HistoryDetailViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.card_history)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.ai_assistant_back_description))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is HistoryDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is HistoryDetailUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is HistoryDetailUiState.Success -> {
                    HistoryDetailContent(scan = state.scan)
                }
            }
        }
    }
}

@Composable
fun HistoryDetailContent(scan: ScanHistory) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            AsyncImage(
                model = scan.imagePath,
                contentDescription = "Scanned Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(scan.chemicalName, style = MaterialTheme.typography.headlineSmall)
            Text(scan.chemicalFormula, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Row {
                Button(onClick = { 
                    if (!scan.smilesNotation.isNullOrBlank()) {
                        val intent = Intent(context, MoleculeViewActivity::class.java).apply {
                            putExtra("SMILES_STRING", scan.smilesNotation)
                        }
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "SMILES notation not available for 3D model.", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text(stringResource(R.string.detail_view_3d_button))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text(
                stringResource(R.string.detail_sustainability_score_label),
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            val score = scan.sustainabilityScore
            if (score != null) {
                SustainabilityChart(score = score)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        item {
            Text("Result Description", fontWeight = FontWeight.Bold)
            Text(scan.resultDescription ?: "No description.")
            
            val hash = scan.blockchainHash
            if (hash != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Blockchain Notary Seal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(hash, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun SustainabilityChart(score: Double) {
    AndroidView(
        factory = { context ->
            HorizontalBarChart(context).apply {
                setDrawBarShadow(false)
                setDrawValueAboveBar(true)
                description.isEnabled = false
                setPinchZoom(false)
                isDoubleTapToZoomEnabled = false
                setScaleEnabled(false)
                legend.isEnabled = false
                axisLeft.isEnabled = false
                axisRight.isEnabled = false
                xAxis.isEnabled = false
                setTouchEnabled(false)
                axisLeft.axisMinimum = 0f
                axisLeft.axisMaximum = 100f

                val entry = BarEntry(0f, score.toFloat())
                val dataSet = BarDataSet(listOf(entry), "Sustainability Score")

                val chartColor = when {
                    score >= 75 -> android.graphics.Color.GREEN
                    score >= 40 -> android.graphics.Color.YELLOW
                    else -> android.graphics.Color.RED
                }
                dataSet.color = chartColor

                val barData = BarData(dataSet)
                barData.setValueTextSize(14f)
                barData.barWidth = 0.9f

                this.data = barData
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        update = {
            it.invalidate()
        }
    )
}
