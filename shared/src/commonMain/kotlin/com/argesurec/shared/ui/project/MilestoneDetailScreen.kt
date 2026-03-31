package com.argesurec.shared.ui.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import com.argesurec.shared.ui.components.ErrorScreen
import com.argesurec.shared.ui.components.LoadingScreen
import com.argesurec.shared.ui.theme.ArgepColors
import com.argesurec.shared.util.UiState
import com.argesurec.shared.viewmodel.MilestoneViewModel

class MilestoneDetailScreen(private val milestoneId: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<MilestoneViewModel>()
        val state by viewModel.state.collectAsState()

        val milestone = (state as? UiState.Success)?.data?.milestones?.find { it.id == milestoneId }

        LaunchedEffect(milestoneId) {
            viewModel.loadMilestoneDetail(milestoneId)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Milestone Detayı", style = MaterialTheme.typography.titleLarge)
                            Text("ID: ${milestoneId.take(8)}", style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate500)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null) }
                    },
                    actions = {
                        Button(
                            onClick = { /* Görevleri Gör */ },
                            colors = ButtonDefaults.buttonColors(containerColor = ArgepColors.Navy700),
                            shape = RoundedCornerShape(7.dp)
                        ) {
                            Text("Kanban'a Git", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ArgepColors.White)
                )
            },
            containerColor = ArgepColors.Slate100
        ) { padding ->
            if (milestone == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                Column(modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(milestone.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                StatusBadge(milestone.status ?: "Bekliyor")
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                                MetaInfoItem("Hedef Bitiş", milestone.dueDate ?: "Belirtilmedi", "📅")
                                MetaInfoItem("Öncelik", "Yüksek", "🔥")
                                MetaInfoItem("Sorumlu", "Teknik Ekip", "👥")
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 32.dp), color = ArgepColors.Slate100)
                            
                            Text("NOTLAR & DETAYLAR", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ArgepColors.Slate500)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Bu milestone projenin temel altyapısının tamamlanmasını hedefler. " +
                                "Veritabanı şeması, auth modülü ve temel UI navigasyon bu fazda bitirilmelidir.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = ArgepColors.Slate700
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetaInfoItem(label: String, value: String, icon: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(modifier = Modifier.size(32.dp), color = ArgepColors.Slate50, shape = RoundedCornerShape(8.dp)) {
            Box(contentAlignment = Alignment.Center) { Text(icon, fontSize = 16.sp) }
        }
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate500)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ArgepColors.Navy900)
        }
    }
}
