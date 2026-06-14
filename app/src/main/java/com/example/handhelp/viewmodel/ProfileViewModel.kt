package com.example.handhelp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handhelp.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    object Success : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // ─────────────────────────────────────────
    // Mettre à jour les informations du profil
    // ─────────────────────────────────────────
    fun updateProfile(uid: String, displayName: String, phone: String, bio: String) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            val result = profileRepository.updateProfile(uid, displayName, phone, bio)
            result.onSuccess {
                _uiState.value = ProfileUiState.Success
            }.onFailure { e ->
                _uiState.value = ProfileUiState.Error(e.localizedMessage ?: "Erreur mise à jour")
            }
        }
    }

    fun resetState() {
        _uiState.value = ProfileUiState.Idle
    }
}