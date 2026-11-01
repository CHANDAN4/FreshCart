package com.app.freshcart.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.freshcart.common.UserRole
import com.app.freshcart.domain.model.User
import com.app.freshcart.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    sealed interface SplashEvent {
        data class NavigateToHome(val user: User) : SplashEvent
        data class NavigateToAdmin(val admin: User) : SplashEvent
        data object NavigateToLogin : SplashEvent
    }

    private val _event = MutableSharedFlow<SplashEvent>()
    val event: SharedFlow<SplashEvent> = _event.asSharedFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            // Smooth splash display duration
            delay(2200)

            val currentUser = authRepository.getCurrentUser().firstOrNull()
            if (currentUser != null) {
                if (currentUser.role == UserRole.ADMIN) {
                    _event.emit(SplashEvent.NavigateToAdmin(currentUser))
                } else {
                    _event.emit(SplashEvent.NavigateToHome(currentUser))
                }
            } else {
                _event.emit(SplashEvent.NavigateToLogin)
            }
        }
    }
}
