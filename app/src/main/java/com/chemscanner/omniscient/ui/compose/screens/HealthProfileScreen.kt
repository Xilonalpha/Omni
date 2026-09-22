package com.chemscanner.omniscient.ui.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chemscanner.omniscient.R
import com.chemscanner.omniscient.ui.viewmodels.HealthProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthProfileScreen(
    viewModel: HealthProfileViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var newAllergy by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.health_profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.ai_assistant_back_description))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.health_profile_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newAllergy,
                    onValueChange = { newAllergy = it },
                    label = { Text(stringResource(R.string.health_profile_add_allergy_label)) },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    viewModel.addAllergy(newAllergy)
                    newAllergy = ""
                }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.health_profile_add_allergy_description))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.health_profile_allergies_list_title),
                style = MaterialTheme.typography.titleMedium
            )
            
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(uiState.allergies) { allergy ->
                    AllergyListItem(allergy = allergy, onRemove = { viewModel.removeAllergy(allergy) })
                }
            }
        }
    }
}

@Composable
private fun AllergyListItem(allergy: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = allergy, style = MaterialTheme.typography.bodyLarge)
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.health_profile_remove_allergy_description, allergy))
        }
    }
}
