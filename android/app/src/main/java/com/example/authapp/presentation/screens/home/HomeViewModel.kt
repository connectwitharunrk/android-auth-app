package com.example.authapp.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authapp.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<HomeState>(HomeState.Loading)
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _event = Channel<HomeEvent>()
    val event = _event.receiveAsFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        _state.value = HomeState.Loaded(userEmail = "user@example.com")
    }

    fun processIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.Logout -> handleLogout()
        }
    }

    private fun handleLogout() {
        viewModelScope.launch {
            _state.value = HomeState.Loading
            val result = logoutUseCase()
            result.onSuccess {
                _event.send(HomeEvent.LogoutSuccess)
            }
            result.onFailure { error ->
                _state.value = HomeState.Error(error.message ?: "Logout failed")
            }
        }
    }
}

// MVI Pattern
sealed class HomeIntent {
    object Logout : HomeIntent()
}

sealed class HomeState {
    object Loading : HomeState()
    data class Loaded(val userEmail: String) : HomeState()
    data class Error(val message: String) : HomeState()
}

sealed class HomeEvent {
    object LogoutSuccess : HomeEvent()
}
