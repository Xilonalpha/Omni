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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chemscanner.omniscient.ui.ar.DemonstrationARRenderer
import com.chemscanner.omniscient.ui.viewmodels.ExperimentDetailsViewModel
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.*
import java.util.concurrent.ArrayBlockingQueue
import kotlinx.coroutines.delay
import timber.log.Timber
import java.nio.ByteBuffer

@Composable
fun DemonstrationARScreen(
    viewModel: ExperimentDetailsViewModel = hiltViewModel(),
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
private fun ARCoreContainer(viewModel: ExperimentDetailsViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val queuedSingleTaps = remember { ArrayBlockingQueue<MotionEvent>(16) }
    var renderer: DemonstrationARRenderer? by remember { mutableStateOf(null) }
    var session: Session? by remember { mutableStateOf(null) }
    var glSurfaceView: GLSurfaceView? by remember { mutableStateOf(null) }

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

                    val newRenderer = DemonstrationARRenderer(ctx, this)
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
                            val localSession = session ?: return
                            val localRenderer = renderer ?: return

                            try {
                                val frame = localSession.update()
                                
                                queuedSingleTaps.poll()?.let { tap ->
                                    if (frame.camera.trackingState == TrackingState.TRACKING) {
                                        for (hit in frame.hitTest(tap)) {
                                            val trackable = hit.trackable
                                            if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                                                // Logica de plasare manuala daca este necesar
                                                break
                                            }
                                        }
                                    }
                                }
                                
                                if (frame.camera.trackingState == TrackingState.TRACKING) {
                                    localRenderer.render(frame)
                                }
                            } catch (e: Exception) {
                                // Ignore frame update errors
                            }
                        }
                    })
                    renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                    setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    LaunchedEffect(uiState.arScenario, session) {
        if (session == null) return@LaunchedEffect
        
        for (command in uiState.arScenario) {
            val parts = command.split(": ")
            val action = parts[0]
            val parameter = parts.getOrNull(1)

            when (action) {
                "NARRATOR" -> {
                    Toast.makeText(context, parameter, Toast.LENGTH_LONG).show()
                    delay(3000)
                }
                "SHOW_MODEL" -> {
                    val modelName = parameter ?: "water"
                    renderer?.let { r ->
                        val frame = session?.update()
                        if (frame?.camera?.trackingState == TrackingState.TRACKING) {
                            // Find a plane to attach the model
                            val planes = session?.getAllTrackables(Plane::class.java)
                            val plane = planes?.find { it.trackingState == TrackingState.TRACKING }
                            if (plane != null) {
                                val anchor = session?.createAnchor(plane.centerPose)
                                if (anchor != null) {
                                    try {
                                        val modelPath = "models/$modelName.glb"
                                        val buffer = context.assets.open(modelPath).use { it.readBytes() }
                                        val byteBuffer = ByteBuffer.allocateDirect(buffer.size).put(buffer)
                                        byteBuffer.flip()
                                        r.addModel(anchor, modelName, byteBuffer)
                                    } catch (e: Exception) {
                                        Timber.e(e, "Error loading model $modelName")
                                    }
                                }
                            }
                        }
                        r.showModel(modelName)
                    }
                }
                "HIDE_MODELS" -> {
                    parameter?.split(",")?.forEach { name ->
                        renderer?.hideModel(name.trim())
                    }
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (session == null) {
                        try {
                            // Deep Error Trapping for unsupported devices
                            session = Session(context)
                        } catch (e: Throwable) {
                            Timber.e(e, "AR Session creation failed in DemonstrationAR")
                            Toast.makeText(context, "AR Mode is not supported on this device's hardware.", Toast.LENGTH_LONG).show()
                            onBack() // Graceful exit back to details
                        }
                    }
                    try {
                        session?.resume()
                    } catch (e: Exception) {
                        session = null
                        onBack()
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
