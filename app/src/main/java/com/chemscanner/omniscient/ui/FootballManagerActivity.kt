package com.chemscanner.omniscient.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.marrow.data.models.Team // FIXED IMPORT
import com.chemscanner.omniscient.marrow.data.models.Stadium // FIXED IMPORT
import com.chemscanner.omniscient.ui.viewmodels.FootballManagerViewModel
import com.chemscanner.omniscient.marrow.utils.LanguageManager // FIXED IMPORT
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FootballManagerActivity : AppCompatActivity() {

    private val viewModel: FootballManagerViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FootballManagerScreen(
                viewModel = viewModel,
                onBack = { finish() },
                onStartMatch = { userTeam, opponentTeam, stadium ->
                    val intent = Intent(this, QuantumFootballActivity::class.java).apply {
                        putExtra("USER_TEAM_ID", userTeam.id)
                        putExtra("OPPONENT_TEAM_ID", opponentTeam.id)
                        putExtra("STADIUM_ID", stadium.id)
                    }
                    startActivity(intent)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FootballManagerScreen(
    viewModel: FootballManagerViewModel,
    onBack: () -> Unit,
    onStartMatch: (Team, Team, Stadium) -> Unit // FIXED TYPE
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("FOOTBALL MANAGER PRO", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color(0xFF020508)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. SELECT YOUR TEAM
            item {
                SectionHeader("SELECT YOUR CLUB")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.teams) { team ->
                        ManagerTeamCard(team, isSelected = uiState.selectedUserTeam?.id == team.id) {
                            viewModel.selectUserTeam(team)
                        }
                    }
                }
            }

            // 2. SELECT OPPONENT
            item {
                SectionHeader("CHOOSE ADVERSARY")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.teams.filter { it.id != uiState.selectedUserTeam?.id }) { team ->
                        ManagerTeamCard(team, isSelected = uiState.selectedOpponentTeam?.id == team.id) {
                            viewModel.selectOpponentTeam(team)
                        }
                    }
                }
            }

            // 3. STADIUM SELECTION
            item {
                SectionHeader("SELECT ARENA")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.stadiums) { stadium ->
                        StadiumCard(stadium, isSelected = uiState.selectedStadium?.id == stadium.id) {
                            viewModel.selectStadium(stadium)
                        }
                    }
                }
            }

            // 4. TEAM SQUAD PREVIEW
            uiState.selectedUserTeam?.let { team ->
                item {
                    SectionHeader("SQUAD: ${team.name}")
                    // Mocking players display as it was expected in original code
                    // If Team model doesn't have players, we can add a placeholder or update model
                    Text("Squad analysis in progress...", color = Color.Gray, fontSize = 12.sp)
                }
            }

            // 5. START MATCH BUTTON
            item {
                Button(
                    onClick = { 
                        if (uiState.selectedUserTeam != null && uiState.selectedOpponentTeam != null && uiState.selectedStadium != null) {
                            onStartMatch(uiState.selectedUserTeam!!, uiState.selectedOpponentTeam!!, uiState.selectedStadium!!)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.SportsSoccer, null)
                    Spacer(Modifier.width(12.dp))
                    Text("ENTER ARENA", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(title, color = Color.Cyan, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
fun ManagerTeamCard(team: Team, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(140.dp, 100.dp).clickable { onClick() },
        color = if (isSelected) Color(team.primaryColor).copy(0.2f) else Color.White.copy(0.05f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, if (isSelected) Color(team.primaryColor) else Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(modifier = Modifier.size(32.dp).background(Color(team.primaryColor), CircleShape).border(1.dp, Color.White, CircleShape))
            Spacer(Modifier.height(8.dp))
            Text(team.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StadiumCard(stadium: Stadium, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(160.dp, 80.dp).clickable { onClick() },
        color = if (isSelected) Color.Cyan.copy(0.1f) else Color.White.copy(0.05f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color.Cyan else Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.Center) {
            Text(stadium.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("${stadium.capacity} SEATS", color = Color.Gray, fontSize = 10.sp)
            Text(stadium.city, color = Color.Cyan, fontSize = 8.sp, fontWeight = FontWeight.Black)
        }
    }
}
