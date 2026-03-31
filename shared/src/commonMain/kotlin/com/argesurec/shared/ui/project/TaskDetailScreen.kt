package com.argesurec.shared.ui.project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.argesurec.shared.ui.theme.ArgepColors
import com.argesurec.shared.util.UiState
import com.argesurec.shared.viewmodel.TaskViewModel

class TaskDetailScreen(private val taskId: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<TaskViewModel>()
        val state by viewModel.state.collectAsState()

        val task = (state as? UiState.Success)?.data?.tasks?.find { it.id == taskId }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Görev Detayı", style = MaterialTheme.typography.titleLarge)
                            Text("T-142 · Proje Bilgisi", style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate500)
                        }
                    },
                    actions = {
                        IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.Notifications, null) }
                        IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.Close, null) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ArgepColors.White)
                )
            },
            containerColor = ArgepColors.Slate100
        ) { padding ->
            if (task == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                Row(modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // LEFT COLUMN (Main Content)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(color = ArgepColors.Phase1Light, shape = RoundedCornerShape(20.dp)) {
                                        Text("Geliştirme", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = ArgepColors.Phase1)
                                    }
                                    SelectionPriorityBadge(task.priority)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(task.title, style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp), fontWeight = FontWeight.Bold, color = ArgepColors.Navy900)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(task.description ?: "Açıklama belirtilmemiş.", style = MaterialTheme.typography.bodyLarge, color = ArgepColors.Slate700)
                                
                                HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = ArgepColors.Slate100)
                                
                                // Status Segmented-like Buttons
                                Text("DURUM GÜNCELLE", style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate500, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    StatusButton("Bekliyor", task.status == TaskStatus.TODO, ArgepColors.Phase2)
                                    StatusButton("Devam Ediyor", task.status == TaskStatus.IN_PROGRESS, ArgepColors.Phase1)
                                    StatusButton("Tamamlandı", task.status == TaskStatus.DONE, ArgepColors.Phase3)
                                }
                            }
                        }
                    }

                    // RIGHT COLUMN (Meta Panel)
                    Column(modifier = Modifier.width(320.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        InfoCard("Görev Detayları") {
                            DetailRow("Atanan", {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(modifier = Modifier.size(24.dp), color = ArgepColors.Navy600, shape = CircleShape) {
                                        Box(contentAlignment = Alignment.Center) { Text("A", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                    }
                                    Text("Ali Veli", style = MaterialTheme.typography.bodyMedium)
                                }
                            })
                            DetailRow("Bitiş Tarihi", { Text("24 Haziran 2025", style = MaterialTheme.typography.bodyMedium) })
                            DetailRow("Oluşturan", { Text("Zeynep Ak", style = MaterialTheme.typography.bodyMedium) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusButton(label: String, isActive: Boolean, activeColor: Color) {
    Surface(
        modifier = Modifier.height(40.dp).clickable { /* Update */ },
        color = if (isActive) activeColor else ArgepColors.Slate50,
        shape = RoundedCornerShape(8.dp),
        border = if (!isActive) androidx.compose.foundation.BorderStroke(1.dp, ArgepColors.Slate200) else null
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(
                label, 
                style = MaterialTheme.typography.labelSmall, 
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) Color.White else ArgepColors.Slate600
            )
        }
    }
}

@Composable
fun SelectionPriorityBadge(priority: TaskPriority) {
    val color = when (priority) {
        TaskPriority.HIGH -> ArgepColors.Error
        TaskPriority.MEDIUM -> ArgepColors.Phase2
        TaskPriority.LOW -> ArgepColors.Phase3
    }
    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(20.dp), border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))) {
        Text(priority.name, modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = color)
    }
}
