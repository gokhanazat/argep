package com.argesurec.shared.ui.project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.viewmodel.koinViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.argesurec.shared.model.*
import com.argesurec.shared.ui.components.EmptyState
import com.argesurec.shared.ui.components.ErrorScreen
import com.argesurec.shared.ui.components.LoadingScreen
import com.argesurec.shared.ui.theme.ArgepColors
import com.argesurec.shared.viewmodel.ExpenseViewModel
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.argesurec.shared.util.ProjectRole
import com.argesurec.shared.util.UiState
import com.argesurec.shared.viewmodel.MilestoneViewModel
import com.argesurec.shared.viewmodel.MilestoneData
import com.argesurec.shared.viewmodel.ProjectsViewModel
import com.argesurec.shared.viewmodel.ProjectsData
import com.argesurec.shared.viewmodel.TeamViewModel
import com.argesurec.shared.viewmodel.TeamData

private fun formatDate(date: String?): String {
    if (date.isNullOrBlank()) return "-"
    val datePart = date.split("T")[0]
    val parts = datePart.split("-")
    return if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else datePart
}

class ProjectDetailScreen(private val projectId: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        
        val projectsViewModel = koinViewModel<ProjectsViewModel>()
        val milestoneViewModel = koinViewModel<com.argesurec.shared.viewmodel.MilestoneViewModel>()
        val teamViewModel = koinViewModel<com.argesurec.shared.viewmodel.TeamViewModel>()
        val projectFilesViewModel = koinViewModel<com.argesurec.shared.viewmodel.ProjectFilesViewModel>()
        val expenseViewModel = koinViewModel<ExpenseViewModel>()

        val projectsState by projectsViewModel.state.collectAsState()
        val milestoneState by milestoneViewModel.state.collectAsState()
        val teamState by teamViewModel.state.collectAsState()
        val filesState by projectFilesViewModel.state.collectAsState()
        val actionState by projectFilesViewModel.actionState.collectAsState()
        val expenseState by expenseViewModel.state.collectAsState()

        var activeTab by remember { mutableStateOf(0) }
        var showEditDialog by remember { mutableStateOf(false) }
        var showAddMilestoneDialog by remember { mutableStateOf(false) }
        var showAddExpenseDialog by remember { mutableStateOf(false) }
        val snackbarHostState = remember { SnackbarHostState() }
        
        val projectsActionMessage by projectsViewModel.actionMessage.collectAsState()
        val teamActionMessage by teamViewModel.actionMessage.collectAsState()
        val expenseActionMessage by expenseViewModel.actionMessage.collectAsState()

        val filePicker = com.argesurec.shared.util.rememberFilePicker { file ->
            file?.let { projectFilesViewModel.uploadFile(projectId, it) }
        }

        LaunchedEffect(projectId) {
            projectsViewModel.loadProjects()
            milestoneViewModel.loadMilestones(projectId)
            teamViewModel.loadTeamForProject(projectId)
            projectFilesViewModel.loadFiles(projectId)
            expenseViewModel.loadExpenses(projectId)
        }

        LaunchedEffect(projectsActionMessage) {
            projectsActionMessage?.let {
                snackbarHostState.showSnackbar(it)
                projectsViewModel.clearActionMessage()
            }
        }

        LaunchedEffect(teamActionMessage) {
            teamActionMessage?.let {
                snackbarHostState.showSnackbar(it)
                teamViewModel.clearActionMessage()
            }
        }

        LaunchedEffect(expenseActionMessage) {
            expenseActionMessage?.let {
                snackbarHostState.showSnackbar(it)
                expenseViewModel.clearActionMessage()
            }
        }

        // State'ten ilgili projeyi bul
        val projectWithTeam = (projectsState as? UiState.Success<ProjectsData>)?.data?.projects?.find { it.id == projectId }
        val project = projectWithTeam?.toProject()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Row(
                                modifier = Modifier.clickable { navigator.pop() },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(14.dp), tint = ArgepColors.Slate500)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Projeler", style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate500)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(project?.name ?: "Yükleniyor...", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                if (project != null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    PhaseBadge(project.phase)
                                }
                            }
                        }
                    },
                    actions = {
                        Button(
                            onClick = { showEditDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ArgepColors.Slate200, contentColor = ArgepColors.Slate700),
                            shape = RoundedCornerShape(7.dp)
                        ) {
                            Text("Düzenle", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))

                        when (activeTab) {
                            0 -> Button(
                                onClick = { showAddMilestoneDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ArgepColors.Navy700),
                                shape = RoundedCornerShape(7.dp)
                            ) {
                                Text("+ Milestone Ekle", fontSize = 12.sp)
                            }
                            1 -> Button(
                                onClick = { filePicker.launch() },
                                colors = ButtonDefaults.buttonColors(containerColor = ArgepColors.Phase3),
                                shape = RoundedCornerShape(7.dp)
                            ) {
                                Text("+ Dosya Yükle", fontSize = 12.sp, color = Color.White)
                            }
                            2 -> Button(
                                onClick = { showAddExpenseDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ArgepColors.Navy700),
                                shape = RoundedCornerShape(7.dp)
                            ) {
                                Text("+ Harcama Ekle", fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ArgepColors.White)
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = ArgepColors.Slate100
        ) { padding ->
            when (val pState = projectsState) {
                is UiState.Loading -> if (project == null) LoadingScreen("Proje bilgileri alınıyor...")
                is UiState.Error -> ErrorScreen(pState.message, onRetry = { projectsViewModel.loadProjects(force = true) })
                is UiState.Success -> {
                    if (project == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Proje bulunamadı.", color = ArgepColors.Error)
                                Button(onClick = { navigator.pop() }, modifier = Modifier.padding(top = 16.dp)) {
                                    Text("Geri Dön")
                                }
                            }
                        }
                    } else {
                        ProjectDetailContent(
                            padding = padding,
                            project = project!!,
                            milestoneState = milestoneState,
                            teamState = teamState,
                            filesState = filesState,
                            expenseState = expenseState,
                            actionState = actionState,
                            activeTab = activeTab,
                            onTabChange = { activeTab = it },
                            onMilestoneClick = { id -> navigator.push(MilestoneDetailScreen(id)) },
                            onDeleteFile = { path -> projectFilesViewModel.deleteFile(projectId, path) },
                            onUploadClick = { filePicker.launch() },
                            onAddExpenseClick = { showAddExpenseDialog = true },
                            onAddMilestoneClick = { showAddMilestoneDialog = true }
                        )

                        if (showAddExpenseDialog) {
                            AddExpenseDialog(
                                onDismiss = { showAddExpenseDialog = false },
                                onCreate = { amount, desc, cat -> 
                                    expenseViewModel.addExpense(projectId, amount, desc, cat)
                                    showAddExpenseDialog = false
                                }
                            )
                        }

                        if (showEditDialog) {
                            EditProjectDialog(
                                project = project!!,
                                onDismiss = { showEditDialog = false },
                                onUpdate = { updatedProject ->
                                    projectsViewModel.updateProject(updatedProject)
                                    showEditDialog = false
                                }
                            )
                        }

                        if (showAddMilestoneDialog) {
                            AddMilestoneDialog(
                                onDismiss = { showAddMilestoneDialog = false },
                                onCreate = { title, date ->
                                    milestoneViewModel.createMilestone(projectId, title, date)
                                    showAddMilestoneDialog = false
                                }
                            )
                        }
                    }
                }
            }
        }

        LaunchedEffect(actionState) {
            if (actionState is UiState.Success) {
                projectFilesViewModel.clearActionState()
            }
        }
    }
}

@Composable
fun ProjectDetailContent(
    padding: PaddingValues,
    project: Project,
    milestoneState: UiState<MilestoneData>,
    teamState: UiState<TeamData>,
    filesState: UiState<com.argesurec.shared.viewmodel.FileData>,
    expenseState: UiState<com.argesurec.shared.viewmodel.ExpenseData>,
    actionState: UiState<Unit>?,
    activeTab: Int,
    onTabChange: (Int) -> Unit,
    onMilestoneClick: (String) -> Unit,
    onDeleteFile: (String) -> Unit,
    onUploadClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onAddMilestoneClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = Modifier.padding(padding).fillMaxSize()
    ) {
        val isMobile = maxWidth < 800.dp
        
        if (isMobile) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val progress = if ((project.budgetTotal ?: 0.0) > 0) ((project.budgetSpent ?: 0.0) / (project.budgetTotal ?: 1.0)).toFloat() else 0f
                ProgressOverviewCard(project, progress.coerceIn(0f, 1f))
                
                // Tabs
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color.Transparent,
                    contentColor = ArgepColors.Navy700,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                            color = ArgepColors.Navy700
                        )
                    }
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { onTabChange(0) },
                        text = { Text("Milestone'lar", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.DateRange, null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { onTabChange(1) },
                        text = { Text("Dosyalar", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Info, null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { onTabChange(2) },
                        text = { Text("Harcamalar", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(18.dp)) }
                    )
                }

                when(activeTab) {
                    0 -> MilestonesCard(milestoneState, onMilestoneClick)
                    1 -> FilesCard(project, filesState, actionState, onDeleteFile, onUploadClick)
                    2 -> ExpensesCard(expenseState, onAddExpenseClick)
                }

                // Quick Action Cards
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Hızlı İşlemler", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ArgepColors.Slate500)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickActionCard(
                            title = "Milestone Ekle",
                            icon = Icons.Default.DateRange,
                            color = ArgepColors.Navy700,
                            modifier = Modifier.weight(1f),
                            onClick = { onAddMilestoneClick() }
                        )
                        QuickActionCard(
                            title = "Harcama Ekle",
                            icon = Icons.Default.ShoppingCart,
                            color = ArgepColors.Phase3,
                            modifier = Modifier.weight(1f),
                            onClick = { onAddExpenseClick() }
                        )
                    }
                }

                InfoPanel(project, teamState)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // LEFT COLUMN (Main Content)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    val progress = if ((project.budgetTotal ?: 0.0) > 0) ((project.budgetSpent ?: 0.0) / (project.budgetTotal ?: 1.0)).toFloat() else 0f
                    ProgressOverviewCard(project, progress.coerceIn(0f, 1f))

                    // Tabs
                    TabRow(
                        selectedTabIndex = activeTab,
                        containerColor = Color.Transparent,
                        contentColor = ArgepColors.Navy700,
                        divider = {},
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                                color = ArgepColors.Navy700
                            )
                        }
                    ) {
                        Tab(
                            selected = activeTab == 0,
                            onClick = { onTabChange(0) },
                            text = { Text("Milestone'lar", fontSize = 12.sp) },
                            icon = { Icon(Icons.Default.DateRange, null, modifier = Modifier.size(18.dp)) }
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { onTabChange(1) },
                            text = { Text("Dosyalar", fontSize = 12.sp) },
                            icon = { Icon(Icons.Default.Info, null, modifier = Modifier.size(18.dp)) }
                        )
                        Tab(
                            selected = activeTab == 2,
                            onClick = { onTabChange(2) },
                            text = { Text("Harcamalar", fontSize = 12.sp) },
                            icon = { Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(18.dp)) }
                        )
                    }

                    when(activeTab) {
                        0 -> MilestonesCard(milestoneState, onMilestoneClick)
                        1 -> FilesCard(project, filesState, actionState, onDeleteFile, onUploadClick)
                        2 -> ExpensesCard(expenseState, onAddExpenseClick)
                    }

                    // Quick Action Cards
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Hızlı İşlemler", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ArgepColors.Slate500)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            QuickActionCard(
                                title = "Milestone Ekle",
                                icon = Icons.Default.DateRange,
                                color = ArgepColors.Navy700,
                                modifier = Modifier.weight(1f),
                                onClick = { onAddMilestoneClick() }
                            )
                            QuickActionCard(
                                title = "Harcama Ekle",
                                icon = Icons.Default.ShoppingCart,
                                color = ArgepColors.Phase3,
                                modifier = Modifier.weight(1f),
                                onClick = { onAddExpenseClick() }
                            )
                        }
                    }
                }

                // RIGHT COLUMN (Info Panel)
                Column(modifier = Modifier.width(320.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoPanel(project, teamState)
                }
            }
        }
    }
}

@Composable
fun MilestonesCard(
    milestoneState: UiState<MilestoneData>,
    onMilestoneClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ArgepColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Yol Haritası", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            when (val mState = milestoneState) {
                is UiState.Loading -> Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is UiState.Error -> Text(mState.message, color = ArgepColors.Error)
                is UiState.Success -> {
                    val milestones = mState.data.milestones
                    if (milestones.isEmpty()) {
                        EmptyState("Henüz milestone eklenmemiş.")
                    } else {
                        Column {
                            milestones.forEachIndexed { index, milestone ->
                                MilestoneTimelineItem(milestone) {
                                    milestone.id?.let { onMilestoneClick(it) }
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
fun FilesCard(
    project: Project,
    filesState: UiState<com.argesurec.shared.viewmodel.FileData>,
    actionState: UiState<Unit>?,
    onDeleteFile: (String) -> Unit,
    onUploadClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ArgepColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Proje Dosyaları", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                if (actionState is UiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // New Prominent Upload Area
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUploadClick() },
                color = ArgepColors.Slate50,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ArgepColors.Slate200)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = ArgepColors.Phase3, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Yeni Dosya Yükle", style = MaterialTheme.typography.titleSmall, color = ArgepColors.Navy900)
                    Text("Dosya seçmek için tıklayın", style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate500)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val fState = filesState) {
                is UiState.Loading -> Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is UiState.Error -> Text(fState.message, color = ArgepColors.Error)
                is UiState.Success -> {
                    val filesData = fState.data
                    if (filesData.files.isEmpty()) {
                        EmptyState("Bu projeye ait dosya bulunamadı.")
                    } else {
                        Column {
                            filesData.files.forEach { file ->
                                ProjectFileItem(file) {
                                    onDeleteFile("projects/${project.id}/${file.name}")
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
fun InfoPanel(
    project: Project,
    teamState: UiState<TeamData>
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ArgepColors.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Proje Bilgileri", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                DetailRow("Durum", { StatusBadge(project.status ?: "Aktif") })
                DetailRow("Başlangıç", { Text(formatDate(project.startDate ?: project.createdAt), style = MaterialTheme.typography.bodyMedium) })
                DetailRow("Bütçe", { Text("${project.budgetSpent} / ${project.budgetTotal} ₺", style = MaterialTheme.typography.bodyMedium) })
                DetailRow("Faz", { PhaseBadge(project.phase) })

                HorizontalDivider(color = ArgepColors.Slate100)
                
                Text(project.description ?: "Açıklama belirtilmemiş.", style = MaterialTheme.typography.bodyMedium, color = ArgepColors.Slate600)
            }
        }

        // Team Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ArgepColors.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            var showInviteDialog by remember { mutableStateOf(false) }
            val navigator = LocalNavigator.currentOrThrow
            val teamViewModel = koinViewModel<TeamViewModel>()
            val isActionLoading by teamViewModel.isActionLoading.collectAsState()

            if (showInviteDialog) {
                com.argesurec.shared.ui.team.InviteMemberDialog(
                    onDismiss = { showInviteDialog = false },
                    onInvite = { email, role ->
                        teamViewModel.inviteMember(email, role, project.id!!)
                        showInviteDialog = false
                    }
                )
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Proje Ekibi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = { showInviteDialog = true },
                        modifier = Modifier.size(32.dp).background(ArgepColors.Navy700, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                when (val tState = teamState) {
                    is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    is UiState.Error -> Text(tState.message, color = ArgepColors.Error)
                    is UiState.Success -> {
                        val members = tState.data.members
                        if (members.isEmpty()) {
                            EmptyState("Henüz üye eklenmemiş.")
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                members.take(5).forEach { member ->
                                    TeamMemberAvatarRow(member.profile?.fullName ?: "Bilinmiyor", member.role ?: "Üye")
                                }
                                
                                if (members.size > 5) {
                                    TextButton(
                                        onClick = { navigator.push(com.argesurec.shared.ui.team.TeamScreen(project.id)) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Tümünü Gör (${members.size})", color = ArgepColors.Navy700)
                                    }
                                } else {
                                    // Az üye olsa bile detaylı yönetime gitmek isteyebilir
                                    TextButton(
                                        onClick = { navigator.push(com.argesurec.shared.ui.team.TeamScreen(project.id)) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Ekibi Yönet", color = ArgepColors.Navy700)
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
fun ProjectFileItem(file: io.github.jan.supabase.storage.FileObject, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(ArgepColors.Slate50, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Default.List,
            contentDescription = null,
            tint = ArgepColors.Navy600,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            val size = file.metadata?.get("size")?.toString()?.toLongOrNull() ?: 0L
            Text(file.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ArgepColors.Navy900)
            Text("${size / 1024} KB", style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate500)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = { /* İndirme henüz eklenmedi */ }) {
                Icon(Icons.Default.Share, contentDescription = "İndir", tint = ArgepColors.Slate600)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Sil", tint = ArgepColors.Error)
            }
        }
    }
}

@Composable
fun ProgressOverviewCard(project: com.argesurec.shared.model.Project, progress: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ArgepColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Genel İlerleme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.headlineMedium, color = ArgepColors.Phase3)
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
                Text("📅 Başlangıç: ${formatDate(project.startDate ?: project.createdAt)}", style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate500)
                Text("🎯 Bitiş: ${formatDate(project.endDate)}", style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate500)
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

@Composable
fun ExpensesCard(
    expenseState: UiState<com.argesurec.shared.viewmodel.ExpenseData>,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ArgepColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Bütçe ve Harcamalar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = ArgepColors.Navy900)
                    Text("Aylık harcama dağılımı", style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate500)
                }
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ArgepColors.Navy700),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Harcama Ekle", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when(expenseState) {
                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                is UiState.Error -> Text("Hata: ${expenseState.message}", color = ArgepColors.Error)
                is UiState.Success -> {
                    val data = expenseState.data
                    
                    // Grafik Bölümü
                    if (data.expenses.isNotEmpty()) {
                        ExpenseLineChart(data.expenses)
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Liste Bölümü
                    if (data.expenses.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            Text("Henüz bir harcama kaydı yok.", color = ArgepColors.Slate400, style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        data.expenses.forEach { expense ->
                            ExpenseItem(expense)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ArgepColors.Slate100)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseLineChart(expenses: List<com.argesurec.shared.model.Expense>) {
    val chartData = expenses.groupBy { it.expenseDate?.take(7) ?: "Bilinmiyor" }
        .mapValues { it.value.sumOf { exp -> exp.amount } }
        .entries.sortedBy { it.key }.map { it.value }

    if (chartData.size < 2) {
        Text("Grafik için daha fazla kayıt gerekli", style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate400)
        return
    }

    val maxAmount = chartData.maxOrNull()?.toFloat() ?: 1f

    Column {
        Text("HARCAMA TRENDİ", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ArgepColors.Slate500)
        Spacer(modifier = Modifier.height(16.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            val width = size.width
            val height = size.height
            val spacing = width / (chartData.size - 1)
            
            val path = Path()
            chartData.forEachIndexed { index, amount ->
                val x = index.toFloat() * spacing
                val y = height - (amount.toFloat() / maxAmount * height)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                
                drawCircle(color = ArgepColors.Phase3, radius = 4.dp.toPx(), center = androidx.compose.ui.geometry.Offset(x, y))
            }
            
            drawPath(
                path = path,
                color = ArgepColors.Phase3,
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}

@Composable
fun ExpenseItem(expense: com.argesurec.shared.model.Expense) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(40.dp),
            color = when(expense.category) {
                "Personel" -> ArgepColors.Phase1.copy(alpha = 0.1f)
                "Yazılım" -> ArgepColors.Phase2.copy(alpha = 0.1f)
                "Donanım" -> ArgepColors.Phase3.copy(alpha = 0.1f)
                else -> ArgepColors.Slate100
            },
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = when(expense.category) {
                        "Personel" -> Icons.Default.Person
                        "Yazılım" -> Icons.Default.Settings
                        "Donanım" -> Icons.Default.Build
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = when(expense.category) {
                        "Personel" -> ArgepColors.Phase1
                        "Yazılım" -> ArgepColors.Phase2
                        "Donanım" -> ArgepColors.Phase3
                        else -> ArgepColors.Slate500
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(expense.description ?: "Açıklama yok", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ArgepColors.Navy900)
            Text("${expense.category} • ${expense.expenseDate?.take(10) ?: "-"}", style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate500)
        }
        
        Text("₺${expense.amount.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = ArgepColors.Navy900)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onCreate: (Double, String, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Yazılım") }
    val categories = listOf("Personel", "Yazılım", "Donanım", "Hizmet", "Diğer")

    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if(it.all { c -> c.isDigit() || c == '.' }) amount = it },
                    label = { Text("Miktar (₺)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Açıklama") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("KATEGORİ", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ArgepColors.Navy700,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(amount.toDoubleOrNull() ?: 0.0, description, selectedCategory) },
                colors = ButtonDefaults.buttonColors(containerColor = ArgepColors.Navy700),
                enabled = amount.isNotBlank() && description.isNotBlank()
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Vazgeç") }
        }
    )
}

@Composable
fun EditProjectDialog(
    project: Project,
    onDismiss: () -> Unit,
    onUpdate: (Project) -> Unit
) {
    var name by remember { mutableStateOf(project.name) }
    var description by remember { mutableStateOf(project.description ?: "") }
    var budgetTotal by remember { mutableStateOf(project.budgetTotal?.toString() ?: "0.0") }
    var budgetSpent by remember { mutableStateOf(project.budgetSpent?.toString() ?: "0.0") }
    var selectedPhase by remember { mutableStateOf(project.phase ?: com.argesurec.shared.model.ProjectPhase.DEVELOPMENT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Projeyi Düzenle") },
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
                onUpdate(project.copy(
                    name = name,
                    description = description,
                    phase = selectedPhase,
                    budgetTotal = budgetTotal.toDoubleOrNull() ?: 0.0,
                    budgetSpent = budgetSpent.toDoubleOrNull() ?: 0.0
                )) 
            }) {
                Text("Güncelle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

@Composable
fun AddMilestoneDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Milestone Ekle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Milestone Başlığı") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("Teslim Tarihi (GG/AA/YYYY)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { 
                onCreate(title, dueDate.ifEmpty { null }) 
            }) {
                Text("Ekle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

@Composable
fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = ArgepColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ArgepColors.Navy900)
        }
    }
}
