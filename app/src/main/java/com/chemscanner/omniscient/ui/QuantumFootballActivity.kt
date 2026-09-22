package com.chemscanner.omniscient.ui

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.chemscanner.omniscient.marrow.data.models.*
import com.chemscanner.omniscient.marrow.repository.NegotiationResult
import com.chemscanner.omniscient.ui.ar.FootballARRenderer
import com.chemscanner.omniscient.ui.viewmodels.*
import com.chemscanner.omniscient.ui.compose.screens.CareerHubScreen // IMPORT ACTIVAT
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.ArCoreApk
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

@AndroidEntryPoint
class QuantumFootballActivity : AppCompatActivity() {

    private val viewModel: QuantumFootballViewModel by viewModels()
    private val careerViewModel: CareerViewModel by viewModels()
    @Inject lateinit var renderer: FootballARRenderer
    
    private var arSession: Session? = null
    private var glSurfaceView: GLSurfaceView? = null
    private var isArSupported = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkArSupportAndSetup()
        
        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val careerState by careerViewModel.uiState.collectAsState()
            var showCareerMenu by remember { mutableStateOf(true) }

            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                if (showCareerMenu) {
                    CareerHubScreen(
                        state = careerState,
                        onPlayMatch = { opponent: Team -> // FIX: Explicit Type
                            viewModel.initMatch(careerState.myTeam?.id ?: "RO1", opponent.id, "S5")
                            if (!isArSupported.value) {
                                renderer.enableFallbackMode()
                                viewModel.onStadiumPlaced(null)
                            }
                            showCareerMenu = false
                        }
                    )
                } else {
                    ARViewportContainer()
                    SovereignFootballHUD(uiState, viewModel) {
                        showCareerMenu = true
                        viewModel.resetToIdle()
                    }
                    if (!uiState.isTrackingActive && isArSupported.value) {
                        SurfacePlacementHint()
                    }
                }
            }
        }
    }

    @Composable
    private fun ARViewportContainer() {
        AndroidView(
            factory = { context ->
                GLSurfaceView(context).apply {
                    setEGLContextClientVersion(3)
                    setRenderer(object : GLSurfaceView.Renderer {
                        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) { 
                            renderer.onSurfaceCreated(holder.surface, display) 
                        }
                        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) { 
                            renderer.onSurfaceChanged(width, height) 
                        }
                        override fun onDrawFrame(gl: GL10?) {
                            val session = arSession
                            if (isArSupported.value && session != null) {
                                try {
                                    session.setCameraTextureName(renderer.getTextureId())
                                    val frame = session.update()
                                    renderer.render(frame, viewModel.uiState.value)
                                } catch (e: Exception) {
                                    renderer.render(null, viewModel.uiState.value)
                                }
                            } else {
                                renderer.render(null, viewModel.uiState.value)
                            }
                        }
                    })
                    renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                    glSurfaceView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    @Composable
    fun SovereignFootballHUD(uiState: FootballUiState, viewModel: QuantumFootballViewModel, onExit: () -> Unit) {
        Box(Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Black.copy(0.7f), Color.Transparent))).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onExit) { Icon(Icons.Default.Close, null, tint = Color.White) }
                BroadcastScoreboard(uiState)
                Box(Modifier.size(40.dp))
            }

            val selected = uiState.players.find { it.isSelected }
            selected?.let { p ->
                Box(Modifier.fillMaxSize().padding(bottom = 200.dp), contentAlignment = Alignment.BottomCenter) {
                    Text(p.name.uppercase(), color = Color.White, fontWeight = FontWeight.Black, modifier = Modifier.background(Color.Cyan.copy(0.3f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }

            Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(24.dp)) {
                Box(Modifier.align(Alignment.BottomStart)) { 
                    VirtualJoystickPro { viewModel.onJoystickMove(it) } 
                }
                ActionButtonsPanel(uiState, viewModel)
            }

            if (uiState.gameState == GameState.Finished) {
                PostMatchSummaryOverlay(uiState, onExit)
            }
        }
    }

    @Composable
    fun ActionButtonsPanel(uiState: FootballUiState, viewModel: QuantumFootballViewModel) {
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = { viewModel.setSprint(!uiState.isSprinting) }, 
                modifier = Modifier.background(if(uiState.isSprinting) Color.Yellow else Color.White.copy(0.1f), CircleShape).size(50.dp)
            ) { Icon(Icons.Default.Speed, null, tint = if(uiState.isSprinting) Color.Black else Color.White) }
            
            Spacer(Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GameActionButton("THR", Color.Blue) { viewModel.performThroughBall() }
                GameActionButton("PASS", Color.Green) { viewModel.performPass() }
                GameActionButton("SHOOT", Color.Red) { viewModel.releaseShoot() }
            }
        }
    }

    @Composable
    fun GameActionButton(label: String, color: Color, onClick: () -> Unit) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = color.copy(0.2f), contentColor = color),
            border = androidx.compose.foundation.BorderStroke(1.dp, color),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(65.dp, 45.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(label, fontWeight = FontWeight.Black, fontSize = 10.sp)
        }
    }

    @Composable
    fun BroadcastScoreboard(uiState: FootballUiState) {
        Surface(color = Color.Black.copy(0.6f), shape = RoundedCornerShape(8.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.1f))) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(uiState.userTeam?.id ?: "USER", color = Color.Cyan, fontWeight = FontWeight.Bold)
                Text(" ${uiState.scoreUser} - ${uiState.scoreAi} ", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text(uiState.opponentTeam?.id ?: "AI", color = Color.Red, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(12.dp))
                Text("${uiState.matchTimeSeconds/60}:00", color = Color.Yellow, fontSize = 12.sp)
            }
        }
    }

    @Composable
    fun VirtualJoystickPro(onMove: (Offset) -> Unit) {
        var off by remember { mutableStateOf(Offset.Zero) }
        Box(modifier = Modifier.size(130.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(100.dp).border(2.dp, Color.White.copy(0.2f), CircleShape))
            Box(
                Modifier
                    .offset { IntOffset(off.x.toInt(), off.y.toInt()) }
                    .size(45.dp)
                    .background(Color.White.copy(0.8f), CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val newOff = off + dragAmount
                                if (newOff.getDistance() < 60f) {
                                    off = newOff
                                    onMove(off / 60f)
                                }
                            },
                            onDragEnd = {
                                off = Offset.Zero
                                onMove(Offset.Zero)
                            }
                        )
                    }
            )
        }
    }

    @Composable
    private fun SurfacePlacementHint() {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.4f)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color.Cyan)
                Spacer(Modifier.height(16.dp))
                Text("SCANNING ENVIRONMENT...\nTAP FLOOR TO PLACE ARENA", color = Color.Cyan, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, letterSpacing = 1.sp)
            }
        }
    }

    @Composable
    fun PostMatchSummaryOverlay(uiState: FootballUiState, onExit: () -> Unit) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(0.85f)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("FINAL SCORE", color = Color.Gray, fontSize = 12.sp)
                Text("${uiState.scoreUser} - ${uiState.scoreAi}", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(24.dp))
                Button(onClick = onExit, colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan, contentColor = Color.Black)) {
                    Text("RETURN TO SOVEREIGN HUB", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    private fun checkArSupportAndSetup() {
        val availability = ArCoreApk.getInstance().checkAvailability(this)
        if (availability.isSupported) {
            try {
                arSession = Session(this)
                val config = Config(arSession)
                config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                config.focusMode = Config.FocusMode.AUTO
                arSession?.configure(config)
                isArSupported.value = true
            } catch (e: Exception) { isArSupported.value = false ; renderer.enableFallbackMode() }
        } else { isArSupported.value = false ; renderer.enableFallbackMode() }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP && isArSupported.value) {
            val session = arSession ?: return false
            try {
                val frame = session.update()
                val hits = frame.hitTest(event.x, event.y)
                for (hit in hits) {
                    val trackable = hit.trackable
                    if (trackable is com.google.ar.core.Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                        val anchor = hit.createAnchor()
                        viewModel.onStadiumPlaced(anchor); renderer.setStadiumAnchor(anchor); break
                    }
                }
            } catch (e: Exception) {}
        }
        return super.onTouchEvent(event)
    }

    override fun onResume() { super.onResume() ; if (isArSupported.value) try { arSession?.resume() } catch (e: Exception) {} ; glSurfaceView?.onResume() }
    override fun onPause() { super.onPause() ; if (isArSupported.value) arSession?.pause() ; glSurfaceView?.onPause() ; renderer.releaseSurface() }
    override fun onDestroy() { super.onDestroy() ; if (isArSupported.value) arSession?.close() }
}
