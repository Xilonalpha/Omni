package com.chemscanner.omniscient.ui.compose.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chemscanner.omniscient.ui.viewmodels.ScannerViewModel
import com.chemscanner.omniscient.marrow.utils.ImageProcessing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel,
    onScanResult: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()
    val flashState by viewModel.flashState.collectAsStateWithLifecycle()
    
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var camera: Camera? by remember { mutableStateOf(null) }

    // REPARARE GALERIE: Launcher pentru selectarea imaginii
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            }
            viewModel.analyzeFromBitmap(bitmap)
        }
    }

    // Actualizare Flash în timp real
    LaunchedEffect(flashState) {
        camera?.cameraControl?.enableTorch(flashState)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse), label = "laserMove"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }.also {
                    startCamera(it, lifecycleOwner, context) { ic, cam -> 
                        imageCapture = ic 
                        camera = cam
                        cam.cameraControl.enableTorch(flashState)
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay & Laser UI (nemodificat pentru design)
        Box(modifier = Modifier.fillMaxSize().background(Brush.radialGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)), radius = 1500f)))
        Box(modifier = Modifier.size(280.dp).align(Alignment.Center).border(2.dp, Color.Cyan.copy(alpha = 0.5f), RoundedCornerShape(24.dp))) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val y = size.height * laserOffset
                drawLine(color = Color.Cyan, start = Offset(40f, y), end = Offset(size.width - 40f, y), strokeWidth = 4f, alpha = 0.8f)
            }
        }

        // Top Controls
        Row(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Surface(color = Color.Black.copy(alpha = 0.5f), shape = CircleShape, modifier = Modifier.size(48.dp).clickable { onBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.padding(12.dp))
            }
            Text("ANALIZĂ MOLECULARĂ", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Surface(color = if (flashState) Color.Cyan else Color.Black.copy(alpha = 0.5f), shape = CircleShape, modifier = Modifier.size(48.dp).clickable { viewModel.toggleFlash() }) {
                Icon(if (flashState) Icons.Default.FlashOn else Icons.Default.FlashOff, contentDescription = "Flash", tint = if (flashState) Color.Black else Color.White, modifier = Modifier.padding(12.dp))
            }
        }

        // Bottom Controls
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                IconButton(
                    onClick = { galleryLauncher.launch("image/*") }, // REPARAT: Deschide galeria
                    modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = Color.White)
                }

                Box(
                    modifier = Modifier.size(84.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f))
                        .clickable {
                            imageCapture?.let { ic ->
                                ic.takePicture(ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        viewModel.captureAndAnalyze(image)
                                    }
                                    override fun onError(exc: ImageCaptureException) {}
                                })
                            }
                        }.padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.White))
                }

                IconButton(onClick = { /* Switch to AR */ }, modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)) {
                    Icon(Icons.Default.ViewInAr, contentDescription = "AR Mode", tint = Color.White)
                }
            }
        }

        if (scanState is ScannerViewModel.ScanState.Processing) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.Cyan)
                    Spacer(Modifier.height(16.dp))
                    Text("IDENTIFICARE STRUCTURĂ...", color = Color.Cyan, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    LaunchedEffect(scanState) {
        if (scanState is ScannerViewModel.ScanState.Success) {
            onScanResult((scanState as ScannerViewModel.ScanState.Success).chemicalName)
        }
    }
}

private fun startCamera(
    previewView: PreviewView,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    context: android.content.Context,
    onReady: (ImageCapture, Camera) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
            onReady(imageCapture, camera)
        } catch (exc: Exception) {}
    }, ContextCompat.getMainExecutor(context))
}
