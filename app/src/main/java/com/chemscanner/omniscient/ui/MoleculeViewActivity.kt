package com.chemscanner.omniscient.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chemscanner.omniscient.marrow.repository.MainRepository
import com.chemscanner.omniscient.marrow.services.Molecule3DGenerator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MoleculeViewActivity : AppCompatActivity() {

    @Inject lateinit var mainRepository: MainRepository
    @Inject lateinit var molecule3DGenerator: Molecule3DGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Preluăm un identificator SMILES din intent (ex: de la o scanare anterioară)
        val smiles = intent.getStringExtra("EXTRA_SMILES") ?: "C1=CC=CC=C1" // Default: Benzen
        
        // Activăm generarea 3D
        renderMolecule(smiles)
    }

    private fun renderMolecule(smiles: String) {
        lifecycleScope.launch {
            try {
                Timber.d("MoleculeView: Initiating 3D generation for $smiles")
                val gltfJson = molecule3DGenerator.generateGltfFromSmiles(smiles)
                
                if (gltfJson != null) {
                    Timber.i("MoleculeView: 3D GLTF data received. Length: ${gltfJson.length}")
                    // Aici s-ar integra un renderer 3D precum SceneView sau Filament
                    // momentan activăm logica de business prin generarea datelor.
                } else {
                    Timber.w("MoleculeView: Failed to generate 3D model for $smiles")
                }
            } catch (e: Exception) {
                Timber.e(e, "MoleculeView: Rendering error")
            }
        }
    }
}
