package com.chemscanner.omniscient.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.marrow.repository.PharmaGenome

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmaGenomeScreen(
    state: PharmaGenome,
    onSearch: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .padding(16.dp)
    ) {
        Text(
            text = "PHARMA GENOME ANALYZER",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Cyan,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Enter Drug Name (e.g. Aspirin, Ibuprofen)", color = Color.Gray) },
            trailingIcon = {
                IconButton(onClick = { onSearch(searchQuery) }) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Cyan)
                }
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Cyan,
                unfocusedBorderColor = Color.DarkGray,
                cursorColor = Color.Cyan,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (state.drugName.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Results for: ${state.drugName}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Yellow
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.DarkGray)
                    
                    Text("Toxicity Risk: ${(state.toxicityRisk * 100).toInt()}%", color = if(state.toxicityRisk > 0.5) Color.Red else Color.Green)
                    Text("Precision Dosage: ${state.precisionDosage}", color = Color.White)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Target Genes:", fontWeight = FontWeight.Bold, color = Color.Cyan)
                    state.targetGenes.forEach { gene ->
                        Text("- $gene", color = Color.LightGray, modifier = Modifier.padding(start = 8.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("Interactions:", fontWeight = FontWeight.Bold, color = Color.Cyan)
                    state.interactions.forEach { interaction ->
                        Text("- $interaction", color = Color.LightGray, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Waiting for molecular input...", color = Color.DarkGray, fontSize = 18.sp)
            }
        }
    }
}
