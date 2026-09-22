package com.chemscanner.omniscient.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.marrow.utils.LanguageManager // FIXED IMPORT
import dagger.hilt.android.AndroidEntryPoint

data class Element(
    val symbol: String,
    val name: String,
    val number: Int,
    val weight: Double,
    val config: String,
    val category: String,
    val color: Color,
    val state: String // Solid, Liquid, Gas
)

@AndroidEntryPoint
class PeriodicTableActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PeriodicTableScreen(onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodicTableScreen(onBack: () -> Unit) {
    val elements = getFullPeriodicElements()
    var selectedElement by remember { mutableStateOf<Element?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF04090B), Color(0xFF0D1B1E))
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MATRICEA ELEMENTELOR", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White)
            )
        },
        containerColor = Color(0xFF04090B)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(backgroundGradient).padding(padding)) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                
                // Legendă mică
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    LegendItem("Metale", Color(0xFFFF5252))
                    LegendItem("Non-metale", Color(0xFF00E5FF))
                    LegendItem("Gaze", Color(0xFFE040FB))
                }

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(60.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(elements) { element ->
                        ElementCardSmall(element) {
                            selectedElement = element
                            showSheet = true
                        }
                    }
                }
            }

            if (showSheet && selectedElement != null) {
                ModalBottomSheet(
                    onDismissRequest = { showSheet = false },
                    sheetState = sheetState,
                    containerColor = Color(0xFF162C3A),
                    contentColor = Color.White
                ) {
                    ElementDetailContent(selectedElement!!)
                }
            }
        }
    }
}

@Composable
fun ElementDetailContent(element: Element) {
    Column(modifier = Modifier.padding(24.dp).padding(bottom = 40.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(80.dp),
                color = element.color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, element.color)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(element.symbol, fontSize = 32.sp, fontWeight = FontWeight.Black, color = element.color)
                }
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text(element.name.uppercase(), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text(element.category, color = element.color, fontSize = 14.sp)
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DetailInfo("NUMĂR ATOMIC", element.number.toString())
            DetailInfo("MASĂ ATOMICĂ", element.weight.toString())
        }
        
        Spacer(Modifier.height(16.dp))
        
        DetailInfo("CONFIGURAȚIE", element.config)
        
        Spacer(Modifier.height(16.dp))
        
        DetailInfo("STARE (STP)", element.state)
    }
}

@Composable
fun DetailInfo(label: String, value: String) {
    Column {
        Text(label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ElementCardSmall(element: Element, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.aspectRatio(1f).clickable { onClick() },
        color = element.color.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, element.color.copy(alpha = 0.2f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(element.symbol, color = element.color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(element.number.toString(), modifier = Modifier.align(Alignment.TopStart).padding(4.dp), fontSize = 8.sp, color = Color.Gray)
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(4.dp))
        Text(label, color = Color.Gray, fontSize = 10.sp)
    }
}

fun getFullPeriodicElements() = listOf(
    Element("H", "Hidrogen", 1, 1.008, "1s1", "Non-metal", Color(0xFF00E5FF), "Gaz"),
    Element("He", "Heliu", 2, 4.0026, "1s2", "Gaz Nobil", Color(0xFFE040FB), "Gaz"),
    Element("Li", "Litiu", 3, 6.94, "[He] 2s1", "Metal Alcalin", Color(0xFFFF5252), "Solid"),
    Element("Be", "Beriliu", 4, 9.0122, "[He] 2s2", "Metal Alcalino-pământos", Color(0xFFFFAB40), "Solid"),
    Element("B", "Bor", 5, 10.81, "[He] 2s2 2p1", "Metaloid", Color(0xFF00E676), "Solid"),
    Element("C", "Carbon", 6, 12.011, "[He] 2s2 2p2", "Non-metal", Color(0xFF00E5FF), "Solid"),
    Element("N", "Azot", 7, 14.007, "[He] 2s2 2p3", "Non-metal", Color(0xFF00E5FF), "Gaz"),
    Element("O", "Oxigen", 8, 15.999, "[He] 2s2 2p4", "Non-metal", Color(0xFF00E5FF), "Gaz"),
    Element("F", "Fluor", 9, 18.998, "[He] 2s2 2p5", "Halogen", Color(0xFFFFD600), "Gaz"),
    Element("Ne", "Neon", 10, 20.180, "[He] 2s2 2p6", "Gaz Nobil", Color(0xFFE040FB), "Gaz"),
    Element("Na", "Sodiu", 11, 22.990, "[Ne] 3s1", "Metal Alcalin", Color(0xFFFF5252), "Solid"),
    Element("Mg", "Magneziu", 12, 24.305, "[Ne] 3s2", "Metal Alcalino-pământos", Color(0xFFFFAB40), "Solid"),
    Element("Al", "Aluminiu", 13, 26.982, "[Ne] 3s2 3p1", "Metal post-tranziție", Color(0xFF40C4FF), "Solid"),
    Element("Si", "Siliciu", 14, 28.085, "[Ne] 3s2 3p2", "Metaloid", Color(0xFF00E676), "Solid"),
    Element("P", "Fosfor", 15, 30.974, "[Ne] 3s2 3p3", "Non-metal", Color(0xFF00E5FF), "Solid"),
    Element("S", "Sulf", 16, 32.06, "[Ne] 3s2 3p4", "Non-metal", Color(0xFF00E5FF), "Solid"),
    Element("Cl", "Clor", 17, 35.45, "[Ne] 3s2 3p5", "Halogen", Color(0xFFFFD600), "Gaz"),
    Element("Ar", "Argon", 18, 39.948, "[Ne] 3s2 3p6", "Gaz Nobil", Color(0xFFE040FB), "Gaz"),
    Element("K", "Potasiu", 19, 39.098, "[Ar] 4s1", "Metal Alcalin", Color(0xFFFF5252), "Solid"),
    Element("Ca", "Calciu", 20, 40.078, "[Ar] 4s2", "Metal Alcalino-pământos", Color(0xFFFFAB40), "Solid"),
    Element("Fe", "Fier", 26, 55.845, "[Ar] 3d6 4s2", "Metal de tranziție", Color(0xFFFFEA00), "Solid"),
    Element("Cu", "Cupru", 29, 63.546, "[Ar] 3d10 4s1", "Metal de tranziție", Color(0xFFFFEA00), "Solid"),
    Element("Ag", "Argint", 47, 107.87, "[Kr] 4d10 5s1", "Metal de tranziție", Color(0xFFFFEA00), "Solid"),
    Element("Au", "Aur", 79, 196.97, "[Xe] 4f14 5d10 6s1", "Metal de tranziție", Color(0xFFFFD700), "Solid"),
    Element("Hg", "Mercur", 80, 200.59, "[Xe] 4f14 5d10 6s2", "Metal de tranziție", Color(0xFFFFEA00), "Lichid"),
    Element("Pb", "Plumb", 82, 207.2, "[Xe] 4f14 5d10 6s2 6p2", "Metal post-tranziție", Color(0xFF40C4FF), "Solid"),
    Element("U", "Uraniu", 92, 238.03, "[Rn] 5f3 6d1 7s2", "Actinid", Color(0xFFD4E157), "Solid")
)
