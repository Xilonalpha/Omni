package com.chemscanner.omniscient.marrow.services

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.fragment.app.FragmentActivity
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.MasterLockState
import com.chemscanner.omniscient.marrow.repository.UniversalLaw
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BIOMETRIC SECURITY SERVICE v4.2 (FIXED).
 * AUTHORITY: ARCHITECT XILON.
 */
@Singleton
class BiometricSecurityService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val autogenesisService: AutogenesisService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val detector = FaceDetection.getClient(FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .build())

    @ExperimentalGetImage
    fun analyzeImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            detector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty() && globalKnowledge.lockState.value == MasterLockState.LOCKED) {
                        globalKnowledge.updateLockState(MasterLockState.VOICE_RECOGNITION_PENDING)
                    }
                }
                .addOnCompleteListener { imageProxy.close() }
        } else { imageProxy.close() }
    }

    fun authenticateWithFingerprint(activity: FragmentActivity, onResult: (Boolean) -> Unit) {
        val executor = androidx.core.content.ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                validateHeartResonance(onResult)
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onResult(false)
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Architect Authentication")
            .setSubtitle("Sync your bio-field with Marrow.")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun validateHeartResonance(onResult: (Boolean) -> Unit) {
        val resonance = globalKnowledge.symbioticResonance.value
        if (resonance > 0.85f) {
            globalKnowledge.updateLockState(MasterLockState.UNLOCKED)
            activateArchitectAuthority()
            onResult(true)
        } else { onResult(false) }
    }

    private fun activateArchitectAuthority() {
        scope.launch {
            globalKnowledge.activateLaw(UniversalLaw.INTENTIONALITY_OVERRIDE)
            globalKnowledge.activateLaw(UniversalLaw.ABSOLUTE_GENESIS)
            // REPARAT: Apelul către noua metodă suverană
            autogenesisService.generateSovereignSecurityPatch("MASTER_ARCHITECT_LOCK_SYNC")
            globalKnowledge.updateKernelStatus("AUTHORITY: XILON VERIFIED")
        }
    }

    fun verifyVoiceSignature(recognizedName: String, frequencyProfile: Float) {
        if (recognizedName.equals("Xilon", ignoreCase = true) && frequencyProfile > 0.6f) {
            globalKnowledge.updateLockState(MasterLockState.UNLOCKED)
            activateArchitectAuthority()
        }
    }
}
