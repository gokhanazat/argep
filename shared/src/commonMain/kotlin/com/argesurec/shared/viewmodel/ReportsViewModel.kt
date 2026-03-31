package com.argesurec.shared.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argesurec.shared.model.*
import com.argesurec.shared.repository.ProjectRepository
import com.argesurec.shared.repository.TaskRepository
import com.argesurec.shared.repository.MilestoneRepository
import com.argesurec.shared.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class ProjectHealth(
    val id: String,
    val name: String,
    val phase: ProjectPhase,
    val progress: Float,
    val statusText: String
)

data class PortfolioReport(
    val efficiency: Int = 0,
    val efficiencyTrend: String = "↑ %4",
    val teamScore: Double = 4.8,
    val scoreTrend: String = "↑ 0.2",
    val budgetUsage: Int = 0,
    val budgetTrend: String = "↓ %10",
    val riskLevel: String = "Düşük",
    val riskTrend: String = "Stabil",
    val projectHealths: List<ProjectHealth> = emptyList()
)

class ReportsViewModel(
    private val projectRepository: ProjectRepository,
    private val milestoneRepository: MilestoneRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<PortfolioReport>>(UiState.Loading)
    val state: StateFlow<UiState<PortfolioReport>> = _state.asStateFlow()

    init {
        loadReport()
    }

    fun loadReport() {
        viewModelScope.launch {
            _state.emit(UiState.Loading)
            try {
                val projects = projectRepository.getAll().firstOrNull() ?: emptyList()
                val milestones = milestoneRepository.getAll().firstOrNull() ?: emptyList()
                val tasks = taskRepository.getAll().firstOrNull() ?: emptyList()
                
                // 1. Efficiency (Total Done / Total Tasks)
                val totalTasksCount = tasks.size
                val doneTasksCount = tasks.count { it.status == TaskStatus.DONE }
                val efficiency = if (totalTasksCount > 0) (doneTasksCount * 100) / totalTasksCount else 0
                
                // 2. Budget Usage (Total Spent / Total Budget)
                val totalBudget = projects.sumOf { it.budgetTotal ?: 0.0 }
                val totalSpent = projects.sumOf { it.budgetSpent ?: 0.0 }
                val budgetUsage = if (totalBudget > 0.0) ((totalSpent / totalBudget) * 100).toInt() else 0

                // 3. Risk Analysis
                val totalMilestones = milestones.size
                val overdueMilestones = milestones.count { 
                    it.dueDate != null && it.status != "COMPLETED" 
                }
                val riskLevel = when {
                    overdueMilestones > totalMilestones * 0.3 -> "Yüksek"
                    overdueMilestones > totalMilestones * 0.1 -> "Orta"
                    else -> "Düşük"
                }

                // 4. Project Healths
                val projectHealths = projects.map { project ->
                    val projectMilestones = milestones.filter { it.projectId == project.id }
                    val projectMilestoneIds = projectMilestones.map { it.id }
                    val projectTasks = tasks.filter { it.milestoneId in projectMilestoneIds }
                    
                    val projectTotalTasks = projectTasks.size
                    val projectDoneTasks = projectTasks.count { it.status == TaskStatus.DONE }
                    val progress = if (projectTotalTasks > 0) projectDoneTasks.toFloat() / projectTotalTasks else 0f
                    
                    ProjectHealth(
                        id = project.id ?: "",
                        name = project.name,
                        phase = project.phase,
                        progress = progress,
                        statusText = getPhaseName(project.phase)
                    )
                }

                // Simulated Trends (In a real app, these would come from historical data)
                _state.emit(UiState.Success(PortfolioReport(
                    efficiency = efficiency,
                    efficiencyTrend = if (efficiency > 70) "↑ %5" else "↓ %2",
                    teamScore = 4.2 + (efficiency / 200.0),
                    scoreTrend = "↑ 0.1",
                    budgetUsage = budgetUsage,
                    budgetTrend = if (budgetUsage < 80) "↓ %5" else "↑ %12",
                    riskLevel = riskLevel,
                    riskTrend = if (riskLevel == "Düşük") "Stabil" else "Artıyor",
                    projectHealths = projectHealths
                )))
            } catch (e: Exception) {
                _state.emit(UiState.Error(e.message ?: "Rapor yüklenemedi."))
            }
        }
    }

    private fun getPhaseName(phase: ProjectPhase): String {
        return when(phase) {
            ProjectPhase.INCUBATION -> "Kuluçka"
            ProjectPhase.DEVELOPMENT -> "Geliştirme"
            ProjectPhase.COMMERCIALIZATION -> "Ticarileşme"
        }
    }
}
