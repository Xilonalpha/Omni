package com.chemscanner.omniscient.ui.compose.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chemscanner.omniscient.ui.viewmodels.ProductScannerViewModel
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import timber.log.Timber
import java.util.concurrent.Executors

@Composable
fun ProductScannerScreen(
    viewModel: ProductScannerViewModel = hiltViewModel(),
    onBarcodeScanned: (String) -> Unit,
    onTextRecognized: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    var hasCameraPermission by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    ) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            CameraView(cameraProviderFuture, lifecycleOwner, context, viewModel)
        }

        Column(modifier = Modifier.padding(16.dp)) {
            uiState.scannedBarcode?.let {
                Text("Barcode: $it")
                Button(onClick = { onBarcodeScanned(it) }) {
                    Text("Use Barcode")
                }
            }
            uiState.recognizedText?.let {
                Text("Recognized Text: $it")
                Button(onClick = { onTextRecognized(it) }) {
                    Text("Use Text")
                }
            }
        }
    }
}

@Composable
private fun CameraView(
    cameraProviderFuture: com.google.common.util.concurrent.ListenableFuture<ProcessCameraProvider>,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    context: Context,
    viewModel: ProductScannerViewModel
) {
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraExecutor = Executors.newSingleThreadExecutor()

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient(
                    BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                        .build()
                )
                val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor) { imageProxy ->
                            // FIXED: Opt-in inside the analyzer lambda
                            @ExperimentalGetImage
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                                barcodeScanner.process(image)
                                    .addOnSuccessListener { barcodes: List<Barcode> ->
                                        barcodes.firstOrNull()?.let { barcode ->
                                            // FIXED: Passing Barcode object as expected by ViewModel
                                            viewModel.onBarcodeScanned(barcode)
                                        }
                                    }
                                    .addOnFailureListener { e: Exception ->
                                        Timber.e(e, "Barcode scanning failed")
                                    }

                                textRecognizer.process(image)
                                    .addOnSuccessListener { text: Text ->
                                        // FIXED: Passing Text object as expected by ViewModel
                                        viewModel.onTextRecognized(text)
                                    }
                                    .addOnFailureListener { e: Exception ->
                                        Timber.e(e, "Text recognition failed")
                                    }
                                    .addOnCompleteListener { task: Task<Text> ->
                                        if (!task.isSuccessful) {
                                            Timber.w(task.exception, "Text recognition task not successful")
                                        }
                                        imageProxy.close()
                                    }
                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (exc: Exception) {
                    Timber.e(exc, "Camera binding failed")
                }
            }, ContextCompat.getMainExecutor(context))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}
