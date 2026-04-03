package com.argesurec.shared.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argesurec.shared.model.TeamMemberWithProfile
import com.argesurec.shared.repository.TeamRepository
import com.argesurec.shared.util.UiState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class TeamData(
    val members: List<TeamMemberWithProfile> = emptyList()
)

@kotlinx.serialization.Serializable
data class InviteResponse(
    val success: Boolean = false,
    val status: String = "",
    val error: String? = null
)

class TeamViewModel(
    private val repository: TeamRepository,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<TeamData>>(UiState.Loading)
    val state: StateFlow<UiState<TeamData>> = _state.asStateFlow()

    private val _isActionLoading = MutableStateFlow(false)
    val isActionLoading: StateFlow<Boolean> = _isActionLoading.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage = _actionMessage.asStateFlow()

    fun clearActionMessage() { _actionMessage.value = null }

    // debugInfo artık kullanılmıyor ama derleme hatası çıkmaması için tutuyoruz
    private val _debugInfo = MutableStateFlow<String?>(null)
    val debugInfo: StateFlow<String?> = _debugInfo.asStateFlow()
    fun clearDebugInfo() { _debugInfo.value = null }

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
            _actionMessage.value = "Üye ekleniyor..."
            _isActionLoading.value = true
            try {
                if (projectId == "global" || projectId.isEmpty()) {
                    _actionMessage.value = "Lütfen önce projenin içine girip oradan ekleyin."
                    return@launch
                }

                // Edge Function çağrısını 15 saniye ile sınırla
                val response = kotlinx.coroutines.withTimeout(15000) {
                    supabase.functions.invoke(
                        function = "invite-member",
                        body = buildJsonObject {
                            put("email", email)
                            put("projectId", projectId)
                            put("role", role)
                        }
                    )
                }

                val responseBody = response.bodyAsText()
                val status = response.status.value

                val result = try {
                    Json { ignoreUnknownKeys = true }.decodeFromString<InviteResponse>(responseBody)
                } catch (parseEx: Exception) {
                    // JSON parse edilemiyorsa ham body'yi göster
                    _actionMessage.value = "Sunucu yanıtı: $responseBody"
                    return@launch
                }

                if (result.success) {
                    _actionMessage.value = "Üye başarıyla eklendi."
                    loadTeamForProject(projectId)
                } else {
                    _actionMessage.value = result.error ?: "Hata (HTTP $status)"
                }
            } catch (e: Exception) {
                val errorMsg = "Hata: ${e::class.simpleName} - ${e.message ?: "Bağlantı kesildi/Zaman aşımı"}"
                _actionMessage.value = errorMsg
            } finally {
                _isActionLoading.value = false
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
