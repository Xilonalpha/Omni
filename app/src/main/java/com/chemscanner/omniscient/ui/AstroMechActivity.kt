package com.chemscanner.omniscient.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chemscanner.omniscient.ui.viewmodels.AstroMechViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@AndroidEntryPoint
class AstroMechActivity : AppCompatActivity(), SensorEventListener {

    private val viewModel: AstroMechViewModel by viewModels()
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null

    private val gravityValues = FloatArray(3)
    private val geomagneticValues = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        setContent {
            AstroMechScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager?.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravityValues, 0, 3)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, geomagneticValues, 0, 3)
        }

        val rotationMatrix = FloatArray(9)
        if (SensorManager.getRotationMatrix(rotationMatrix, null, gravityValues, geomagneticValues)) {
            val orientationValues = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientationValues)
            viewModel.updateDeviceOrientation(
                azimuth = Math.toDegrees(orientationValues[0].toDouble()).toFloat(),
                pitch = Math.toDegrees(orientationValues[1].toDouble()).toFloat()
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AstroMechScreen(
    viewModel: AstroMechViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val density = LocalDensity.current
    
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1500), repeatMode = RepeatMode.Reverse), label = "alpha"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ASTROMECH QUANTUM COMMAND", color = Color.Cyan, style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black))
                        Text(if (uiState.isBlackHoleMode) "SINGULARITY: ACCRETION ACTIVE" else "SOL SYSTEM DYNAMICS", color = if (uiState.isBlackHoleMode) Color.Magenta else Color.Green, fontSize = 8.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                actions = {
                    IconButton(onClick = { viewModel.syncWithRealDiscovery() }) { Icon(Icons.Default.Sync, null, tint = Color.Cyan) }
                    IconButton(onClick = { viewModel.toggleBlackHole() }) {
                        Icon(if (uiState.isBlackHoleMode) Icons.Default.BrightnessLow else Icons.Default.BrightnessHigh, null, tint = if (uiState.isBlackHoleMode) Color.Magenta else Color.Cyan)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White)
            )
        },
        containerColor = Color(0xFF020405)
    ) { padding ->
        // USE BoxWithConstraints Scope effectively
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight

            // Define a dynamic orbital scale based on screen size
            val baseOrbitScaleDp = (screenWidth / 6f).coerceAtMost(screenHeight / 8f)
            val centerOffset = Offset(with(density) { (screenWidth / 2).toPx() }, with(density) { (screenHeight / 2).toPx() })

            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val center = Offset(w / 2, h / 2)
                val orbitRadiusPx = baseOrbitScaleDp.toPx()

                withTransform({
                    rotate(degrees = -uiState.deviceAzimuth, pivot = center)
                }) {
                    // 1. STARS (Logic Preserved)
                    for (i in 0..120) {
                        drawCircle(Color.White, radius = 1.2f, center = Offset(w * (i * 0.17f % 1f), h * (i * 0.83f % 1f)), alpha = starAlpha * (i % 3 / 3f))
                    }

                    // 2. GRAVITATIONAL LENSING
                    val gridStep = 60f
                    val massEffect = if (uiState.isBlackHoleMode) uiState.gravity * 35f else uiState.gravity * 8f
                    for (x in 0..(w / gridStep).toInt()) {
                        for (y in 0..(h / 30).toInt()) {
                            val px = x * gridStep
                            val py = y * 30f
                            val dx = px - center.x
                            val dy = py - center.y
                            val distance = sqrt(dx.pow(2) + dy.pow(2))
                            val distortion = if (distance > 10f) (massEffect * 500f) / (distance.pow(1.5f)) else 0f
                            val angle = atan2(dy, dx)
                            val nx = px - cos(angle) * distortion.coerceAtMost(distance * 0.8f)
                            val ny = py - sin(angle) * distortion.coerceAtMost(distance * 0.8f)
                            drawCircle(color = if (uiState.isBlackHoleMode) Color.Magenta.copy(0.15f) else Color.Cyan.copy(0.1f), radius = 1f, center = Offset(nx, ny))
                        }
                    }

                    // 3. HAWKING RADIATION
                    uiState.hawkingParticles.forEach { p ->
                        drawCircle(Color.White.copy(alpha = p.life), radius = 1.5f, center = center + Offset((p.x * 500).toFloat(), (p.y * 500).toFloat()))
                    }

                    // 4. PHOTON SPHERE
                    if (uiState.isBlackHoleMode) {
                        val rs = uiState.schwarzschildRadius * 200f
                        drawCircle(brush = Brush.sweepGradient(listOf(Color.Transparent, Color.Magenta.copy(0.3f), Color.Transparent)), radius = rs * 2.5f, center = center)
                        drawCircle(Color.Cyan.copy(0.1f), radius = rs * 1.5f, center = center, style = Stroke(1f))
                        drawCircle(brush = Brush.radialGradient(listOf(Color.Black, Color.Transparent)), radius = rs, center = center)
                    }

                    // 5. ORBITAL TRAILS (Precision Link with baseOrbitScaleDp)
                    uiState.activeBodies.filter { it.type == "Planet" }.forEach { body ->
                        drawCircle(Color.Cyan.copy(alpha = 0.05f), radius = (body.orbitDist * orbitRadiusPx).toFloat(), center = center, style = Stroke(1f))
                    }

                    if (uiState.planetTrail.size > 2) {
                        val path = Path()
                        path.moveTo(center.x + uiState.planetTrail[0].first * orbitRadiusPx, center.y + uiState.planetTrail[0].second * orbitRadiusPx)
                        uiState.planetTrail.forEach { (tx, ty) ->
                            path.lineTo(center.x + tx * orbitRadiusPx, center.y + ty * orbitRadiusPx)
                        }
                        drawPath(path, Color.Cyan.copy(0.15f), style = Stroke(width = 2f))
                    }
                }
            }

            // 6. CORE OBJECTS - Fully Synced with BoxWithConstraints scale
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(rotationZ = -uiState.deviceAzimuth),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isBlackHoleMode) {
                    Box(modifier = Modifier.size((uiState.schwarzschildRadius * 200).dp).background(Color.Black, CircleShape).border(2.dp, Color.Magenta.copy(0.5f), CircleShape))
                }

                uiState.activeBodies.forEach { body ->
                    val color = when(body.color) {
                        "Orange" -> Color(0xFFFFA000); "Gray" -> Color.Gray
                        "Yellow" -> Color.Yellow; "Cyan" -> Color.Cyan; "Red" -> Color.Red
                        "Brown" -> Color(0xFF795548); "Gold" -> Color(0xFFFFD700)
                        "LightBlue" -> Color(0xFFADD8E6); "Blue" -> Color.Blue
                        else -> Color.White
                    }
                    val size = if (body.type == "Star") 40.dp else (8 + (log10(body.mass / 1e23) * 2)).coerceIn(6.0, 24.0).dp

                    Box(
                        modifier = Modifier
                            .offset(x = (body.x * baseOrbitScaleDp.value).dp, y = (body.y * baseOrbitScaleDp.value).dp)
                            .size(size)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(color, color.copy(alpha = 0.6f))))
                            .border(if(body.type == "Star") 2.dp else 1.dp, Color.White.copy(0.3f), CircleShape)
                    )
                }

                uiState.activeProbes.forEach { probe ->
                    val distToCenter = sqrt(probe.x.pow(2) + probe.y.pow(2))
                    val redshiftFactor = (distToCenter / 1.5).coerceIn(0.0, 1.0)
                    val probeColor = if (probe.isWarpActive) Color.Yellow else Color(red = (1.0f - redshiftFactor.toFloat()).coerceIn(0f, 1f), green = redshiftFactor.toFloat(), blue = redshiftFactor.toFloat())
                    Box(
                        modifier = Modifier
                            .offset(x = (probe.x * baseOrbitScaleDp.value).dp, y = (probe.y * baseOrbitScaleDp.value).dp)
                            .graphicsLayer(scaleY = if(probe.isStretched) 3f else 1f, rotationZ = Math.toDegrees(atan2(probe.y, probe.x)).toFloat() + 90f)
                            .size(if(probe.isWarpActive) 8.dp else 6.dp)
                            .background(probeColor, if(probe.isStretched) RoundedCornerShape(2.dp) else CircleShape)
                            .border(1.dp, Color.White.copy(0.5f), if(probe.isStretched) RoundedCornerShape(2.dp) else CircleShape)
                    )
                }
            }

            // 7. HUD
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    AstroTelemetryItem("AZIMUTH", "${uiState.deviceAzimuth.toInt()}°", Color.Cyan)
                    AstroTelemetryItem("GRAVITY", "${uiState.gravity.toInt()} M_sol", Color.Magenta)
                    AstroTelemetryItem("SCALE", "${"%.2f".format(baseOrbitScaleDp.value)}", Color.Green)
                }

                if (uiState.lastDiscoveryName != null) {
                    Surface(color = Color.Cyan.copy(0.1f), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().border(0.5.dp, Color.Cyan.copy(0.3f), RoundedCornerShape(8.dp))) {
                        Text("NASA SYNC: ${uiState.lastDiscoveryName}", color = Color.Cyan, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(8.dp))
                    }
                }

                Surface(modifier = Modifier.fillMaxWidth(), color = Color.Black.copy(alpha = 0.9f), shape = RoundedCornerShape(28.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color.Cyan.copy(alpha = 0.3f))) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("MASS MAGNITUDE", color = Color.White.copy(0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Slider(value = uiState.gravity, onValueChange = { viewModel.updateGravity(it) }, valueRange = 5f..250f, colors = SliderDefaults.colors(thumbColor = Color.Cyan, activeTrackColor = Color.Cyan))
                        Text("TEMPORAL VELOCITY", color = Color.White.copy(0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Slider(value = uiState.timeScale, onValueChange = { viewModel.updateTimeScale(it) }, valueRange = 0.1f..8f, colors = SliderDefaults.colors(thumbColor = Color.Magenta, activeTrackColor = Color.Magenta))
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { viewModel.launchSovereignProbe(false) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan, contentColor = Color.Black)) { Text("LAUNCH PROBE", fontWeight = FontWeight.Black) }
                            Button(onClick = { viewModel.launchSovereignProbe(true) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black)) { Text("WARP DRIVE", fontWeight = FontWeight.Black) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AstroTelemetryItem(label: String, value: String, color: Color) {
    Column {
        Text(label, color = color.copy(0.6f), fontSize = 8.sp, fontWeight = FontWeight.Black)
        Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
    }
}
