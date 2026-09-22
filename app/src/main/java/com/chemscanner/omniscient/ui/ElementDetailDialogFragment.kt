package com.chemscanner.omniscient.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.chemscanner.omniscient.marrow.data.models.PeriodicElement // FIXED IMPORT
import com.chemscanner.omniscient.databinding.DialogElementDetailBinding

class ElementDetailDialogFragment : DialogFragment() {

    private var _binding: DialogElementDetailBinding? = null
    private val binding get() = _binding!!

    private var element: PeriodicElement? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            element = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable("element", PeriodicElement::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.getParcelable("element")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogElementDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateViews()
        binding.closeDialogButton.setOnClickListener {
            dismiss()
        }
    }

    private fun populateViews() {
        element?.let {
            binding.detailSymbol.text = it.symbol
            binding.detailName.text = it.name
            binding.detailAtomicNumber.text = "Atomic Number: ${it.atomicNumber}"
            binding.detailCategory.text = "Category: ${it.category}"
            binding.detailAtomicMass.text = "Atomic Mass: ${it.atomicMass} u"
            
            // New Advanced Properties
            binding.detailElectronConfig.text = "Electron Config: ${it.electronConfiguration ?: "N/A"}"
            binding.detailElectronegativity.text = "Electronegativity: ${it.electronegativity ?: "N/A"}"
            
            // Isotopes
            if (it.isotopes.isNotEmpty()) {
                binding.detailIsotopes.text = it.isotopes.joinToString(", ")
            } else {
                binding.detailIsotopes.text = "No common isotopes listed."
            }

            binding.detailDensity.text = it.density?.let { d -> "Density: $d g/L" } ?: "Density: N/A"
            binding.detailMelt.text = it.melt?.let { m -> "Melting Point: $m K" } ?: "Melting Point: N/A"
            binding.detailBoil.text = it.boil?.let { b -> "Boiling Point: $b K" } ?: "Boiling Point: N/A"
            binding.detailDiscoveredBy.text = "Discovered by: ${it.discoveredBy ?: "N/A"}"
            binding.detailSummary.text = it.summary ?: "No summary available."
        }
    }

    override fun getTheme(): Int {
        return super.getTheme()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(element: PeriodicElement): ElementDetailDialogFragment {
            val args = Bundle().apply {
                putParcelable("element", element)
            }
            return ElementDetailDialogFragment().apply {
                arguments = args
            }
        }
    }
}
