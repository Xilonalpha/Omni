package com.chemscanner.omniscient.ui.dialogs

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chemscanner.omniscient.databinding.FragmentProDialogBinding
import com.chemscanner.omniscient.utils.billing.BillingManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ProDialogFragment(private val billingManager: BillingManager) : BottomSheetDialogFragment() {

    private var _binding: FragmentProDialogBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProDialogBinding.inflate(inflater, container, false)

        binding.btnProYearly.setOnClickListener {
            billingManager.launchProBillingFlow(requireActivity())
            dismiss()
        }

        if (billingManager.isProUser()) {
            binding.tvProStatus.text = "PRO ACTIV"
            binding.btnProYearly.visibility = View.GONE
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}