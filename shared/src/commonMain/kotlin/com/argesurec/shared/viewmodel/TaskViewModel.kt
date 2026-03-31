package com.argesurec.shared.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argesurec.shared.model.Task
import com.argesurec.shared.model.TaskPriority
import com.argesurec.shared.model.TaskStatus
import com.argesurec.shared.repository.TaskRepository
import com.argesurec.shared.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskData(
    val tasks: List<Task> = emptyList(),
    val assignedTasks: List<Task> = emptyList(),
    val allTasks: List<Task> = emptyList(),
    val pendingTasksCount: Int = 0,
    val completedTasksCount: Int = 0
)

class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<TaskData>>(UiState.Loading)
    val state: StateFlow<UiState<TaskData>> = _state.asStateFlow()

    private val _detailState = MutableStateFlow<UiState<Task>>(UiState.Loading)
    val detailState: StateFlow<UiState<Task>> = _detailState.asStateFlow()

    fun loadTasks(milestoneId: String) {
        viewModelScope.launch {
            _state.emit(UiState.Loading)
            try {
                repository.getByMilestone(milestoneId).collect { tasks ->
                    _state.emit(UiState.Success(TaskData(tasks = tasks)))
                }
            } catch (e: Exception) {
                _state.emit(UiState.Error(e.message ?: "Görevler yüklenemedi."))
            }
        }
    }

    fun loadAssignedTasks() {
        viewModelScope.launch {
            _state.emit(UiState.Loading)
            try {
                repository.getAssignedToMe().collect { tasks ->
                    val pendingCount = tasks.count { it.status != TaskStatus.DONE }
                    val completedCount = tasks.count { it.status == TaskStatus.DONE }
                    _state.emit(UiState.Success(TaskData(
                        assignedTasks = tasks,
                        pendingTasksCount = pendingCount,
                        completedTasksCount = completedCount
                    )))
                }
            } catch (e: Exception) {
                _state.emit(UiState.Error(e.message ?: "Size atanan görevler yüklenemedi."))
            }
        }
    }

    fun loadAllTasks() {
        viewModelScope.launch {
            _state.emit(UiState.Loading)
            try {
                repository.getAll().collect { tasks ->
                    _state.emit(UiState.Success(TaskData(allTasks = tasks)))
                }
            } catch (e: Exception) {
                _state.emit(UiState.Error(e.message ?: "Tüm görevler yüklenemedi."))
            }
        }
    }

    fun createTask(
        milestoneId: String,
        titleInput: String,
        descriptionInput: String?,
        priorityInput: TaskPriority,
        assignedToInput: String?
    ) {
        viewModelScope.launch {
            val newTask = Task(
                id = "",
                milestoneId = milestoneId,
                title = titleInput,
                description = descriptionInput,
                priority = priorityInput,
                status = TaskStatus.TODO,
                assignedTo = assignedToInput,
                createdAt = "",
                updatedAt = ""
            )
            val result = repository.insert(newTask)
            if (result.isSuccess) {
                loadTasks(milestoneId)
            } else {
                _state.emit(UiState.Error(result.exceptionOrNull()?.message ?: "Görev oluşturulamadı."))
            }
        }
    }

    fun updateTaskStatus(taskId: String, milestoneId: String?, newStatus: TaskStatus) {
        viewModelScope.launch {
            val task = repository.getById(taskId) ?: return@launch
            val updatedTask = task.copy(status = newStatus)
            val result = repository.update(updatedTask)
            if (result.isSuccess) {
                if (milestoneId != null) loadTasks(milestoneId)
                loadTaskDetail(taskId)
            }
        }
    }

    fun loadTaskDetail(taskId: String) {
        viewModelScope.launch {
            _detailState.emit(UiState.Loading)
            val task = repository.getById(taskId)
            if (task != null) {
                _detailState.emit(UiState.Success(task))
            } else {
                _detailState.emit(UiState.Error("Görev bulunamadı."))
            }
        }
    }

    fun deleteTask(taskId: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            val result = repository.delete(taskId)
            if (result.isSuccess) {
                onDeleted()
            }
        }
    }
}
