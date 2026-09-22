package com.chemscanner.omniscient.ui.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.ui.ar.ARMode
import com.chemscanner.omniscient.ui.ar.InteractionMode
import com.chemscanner.omniscient.marrow.services.BlockchainNotaryService
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.ui.ar.ObjectRenderer
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import com.google.ar.core.Anchor

/**
 * AR VIEW MODEL v2.6 (STABILIZED CORE).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Bridging UI events with 3D Rendering and Blockchain Notarization.
 * v2.6: Corrected spectral color reference to align with repository sync.
 */
@HiltViewModel
class ARViewModel @Inject constructor(
    private val blockchainNotary: BlockchainNotaryService,
    private val globalKnowledge: GlobalKnowledgeRepository
) : ViewModel() {

    private val _currentMode = MutableLiveData(ARMode.STRUCTURE)
    val currentMode: LiveData<ARMode> = _currentMode

    private val _interactionMode = MutableLiveData(InteractionMode.ROTATE)
    val interactionMode: LiveData<InteractionMode> = _interactionMode
    
    // ACTIVARE: Referință către motorul grafic (gestionată de Activity dar controlată de VM)
    private var objectRenderer: ObjectRenderer? = null

    init {
        observeSpectralSync()
    }

    /**
     * ACTIVARE updateXilonAura: Sincronizăm aura 3D cu datele spectrale reale.
     */
    private fun observeSpectralSync() {
        viewModelScope.launch {
            globalKnowledge.spectralHexColor.collect { hex ->
                // Exemplu: Conversie hex în FloatArray (RGBA) și actualizare Renderer
                val r = 0f; val g = 1f; val b = 0.5f // Placeholder logic pentru aura
                objectRenderer?.updateXilonAura(floatArrayOf(r, g, b, 1.0f))
            }
        }
    }

    fun attachRenderer(renderer: ObjectRenderer) {
        this.objectRenderer = renderer
        Timber.d("AR_CORE: ObjectRenderer linked to ViewModel.")
    }

    fun setARMode(mode: ARMode) {
        _currentMode.value = mode
        globalKnowledge.logEvent("AR_CORE", "Mode changed to: $mode", 2)
    }

    fun setInteractionMode(mode: InteractionMode) {
        _interactionMode.value = mode
        Timber.d("AR_CORE: Interaction mode updated to $mode")
    }

    /**
     * ACTIVARE onRotate: Transmitem inputul de gesturi către motorul 3D.
     */
    fun handleRotation(anchor: Anchor, delta: Float) {
        objectRenderer?.onRotate(anchor, delta)
    }

    fun captureARScreenshot(bitmap: Bitmap) {
        Timber.d("AR_CORE: Capturing neural AR snapshot. Dimensions: ${bitmap.width}x${bitmap.height}")
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val mode = _currentMode.value ?: ARMode.STRUCTURE
            val metadata = "AR_SNAPSHOT | Time: $timestamp | Mode: $mode | Res: ${bitmap.width}x${bitmap.height}"
            try {
                val txHash = blockchainNotary.notarizeDiscovery("AKASHA_AR_CAPTURE", metadata)
                globalKnowledge.logEvent("AR_CORE", "Snapshot notarized in Akasha Ledger: ${txHash.take(12)}...", 5)
            } catch (e: Exception) {
                Timber.e(e, "AR Notarization Failure")
            }
        }
    }
}
