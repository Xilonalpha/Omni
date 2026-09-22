package com.chemscanner.omniscient.ui.compose.screens

import android.Manifest
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chemscanner.omniscient.ui.ar.ExperimentARRenderer
import com.chemscanner.omniscient.ui.viewmodels.ExperimentARViewModel
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableException
import java.util.concurrent.ArrayBlockingQueue

@Composable
fun ExperimentARScreen(
    viewModel: ExperimentARViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        ARCoreContainer(viewModel, onBack)
    }
}

@Composable
private fun ARCoreContainer(viewModel: ExperimentARViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val queuedSingleTaps = remember { ArrayBlockingQueue<MotionEvent>(16) }
    var renderer: ExperimentARRenderer? by remember { mutableStateOf(null) }
    var session: Session? by remember { mutableStateOf(null) }
    var glSurfaceView: GLSurfaceView? by remember { mutableStateOf(null) }
    var isArSupported by remember { mutableStateOf(true) }

    fun requestArCoreInstallation() {
        try {
            val availability = ArCoreApk.getInstance().checkAvailability(context)
            if (!availability.isSupported) {
                isArSupported = false
                return
            }

            when (ArCoreApk.getInstance().requestInstall(context as ComponentActivity, true)) {
                ArCoreApk.InstallStatus.INSTALLED -> {
                    session = Session(context)
                }
                ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                    // Activity will pause/resume
                }
            }
        } catch (e: UnavailableException) {
            isArSupported = false
            Toast.makeText(context, "AR not supported, switching to 3D mode", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            isArSupported = false
        }
    }

    val gestureDetector = remember {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                queuedSingleTaps.offer(e)
                return true
            }
            override fun onDown(e: MotionEvent): Boolean = true
        })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                GLSurfaceView(ctx).apply {
                    preserveEGLContextOnPause = true
                    setEGLContextClientVersion(3)
                    setEGLConfigChooser(8, 8, 8, 8, 16, 0)

                    val newRenderer = ExperimentARRenderer(ctx, this)
                    renderer = newRenderer
                    glSurfaceView = this

                    setRenderer(object : GLSurfaceView.Renderer {
                        override fun onSurfaceCreated(gl: javax.microedition.khronos.opengles.GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
                            renderer?.onSurfaceCreated(holder.surface)
                        }

                        override fun onSurfaceChanged(gl: javax.microedition.khronos.opengles.GL10?, width: Int, height: Int) {
                            renderer?.onSurfaceChanged(width, height)
                        }

                        override fun onDrawFrame(gl: javax.microedition.khronos.opengles.GL10?) {
                            val localRenderer = renderer ?: return
                            val localSession = session

                            if (localSession != null) {
                                try {
                                    val frame = localSession.update()
                                    
                                    queuedSingleTaps.poll()?.let { tap ->
                                        if (frame.camera.trackingState == TrackingState.TRACKING) {
                                            val remainingLabels = uiState.remainingLabels
                                            if (remainingLabels.isNotEmpty()) {
                                                for (hit in frame.hitTest(tap)) {
                                                    val trackable = hit.trackable
                                                    if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                                                        val anchor = hit.createAnchor()
                                                        localRenderer.addLabel(anchor)
                                                        viewModel.onLabelPlaced(remainingLabels.first())
                                                        break
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    localRenderer.render(frame)
                                } catch (e: CameraNotAvailableException) {
                                    localRenderer.renderFallback(System.nanoTime())
                                }
                            } else {
                                // Fallback to 3D mode without ARCore
                                localRenderer.renderFallback(System.nanoTime())
                                
                                // Auto-place first label in 3D mode if needed for demo
                                queuedSingleTaps.poll()?.let {
                                    if (uiState.remainingLabels.isNotEmpty()) {
                                        localRenderer.addLabelAtDefault()
                                        viewModel.onLabelPlaced(uiState.remainingLabels.first())
                                    }
                                }
                            }
                        }
                    })
                    renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                    setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // ACTIVATE 'onBack': Adăugăm un buton de back vizibil peste vizualizarea AR
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (session == null && isArSupported) {
                        requestArCoreInstallation()
                    }
                    try {
                        session?.resume()
                    } catch (e: CameraNotAvailableException) {
                        session = null
                    }
                    glSurfaceView?.onResume()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    glSurfaceView?.onPause()
                    session?.pause()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    session?.close()
                    session = null
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
