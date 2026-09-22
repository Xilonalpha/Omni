package com.chemscanner.omniscient.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsFootball
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.marrow.data.models.Team
import com.chemscanner.omniscient.ui.viewmodels.CareerUiState

@Composable
fun CareerHubScreen(
    state: CareerUiState,
    onPlayMatch: (Team) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text(
            text = "MOD CARIERĂ SUVERAN",
            color = Color.Cyan,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        state.myTeam?.let { team ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.SportsFootball, contentDescription = null, tint = Color.Cyan)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(team.name, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Buget: ${state.budget} €", color = Color.LightGray, fontSize = 12.sp)
                    }
                }
            }
        }

        Text("MECIUL URMĂTOR", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

        state.nextOpponent?.let { opponent ->
            Button(
                onClick = { onPlayMatch(opponent) },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan, contentColor = Color.Black)
            ) {
                Text("JOACĂ ÎMPOTRIVA ${opponent.name}", fontWeight = FontWeight.ExtraBold)
            }
        } ?: Text("Căutăm adversar în rețeaua Marrow...", color = Color.Gray)

        Spacer(Modifier.height(24.dp))
        
        Text("CLASAMENT ${state.currentLeagueName.uppercase()}", color = Color.White, fontWeight = FontWeight.Bold)
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(state.leagueTable) { index, entry ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("${index + 1}.", color = Color.Cyan, modifier = Modifier.width(30.dp))
                    Text(entry.teamName, color = Color.White, modifier = Modifier.weight(1f))
                    Text("${entry.points}p", color = Color.Yellow)
                }
            }
        }
    }
}
