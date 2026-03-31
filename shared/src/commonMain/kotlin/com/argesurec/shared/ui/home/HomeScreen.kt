package com.argesurec.shared.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.jsonPrimitive
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.viewmodel.koinViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.argesurec.shared.ui.components.*
import com.argesurec.shared.ui.project.TaskDetailScreen
import com.argesurec.shared.ui.theme.ArgepColors
import com.argesurec.shared.util.UiState
import com.argesurec.shared.viewmodel.AuthViewModel
import com.argesurec.shared.viewmodel.ProjectsViewModel
import com.argesurec.shared.viewmodel.ProjectsData
import com.argesurec.shared.viewmodel.TaskViewModel
import com.argesurec.shared.viewmodel.TaskData

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authViewModel = koinViewModel<AuthViewModel>()
        val taskViewModel = koinViewModel<TaskViewModel>()
        val projectsViewModel = koinViewModel<ProjectsViewModel>()
        
        val authState by authViewModel.state.collectAsState()
        val taskUiState by taskViewModel.state.collectAsState()
        val projectsUiState by projectsViewModel.state.collectAsState()

        LaunchedEffect(Unit) {
            taskViewModel.loadAssignedTasks()
            projectsViewModel.loadProjects()
        }

        Scaffold(
            topBar = {
                val userName = authState.currentUser?.userMetadata?.get("full_name")?.jsonPrimitive?.content ?: "Kullanıcı"
                DashboardTopBar(userName)
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(ArgepColors.Slate100)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Stat Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val activePrj = (projectsUiState as? UiState.Success<ProjectsData>)?.data?.activeProjectsCount ?: 0
                    val pendingTsk = (taskUiState as? UiState.Success<TaskData>)?.data?.pendingTasksCount ?: 0
                    val completedTsk = (taskUiState as? UiState.Success<TaskData>)?.data?.completedTasksCount ?: 0
                    
                    PremiumStatCard("Aktif Proje", activePrj.toString(), "Toplam proje", "◫", modifier = Modifier.weight(1f))
                    PremiumStatCard("Bekleyen Görev", pendingTsk.toString(), "Size atanan", "⏳", iconBg = Color(0xFFFEF3C7), modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val completedTsk = (taskUiState as? UiState.Success<TaskData>)?.data?.completedTasksCount ?: 0
                    PremiumStatCard("Tamamlanan", completedTsk.toString(), "Toplam tamamlanan", "✓", iconBg = ArgepColors.Phase3Light, modifier = Modifier.weight(1f))
                    PremiumStatCard("Gecikme", "0", "Aksiyon gerekli", "⚠", iconBg = ArgepColors.Error.copy(alpha = 0.1f), modifier = Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                // Content Grid (Main Dashboard Body)
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Assigned Tasks Card
                    DashboardSectionCard(title = "Bana Atanan Görevler", badge = "7 Görev") {
                        when (val uiState = taskUiState) {
                            is UiState.Loading -> Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                            is UiState.Error -> Text(uiState.message, color = ArgepColors.Error)
                            is UiState.Success<TaskData> -> {
                                uiState.data.assignedTasks.take(5).forEach { task ->
                                    PremiumTaskRow(task, onClick = { navigator.push(TaskDetailScreen(task.id)) })
                                }
                            }
                        }
                    }

                    // Project Progress Card
                    DashboardSectionCard(title = "Proje İlerlemeleri") {
                        when (val uiState = projectsUiState) {
                            is UiState.Loading -> Box(Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
                            is UiState.Error -> Text(uiState.message, color = ArgepColors.Error)
                            is UiState.Success<ProjectsData> -> {
                                if (uiState.data.projects.isEmpty()) {
                                    Text("Henüz proje bulunmuyor.", color = ArgepColors.Slate500, style = MaterialTheme.typography.bodySmall)
                                } else {
                                    uiState.data.projects.take(3).forEach { project ->
                                        val phaseColor = when (project.phase) {
                                            com.argesurec.shared.model.ProjectPhase.INCUBATION -> ArgepColors.Phase1
                                            com.argesurec.shared.model.ProjectPhase.DEVELOPMENT -> ArgepColors.Phase2
                                            com.argesurec.shared.model.ProjectPhase.COMMERCIALIZATION -> ArgepColors.Phase3
                                            else -> ArgepColors.Navy500
                                        }
                                        val phaseLightColor = when (project.phase) {
                                            com.argesurec.shared.model.ProjectPhase.INCUBATION -> ArgepColors.Phase1Light
                                            com.argesurec.shared.model.ProjectPhase.DEVELOPMENT -> ArgepColors.Phase2Light
                                            com.argesurec.shared.model.ProjectPhase.COMMERCIALIZATION -> ArgepColors.Phase3Light
                                            else -> ArgepColors.Navy100
                                        }
                                        val phaseName = when (project.phase) {
                                            com.argesurec.shared.model.ProjectPhase.INCUBATION -> "Kuluçka"
                                            com.argesurec.shared.model.ProjectPhase.DEVELOPMENT -> "Geliştirme"
                                            com.argesurec.shared.model.ProjectPhase.COMMERCIALIZATION -> "Ticarileşme"
                                            else -> "Belirsiz"
                                        }
                                        ProjectProgressRow(project.name, phaseName, 0.45f, phaseColor, phaseLightColor)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(userName: String) {
    TopAppBar(
        title = {
            Column {
                Text("Dashboard", style = MaterialTheme.typography.titleLarge, color = ArgepColors.Navy900)
                Text("Merhaba, $userName", style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate500)
            }
        },
        actions = {
            IconButton(onClick = {}) { Icon(Icons.Default.Search, contentDescription = null) }
            IconButton(onClick = {}) { 
                BadgedBox(badge = { Badge { Text(" ") } }) {
                    Icon(Icons.Default.Notifications, contentDescription = null)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = ArgepColors.White)
    )
}

@Composable
fun DashboardSectionCard(
    title: String,
    badge: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ArgepColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                if (badge != null) {
                    Surface(color = ArgepColors.Navy100, shape = RoundedCornerShape(20.dp)) {
                        Text(badge, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = ArgepColors.Navy700)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}
