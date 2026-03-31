package com.argesurec.shared.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argesurec.shared.model.Project
import com.argesurec.shared.model.ProjectPhase
import com.argesurec.shared.repository.ProjectRepository
import com.argesurec.shared.util.UiState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProjectsData(
    val projects: List<Project> = emptyList(),
    val selectedPhase: ProjectPhase? = null,
    val activeProjectsCount: Int = 0,
    val completedProjectsCount: Int = 0
)

class ProjectsViewModel(
    private val repository: ProjectRepository,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<ProjectsData>>(UiState.Loading)
    val state: StateFlow<UiState<ProjectsData>> = _state.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    fun clearActionMessage() { _actionMessage.value = null }

    init {
        loadProjects()
    }

    fun loadProjects() {
        viewModelScope.launch {
            _state.emit(UiState.Loading)
            try {
                repository.getAll().collect { projects ->
                    val activeCount = projects.size // Şimdilik hepsi aktif sayılabilir veya filtre eklenebilir
                    val completedCount = projects.count { it.status == "Tamamlandı" }
                    _state.emit(UiState.Success(ProjectsData(
                        projects = projects,
                        activeProjectsCount = activeCount,
                        completedProjectsCount = completedCount
                    )))
                }
            } catch (e: Exception) {
                _state.emit(UiState.Error(e.message ?: "Beklenmedik bir hata oluştu."))
            }
        }
    }

    fun loadByPhase(phase: ProjectPhase) {
        viewModelScope.launch {
            _state.emit(UiState.Loading)
            try {
                repository.getByPhase(phase).collect { projects ->
                    _state.emit(UiState.Success(ProjectsData(projects = projects, selectedPhase = phase)))
                }
            } catch (e: Exception) {
                _state.emit(UiState.Error(e.message ?: "Filtreleme sırasında hata oluştu."))
            }
        }
    }

    fun createProject(nameInput: String, descriptionInput: String?, phaseInput: ProjectPhase, budgetTotal: Double = 0.0, budgetSpent: Double = 0.0) {
        viewModelScope.launch {
            val ownerId = supabase.auth.currentUserOrNull()?.id
            if (ownerId == null) {
                _actionMessage.emit("Oturum açık değil.")
                return@launch
            }

            val newProject = Project(
                id = null,
                name = nameInput,
                description = descriptionInput,
                phase = phaseInput,
                ownerId = ownerId,
                budgetTotal = budgetTotal,
                budgetSpent = budgetSpent,
                createdAt = null
            )

            val result = repository.insert(newProject)
            if (result.isSuccess) {
                loadProjects()
                _actionMessage.emit("Proje başarıyla oluşturuldu.")
            } else {
                _actionMessage.emit(result.exceptionOrNull()?.message ?: "Proje oluşturulamadı.")
            }
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            val result = repository.delete(id)
            if (result.isSuccess) {
                loadProjects()
            } else {
                _state.emit(UiState.Error(result.exceptionOrNull()?.message ?: "Silme işlemi başarısız."))
            }
        }
    }
}
