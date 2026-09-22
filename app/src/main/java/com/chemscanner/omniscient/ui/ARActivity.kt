package com.chemscanner.omniscient.ui

import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.services.BioOpticalScanner
import com.chemscanner.omniscient.marrow.services.MoleculeRenderService
import com.chemscanner.omniscient.ui.ar.ARMode
import com.chemscanner.omniscient.ui.ar.InteractionMode
import com.chemscanner.omniscient.ui.ar.ObjectRenderer
import com.chemscanner.omniscient.ui.viewmodels.ARViewModel
import com.google.ar.core.Session
import com.google.ar.core.exceptions.CameraNotAvailableException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ARActivity : AppCompatActivity() {

    @Inject lateinit var globalKnowledge: GlobalKnowledgeRepository
    @Inject lateinit var bioOpticalScanner: BioOpticalScanner
    @Inject lateinit var moleculeRenderService: MoleculeRenderService
    
    private val arViewModel: ARViewModel by viewModels()
    private var objectRenderer: ObjectRenderer? = null
    private var arSession: Session? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            arSession = Session(this)
        } catch (e: Exception) {
            Timber.e(e, "Failed to create ARCore session")
        }

        setContent {
            ARCoreSkyScreen(
                globalKnowledge = globalKnowledge,
                viewModel = arViewModel,
                onBack = { finish() },
                onScanRequest = { performSpectralScan() },
                onCaptureRequest = { captureAndNotarize() },
                onSurfaceAvailable = { glSurfaceView ->
                    // ACTIVARE: Initializare ObjectRenderer si legare la ViewModel
                    if (objectRenderer == null) {
                        objectRenderer = ObjectRenderer(this@ARActivity, glSurfaceView)
                        arViewModel.attachRenderer(objectRenderer!!)
                        
                        glSurfaceView.setRenderer(object : GLSurfaceView.Renderer {
                            override fun onSurfaceCreated(gl: javax.microedition.khronos.opengles.GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
                                objectRenderer?.onSurfaceCreated(glSurfaceView.holder.surface)
                            }

                            override fun onSurfaceChanged(gl: javax.microedition.khronos.opengles.GL10?, width: Int, height: Int) {
                                objectRenderer?.onSurfaceChanged(width, height)
                            }

                            override fun onDrawFrame(gl: javax.microedition.khronos.opengles.GL10?) {
                                arSession?.let { session ->
                                    try {
                                        val frame = session.update()
                                        objectRenderer?.render(frame)
                                    } catch (e: CameraNotAvailableException) {
                                        Timber.e(e)
                                    }
                                }
                            }
                        })
                        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                    }
                }
            )
        }
    }

    private fun performSpectralScan() {
        lifecycleScope.launch {
            val mockBitmap = createBitmap(100, 100, Bitmap.Config.ARGB_8888)
            mockBitmap.applyCanvas {
                drawColor(0xFF0000FF.toInt()) 
            }
            val result = bioOpticalScanner.analyzeSpectrum(mockBitmap)
            Toast.makeText(this@ARActivity, "Scanare Marrow: $result", Toast.LENGTH_LONG).show()
        }
    }

    private fun captureAndNotarize() {
        val snapshot = createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
        arViewModel.captureARScreenshot(snapshot)
        Toast.makeText(this, "Snapshot trimis către Akasha Archive", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        try {
            arSession?.resume()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onPause() {
        super.onPause()
        arSession?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        // ACTIVARE: onSurfaceDestroyed
        objectRenderer?.onSurfaceDestroyed()
        arSession?.close()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ARCoreSkyScreen(
    globalKnowledge: GlobalKnowledgeRepository,
    viewModel: ARViewModel,
    onBack: () -> Unit,
    onScanRequest: () -> Unit,
    onCaptureRequest: () -> Unit,
    onSurfaceAvailable: (GLSurfaceView) -> Unit
) {
    val currentMode by viewModel.currentMode.observeAsState(ARMode.STRUCTURE)
    val interactionMode by viewModel.interactionMode.observeAsState(InteractionMode.ROTATE)
    val orbits by globalKnowledge.orbits.collectAsState()
    val spectralColor by globalKnowledge.spectralHexColor.collectAsState()
    
    val currentColor = remember(spectralColor) {
        try { 
            Color(spectralColor.toColorInt())
        }
        catch (e: Exception) { 
            Timber.v(e, "Spectral Color Sync Failure")
            Color.Cyan 
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ACTIVARE: GLSurfaceView pentru ObjectRenderer
        AndroidView(
            factory = { context ->
                GLSurfaceView(context).apply {
                    setEGLContextClientVersion(3)
                    onSurfaceAvailable(this)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { glView ->
                // ACTIVARE: onSurfaceChanged (implicit prin layout-ul Compose)
                // Filament se ocupa de redimensionare intern daca viewport-ul este setat in renderer
            }
        )

        Box(modifier = Modifier.fillMaxSize().background(
            Brush.radialGradient(
                colors = listOf(Color.Transparent, currentColor.copy(alpha = 0.15f)),
                radius = 1500f
            )
        ))

        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.background(Color.Black.copy(0.5f), CircleShape)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("MARROW AR VISION", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text("SYNC: $currentMode | INT: $interactionMode", color = currentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                InteractionChip(
                    selected = interactionMode == InteractionMode.ROTATE,
                    icon = Icons.AutoMirrored.Filled.RotateRight,
                    onClick = { viewModel.setInteractionMode(InteractionMode.ROTATE) }
                )
                Spacer(Modifier.width(8.dp))
                InteractionChip(
                    selected = interactionMode == InteractionMode.TRANSLATE,
                    icon = Icons.Default.OpenWith,
                    onClick = { viewModel.setInteractionMode(InteractionMode.TRANSLATE) }
                )
                Spacer(Modifier.width(8.dp))
                InteractionChip(
                    selected = interactionMode == InteractionMode.SCALE,
                    icon = Icons.Default.ZoomOutMap,
                    onClick = { viewModel.setInteractionMode(InteractionMode.SCALE) }
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = onScanRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = currentColor.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Icon(Icons.Default.Camera, null); Spacer(Modifier.width(4.dp))
                    Text("SCAN", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onCaptureRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).padding(start = 8.dp).border(1.dp, Color.White.copy(0.3f), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.PhotoCamera, null, tint = Color.White); Spacer(Modifier.width(4.dp))
                    Text("AKASHA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                FilterChip(
                    selected = currentMode == ARMode.STRUCTURE,
                    onClick = { viewModel.setARMode(ARMode.STRUCTURE) },
                    label = { Text("MOLECULAR") },
                    leadingIcon = { Icon(Icons.Default.Public, null, modifier = Modifier.size(16.dp)) }
                )
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = currentMode == ARMode.SATELLITES,
                    onClick = { viewModel.setARMode(ARMode.SATELLITES) },
                    label = { Text("SKY GUARDIAN") },
                    leadingIcon = { Icon(Icons.Default.SatelliteAlt, null, modifier = Modifier.size(16.dp)) }
                )
            }
        }

        if (currentMode == ARMode.SATELLITES) {
            orbits.take(15).forEach { sat ->
                SatelliteArNode(sat.name, sat.azimuth, sat.elevation)
            }
        }
    }
}

@Composable
fun InteractionChip(selected: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .background(if (selected) Color.Cyan.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.4f), CircleShape)
            .border(1.dp, if (selected) Color.Cyan else Color.White.copy(alpha = 0.2f), CircleShape)
    ) {
        Icon(icon, null, tint = if (selected) Color.Black else Color.White, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun SatelliteArNode(name: String, azimuth: Float, elevation: Float) {
    val xOffset = (azimuth % 360) - 180
    val yOffset = (elevation % 90) - 45
    Box(modifier = Modifier.fillMaxSize().offset(x = xOffset.dp, y = yOffset.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(6.dp).background(Color.Cyan, CircleShape))
            Text(name, color = Color.Cyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
    }
}
