package com.argesurec.shared.ui.project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.argesurec.shared.model.Project
import com.argesurec.shared.ui.components.EmptyState
import com.argesurec.shared.ui.components.ErrorScreen
import com.argesurec.shared.ui.components.LoadingScreen
import com.argesurec.shared.ui.theme.ArgepColors
import com.argesurec.shared.util.UiState
import com.argesurec.shared.viewmodel.ProjectsViewModel

class ProjectsScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<ProjectsViewModel>()
        val state by viewModel.state.collectAsState()
        
        var selectedFilter by remember { mutableStateOf("Tümü") }
        val filters = listOf("Tümü", "Kuluçka", "Geliştirme", "Ticarileşme")
        var showCreateDialog by remember { mutableStateOf(false) }
        
        val snackbarHostState = remember { SnackbarHostState() }
        val actionMessage by viewModel.actionMessage.collectAsState()

        LaunchedEffect(actionMessage) {
            actionMessage?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.clearActionMessage()
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Projeler", style = MaterialTheme.typography.titleLarge) },
                    actions = {
                        FilterChips(filters, selectedFilter) { selectedFilter = it }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { showCreateDialog = true },
                            shape = RoundedCornerShape(7.dp),
                            modifier = Modifier.padding(end = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ArgepColors.Navy700)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Yeni Proje", fontSize = 13.sp)
                        }
                        
                        if (showCreateDialog) {
                            CreateProjectDialog(
                                onDismiss = { showCreateDialog = false },
                                onCreate = { name, desc, phase, total, spent ->
                                    viewModel.createProject(name, desc, phase, total, spent)
                                    showCreateDialog = false
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = ArgepColors.Slate50
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                when (val uiState = state) {
                    is UiState.Loading -> LoadingScreen("Projeler yükleniyor...")
                    is UiState.Error -> ErrorScreen(uiState.message, onRetry = { viewModel.loadProjects() })
                    is UiState.Success -> {
                        val allProjects = uiState.data.projects
                        val filteredProjects = if (selectedFilter == "Tümü") {
                            allProjects
                        } else {
                            allProjects.filter { project ->
                                val enumName = when (project.phase) {
                                    com.argesurec.shared.model.ProjectPhase.INCUBATION -> "Kuluçka"
                                    com.argesurec.shared.model.ProjectPhase.DEVELOPMENT -> "Geliştirme"
                                    com.argesurec.shared.model.ProjectPhase.COMMERCIALIZATION -> "Ticarileşme"
                                    null -> "Belirsiz"
                                }
                                enumName == selectedFilter
                            }
                        }

                        if (filteredProjects.isEmpty()) {
                            EmptyState("Aranan kriterlerde proje bulunamadı.")
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(24.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredProjects) { project ->
                                    PremiumProjectCard(project) {
                                        project.id?.let { id ->
                                            navigator.push(ProjectDetailScreen(id))
                                        }
                                    }
                                }
                                
                                // Yeni Proje Ekle Kartı (Dashed)
                                item {
                                    NewProjectPlaceholderCard(onClick = { showCreateDialog = true })
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
fun FilterChips(filters: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(modifier = Modifier.padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        filters.forEach { filter ->
            val isActive = selected == filter
            Surface(
                modifier = Modifier.clickable { onSelect(filter) },
                color = if (isActive) ArgepColors.Navy700 else ArgepColors.White,
                shape = RoundedCornerShape(20.dp),
                border = if (!isActive) androidx.compose.foundation.BorderStroke(1.dp, ArgepColors.Slate300) else null
            ) {
                Text(
                    text = filter,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = if (isActive) Color.White else ArgepColors.Slate600
                    )
                )
            }
        }
    }
}

@Composable
fun PremiumProjectCard(project: Project, onClick: () -> Unit) {
    val phaseName = when (project.phase) {
        com.argesurec.shared.model.ProjectPhase.INCUBATION -> "Kuluçka"
        com.argesurec.shared.model.ProjectPhase.DEVELOPMENT -> "Geliştirme"
        com.argesurec.shared.model.ProjectPhase.COMMERCIALIZATION -> "Ticarileşme"
        else -> "Belirsiz"
    }

    val phaseColor = when (project.phase) {
        com.argesurec.shared.model.ProjectPhase.INCUBATION -> ArgepColors.Phase1
        com.argesurec.shared.model.ProjectPhase.DEVELOPMENT -> ArgepColors.Phase2
        com.argesurec.shared.model.ProjectPhase.COMMERCIALIZATION -> ArgepColors.Phase3
        else -> ArgepColors.Navy500
    }
    
    val phaseBg = when (project.phase) {
        com.argesurec.shared.model.ProjectPhase.INCUBATION -> ArgepColors.Phase1Light
        com.argesurec.shared.model.ProjectPhase.DEVELOPMENT -> ArgepColors.Phase2Light
        com.argesurec.shared.model.ProjectPhase.COMMERCIALIZATION -> ArgepColors.Phase3Light
        else -> ArgepColors.Navy50
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ArgepColors.Slate200)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(project.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), color = ArgepColors.Navy900)
                    Text(project.description ?: "Açıklama yok", style = MaterialTheme.typography.bodySmall, color = ArgepColors.Slate500, maxLines = 1)
                }
                Surface(color = phaseBg, shape = RoundedCornerShape(6.dp)) {
                    Text(
                        phaseName.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                        color = phaseColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Progress Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("İlerleme", style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate600)
                    Text("%0", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = ArgepColors.Navy900)
                }
                LinearProgressIndicator(
                    progress = 0f,
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = phaseColor,
                    trackColor = phaseBg
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Budget Summary
            Row(modifier = Modifier.fillMaxWidth().background(ArgepColors.Slate50, RoundedCornerShape(8.dp)).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("BÜTÇE", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold), color = ArgepColors.Slate400)
                    Text("${project.budgetTotal ?: 0.0} ₺", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = ArgepColors.Navy800)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("HARCANAN", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold), color = ArgepColors.Slate400)
                    Text("${project.budgetSpent ?: 0.0} ₺", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = ArgepColors.Navy800)
                }
            }
        }
    }
}

@Composable
fun NewProjectPlaceholderCard(onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(180.dp).clickable { onClick() },
        color = ArgepColors.Slate50,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, ArgepColors.Slate200) 
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("+", fontSize = 28.sp, color = ArgepColors.Slate400, fontWeight = FontWeight.Light)
            Text("Yeni Proje Oluştur", style = MaterialTheme.typography.titleSmall, color = ArgepColors.Slate400, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String?, com.argesurec.shared.model.ProjectPhase, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var budgetTotal by remember { mutableStateOf("") }
    var budgetSpent by remember { mutableStateOf("") }
    var selectedPhase by remember { mutableStateOf(com.argesurec.shared.model.ProjectPhase.DEVELOPMENT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Proje Oluştur") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Proje Adı") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Açıklama") }, modifier = Modifier.fillMaxWidth())
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = budgetTotal, 
                        onValueChange = { if(it.all { c -> c.isDigit() || c == '.' }) budgetTotal = it }, 
                        label = { Text("Toplam Bütçe") }, 
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = budgetSpent, 
                        onValueChange = { if(it.all { c -> c.isDigit() || c == '.' }) budgetSpent = it }, 
                        label = { Text("Harcanan Bütçe") }, 
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("PROJE AŞAMASI", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = ArgepColors.Slate500)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    com.argesurec.shared.model.ProjectPhase.entries.forEach { phase ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically, 
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedPhase = phase }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = selectedPhase == phase, onClick = { selectedPhase = phase })
                            Text(
                                when(phase) {
                                    com.argesurec.shared.model.ProjectPhase.INCUBATION -> "Kuluçka"
                                    com.argesurec.shared.model.ProjectPhase.DEVELOPMENT -> "Geliştirme"
                                    com.argesurec.shared.model.ProjectPhase.COMMERCIALIZATION -> "Ticarileşme"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (selectedPhase == phase) ArgepColors.Navy900 else ArgepColors.Slate600
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                onCreate(name, description, selectedPhase, budgetTotal.toDoubleOrNull() ?: 0.0, budgetSpent.toDoubleOrNull() ?: 0.0) 
            }) {
                Text("Oluştur")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}
