package com.chemscanner.omniscient.ui

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.chemscanner.omniscient.ui.viewmodels.DermalArchitectViewModel
import com.chemscanner.omniscient.marrow.utils.ImageProcessing
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DermalArchitectActivity : ComponentActivity() {
    private val viewModel: DermalArchitectViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DermalArchitectScreen(viewModel, onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DermalArchitectScreen(viewModel: DermalArchitectViewModel, onBack: () -> Unit) {
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
                title = { Text("DERMAL ARCHITECT", color = Color(0xFFFF4081), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState())) {
            
            // 1. MACRO VIEWPORT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black)
                    .border(1.dp, Color(0xFFFF4081).copy(0.3f), RoundedCornerShape(24.dp)),
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
                
                if (uiState.isAnalyzing) {
                    CircularProgressIndicator(color = Color(0xFFFF4081))
                }
            }

            Spacer(Modifier.height(24.dp))

            // 2. DERMAL RESULTS
            if (uiState.skinAge != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(0.05f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF4081).copy(0.2f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HealthAndSafety, null, tint = Color(0xFFFF4081))
                            Spacer(Modifier.width(12.dp))
                            Text("VÂRSTĂ DERMICĂ: ${uiState.skinAge} ANI", color = Color.White, fontWeight = FontWeight.Black)
                        }
                        Text("HIDRATARE: ${uiState.hydrationLevel}%", color = Color.Cyan, fontSize = 12.sp)
                        Spacer(Modifier.height(12.dp))
                        uiState.recommendations.forEach { rec ->
                            Text("• $rec", color = Color.LightGray, fontSize = 11.sp)
                        }
                    }
                }
            } else {
                Text(uiState.statusMessage, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
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
                                viewModel.analyzeSkin(bitmap)
                            }
                            override fun onError(exception: ImageCaptureException) {
                                Toast.makeText(context, "Eroare macro-captură", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isAnalyzing && hasCameraPermission
            ) {
                Icon(Icons.Default.Camera, null)
                Spacer(Modifier.width(12.dp))
                Text("SCANARE MACRO PIELE", fontWeight = FontWeight.Black)
            }
        }
    }
}
