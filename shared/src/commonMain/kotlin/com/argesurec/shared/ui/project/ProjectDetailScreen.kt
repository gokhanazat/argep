package com.argesurec.shared.ui.project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.argesurec.shared.model.Milestone
import com.argesurec.shared.model.Project
import com.argesurec.shared.ui.components.EmptyState
import com.argesurec.shared.ui.components.ErrorScreen
import com.argesurec.shared.ui.components.LoadingScreen
import com.argesurec.shared.ui.theme.ArgepColors
import com.argesurec.shared.util.ProjectRole
import com.argesurec.shared.util.UiState
import com.argesurec.shared.viewmodel.MilestoneViewModel
import com.argesurec.shared.viewmodel.MilestoneData
import com.argesurec.shared.viewmodel.ProjectsViewModel
import com.argesurec.shared.viewmodel.ProjectsData
import com.argesurec.shared.viewmodel.TeamViewModel
import com.argesurec.shared.viewmodel.TeamData

class ProjectDetailScreen(private val projectId: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val projectsViewModel = koinViewModel<ProjectsViewModel>()
        val milestoneViewModel = koinViewModel<MilestoneViewModel>()
        val teamViewModel = koinViewModel<TeamViewModel>()

        val projectState by projectsViewModel.state.collectAsState()
        val milestoneState by milestoneViewModel.state.collectAsState()
        val teamState by teamViewModel.state.collectAsState()

        LaunchedEffect(projectId) {
            milestoneViewModel.loadMilestones(projectId)
            teamViewModel.loadTeamForProject(projectId)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("← Projeler", style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate500)
                            val project = (projectState as? UiState.Success<ProjectsData>)?.data?.projects?.find { it.id == projectId }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(project?.name ?: "Proje Detay", style = MaterialTheme.typography.titleLarge)
                                if (project != null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    PhaseBadge(project.phase)
                                } else {
                                    Text("...", style = MaterialTheme.typography.titleLarge)
                                }
                            }
                        }
                    },
                    actions = {
                        Button(
                            onClick = { /* Düzenle */ },
                            colors = ButtonDefaults.buttonColors(containerColor = ArgepColors.Slate200, contentColor = ArgepColors.Slate700),
                            shape = RoundedCornerShape(7.dp)
                        ) {
                            Text("Düzenle", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { /* Milestone Ekle */ },
                            colors = ButtonDefaults.buttonColors(containerColor = ArgepColors.Navy700),
                            shape = RoundedCornerShape(7.dp)
                        ) {
                            Text("+ Milestone Ekle", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ArgepColors.White)
                )
            },
            containerColor = ArgepColors.Slate100
        ) { padding ->
            val project = (projectState as? UiState.Success<ProjectsData>)?.data?.projects?.find { it.id == projectId }
            
            if (project == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = ArgepColors.Navy700)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Proje bilgileri yükleniyor...", color = ArgepColors.Slate500)
                        Button(onClick = { navigator.pop() }, modifier = Modifier.padding(top = 16.dp)) {
                            Text("Geri Dön")
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // LEFT COLUMN (Main Content)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ProgressOverviewCard(0.45f) // İlerleme hesabı eklenebilir
                        
                        // Milestones Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = ArgepColors.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Milestone'lar", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                    val count = (milestoneState as? UiState.Success<MilestoneData>)?.data?.milestones?.size ?: 0
                                    Surface(color = ArgepColors.Navy100, shape = RoundedCornerShape(20.dp)) {
                                        Text("$count milestone", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = ArgepColors.Navy700)
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                when (val mState = milestoneState) {
                                    is UiState.Loading -> Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                                    is UiState.Error -> Text(mState.message, color = ArgepColors.Error)
                                    is UiState.Success<MilestoneData> -> {
                                        mState.data.milestones.forEach { milestone ->
                                            MilestoneTimelineItem(milestone) {
                                                milestone.id?.let { id ->
                                                    navigator.push(MilestoneDetailScreen(id))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // RIGHT COLUMN (Info Panel)
                    Column(modifier = Modifier.width(320.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        InfoCard("Proje Bilgileri") {
                            DetailRow("Faz", { PhaseBadge(project.phase) })
                            DetailRow("Durum", { StatusBadge(project.status ?: "Aktif") })
                            DetailRow("Sahip", { Text(project.ownerId.take(8), style = MaterialTheme.typography.bodyMedium) })
                            DetailRow("Oluşturulma", { Text(project.createdAt?.take(10) ?: "-", style = MaterialTheme.typography.bodyMedium) })
                        }

                        InfoCard("Ekip") {
                            when (val tState = teamState) {
                                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                is UiState.Error -> Text(tState.message, color = ArgepColors.Error)
                                is UiState.Success<TeamData> -> {
                                    if (tState.data.members.isEmpty()) {
                                        Text("Ekip üyesi bulunamadı.", style = MaterialTheme.typography.bodySmall)
                                    } else {
                                        tState.data.members.forEach { member ->
                                            TeamMemberAvatarRow(
                                                name = member.profile?.fullName ?: "İsimsiz",
                                                role = member.role ?: "Üye"
                                            )
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
}

@Composable
fun ProgressOverviewCard(progress: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ArgepColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Genel İlerleme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.headlineMedium, color = ArgepColors.Phase3, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                color = ArgepColors.Phase3,
                trackColor = ArgepColors.Slate200
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("📅 Başlangıç: Oca 2024", style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate500)
                Text("🎯 Bitiş: Haz 2025", style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate500)
                Text("👥 5 ekip üyesi", style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate500)
            }
        }
    }
}

@Composable
fun MilestoneTimelineItem(milestone: Milestone, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(ArgepColors.Phase3))
        Column(modifier = Modifier.weight(1f)) {
            Text(milestone.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Text(milestone.dueDate ?: "Tarih Belirtilmedi", style = MaterialTheme.typography.bodySmall, color = ArgepColors.Slate500)
        }
        StatusBadge(milestone.status ?: "Bekliyor")
    }
}

@Composable
fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ArgepColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ArgepColors.Slate100)
            content()
        }
    }
}

@Composable
fun DetailRow(label: String, valueContent: @Composable () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate500, modifier = Modifier.width(110.dp))
        valueContent()
    }
}

@Composable
fun TeamMemberAvatarRow(name: String, role: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(modifier = Modifier.size(30.dp), shape = CircleShape, color = ArgepColors.Navy600) {
            Box(contentAlignment = Alignment.Center) {
                Text(name.take(1), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Column {
            Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(role, style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate500)
        }
    }
}

@Composable
fun PhaseBadge(phase: com.argesurec.shared.model.ProjectPhase?) {
    val phaseName = when (phase) {
        com.argesurec.shared.model.ProjectPhase.INCUBATION -> "Kuluçka"
        com.argesurec.shared.model.ProjectPhase.DEVELOPMENT -> "Geliştirme"
        com.argesurec.shared.model.ProjectPhase.COMMERCIALIZATION -> "Ticarileşme"
        else -> "Belirsiz"
    }
    
    val phaseColor = when (phase) {
        com.argesurec.shared.model.ProjectPhase.INCUBATION -> ArgepColors.Phase1
        com.argesurec.shared.model.ProjectPhase.DEVELOPMENT -> ArgepColors.Phase2
        com.argesurec.shared.model.ProjectPhase.COMMERCIALIZATION -> ArgepColors.Phase3
        else -> ArgepColors.Navy500
    }
    val phaseBg = when (phase) {
        com.argesurec.shared.model.ProjectPhase.INCUBATION -> ArgepColors.Phase1Light
        com.argesurec.shared.model.ProjectPhase.DEVELOPMENT -> ArgepColors.Phase2Light
        com.argesurec.shared.model.ProjectPhase.COMMERCIALIZATION -> ArgepColors.Phase3Light
        else -> ArgepColors.Navy50
    }
    Surface(color = phaseBg, shape = RoundedCornerShape(20.dp)) {
        Text(phaseName, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = phaseColor)
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "Tamamlandı", "Devam" -> ArgepColors.Phase3
        "Bekliyor" -> ArgepColors.Slate500
        else -> ArgepColors.Error
    }
    Surface(color = ArgepColors.Slate100, shape = RoundedCornerShape(20.dp), border = androidx.compose.foundation.BorderStroke(1.dp, ArgepColors.Slate200)) {
        Text(status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = color)
    }
}
