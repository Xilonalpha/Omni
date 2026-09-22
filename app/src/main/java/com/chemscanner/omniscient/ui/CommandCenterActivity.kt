package com.chemscanner.omniscient.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.JwstObservation
import com.chemscanner.omniscient.marrow.repository.ServiceState
import com.chemscanner.omniscient.marrow.repository.VoidState
import com.chemscanner.omniscient.marrow.services.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class CommandCenterActivity : AppCompatActivity() {

    @Inject lateinit var globalKnowledge: GlobalKnowledgeRepository
    @Inject lateinit var copernicusService: CopernicusSatelliteService
    @Inject lateinit var sentinel2Service: Sentinel2SatelliteService
    @Inject lateinit var sciService: ScientificObservatoryService
    @Inject lateinit var defenseService: PlanetaryDefenseService
    @Inject lateinit var chineseService: ChineseSatelliteService
    @Inject lateinit var jwstService: JamesWebbSpaceTelescopeService
    @Inject lateinit var voidService: DeepSpaceVoidService
    
    @Inject lateinit var dsnService: DsnLiveService
    @Inject lateinit var cernService: CernDataService
    @Inject lateinit var rubinService: VeraRubinAlertService
    @Inject lateinit var starlinkService: StarlinkMeshService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContent {
                MaterialTheme {
                    CommandCenterScreen(
                        globalKnowledge = globalKnowledge, 
                        copernicusService = copernicusService, 
                        sentinel2Service = sentinel2Service, 
                        sciService = sciService, 
                        defenseService = defenseService,
                        chineseService = chineseService,
                        jwstService = jwstService,
                        voidService = voidService,
                        dsnService = dsnService,
                        cernService = cernService,
                        rubinService = rubinService,
                        starlinkService = starlinkService
                    ) { finish() }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "CommandCenter: Content set failed")
            finish()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandCenterScreen(
    globalKnowledge: GlobalKnowledgeRepository,
    copernicusService: CopernicusSatelliteService,
    sentinel2Service: Sentinel2SatelliteService,
    sciService: ScientificObservatoryService,
    defenseService: PlanetaryDefenseService,
    chineseService: ChineseSatelliteService,
    jwstService: JamesWebbSpaceTelescopeService,
    voidService: DeepSpaceVoidService,
    dsnService: DsnLiveService,
    cernService: CernDataService,
    rubinService: VeraRubinAlertService,
    starlinkService: StarlinkMeshService,
    onBack: () -> Unit
) {
    val serviceState by globalKnowledge.serviceState.collectAsState(initial = ServiceState())
    val kernelStatus by globalKnowledge.kernelStatus.collectAsState()
    val neuralLoad by globalKnowledge.neuralLoad.collectAsState()
    val worldMeshStatus by globalKnowledge.worldMeshStatus.collectAsState()
    
    val copernicusData by globalKnowledge.copernicusData.collectAsState()
    val sentinel2Data by globalKnowledge.sentinel2Data.collectAsState()
    val solarData by globalKnowledge.solarData.collectAsState()
    val galacticData by globalKnowledge.galacticData.collectAsState()
    val issData by globalKnowledge.issData.collectAsState()
    val defenseData by globalKnowledge.defenseData.collectAsState()
    val chineseData by globalKnowledge.chineseData.collectAsState()
    val russianData by globalKnowledge.russianData.collectAsState()
    val europeanData by globalKnowledge.europeanData.collectAsState()
    val jwstObservations by globalKnowledge.jwstObservations.collectAsState()
    val voidData by globalKnowledge.voidState.collectAsState()

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("STRATEGIC COMMAND", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Text("MESH: ${worldMeshStatus.uppercase()}", color = if(worldMeshStatus.contains("Active")) Color.Green else Color.Cyan.copy(0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black.copy(0.9f))
            )
        },
        containerColor = Color(0xFF010203)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(modifier = Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF001220), Color.Transparent), radius = 1000f)))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    CommandHeader("SENSORS & SCANNING CORE", neuralLoad)
                }

                // JWST DEEP INTERCEPT (RAW DATA 2026)
                item { SectionLabel("JWST MAST DEEP INTERCEPT") }
                item {
                    ToggleCard(
                        title = "JWST INTERCEPTOR",
                        subtitle = "Bypass Public Filters - Raw Archive Access",
                        isActive = serviceState.isJwstActive,
                        icon = Icons.Default.FilterTiltShift,
                        color = Color(0xFFE040FB),
                        modifier = Modifier.fillMaxWidth()
                    ) { 
                        val next = !serviceState.isJwstActive
                        globalKnowledge.updateServiceState(serviceState.copy(isJwstActive = next))
                        if (next) {
                            scope.launch { jwstService.forceImmediateIntercept() }
                        }
                    }
                }

                if (serviceState.isJwstActive) {
                    item { JwstInterceptPanel(jwstObservations) }
                }

                // EXTRAGALACTIC VOID ANALYZER
                item { SectionLabel("EXTRAGALACTIC VOID ANALYZER (NASA NED)") }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { voidService.monitorVoid("Boötes Void") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.05f))
                        ) { Text("BOOTES", fontSize = 10.sp) }
                        Button(
                            onClick = { voidService.monitorVoid("Eridanus Supervoid") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.05f))
                        ) { Text("ERIDANUS", fontSize = 10.sp) }
                    }
                }
                item { VoidTelemetryPanel(voidData) }

                // PLANETARY DEFENSE & SECURITY
                item { SectionLabel("PLANETARY DEFENSE & SECURITY") }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToggleCard(
                            title = "DEFENSE SHIELD",
                            subtitle = "Asteroid Monitor",
                            isActive = serviceState.isPlanetaryDefenseActive,
                            icon = Icons.Default.Security,
                            color = Color(0xFFFF5252),
                            modifier = Modifier.weight(1f)
                        ) {
                            val next = !serviceState.isPlanetaryDefenseActive
                            globalKnowledge.updateServiceState(serviceState.copy(isPlanetaryDefenseActive = next))
                            defenseService.updateServiceStatus(next)
                        }

                        ToggleCard(
                            title = "DRAGON LINK",
                            subtitle = "Chinese CNSA Array",
                            isActive = serviceState.isDragonLinkActive,
                            icon = Icons.Default.Hub,
                            color = Color(0xFFFF1744),
                            modifier = Modifier.weight(1f)
                        ) {
                            val next = !serviceState.isDragonLinkActive
                            globalKnowledge.updateServiceState(serviceState.copy(isDragonLinkActive = next))
                            chineseService.updateServiceStatus(next)
                        }
                    }
                }

                if (serviceState.isPlanetaryDefenseActive) {
                    item { MultiDefenseTelemetryPanel(defenseData) }
                }

                if (serviceState.isDragonLinkActive) {
                    item { ChineseTelemetryPanel(chineseData) }
                }

                // GLOBAL MILITARY & GOV NETWORKS
                item { SectionLabel("GLOBAL MILITARY & GOV NETWORKS") }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToggleCard(
                            title = "KOSMOS LINK",
                            subtitle = "Russian Mil-Net",
                            isActive = serviceState.isKosmosLinkActive,
                            icon = Icons.Default.Language,
                            color = Color(0xFFB0BEC5),
                            modifier = Modifier.weight(1f)
                        ) {
                            val next = !serviceState.isKosmosLinkActive
                            globalKnowledge.updateServiceState(serviceState.copy(isKosmosLinkActive = next))
                        }

                        ToggleCard(
                            title = "GALILEO PRS",
                            subtitle = "European Secure-Net",
                            isActive = serviceState.isGalileoLinkActive,
                            icon = Icons.Default.GpsFixed,
                            color = Color(0xFF1976D2),
                            modifier = Modifier.weight(1f)
                        ) {
                            val next = !serviceState.isGalileoLinkActive
                            globalKnowledge.updateServiceState(serviceState.copy(isGalileoLinkActive = next))
                        }
                    }
                }

                if (serviceState.isKosmosLinkActive) {
                    item { RussianTelemetryPanel(russianData) }
                }
                if (serviceState.isGalileoLinkActive) {
                    item { EuropeanTelemetryPanel(europeanData) }
                }

                // EARTH OBSERVATION ARRAY
                item { SectionLabel("EARTH OBSERVATION ARRAY") }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToggleCard(
                            title = "COPERNICUS",
                            subtitle = "Sentinel-5P Biosphere",
                            isActive = serviceState.isCopernicusActive,
                            icon = Icons.Default.Eco,
                            color = Color(0xFF00E676),
                            modifier = Modifier.weight(1f)
                        ) {
                            val next = !serviceState.isCopernicusActive
                            globalKnowledge.updateServiceState(serviceState.copy(isCopernicusActive = next))
                            copernicusService.updateServiceStatus(next)
                        }

                        ToggleCard(
                            title = "SENTINEL 2",
                            subtitle = "Multispectral Eye",
                            isActive = serviceState.isSentinel2Active,
                            icon = Icons.Default.FilterHdr,
                            color = Color(0xFF81C784),
                            modifier = Modifier.weight(1f)
                        ) {
                            val next = !serviceState.isSentinel2Active
                            globalKnowledge.updateServiceState(serviceState.copy(isSentinel2Active = next))
                            sentinel2Service.updateServiceStatus(next)
                        }
                    }
                }

                if (serviceState.isCopernicusActive || serviceState.isSentinel2Active) {
                    item { CopernicusAndSentinelPanel(serviceState.isCopernicusActive, serviceState.isSentinel2Active, copernicusData, sentinel2Data) }
                }

                // SOLAR & DEEP SPACE OBSERVATORIES
                item { SectionLabel("SOLAR & DEEP SPACE OBSERVATORIES") }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToggleCard(
                            title = "SOLAR (SDO)",
                            subtitle = "Sun Corona Monitor",
                            isActive = serviceState.isSolarActive,
                            icon = Icons.Default.WbSunny,
                            color = Color(0xFFFFAB40),
                            modifier = Modifier.weight(1f)
                        ) {
                            val next = !serviceState.isSolarActive
                            globalKnowledge.updateServiceState(serviceState.copy(isSolarActive = next))
                            sciService.updateSolarStatus(next)
                        }

                        ToggleCard(
                            title = "GALACTIC",
                            subtitle = "Hubble / TESS Link",
                            isActive = serviceState.isHubbleActive,
                            icon = Icons.Default.AutoAwesomeMotion,
                            color = Color(0xFF7C4DFF),
                            modifier = Modifier.weight(1f)
                        ) {
                            val next = !serviceState.isHubbleActive
                            globalKnowledge.updateServiceState(serviceState.copy(isHubbleActive = next, isTessActive = next))
                            sciService.updateGalacticStatus(next)
                        }
                    }
                }

                if (serviceState.isSolarActive) {
                    item { SolarTelemetryPanel(solarData) }
                }

                if (serviceState.isHubbleActive) {
                    item { GalacticTelemetryPanel(galacticData) }
                }

                // ORBITAL INFRASTRUCTURE
                item { SectionLabel("ORBITAL INFRASTRUCTURE") }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToggleCard(
                            title = "ISS TRACKER",
                            subtitle = "Station Telemetry",
                            isActive = serviceState.isIssActive,
                            icon = Icons.Default.Rocket,
                            color = Color(0xFF4FC3F7),
                            modifier = Modifier.weight(1f)
                        ) {
                            val next = !serviceState.isIssActive
                            globalKnowledge.updateServiceState(serviceState.copy(isIssActive = next))
                            sciService.updateIssStatus(next)
                        }

                        ToggleCard(
                            title = "STARLINK",
                            subtitle = "Laser Mesh Constellation",
                            isActive = serviceState.isStarlinkActive,
                            icon = Icons.Default.Wifi,
                            color = Color(0xFF00E5FF),
                            modifier = Modifier.weight(1f)
                        ) {
                            val next = !serviceState.isStarlinkActive
                            globalKnowledge.updateServiceState(serviceState.copy(isStarlinkActive = next))
                            starlinkService.updateMeshStatus(next)
                        }
                    }
                }

                if (serviceState.isIssActive) {
                    item { IssTelemetryPanel(issData) }
                }

                // INTERSTELLAR & PARTICLE LINKS
                item { SectionLabel("INTERSTELLAR & PARTICLE LINKS") }
                item {
                    ToggleCard(
                        title = "DSN SIGNALS",
                        subtitle = "NASA Deep Space Network Live",
                        isActive = serviceState.isDsnActive,
                        icon = Icons.Default.SettingsInputAntenna,
                        color = Color(0xFFFF5252),
                        modifier = Modifier.fillMaxWidth()
                    ) { 
                        val next = !serviceState.isDsnActive
                        globalKnowledge.updateServiceState(serviceState.copy(isDsnActive = next))
                        if (next) dsnService.startDsnMonitoring()
                    }
                }

                item {
                    ToggleCard(
                        title = "CERN PARTICLE STREAMS",
                        subtitle = "Large Hadron Collider Live Data",
                        isActive = serviceState.isCernActive,
                        icon = Icons.Default.BlurCircular,
                        color = Color(0xFF00E676),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val next = !serviceState.isCernActive
                        globalKnowledge.updateServiceState(serviceState.copy(isCernActive = next))
                        if (next) cernService.startMonitoring()
                    }
                }

                item {
                    ToggleCard(
                        title = "VERA RUBIN",
                        subtitle = "Deep Space Transients",
                        isActive = serviceState.isVeraRubinActive,
                        icon = Icons.Default.AutoGraph,
                        color = Color(0xFFFFD600),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val next = !serviceState.isVeraRubinActive
                        globalKnowledge.updateServiceState(serviceState.copy(isVeraRubinActive = next))
                        if (next) rubinService.startAlertStream()
                    }
                }

                item {
                    Spacer(Modifier.height(24.dp))
                    Text("SYSTEM OVERRIDE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }

                item {
                    Button(
                        onClick = {
                            val allOn = !serviceState.isCernActive
                            val newState = ServiceState(
                                isVeraRubinActive = allOn,
                                isDsnActive = allOn,
                                isStarlinkActive = allOn,
                                isJwstActive = allOn,
                                isCernActive = allOn,
                                isCopernicusActive = allOn,
                                isSentinel2Active = allOn,
                                isSolarActive = allOn,
                                isHubbleActive = allOn,
                                isTessActive = allOn,
                                isIssActive = allOn,
                                isPlanetaryDefenseActive = allOn,
                                isDragonLinkActive = allOn,
                                isKosmosLinkActive = allOn,
                                isGalileoLinkActive = allOn
                            )
                            globalKnowledge.updateServiceState(newState)

                            copernicusService.updateServiceStatus(allOn)
                            sentinel2Service.updateServiceStatus(allOn)
                            sciService.updateSolarStatus(allOn)
                            sciService.updateGalacticStatus(allOn)
                            sciService.updateIssStatus(allOn)
                            defenseService.updateServiceStatus(allOn)
                            chineseService.updateServiceStatus(allOn)

                            if (allOn) {
                                dsnService.startDsnMonitoring()
                                cernService.startMonitoring()
                                rubinService.startAlertStream()
                                starlinkService.startMeshAnalysis()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.05f)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.1f))
                    ) {
                        Text(if(serviceState.isCernActive) "DEACTIVATE ALL SYSTEMS" else "INITIALIZE FULL SCANNER ARRAY", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun JwstInterceptPanel(observations: List<JwstObservation>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFE040FB).copy(0.05f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE040FB).copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FilterTiltShift, null, tint = Color(0xFFE040FB), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("MAST ARCHIVE LIVE INTERCEPT", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(12.dp))
            if (observations.isEmpty()) {
                Text("INTERCEPTING RAW MAST STREAM...", color = Color.Gray, fontSize = 10.sp)
            } else {
                observations.forEach { obs ->
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(obs.targetName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                        Text("${obs.instrument} | Filters: ${obs.filters}", color = Color.Cyan, fontSize = 9.sp)
                        if (obs.hypothesis.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text(obs.hypothesis, color = Color.White.copy(0.7f), fontSize = 10.sp, lineHeight = 14.sp)
                        }
                        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = Color.White.copy(0.05f))
                    }
                }
            }
        }
    }
}

@Composable
fun VoidTelemetryPanel(data: VoidState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Cyan.copy(0.05f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Cyan.copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BlurOn, null, tint = Color.Cyan, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("VOID TELEMETRY: ${data.targetRegion.uppercase()}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                CommandTelemetryItem("DENSITY", "${"%.2e".format(data.matterDensity)}", Color.Cyan)
                CommandTelemetryItem("TEMP", "${"%.4f".format(data.backgroundRadiationTemp)} K", Color.White)
                CommandTelemetryItem("ENTROPY", "${(data.entropyLevel * 100).toInt()}%", if(data.entropyLevel > 0.9) Color.Red else Color.Green)
            }
            Spacer(Modifier.height(8.dp))
            Text("DARK ENERGY INTENSITY: ${(data.darkEnergyIntensity * 100).toInt()}%", color = Color.Magenta, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RussianTelemetryPanel(data: com.chemscanner.omniscient.marrow.repository.RussianSatelliteData) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFB0BEC5).copy(0.05f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB0BEC5).copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Language, null, tint = Color(0xFFB0BEC5), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text("KOSMOS / GLONASS INTERCEPT", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(12.dp))
            Text("MIL-STATUS: ${data.militaryStatus}", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("GLONASS PRECISION: ${data.glonassPrecision}", color = Color.Green, fontSize = 9.sp)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                CommandTelemetryItem("ACTIVE SATS", data.activeSats.toString(), Color.White)
                CommandTelemetryItem("LAST FIX", data.lastKosmosCoordinate, Color.Cyan)
                CommandTelemetryItem("SIGNAL", "${(data.signalHealth * 100).toInt()}%", Color.Yellow)
            }
        }
    }
}

@Composable
fun EuropeanTelemetryPanel(data: com.chemscanner.omniscient.marrow.repository.EuropeanSatelliteData) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1976D2).copy(0.05f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1976D2).copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.GpsFixed, null, tint = Color(0xFF1976D2), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text("GALILEO PRS / ESA ARRAY", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(12.dp))
            Text("PRS CRYPTO: ${data.prsStatus}", color = Color.Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("COPERNICUS LINK: ${data.copernicusLink}", color = Color.White, fontSize = 9.sp)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                CommandTelemetryItem("NODES", data.activeSats.toString(), Color.White)
                CommandTelemetryItem("POSITION", data.lastGalileoCoordinate, Color.Cyan)
                CommandTelemetryItem("HEALTH", "${(data.signalHealth * 100).toInt()}%", Color.Green)
            }
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        color = Color.Cyan.copy(alpha = 0.4f),
        fontSize = 9.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
fun MultiDefenseTelemetryPanel(data: com.chemscanner.omniscient.marrow.repository.PlanetaryDefenseData) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFF5252).copy(0.05f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, null, tint = Color(0xFFFF5252), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text("NEO IMPACT MONITOR", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(12.dp))

            if (data.neoList.isEmpty()) {
                Text("SCANNING NASA NeoWS DATABASE...", color = Color.Gray, fontSize = 10.sp)
            } else {
                data.neoList.take(5).forEach { neo ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(neo.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("${"%.0f".format(neo.missDistanceKm / 1000)}k km", color = Color.Cyan, fontSize = 10.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${neo.diameterMeters.toInt()}m | ${neo.velocityKms.toInt()} km/s", color = Color.Gray, fontSize = 8.sp)
                            if (neo.isHazardous) {
                                Text("HAZARDOUS", color = Color.Red, fontSize = 8.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = Color.White.copy(0.05f))
                    }
                }
            }
        }
    }
}

@Composable
fun ChineseTelemetryPanel(data: com.chemscanner.omniscient.marrow.repository.ChineseSatelliteData) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFF1744).copy(0.05f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF1744).copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Hub, null, tint = Color(0xFFFF1744), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text("DRAGON LINK (CNSA TELEMETRY)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(12.dp))
            Text("YAOGAN INTEL: ${data.yaoganIntelligence}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("TIANGONG STATION: ${data.tiangongStatus}", color = Color.Yellow, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                CommandTelemetryItem("ACTIVE SATS", data.activeSats.toString(), Color.White)
                CommandTelemetryItem("GAOFEN RES", data.gaofenResolution, Color.Cyan)
                CommandTelemetryItem("ENCRYPTION", "${(data.signalEncryption * 100).toInt()}%", Color.Green)
            }
            Spacer(Modifier.height(4.dp))
            Text("LAST BEIDOU FIX: ${data.lastBeidouCoordinate}", color = Color.Gray, fontSize = 7.sp)
        }
    }
}

@Composable
fun SolarTelemetryPanel(data: com.chemscanner.omniscient.marrow.repository.SolarObservatoryData) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFAB40).copy(0.05f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFAB40).copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WbSunny, null, tint = Color(0xFFFFAB40), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text("SDO SOLAR TELEMETRY", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                CommandTelemetryItem("FLARE CLASS", data.solarFlareClass, if(data.solarFlareClass.contains("X")) Color.Red else Color.Yellow)
                CommandTelemetryItem("SUNSPOTS", data.sunspotCount.toString(), Color.White)
                CommandTelemetryItem("WIND SPEED", "%.0f km/s".format(data.solarWindSpeed), Color.Cyan)
            }
        }
    }
}

@Composable
fun GalacticTelemetryPanel(data: com.chemscanner.omniscient.marrow.repository.GalacticObservatoryData) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF7C4DFF).copy(0.05f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C4DFF).copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFF7C4DFF), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text("DEEP SPACE OBSERVATORY", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(12.dp))
            Text("HUBBLE TARGET: ${data.lastHubbleTarget}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                CommandTelemetryItem("TESS CANDIDATES", data.tessExoplanetCandidates.toString(), Color.Cyan)
                CommandTelemetryItem("COSMIC RAYS", "%.2f MeV".format(data.cosmicRayIntensity), Color.Magenta)
            }
        }
    }
}

@Composable
fun IssTelemetryPanel(data: com.chemscanner.omniscient.marrow.repository.IssStationData) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF4FC3F7).copy(0.05f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4FC3F7).copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.RocketLaunch, null, tint = Color(0xFF4FC3F7), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text("ISS LIVE TELEMETRY", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(12.dp))
            Text("CURRENT POSITION: OVER ${data.currentCountryOver}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                CommandTelemetryItem("ALTITUDE", "%.1f km".format(data.altitude), Color.Cyan)
                CommandTelemetryItem("VELOCITY", "%.0f km/h".format(data.velocity), Color.Yellow)
                CommandTelemetryItem("CREW", data.crewCount.toString(), Color.White)
            }
        }
    }
}

@Composable
fun CopernicusAndSentinelPanel(
    isCopernicusActive: Boolean,
    isSentinelActive: Boolean,
    cData: com.chemscanner.omniscient.marrow.repository.CopernicusData,
    sData: com.chemscanner.omniscient.marrow.repository.Sentinel2Data
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF00E676).copy(0.05f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676).copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Public, null, tint = Color(0xFF00E676), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text("EARTH OBSERVATION TELEMETRY", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }

            if (isCopernicusActive) {
                Spacer(Modifier.height(12.dp))
                Text("COPERNICUS (SENTINEL-5P):", color = Color.Green, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    CommandTelemetryItem("AQI", "%.1f".format(cData.airQualityIndex), Color.White)
                    CommandTelemetryItem("NDVI", "%.2f".format(cData.vegetationIndex), Color.Green)
                    CommandTelemetryItem("CH4", "%.0f ppb".format(cData.methaneConcentration), Color.Cyan)
                }
            }

            if (isSentinelActive) {
                Spacer(Modifier.height(12.dp))
                Text("SENTINEL-2 (OPTICAL):", color = Color(0xFF81C784), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    CommandTelemetryItem("CLOUDS", "%.1f%%".format(sData.cloudCover), Color.White)
                    CommandTelemetryItem("WATER", "%.2f".format(sData.waterIndex), Color.Blue)
                    CommandTelemetryItem("MOISTURE", "%.2f".format(sData.moistureIndex), Color.Cyan)
                }
            }
        }
    }
}

@Composable
fun CommandTelemetryItem(label: String, value: String, color: Color) {
    Column {
        Text(label, color = Color.Gray, fontSize = 7.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun CommandHeader(title: String, load: Float) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(Color.Red, CircleShape))
            Spacer(Modifier.width(8.dp))
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { load },
            modifier = Modifier.fillMaxWidth().height(2.dp),
            color = Color.Cyan,
            trackColor = Color.White.copy(0.1f)
        )
    }
}

@Composable
fun ToggleCard(
    title: String,
    subtitle: String,
    isActive: Boolean,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit
) {
    val alpha by animateFloatAsState(if (isActive) 1f else 0.3f, label = "alpha")
    val scale by animateFloatAsState(if (isActive) 1.02f else 1f, label = "scale")

    Surface(
        modifier = modifier.graphicsLayer(scaleX = scale, scaleY = scale).clickable { onToggle() },
        color = Color.Black.copy(0.6f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) color.copy(0.5f) else Color.White.copy(0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = if(isActive) color else Color.Gray, modifier = Modifier.size(24.dp).graphicsLayer(alpha = alpha))
                Switch(
                    checked = isActive,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = color,
                        checkedTrackColor = color.copy(0.3f),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    )
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color.Gray, fontSize = 10.sp, lineHeight = 12.sp)
        }
    }
}
