package com.example.authapp.presentation.screens.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authapp.domain.usecase.SignupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val signupUseCase: SignupUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<SignupState>(SignupState.Idle)
    val state: StateFlow<SignupState> = _state.asStateFlow()

    private val _event = Channel<SignupEvent>()
    val event = _event.receiveAsFlow()

    fun processIntent(intent: SignupIntent) {
        when (intent) {
            is SignupIntent.SignUp -> handleSignUp(intent.email, intent.password, intent.confirmPassword)
        }
    }

    private fun handleSignUp(email: String, password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            _state.value = SignupState.Error("Passwords do not match")
            return
        }

        viewModelScope.launch {
            _state.value = SignupState.Loading
            val result = signupUseCase(email, password)
            result.onSuccess { user ->
                _state.value = SignupState.Success(user)
                _event.send(SignupEvent.NavigateToHome)
            }
            result.onFailure { error ->
                _state.value = SignupState.Error(error.message ?: "Unknown error")
            }
        }
    }
}

// MVI Pattern
sealed class SignupIntent {
    data class SignUp(val email: String, val password: String, val confirmPassword: String) : SignupIntent()
}

sealed class SignupState {
    object Idle : SignupState()
    object Loading : SignupState()
    data class Success(val user: com.example.authapp.domain.model.User) : SignupState()
    data class Error(val message: String) : SignupState()
}

sealed class SignupEvent {
    object NavigateToHome : SignupEvent()
}
