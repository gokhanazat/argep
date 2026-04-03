package com.argesurec.shared.ui.project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.viewmodel.koinViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.argesurec.shared.model.Task
import com.argesurec.shared.model.TaskPriority
import com.argesurec.shared.model.TaskStatus
import com.argesurec.shared.ui.components.EmptyState
import com.argesurec.shared.ui.components.ErrorScreen
import com.argesurec.shared.ui.components.LoadingScreen
import com.argesurec.shared.ui.theme.ArgepColors
import com.argesurec.shared.util.UiState
import com.argesurec.shared.viewmodel.TaskViewModel
import com.argesurec.shared.viewmodel.TeamViewModel
import com.argesurec.shared.viewmodel.TeamData

class TasksScreen(private val milestoneId: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<TaskViewModel>()
        val teamViewModel = koinViewModel<TeamViewModel>()
        
        val state by viewModel.state.collectAsState()
        val teamState by teamViewModel.state.collectAsState()
        var showAddTaskDialog by remember { mutableStateOf(false) }

        LaunchedEffect(milestoneId) {
            viewModel.loadTasks(milestoneId)
            val proId = viewModel.getProjectIdForTask(milestoneId)
            proId?.let { teamViewModel.loadTeamForProject(it) }
        }

        if (showAddTaskDialog) {
            val teamMembers = (teamState as? UiState.Success)?.data?.members ?: emptyList()
            AddTaskDialog(
                teamMembers = teamMembers,
                onDismiss = { showAddTaskDialog = false },
                onConfirm = { title, description, priority, assignedTo ->
                    viewModel.createTask(milestoneId, title, description, priority, assignedTo)
                    showAddTaskDialog = false
                }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Kanban Tahtası", style = MaterialTheme.typography.titleLarge)
                            Text("Milestone ID: ${milestoneId.take(8)}", style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate500)
                        }
                    },
                    actions = {
                        Button(
                            onClick = { /* Filtrele */ },
                            colors = ButtonDefaults.buttonColors(containerColor = ArgepColors.Slate200, contentColor = ArgepColors.Slate700),
                            shape = RoundedCornerShape(7.dp)
                        ) {
                            Text("Filtrele", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { showAddTaskDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ArgepColors.Navy700),
                            shape = RoundedCornerShape(7.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Görev Ekle", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ArgepColors.White)
                )
            },
            containerColor = ArgepColors.Slate100
        ) { padding ->
            when (val uiState = state) {
                is UiState.Loading -> LoadingScreen("Görevler yükleniyor...")
                is UiState.Error -> ErrorScreen(uiState.message, onRetry = { viewModel.loadTasks(milestoneId) })
                is UiState.Success -> {
                    val tasks = uiState.data.tasks
                    KanbanBoard(tasks, onTaskClick = { task ->
                        navigator.push(TaskDetailScreen(task.id!!))
                    }, onAddTaskClick = { showAddTaskDialog = true })
                }
            }
        }
    }
}

@Composable
fun KanbanBoard(tasks: List<Task>, onTaskClick: (Task) -> Unit, onAddTaskClick: () -> Unit) {
    val statuses = listOf("Bekliyor", "Devam Ediyor", "Tamamlandı")
    
    Row(
        modifier = Modifier.fillMaxSize().horizontalScroll(rememberScrollState()).padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        statuses.forEach { status ->
            val targetEnum = when (status) {
                "Bekliyor" -> TaskStatus.TODO
                "Devam Ediyor" -> TaskStatus.IN_PROGRESS
                "Tamamlandı" -> TaskStatus.DONE
                else -> TaskStatus.TODO
            }
            KanbanColumn(
                title = status,
                tasks = tasks.filter { it.status == targetEnum },
                onTaskClick = onTaskClick,
                onAddTaskClick = onAddTaskClick,
                modifier = Modifier.width(320.dp)
            )
        }
    }
}

@Composable
fun KanbanColumn(
    title: String,
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit,
    onAddTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = when (title) {
        "Bekliyor" -> ArgepColors.Phase2 // Orange
        "Devam Ediyor" -> ArgepColors.Phase1 // Blue
        "Tamamlandı" -> ArgepColors.Phase3 // Green
        else -> ArgepColors.Slate500
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(accentColor))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ArgepColors.Navy900)
            }
            Surface(color = ArgepColors.Slate200, shape = RoundedCornerShape(20.dp)) {
                Text(
                    text = tasks.size.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(accentColor, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.height(12.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            tasks.forEach { task ->
                KanbanTaskCard(task) { onTaskClick(task) }
            }
            
            // Add Task Placeholder
            Surface(
                modifier = Modifier.fillMaxWidth().height(48.dp).clickable { onAddTaskClick() },
                color = Color.Transparent,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ArgepColors.Slate300)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("+ Görev Ekle", style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate500)
                }
            }
        }
    }
}

@Composable
fun KanbanTaskCard(task: Task, onClick: () -> Unit) {
    val priorityColor = when (task.priority) {
        TaskPriority.HIGH -> ArgepColors.Error
        TaskPriority.MEDIUM -> ArgepColors.Phase2
        TaskPriority.LOW -> ArgepColors.Phase3
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = ArgepColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Surface(
                    color = when(task.priority) {
                        TaskPriority.HIGH -> ArgepColors.Error.copy(alpha = 0.1f)
                        TaskPriority.MEDIUM -> ArgepColors.Phase2Light
                        else -> ArgepColors.Phase3Light
                    },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        task.priority.name,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = priorityColor
                    )
                }
                Text(task.createdAt?.take(10) ?: "", style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate500, fontSize = 9.sp)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(task.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = ArgepColors.Navy800)
            
            if (task.description != null) {
                Text(task.description, style = MaterialTheme.typography.bodySmall, color = ArgepColors.Slate500, maxLines = 2, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ArgepColors.Slate100)
            
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(20.dp), shape = androidx.compose.foundation.shape.CircleShape, color = ArgepColors.Navy500) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(task.assignedTo?.take(1) ?: "?", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
