package com.chemscanner.omniscient.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.services.GeminiService
import com.chemscanner.omniscient.marrow.services.TextToSpeechService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ScannerPlusActivity : AppCompatActivity() {

    @Inject lateinit var geminiService: GeminiService
    @Inject lateinit var ttsService: TextToSpeechService
    @Inject lateinit var globalKnowledge: GlobalKnowledgeRepository

    private var imageCapture: ImageCapture? = null
    private var selectedPersona = "Nikola Tesla"
    private var selectedLanguage = "ro"
    private var analysisResult = mutableStateOf<String?>(null)

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.CAMERA] == false || permissions[Manifest.permission.RECORD_AUDIO] == false) {
            Toast.makeText(this, "Permisiunile sunt obligatorii.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private val speechLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            spokenText?.let { analyzeEnvironment(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        setContent {
            ScannerPlusScreen(
                analysisText = analysisResult.value,
                onPersonaChange = { selectedPersona = it },
                onLanguageChange = { selectedLanguage = it },
                onVoiceClick = { startVoiceRecognition() },
                onAnalyzeClick = { analyzeEnvironment("Analiză suverană de precizie absolută.") },
                onBack = { finish() },
                onImageCaptureReady = { imageCapture = it }
            )
        }
    }

    private fun checkPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val audioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        if (cameraPermission != PackageManager.PERMISSION_GRANTED || audioPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissionsLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    private fun startVoiceRecognition() {
        val languageCode = when(selectedLanguage) {
            "ro" -> "ro-RO"
            "en" -> "en-US"
            "it" -> "it-IT"
            "fr" -> "fr-FR"
            else -> "ro-RO"
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Omniscient Xilon...")
        }
        speechLauncher.launch(intent)
    }

    private fun analyzeEnvironment(userSpeech: String) {
        val capture = imageCapture ?: return
        capture.takePicture(ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val bitmap = imageProxyToBitmap(image)
                image.close()
                bitmap?.let {
                    lifecycleScope.launch {
                        val langName = when(selectedLanguage) {
                            "ro" -> "ROMÂNĂ"
                            "en" -> "ENGLISH"
                            "it" -> "ITALIANO"
                            "fr" -> "FRANÇAIS"
                            else -> "ROMÂNĂ"
                        }

                        // RESTAURARE LOGICĂ COMPLETĂ v5.1
                        val systemContext = """
                            PROTOCOCOL DE PRECIZIE ABSOLUTĂ XILON v5.1:
                            1. NU oferi răspunsuri bazice. IDENTIFICĂ DIRECT: Marcă, Model, Generație (pt mașini).
                            2. DESCRIE EXHAUSTIV: Culori, texturi, imperfecțiuni, umbre, reflexii.
                            3. ACTIVITATE UMANĂ: Descrie exact acțiunile (ex: copii jucându-se, oameni mergând).
                            4. BIOLOGIE (Fructe/Legume): Grad coacere, bun de mâncat (DA/NU), optim pt cules (DA/NU), boli/dăunători. Pt struguri: estimare grad Brix.
                            5. FĂRĂ HALUCINAȚII: Dacă nu ești sigur 100%, specifică probabilitatea, dar oferă deducția logică.
                            6. PERSOANĂ: $selectedPersona (Limbaj Kardashev I).
                            7. LIMBĂ: $langName.
                            8. QUERY UTILIZATOR: "$userSpeech".
                            
                            IMPORTANT: Respond ONLY in $langName language.
                        """.trimIndent()
                        
                        val analysis = geminiService.analyzeImageWithPersona(it, systemContext, selectedPersona)
                        analysisResult.value = analysis
                        ttsService.speak(analysis, selectedLanguage)
                        globalKnowledge.logEvent("SCAN_PLUS", "Analiză 5.1 livrată.", 5)
                    }
                }
            }
        })
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    override fun onDestroy() {
        ttsService.stop()
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerPlusScreen(
    analysisText: String?,
    onPersonaChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onVoiceClick: () -> Unit,
    onAnalyzeClick: () -> Unit,
    onBack: () -> Unit,
    onImageCaptureReady: (ImageCapture) -> Unit
) {
    var persona by remember { mutableStateOf("Nikola Tesla") }
    var selectedLang by remember { mutableStateOf("ro") }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val personas = listOf("Nikola Tesla", "Albert Einstein", "Marie Curie", "Leonardo da Vinci", "Henri Coandă", "Ana Aslan")
    val languages = listOf("ro" to "🇷🇴 RO", "en" to "🇺🇸 EN", "it" to "🇮🇹 IT", "fr" to "🇫🇷 FR")

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }.also { previewView ->
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                        val imageCapture = ImageCapture.Builder().build()
                        onImageCaptureReady(imageCapture)
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                        } catch (e: Exception) { }
                    }, ContextCompat.getMainExecutor(context))
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        WorldMeshOverlay()
        ScannerHUD()

        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.background(Color.Black.copy(0.5f), CircleShape)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("XILON OMNISCIENT PLUS", color = Color.Cyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("PRECISION MODE: ENABLED", color = Color.Green, fontSize = 8.sp, fontWeight = FontWeight.Black)
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                HudDataWindow("VERITY", "MAX")
                HudDataWindow("LANG", selectedLang.uppercase())
            }

            if (analysisText != null) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    color = Color.Black.copy(0.7f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).border(1.dp, Color.Cyan.copy(0.3f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())) {
                        Text("Sovereign Analysis by $persona:", color = Color.Cyan, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(4.dp))
                        Text(analysisText, color = Color.White, fontSize = 12.sp, lineHeight = 18.sp)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Text("LANGUAGE:", color = Color.Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            LazyRow(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(languages) { (code, label) ->
                    FilterChip(
                        selected = selectedLang == code,
                        onClick = { selectedLang = code; onLanguageChange(code) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color.Yellow, selectedLabelColor = Color.Black)
                    )
                }
            }

            Text("MENTOR:", color = Color.Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            LazyRow(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(personas) { p ->
                    FilterChip(
                        selected = persona == p,
                        onClick = { persona = p; onPersonaChange(p) },
                        label = { Text(p.split(" ").last(), fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color.Cyan, selectedLabelColor = Color.Black)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(onClick = onAnalyzeClick, modifier = Modifier.size(65.dp).border(2.dp, Color.Cyan.copy(0.3f), CircleShape), shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan.copy(alpha = 0.6f))) {
                        Icon(Icons.Default.Analytics, null, modifier = Modifier.size(28.dp), tint = Color.White)
                    }
                    Text("PRECISION", color = Color.Cyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(32.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(onClick = onVoiceClick, modifier = Modifier.size(80.dp).border(2.dp, Color.White.copy(0.3f), CircleShape), shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))) {
                        Icon(Icons.Default.Mic, null, modifier = Modifier.size(36.dp), tint = Color.White)
                    }
                    Text("XILON VOICE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun HudDataWindow(label: String, value: String) {
    Column(modifier = Modifier.background(Color.Black.copy(0.4f), RoundedCornerShape(4.dp)).border(0.5.dp, Color.Cyan.copy(0.5f), RoundedCornerShape(4.dp)).padding(6.dp)) {
        Text(label, color = Color.Cyan, fontSize = 7.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun WorldMeshOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    val alpha by infiniteTransition.animateFloat(initialValue = 0.05f, targetValue = 0.2f, animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Reverse), label = "alpha")
    Canvas(modifier = Modifier.fillMaxSize()) {
        val step = 80f
        for (i in 0..(size.width / step).toInt()) { drawLine(Color.Cyan.copy(alpha = alpha), Offset(i * step, 0f), Offset(i * step, size.height), 1f) }
        for (i in 0..(size.height / step).toInt()) { drawLine(Color.Cyan.copy(alpha = alpha), Offset(0f, i * step), Offset(size.width, i * step), 1f) }
    }
}

@Composable
fun ScannerHUD() {
    val infiniteTransition = rememberInfiniteTransition(label = "hud")
    val scanY by infiniteTransition.animateFloat(initialValue = 0.1f, targetValue = 0.9f, animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Reverse), label = "line")
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(Color.Cyan.copy(alpha = 0.1f), radius = 120f, style = Stroke(width = 1f))
        drawLine(brush = Brush.horizontalGradient(listOf(Color.Transparent, Color.Cyan, Color.Transparent)), start = Offset(0f, size.height * scanY), end = Offset(size.width, size.height * scanY), strokeWidth = 2f, alpha = 0.6f)
    }
}
