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
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun onEmailChange(email: String) {
        uiState = uiState.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        uiState = uiState.copy(password = password)
    }

    fun onLoginClick() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                userRepository.login(uiState.email, uiState.password)
                _navigationEvent.emit(NavigationEvent.NavigateToMain)
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message ?: "An unknown error occurred")
            }
            uiState = uiState.copy(isLoading = false)
        }
    }

    fun onRegisterClick() {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.NavigateToRegister)
        }
    }

    fun onForgotPasswordClick() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                if (uiState.email.isNotBlank()) {
                    userRepository.sendPasswordResetEmail(uiState.email)
                    _navigationEvent.emit(NavigationEvent.PasswordResetSent)
                } else {
                    uiState = uiState.copy(error = "Please enter your email first.")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message ?: "An unknown error occurred")
            }
            uiState = uiState.copy(isLoading = false)
        }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class NavigationEvent {
    object NavigateToMain : NavigationEvent()
    object NavigateToRegister : NavigationEvent()
    object PasswordResetSent : NavigationEvent()
}
