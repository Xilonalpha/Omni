package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.chemscanner.omniscient.marrow.utils.XilonDiscovery
import com.chemscanner.omniscient.marrow.utils.XilonProfManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class XilonProfViewModel @Inject constructor(
    private val xilonProfManager: XilonProfManager
) : ViewModel() {

    val discoveries: StateFlow<List<XilonDiscovery>> = xilonProfManager.discoveries

    fun clearHistory() {
        xilonProfManager.clearDiscoveries()
    }
}
