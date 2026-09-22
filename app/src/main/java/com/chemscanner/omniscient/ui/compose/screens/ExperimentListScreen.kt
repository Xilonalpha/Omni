package com.chemscanner.omniscient.ui.compose.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chemscanner.omniscient.R
import com.chemscanner.omniscient.marrow.data.models.Experiment // FIXED IMPORT
import com.chemscanner.omniscient.ui.viewmodels.ExperimentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentListScreen(
    viewModel: ExperimentViewModel = hiltViewModel(),
    onExperimentClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.experiments_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.ai_assistant_back_description))
                    }
                }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            items(uiState.experiments) { experiment ->
                ExperimentListItem(experiment = experiment, onClick = { onExperimentClick(experiment.id) })
            }
        }
    }
}

@Composable
private fun ExperimentListItem(experiment: Experiment, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = experiment.title, style = MaterialTheme.typography.titleMedium)
            Text(text = experiment.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
