package com.argesurec.shared.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argesurec.shared.model.TeamMemberWithProfile
import com.argesurec.shared.repository.TeamRepository
import com.argesurec.shared.util.UiState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class TeamData(
    val members: List<TeamMemberWithProfile> = emptyList()
)

class TeamViewModel(
    private val repository: TeamRepository,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<TeamData>>(UiState.Loading)
    val state: StateFlow<UiState<TeamData>> = _state.asStateFlow()
    
    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    fun clearActionMessage() { _actionMessage.value = null }
    
    private var currentProjectId: String? = null

    init {
        // No initial load, screen will call loadTeamForProject
    }

    fun loadTeam() {
        viewModelScope.launch {
            _state.emit(UiState.Loading)
            try {
                repository.getAllWithProfiles().collect { members ->
                    _state.emit(UiState.Success(TeamData(members = members)))
                }
            } catch (e: Exception) {
                _state.emit(UiState.Error(e.message ?: "Ekip listesi yüklenemedi."))
            }
        }
    }

    fun loadTeamForProject(projectId: String) {
        currentProjectId = projectId
        viewModelScope.launch {
            _state.emit(UiState.Loading)
            try {
                repository.getByProjectWithProfiles(projectId).collect { members ->
                    _state.emit(UiState.Success(TeamData(members = members)))
                }
            } catch (e: Exception) {
                _state.emit(UiState.Error(e.message ?: "Ekip üyeleri yüklenemedi."))
            }
        }
    }

    fun inviteMember(email: String, role: String, projectId: String) {
        viewModelScope.launch {
            _state.emit(UiState.Loading)
            try {
                // Edge Function: /functions/v1/invite-member
                supabase.functions.invoke("invite-member", buildJsonObject {
                    put("email", email)
                    put("projectId", projectId)
                    put("role", role)
                })
                loadTeamForProject(projectId)
                _actionMessage.emit("Davet başarıyla gönderildi.")
            } catch (e: Exception) {
                _actionMessage.emit(e.message ?: "Davet gönderilemedi.")
            }
        }
    }

    fun updateMemberRole(memberId: String, projectId: String, newRole: String) {
        viewModelScope.launch {
            try {
                val member = repository.getById(memberId)
                if (member != null) {
                    val updated = member.copy(role = newRole)
                    val result = repository.update(updated)
                    if (result.isSuccess) {
                        _actionMessage.emit("Rol güncellendi.")
                        loadTeamForProject(projectId)
                    } else {
                        _actionMessage.emit(result.exceptionOrNull()?.message ?: "Rol güncellenemedi.")
                    }
                }
            } catch (e: Exception) {
                _actionMessage.emit(e.message ?: "Rol güncellenemedi.")
            }
        }
    }

    fun removeMember(userId: String) {
        viewModelScope.launch {
            val result = repository.delete(userId)
            if (result.isSuccess) {
                _actionMessage.emit("Üye başarıyla çıkarıldı.")
                currentProjectId?.let { loadTeamForProject(it) } ?: loadTeam()
            } else {
                _actionMessage.emit(result.exceptionOrNull()?.message ?: "Üye çıkarılamadı.")
            }
        }
    }
}
