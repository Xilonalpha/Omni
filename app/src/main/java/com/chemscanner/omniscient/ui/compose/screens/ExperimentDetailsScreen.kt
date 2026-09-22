package com.chemscanner.omniscient.ui.compose.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chemscanner.omniscient.R
import com.chemscanner.omniscient.marrow.data.models.ExperimentType // FIXED IMPORT
import com.chemscanner.omniscient.ui.viewmodels.ExperimentDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentDetailsScreen(
    viewModel: ExperimentDetailsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onStartARExperiment: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val experiment = uiState.experiment
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(experiment?.title ?: stringResource(R.string.experiment_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.ai_assistant_back_description))
                    }
                },
                actions = {
                    if (uiState.guidedSteps.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.exportReport { file ->
                                if (file != null) {
                                    Toast.makeText(context, "Report saved: ${file.name}", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Failed to export report", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (experiment != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                item {
                    Text(text = experiment.description, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = stringResource(R.string.materials_needed_title), style = MaterialTheme.typography.titleMedium)
                }
                items(experiment.materials) { material ->
                    Text(text = "- $material", style = MaterialTheme.typography.bodyMedium)
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    if (experiment.type == ExperimentType.HANDS_ON) {
                        Button(
                            onClick = { viewModel.startExperiment() },
                            enabled = !uiState.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.start_experiment_button))
                        }
                    }
                    if (experiment.type == ExperimentType.AR_DEMO) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onStartARExperiment(experiment.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Start AR Demonstration")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (uiState.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else {
                    items(uiState.guidedSteps) { step ->
                        Text(text = step, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
