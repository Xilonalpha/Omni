package com.chemscanner.omniscient.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.MainRepository
import com.chemscanner.omniscient.marrow.repository.MasterLockState
import com.chemscanner.omniscient.marrow.repository.UserRepository
import com.chemscanner.omniscient.marrow.services.MasterVoiceService
import com.chemscanner.omniscient.marrow.services.ModelDownloaderService
import com.chemscanner.omniscient.marrow.services.SovereignVpnService
import com.chemscanner.omniscient.marrow.services.RealSignalGateway
import com.chemscanner.omniscient.ui.compose.screens.MainDashboardScreen
import com.chemscanner.omniscient.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var userRepository: UserRepository
    @Inject lateinit var mainRepository: MainRepository
    @Inject lateinit var globalKnowledge: GlobalKnowledgeRepository
    @Inject lateinit var modelDownloader: ModelDownloaderService
    @Inject lateinit var realSignalGateway: RealSignalGateway

    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.RECORD_AUDIO] == true) {
            Timber.d("RECORD_AUDIO granted, restarting MasterVoiceService")
            restartMasterVoiceService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // PRIVACY SHIELD: Previne capturile de ecran și spionajul vizual
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        checkAndRequestPermissions()

        // MARROW SYNC: Asigurăm integritatea modelelor la pornire
        modelDownloader.checkAndDownloadModels()

        setContent {
            val kernelStatus by viewModel.kernelStatus.collectAsState()
            val neuralLoad by viewModel.neuralLoad.collectAsState()
            val downloadStatus by viewModel.downloadStatus.collectAsState()
            val lockState by viewModel.lockState.collectAsState()
            val omegaState by viewModel.omegaState.collectAsState()
            val lastMutation by viewModel.lastMutation.collectAsState()
            val iotNodes by viewModel.iotNodes.collectAsState()
            val envSignals by viewModel.envSignals.collectAsState()
            val activeLaws by viewModel.activeLaws.collectAsState()
            val hiveStatus by viewModel.hiveStatus.collectAsState()
            val minerState by viewModel.minerState.collectAsState()
            val worldMeshStatus by viewModel.worldMeshStatus.collectAsState()
            val bioSync by viewModel.bioSync.collectAsState()
            val userVitality by viewModel.userVitality.collectAsState()

            MainDashboardScreen(
                userName = "Xilon",
                kernelStatus = kernelStatus,
                neuralLoad = neuralLoad,
                bioSync = bioSync,
                realityIntegrity = omegaState.realityIntegrity,
                lastMutation = lastMutation,
                isArchitectAuthorized = lockState == MasterLockState.UNLOCKED,
                iotNodes = iotNodes,
                envSignals = envSignals,
                activeLaws = activeLaws,
                hiveStatus = hiveStatus,
                minerState = minerState,
                downloadStatus = downloadStatus,
                worldMeshStatus = worldMeshStatus,
                omegaState = omegaState,
                userVitality = userVitality,
                onItemClick = { itemId -> handleNavigation(itemId) },
                onLogout = { finish() },
                onSettings = { handleNavigation("SETTINGS") },
                onActivateOmega = { globalKnowledge.activateOmegaProtocol() }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        realSignalGateway.startMonitoring()
    }

    override fun onStop() {
        super.onStop()
        realSignalGateway.stopMonitoring()
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun restartMasterVoiceService() {
        val intent = Intent(this, MasterVoiceService::class.java)
        stopService(intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun handleNavigation(itemId: String) {
        val intent = when (itemId) {
            "STRATEGIC_COMMAND" -> Intent(this, CommandCenterActivity::class.java)
            "PHANTOM_VPN" -> {
                activateSovereignVpn()
                null
            }
            "FORCE_SYNC" -> {
                modelDownloader.checkAndDownloadModels()
                null
            }
            "SOVEREIGN_MESH" -> Intent(this, SovereignMeshActivity::class.java)
            "SCAN" -> Intent(this, ScannerActivity::class.java)
            "SCAN_PLUS" -> Intent(this, ScannerPlusActivity::class.java)
            "AI" -> Intent(this, AiAssistantActivity::class.java)
            "HISTORY" -> Intent(this, HistoryActivity::class.java)
            "GAME" -> Intent(this, AtomicStabilizerActivity::class.java)
            "BIO_AGE" -> Intent(this, BioAgeChronosActivity::class.java)
            "SPECTRAL" -> Intent(this, SpectralEyeActivity::class.java)
            "MULTIVERSE" -> Intent(this, MultiverseOracleActivity::class.java)
            "LAB" -> Intent(this, VirtualLabActivity::class.java)
            "STOICH" -> Intent(this, StoichiometryActivity::class.java)
            "GREEN_CHEM" -> Intent(this, GreenChemistryActivity::class.java)
            "PERIODIC" -> Intent(this, PeriodicTableActivity::class.java)
            "QUIZ" -> Intent(this, QuizActivity::class.java)
            "DARK_MATTER" -> Intent(this, DarkMatterActivity::class.java)
            "BIO_SONIC" -> Intent(this, BioSonicActivity::class.java)
            "AI_LAB" -> Intent(this, AIConsciousnessActivity::class.java)
            "GALAXY_HUB" -> Intent(this, GalaxyMigrationActivity::class.java)
            "EXO_SURVEY" -> Intent(this, ExoPlanetSurveyorActivity::class.java)
            "PLANET_SCAN" -> Intent(this, PlanetScannerActivity::class.java)
            "WARP_DRIVE" -> Intent(this, WarpDriveActivity::class.java)
            "VELOCITY" -> Intent(this, HighVelocityLabActivity::class.java)
            "MESH_CITY" -> Intent(this, MeshCityActivity::class.java)
            "FOOTBALL_PRO" -> Intent(this, FootballManagerActivity::class.java)
            "QUANTUM_FOOTBALL" -> Intent(this, QuantumFootballActivity::class.java)
            "AR" -> Intent(this, ARActivity::class.java)
            "NEURO" -> Intent(this, NeuroPhysActivity::class.java)
            "ASTRO" -> Intent(this, AstroMechActivity::class.java)
            "ROBO" -> Intent(this, RoboPhysActivity::class.java)
            "TEMPORAL" -> Intent(this, TemporalPhysicsActivity::class.java)
            "MICRO" -> Intent(this, MicroVerseActivity::class.java)
            "NEURO_BIO" -> Intent(this, NeuroBioActivity::class.java)
            "GENOMIC" -> Intent(this, GenomicArchitectActivity::class.java)
            "QUANTUM_HUB" -> Intent(this, QuantumHubActivity::class.java)
            "FUSION" -> Intent(this, FusionReactorActivity::class.java)
            "PALEO" -> Intent(this, PaleoScannerActivity::class.java)
            "NANO_FORGE" -> Intent(this, NanoForgeActivity::class.java)
            "SURGEON" -> Intent(this, BioDigitalSurgeonActivity::class.java)
            "NEURO_MOD" -> Intent(this, NeuroModulatorActivity::class.java)
            "TERRAFORMING" -> Intent(this, TerraformingActivity::class.java)
            "VERA_RUBIN" -> Intent(this, VeraRubinActivity::class.java)
            "VOYAGER" -> Intent(this, VoyagerActivity::class.java)
            "STARLINK" -> Intent(this, StarlinkActivity::class.java)
            "CERN" -> Intent(this, CernActivity::class.java)
            "MARS_ROVER" -> Intent(this, MarsRoverActivity::class.java)
            "CELESTIAL_CONTROL" -> Intent(this, CelestialControlActivity::class.java)
            "SETTINGS" -> Intent(this, SettingsActivity::class.java)
            "XILON_PROF" -> Intent(this, XilonProfActivity::class.java)
            "ABYSSAL" -> Intent(this, AbyssalActivity::class.java)
            "BLACK_HOLE" -> Intent(this, BlackHoleActivity::class.java)
            "MICRO_FISH" -> Intent(this, MicroFishActivity::class.java)
            "PHARMA_GENOME" -> Intent(this, PharmaGenomeActivity::class.java)
            "QUANTUM_COLLIDER" -> Intent(this, QuantumColliderActivity::class.java)
            "EMISAR_FUSION" -> Intent(this, EmisarActivity::class.java)
            else -> null
        }
        intent?.let { startActivity(it) }
    }

    private fun activateSovereignVpn() {
        // GHOST ACTIVATION: Folosim applicationContext pentru a fixa eroarea de Securitate UID
        val vpnIntent = try {
            VpnService.prepare(applicationContext)
        } catch (e: Exception) {
            Timber.e(e, "Marrow: VPN Preparation Error")
            null
        }

        if (vpnIntent != null) {
            startActivityForResult(vpnIntent, 102)
        } else {
            startSovereignVpnService()
        }
    }

    private fun startSovereignVpnService() {
        val intent = Intent(this, SovereignVpnService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 102 && resultCode == RESULT_OK) {
            startSovereignVpnService()
        }
    }
}
