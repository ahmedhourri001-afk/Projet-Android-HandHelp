package com.example.handhelp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handhelp.data.model.Mission
import com.example.handhelp.data.model.MissionStatus
import com.example.handhelp.repository.MissionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── États UI ───────────────────────────────
sealed class MissionUiState {
    object Idle : MissionUiState()
    object Loading : MissionUiState()
    object Success : MissionUiState()
    data class Error(val message: String) : MissionUiState()
}

@HiltViewModel
class MissionViewModel @Inject constructor(
    private val missionRepository: MissionRepository
) : ViewModel() {

    // ── Liste des missions actives ──
    private val _missions = MutableStateFlow<List<Mission>>(emptyList())
    val missions: StateFlow<List<Mission>> = _missions.asStateFlow()

    // ── Missions de l'organisateur ──
    private val _myMissions = MutableStateFlow<List<Mission>>(emptyList())
    val myMissions: StateFlow<List<Mission>> = _myMissions.asStateFlow()

    // ── Missions du bénévole (historique) ──
    private val _participatedMissions = MutableStateFlow<List<Mission>>(emptyList())
    val participatedMissions: StateFlow<List<Mission>> = _participatedMissions.asStateFlow()

    // ── Mission sélectionnée ──
    private val _selectedMission = MutableStateFlow<Mission?>(null)
    val selectedMission: StateFlow<Mission?> = _selectedMission.asStateFlow()

    // ── Résultats de recherche ──
    private val _searchResults = MutableStateFlow<List<Mission>>(emptyList())
    val searchResults: StateFlow<List<Mission>> = _searchResults.asStateFlow()

    // ── État UI général ──
    private val _uiState = MutableStateFlow<MissionUiState>(MissionUiState.Idle)
    val uiState: StateFlow<MissionUiState> = _uiState.asStateFlow()

    // ── Catégorie filtrée ──
    private val _selectedCategory = MutableStateFlow("Tous")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // ── Missions filtrées par catégorie ──
    val filteredMissions: StateFlow<List<Mission>>
        get() = _missions // On filtre dans le screen

    // ─────────────────────────────────────────
    // Charger toutes les missions (temps réel)
    // ─────────────────────────────────────────
    fun loadActiveMissions() {
        viewModelScope.launch {
            _uiState.value = MissionUiState.Loading
            missionRepository.getActiveMissions().collect { result ->
                result.onSuccess { missions ->
                    _missions.value = missions
                    _uiState.value = MissionUiState.Idle
                }.onFailure { e ->
                    _uiState.value = MissionUiState.Error(e.localizedMessage ?: "Erreur")
                }
            }
        }
    }

    // ─────────────────────────────────────────
    // Charger les missions de l'organisateur
    // ─────────────────────────────────────────
    fun loadOrganizerMissions(organizerId: String) {
        viewModelScope.launch {
            missionRepository.getMissionsByOrganizer(organizerId).collect { result ->
                result.onSuccess { _myMissions.value = it }
                    .onFailure { e -> _uiState.value = MissionUiState.Error(e.localizedMessage ?: "Erreur") }
            }
        }
    }

    // ─────────────────────────────────────────
    // Charger l'historique du bénévole
    // ─────────────────────────────────────────
    fun loadParticipatedMissions(userId: String) {
        viewModelScope.launch {
            missionRepository.getMissionsByParticipant(userId).collect { result ->
                result.onSuccess { _participatedMissions.value = it }
                    .onFailure { e -> _uiState.value = MissionUiState.Error(e.localizedMessage ?: "Erreur") }
            }
        }
    }

    // ─────────────────────────────────────────
    // Charger une mission par ID
    // ─────────────────────────────────────────
    fun loadMissionById(missionId: String) {
        viewModelScope.launch {
            _uiState.value = MissionUiState.Loading
            val result = missionRepository.getMissionById(missionId)
            result.onSuccess { mission ->
                _selectedMission.value = mission
                _uiState.value = MissionUiState.Idle
            }.onFailure { e ->
                _uiState.value = MissionUiState.Error(e.localizedMessage ?: "Erreur")
            }
        }
    }

    // ─────────────────────────────────────────
    // Créer une mission (organisateur)
    // ─────────────────────────────────────────
    fun createMission(
        title: String,
        description: String,
        category: String,
        location: String,
        date: String,
        time: String,
        volunteersNeeded: Int,
        organizerId: String,
        organizerName: String
    ) {
        viewModelScope.launch {
            _uiState.value = MissionUiState.Loading
            val mission = Mission(
                title = title,
                description = description,
                category = category,
                location = location,
                date = date,
                time = time,
                volunteersNeeded = volunteersNeeded,
                organizerId = organizerId,
                organizerName = organizerName,
                status = MissionStatus.ACTIVE
            )
            val result = missionRepository.createMission(mission)
            result.onSuccess {
                _uiState.value = MissionUiState.Success
            }.onFailure { e ->
                _uiState.value = MissionUiState.Error(e.localizedMessage ?: "Erreur création")
            }
        }
    }

    // ─────────────────────────────────────────
    // S'inscrire à une mission (bénévole)
    // ─────────────────────────────────────────
    fun joinMission(missionId: String, userId: String) {
        viewModelScope.launch {
            _uiState.value = MissionUiState.Loading
            val result = missionRepository.joinMission(missionId, userId)
            result.onSuccess {
                // Recharger la mission pour avoir les données à jour
                loadMissionById(missionId)
                _uiState.value = MissionUiState.Success
            }.onFailure { e ->
                _uiState.value = MissionUiState.Error(e.localizedMessage ?: "Erreur inscription")
            }
        }
    }

    // ─────────────────────────────────────────
    // Se désinscrire d'une mission (bénévole)
    // ─────────────────────────────────────────
    fun leaveMission(missionId: String, userId: String) {
        viewModelScope.launch {
            _uiState.value = MissionUiState.Loading
            val result = missionRepository.leaveMission(missionId, userId)
            result.onSuccess {
                loadMissionById(missionId)
                _uiState.value = MissionUiState.Success
            }.onFailure { e ->
                _uiState.value = MissionUiState.Error(e.localizedMessage ?: "Erreur désinscription")
            }
        }
    }

    // ─────────────────────────────────────────
    // Supprimer une mission (organisateur)
    // ─────────────────────────────────────────
    fun deleteMission(missionId: String) {
        viewModelScope.launch {
            _uiState.value = MissionUiState.Loading
            val result = missionRepository.deleteMission(missionId)
            result.onSuccess {
                _uiState.value = MissionUiState.Success
            }.onFailure { e ->
                _uiState.value = MissionUiState.Error(e.localizedMessage ?: "Erreur suppression")
            }
        }
    }

    // ─────────────────────────────────────────
    // Terminer une mission (organisateur)
    // ─────────────────────────────────────────
    fun completeMission(missionId: String) {
        viewModelScope.launch {
            val result = missionRepository.updateMissionStatus(missionId, MissionStatus.COMPLETED)
            result.onFailure { e -> _uiState.value = MissionUiState.Error(e.localizedMessage ?: "Erreur") }
        }
    }

    // ─────────────────────────────────────────
    // Recherche
    // ─────────────────────────────────────────
    fun search(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _uiState.value = MissionUiState.Loading
            val result = missionRepository.searchMissions(query)
            result.onSuccess {
                _searchResults.value = it
                _uiState.value = MissionUiState.Idle
            }.onFailure { e ->
                _uiState.value = MissionUiState.Error(e.localizedMessage ?: "Erreur recherche")
            }
        }
    }

    // ─────────────────────────────────────────
    // Filtrer par catégorie
    // ─────────────────────────────────────────
    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun resetUiState() {
        _uiState.value = MissionUiState.Idle
    }
}