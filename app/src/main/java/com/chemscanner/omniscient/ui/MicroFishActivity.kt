package com.chemscanner.omniscient.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.chemscanner.omniscient.ui.viewmodels.MicroFishUiState
import com.chemscanner.omniscient.ui.viewmodels.MicroFishViewModel
import com.chemscanner.omniscient.marrow.utils.ImageProcessing
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class MicroFishActivity : ComponentActivity() {
    private val viewModel: MicroFishViewModel by viewModels()
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        setContent {
            MicroFishScreen(viewModel, onBack = { finish() })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicroFishScreen(viewModel: MicroFishViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    val imageCapture = remember { ImageCapture.Builder().build() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MICRO-FISH GENOMIC SCANNER", color = Color(0xFF8BC34A), fontWeight = FontWeight.Black, fontSize = 14.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState())) {
            
            // 1. REAL CAMERA VIEWPORT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black)
                    .border(1.dp, Color(0xFF8BC34A).copy(0.3f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                                } catch (e: Exception) { }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (uiState.isScanning) ScanningMicroscopeOverlay()
            }

            Spacer(Modifier.height(24.dp))

            if (uiState.result != null) {
                GenomicResultPanel(uiState)
                if (uiState.realDnaSequence != null) {
                    Spacer(Modifier.height(16.dp))
                    RealDnaPanel(uiState.realDnaSequence!!)
                }
            } else {
                InfoPanel()
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { 
                    imageCapture.takePicture(
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                val bitmap = ImageProcessing.imageProxyToBitmap(image)
                                image.close()
                                viewModel.processMicroscopeImage(bitmap)
                            }
                            override fun onError(exception: ImageCaptureException) {
                                Toast.makeText(context, "Eșec captură optică", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8BC34A), contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isScanning && hasCameraPermission
            ) {
                if (uiState.isScanning) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.QueryStats, null)
                    Spacer(Modifier.width(12.dp))
                    Text("START REAL GENOMIC SCAN", fontWeight = FontWeight.Black)
                }
            }
            
            if (uiState.result != null) {
                TextButton(onClick = { viewModel.resetScanner() }, modifier = Modifier.fillMaxWidth()) {
                    Text("RESET SCANNER", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun RealDnaPanel(sequence: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF0D1B0D),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8BC34A).copy(0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Source, null, tint = Color(0xFF8BC34A), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text("NCBI REAL DNA SEQUENCE (GenBank)", color = Color(0xFF8BC34A), fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = sequence, color = Color.White, fontSize = 11.sp,
                fontFamily = FontFamily.Monospace, lineHeight = 14.sp,
                modifier = Modifier.background(Color.Black.copy(0.5f), RoundedCornerShape(4.dp)).padding(8.dp)
            )
        }
    }
}

@Composable
fun ScanningMicroscopeOverlay() {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.4f)), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color(0xFF8BC34A))
    }
}

@Composable
fun GenomicResultPanel(uiState: MicroFishUiState) {
    val result = uiState.result ?: return
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(0.05f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8BC34A).copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("SPECIES: ${result.speciesName.uppercase()}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ResultStat("HEALTH", result.healthStatus, Color.Green)
                ResultStat("CONFIDENCE", "${(result.confidence * 100).toInt()}%", Color.Cyan)
            }
        }
    }
}

@Composable
fun ResultStat(label: String, value: String, color: Color) {
    Column {
        Text(label, color = Color.Gray, fontSize = 7.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 11.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun InfoPanel() {
    Surface(modifier = Modifier.fillMaxWidth(), color = Color.White.copy(0.02f), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("NCBI GENOMIC FUSION ACTIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
