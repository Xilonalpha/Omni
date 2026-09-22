package com.chemscanner.omniscient.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.chemscanner.omniscient.marrow.utils.ImageProcessing
import com.chemscanner.omniscient.ui.viewmodels.BioAgeChronosViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class BioAgeChronosActivity : AppCompatActivity() {

    private val viewModel: BioAgeChronosViewModel by viewModels()
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        setContent {
            BioAgeChronosScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BioAgeChronosScreen(
    viewModel: BioAgeChronosViewModel,
    onBack: () -> Unit
) {
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
                title = { Text("BIO-AGE CHRONOS", color = Color(0xFFE91E63), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black)
                    .border(1.dp, Color(0xFFE91E63).copy(0.3f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    try {
                                        val cameraProvider = cameraProviderFuture.get()
                                        val preview = Preview.Builder().build().also {
                                            it.setSurfaceProvider(this.surfaceProvider)
                                        }
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_FRONT_CAMERA,
                                            preview,
                                            imageCapture
                                        )
                                    } catch (e: Exception) {
                                        Timber.e(e, "Marrow Camera Error")
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        update = { /* No-op to prevent constant rebinding on recomposition */ },
                        onRelease = {
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                cameraProvider.unbindAll()
                            } catch (e: Exception) { }
                        }
                    )
                }

                if (uiState.faceMeshActive) {
                    FaceMeshCanvas(uiState.scanProgress)
                }

                if (!uiState.isScanning && uiState.biologicalAge == null) {
                    Button(
                        onClick = { 
                            imageCapture.takePicture(
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        val rawBitmap = ImageProcessing.imageProxyToBitmap(image)
                                        image.close()
                                        val optimizedBitmap = ImageProcessing.resizeBitmap(rawBitmap, 1024)
                                        viewModel.startBioScan(optimizedBitmap)
                                    }
                                    override fun onError(exception: ImageCaptureException) {
                                        Toast.makeText(context, "Eșec captură facială", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                    ) {
                        Icon(Icons.Default.Face, null)
                        Spacer(Modifier.width(8.dp))
                        Text("INITIATE REAL-TIME SCAN")
                    }
                }
                
                if (uiState.isScanning) {
                    CircularProgressIndicator(color = Color(0xFFE91E63))
                }
            }

            if (uiState.biologicalAge != null) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                    color = Color.Black.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(28.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE91E63).copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("ANALIZĂ BIOMETRICĂ COMPLETĂ", color = Color(0xFFE91E63), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { viewModel.resetScanner() }) {
                                Icon(Icons.Default.Refresh, "Retry", tint = Color.Gray)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("VÂRSTĂ BIOLOGICĂ:", color = Color.Gray, fontSize = 12.sp)
                            Spacer(Modifier.width(12.dp))
                            Text("${uiState.biologicalAge} ANI", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        }
                        
                        if (uiState.aiBioReport != null) {
                            Spacer(Modifier.height(12.dp))
                            Text(uiState.aiBioReport!!, color = Color.LightGray, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FaceMeshCanvas(progress: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    val alpha by infiniteTransition.animateFloat(0.1f, 0.4f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "a")

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2
        val cy = size.height / 2
        drawCircle(Color(0xFFE91E63).copy(alpha = alpha), radius = 300f, center = Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(1f))
        
        if (progress > 0 && progress < 1) {
            val lineY = size.height * progress
            drawLine(Color.Cyan.copy(alpha = 0.6f), Offset(0f, lineY), Offset(size.width, lineY), 2f)
        }
    }
}
