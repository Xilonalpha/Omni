package com.chemscanner.omniscient.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.UserRepository // FIXED IMPORT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    var uiState by mutableStateOf(RegisterUiState())
        private set

    private val _navigationEvent = MutableSharedFlow<RegisterNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun onEmailChange(email: String) {
        uiState = uiState.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        uiState = uiState.copy(password = password)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        uiState = uiState.copy(confirmPassword = confirmPassword)
    }

    fun onRegisterClick() {
        if (uiState.password != uiState.confirmPassword) {
            uiState = uiState.copy(error = "Passwords do not match.")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                userRepository.register(uiState.email, uiState.password)
                _navigationEvent.emit(RegisterNavigationEvent.NavigateBack)
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message ?: "An unknown error occurred")
            }
            uiState = uiState.copy(isLoading = false)
        }
    }
}

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class RegisterNavigationEvent {
    object NavigateBack : RegisterNavigationEvent()
}
