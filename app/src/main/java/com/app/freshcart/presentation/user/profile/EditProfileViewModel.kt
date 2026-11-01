package com.app.freshcart.presentation.user.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.freshcart.common.Resource
import com.app.freshcart.domain.model.User
import com.app.freshcart.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileState(
    val user: User? = null,
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val profileImageUrl: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val nameError: String? = null,
    val phoneError: String? = null,
    val addressError: String? = null
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileState())
    val uiState: StateFlow<EditProfileState> = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    private val _saveSuccessEvent = MutableSharedFlow<Unit>()
    val saveSuccessEvent: SharedFlow<Unit> = _saveSuccessEvent.asSharedFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.getCurrentUser().collect { user ->
                user?.let { u ->
                    _uiState.update {
                        it.copy(
                            user = u,
                            name = u.name,
                            phone = u.phone,
                            address = u.address,
                            profileImageUrl = u.profileImageUrl ?: "",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onPhoneChanged(phone: String) {
        _uiState.update { it.copy(phone = phone, phoneError = null) }
    }

    fun onAddressChanged(address: String) {
        _uiState.update { it.copy(address = address, addressError = null) }
    }

    fun onImageUrlChanged(url: String) {
        _uiState.update { it.copy(profileImageUrl = url) }
    }

    fun saveProfile() {
        val state = _uiState.value
        val user = state.user ?: return

        var hasError = false
        if (state.name.trim().isBlank()) {
            _uiState.update { it.copy(nameError = "Name cannot be empty") }
            hasError = true
        }

        if (state.phone.trim().length < 7) {
            _uiState.update { it.copy(phoneError = "Please enter a valid phone number") }
            hasError = true
        }

        if (state.address.trim().isBlank()) {
            _uiState.update { it.copy(addressError = "Delivery address is required") }
            hasError = true
        }

        if (hasError) return

        val updatedUser = user.copy(
            name = state.name.trim(),
            phone = state.phone.trim(),
            address = state.address.trim(),
            profileImageUrl = state.profileImageUrl.trim().ifBlank { null }
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = authRepository.updateProfile(updatedUser)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    _snackbarEvent.emit("Profile updated successfully")
                    _saveSuccessEvent.emit(Unit)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isSaving = false) }
                    _snackbarEvent.emit(result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
