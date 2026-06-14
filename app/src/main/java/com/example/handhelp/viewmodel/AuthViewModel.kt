package com.example.handhelp.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handhelp.data.model.User
import com.example.handhelp.data.model.UserRole
import com.example.handhelp.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.handhelp.service.HandHelpFirebaseMessagingService
import com.example.handhelp.repository.NotificationRepository

// États possibles de l'authentification
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class NeedsRoleSelection(val uid: String) : AuthState()
    data class Error(val message: String) : AuthState()
    object PasswordResetSent : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        checkAuthState()
    }

    // --- Vérifier si déjà connecté ---
    private fun checkAuthState() {
        val firebaseUser = authRepository.currentUser
        if (firebaseUser != null) {
            loadUserData(firebaseUser.uid)
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    private fun loadUserData(uid: String) {
        viewModelScope.launch {
            val result = authRepository.getUserFromFirestore(uid)
            result.onSuccess { user ->
                _currentUser.value = user
                _authState.value = AuthState.Authenticated
                saveFcmToken(uid)
            }.onFailure {
                _authState.value = AuthState.NeedsRoleSelection(uid)
            }
        }
    }

    // --- Login ---
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.loginWithEmail(email, password)
            result.onSuccess { user ->
                loadUserData(user.uid)
            }.onFailure { e ->
                _authState.value = AuthState.Error(e.localizedMessage ?: "Erreur de connexion")
            }
        }
    }

    // --- Register ---
    fun register(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.registerWithEmail(email, password, displayName)
            result.onSuccess { firebaseUser ->
                _authState.value = AuthState.NeedsRoleSelection(firebaseUser.uid)
            }.onFailure { e ->
                _authState.value = AuthState.Error(e.localizedMessage ?: "Erreur d'inscription")
            }
        }
    }

    private fun saveFcmToken(uid: String) {
        HandHelpFirebaseMessagingService.getToken { token ->
            viewModelScope.launch {
                notificationRepository.saveFcmToken(uid, token)
            }
        }
    }

    // --- Sélection du rôle après inscription ---
    fun selectRole(uid: String, role: UserRole, displayName: String, email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val user = User(
                uid = uid,
                email = email,
                displayName = displayName,
                role = role
            )
            val result = authRepository.saveUserToFirestore(user)
            result.onSuccess {
                _currentUser.value = user
                _authState.value = AuthState.Authenticated
            }.onFailure { e ->
                _authState.value = AuthState.Error(e.localizedMessage ?: "Erreur")
            }
        }
    }

    // --- Google Sign-In ---
    fun getGoogleSignInClient(activity: Activity): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("828446504679-dvoleu49tmt0a2sv7th8panfuj08ggfe.apps.googleusercontent.com") // Remplace par ton Web Client ID Firebase
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, gso)
    }

    fun handleGoogleSignInResult(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = authRepository.signInWithCredential(credential)
            result.onSuccess { user ->
                loadUserData(user.uid)
            }.onFailure { e ->
                _authState.value = AuthState.Error(e.localizedMessage ?: "Erreur Google")
            }
        }
    }

    // --- Facebook Sign-In ---
    fun handleFacebookAccessToken(token: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val credential = FacebookAuthProvider.getCredential(token)
            val result = authRepository.signInWithCredential(credential)
            result.onSuccess { user ->
                loadUserData(user.uid)
            }.onFailure { e ->
                _authState.value = AuthState.Error(e.localizedMessage ?: "Erreur Facebook")
            }
        }
    }

    // --- Mot de passe oublié ---
    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.sendPasswordResetEmail(email)
            result.onSuccess {
                _authState.value = AuthState.PasswordResetSent
            }.onFailure { e ->
                _authState.value = AuthState.Error(e.localizedMessage ?: "Erreur")
            }
        }
    }

    // --- Déconnexion ---
    fun logout() {
        authRepository.logout()
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    // ─────────────────────────────────────────
    // Recharger les données utilisateur depuis Firestore
    // (à appeler après modification du profil)
    // ─────────────────────────────────────────
    fun refreshUserData() {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            val result = authRepository.getUserFromFirestore(uid)
            result.onSuccess { user ->
                _currentUser.value = user
            }
        }
    }
}