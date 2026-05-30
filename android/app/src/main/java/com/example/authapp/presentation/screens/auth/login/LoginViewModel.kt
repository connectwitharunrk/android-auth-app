package com.example.authapp.presentation.screens.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authapp.domain.usecase.SigninUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signinUseCase: SigninUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _event = Channel<LoginEvent>()
    val event = _event.receiveAsFlow()

    fun processIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.SignIn -> handleSignIn(intent.email, intent.password)
        }
    }

    private fun handleSignIn(email: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            val result = signinUseCase(email, password)
            result.onSuccess { user ->
                _state.value = LoginState.Success(user)
                _event.send(LoginEvent.NavigateToHome)
            }
            result.onFailure { error ->
                _state.value = LoginState.Error(error.message ?: "Unknown error")
            }
        }
    }
}

// MVI Pattern
sealed class LoginIntent {
    data class SignIn(val email: String, val password: String) : LoginIntent()
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: com.example.authapp.domain.model.User) : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class LoginEvent {
    object NavigateToHome : LoginEvent()
}
