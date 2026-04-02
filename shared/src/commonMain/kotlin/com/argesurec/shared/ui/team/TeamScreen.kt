package com.argesurec.shared.ui.team

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.argesurec.shared.model.UserProfile
import com.argesurec.shared.model.TeamMemberWithProfile
import com.argesurec.shared.ui.components.EmptyState
import com.argesurec.shared.ui.components.ErrorScreen
import com.argesurec.shared.ui.components.LoadingScreen
import com.argesurec.shared.ui.project.PhaseBadge
import com.argesurec.shared.ui.theme.ArgepColors
import com.argesurec.shared.util.ProjectRole
import com.argesurec.shared.util.UiState
import com.argesurec.shared.viewmodel.TeamViewModel

class TeamScreen(private val projectId: String?) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<TeamViewModel>()
        val state by viewModel.state.collectAsState()
        val isActionLoading by viewModel.isActionLoading.collectAsState()

        val snackbarHostState = remember { SnackbarHostState() }
        val actionMessage by viewModel.actionMessage.collectAsState()

        var searchQuery by remember { mutableStateOf("") }
        var showInviteDialog by remember { mutableStateOf(false) }
        var memberToDelete by remember { mutableStateOf<TeamMemberWithProfile?>(null) }
        var memberToEditRole by remember { mutableStateOf<TeamMemberWithProfile?>(null) }

        LaunchedEffect(projectId) {
            if (projectId != null) {
                viewModel.loadTeamForProject(projectId)
            } else {
                viewModel.loadTeam()
            }
        }

        LaunchedEffect(actionMessage) {
            actionMessage?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.clearActionMessage()
            }
        }

        if (showInviteDialog) {
            InviteMemberDialog(
                onDismiss = { showInviteDialog = false },
                onInvite = { email, role ->
                    projectId?.let { viewModel.inviteMember(email, role, it) }
                    showInviteDialog = false
                }
            )
        }

        if (memberToDelete != null) {
            DeleteMemberDialog(
                memberName = memberToDelete?.profile?.fullName ?: "Bu üye",
                onDismiss = { memberToDelete = null },
                onConfirm = {
                    viewModel.removeMember(memberToDelete!!.userId)
                    memberToDelete = null
                }
            )
        }

        if (memberToEditRole != null) {
            EditRoleDialog(
                member = memberToEditRole!!,
                onDismiss = { memberToEditRole = null },
                onConfirm = { newRole ->
                    projectId?.let { viewModel.updateMemberRole(memberToEditRole!!.id, it, newRole) }
                    memberToEditRole = null
                }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Ekip Yönetimi (PROJE MODU)", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = ArgepColors.Navy900)
                            Text(if (projectId != null) "Projeye Dahil Üyeler" else "Tüm Ekip Üyeleri", style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate500)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ArgepColors.White),
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, "Geri", tint = ArgepColors.Navy900)
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = ArgepColors.Slate50
        ) { padding ->
            when (val uiState = state) {
                is UiState.Loading -> LoadingScreen("Ekip listeleniyor...")
                is UiState.Error -> ErrorScreen(uiState.message, onRetry = { viewModel.loadTeam() })
                is UiState.Success -> {
                    val members = if (searchQuery.isEmpty()) {
                        uiState.data.members
                    } else {
                        uiState.data.members.filter { it.profile?.fullName?.contains(searchQuery, true) == true }
                    }
                    
                    Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp)) {
                        // Action Bar (Search & Invite)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Üye ara...", fontSize = 14.sp, color = ArgepColors.Slate400) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp), tint = ArgepColors.Slate400) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ArgepColors.Navy300,
                                    unfocusedBorderColor = ArgepColors.Slate200,
                                    focusedContainerColor = ArgepColors.White,
                                    unfocusedContainerColor = ArgepColors.White
                                )
                            )
                            
                            if (projectId != null) {
                                Button(
                                    onClick = { if (!isActionLoading) showInviteDialog = true },
                                    modifier = Modifier.height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ArgepColors.Navy700),
                                    shape = RoundedCornerShape(10.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                                    enabled = !isActionLoading
                                ) {
                                    if (isActionLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = androidx.compose.ui.graphics.Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Yeni Üye Ekle", style = MaterialTheme.typography.labelLarge)
                                }
                            } else {
                                // Eklenemez uyarısı veya boşluk
                                Text("Üye eklemek için proje seçin", style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate400)
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ArgepColors.Slate100),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column {
                                // Table Header
                                Row(
                                    modifier = Modifier.fillMaxWidth().background(ArgepColors.Slate50).padding(horizontal = 20.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("ÜYE / PERSONEL", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), color = ArgepColors.Slate500, modifier = Modifier.weight(2f))
                                    Text("ROL", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), color = ArgepColors.Slate500, modifier = Modifier.weight(1f))
                                    Text("KATILMA", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), color = ArgepColors.Slate500, modifier = Modifier.weight(1f))
                                    Text("İŞLEMLER", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), color = ArgepColors.Slate500, modifier = Modifier.width(80.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                                }
                                
                                if (members.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                                        Text("Sonuç bulunamadı", color = ArgepColors.Slate400, style = MaterialTheme.typography.bodyMedium)
                                    }
                                } else {
                                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                        items(members) { member ->
                                            TeamMemberTableRow(
                                                member = member,
                                                onEdit = { memberToEditRole = member },
                                                onDelete = { memberToDelete = member }
                                            )
                                            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = ArgepColors.Slate100)
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
fun TeamMemberTableRow(
    member: TeamMemberWithProfile,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Name & Profile
        Row(modifier = Modifier.weight(2f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = ArgepColors.Navy600) {
                Box(contentAlignment = Alignment.Center) {
                    Text(member.profile?.fullName?.take(1) ?: "?", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            Column {
                Text(member.profile?.fullName ?: "İsimsiz", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = ArgepColors.Navy900)
                Text(member.profile?.department ?: "Ar-Ge Departmanı", style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate500)
            }
        }

        // Role Badge
        Box(modifier = Modifier.weight(1f)) {
            val roleEnum = try { ProjectRole.valueOf(member.role ?: "GOZLEMCI") } catch (e: Exception) { ProjectRole.GOZLEMCI }
            val roleColor = when (roleEnum) {
                ProjectRole.PROJE_MUDURU -> ArgepColors.Navy900
                ProjectRole.TEKNIK_LIDER -> ArgepColors.Phase1
                ProjectRole.ARGE_UZMANI -> Color(0xFF8B5CF6) // Purple
                ProjectRole.TEST_MUHENDISI -> ArgepColors.Phase3
                ProjectRole.MALI_UZMAN -> ArgepColors.Phase2
                else -> ArgepColors.Slate500
            }
            Surface(color = roleColor.copy(alpha = 0.1f), shape = RoundedCornerShape(20.dp), border = androidx.compose.foundation.BorderStroke(1.dp, roleColor.copy(alpha = 0.3f))) {
                Text(member.role?.replace("_", " ") ?: "Üye", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium), color = roleColor)
            }
        }

        // Joined Date
        val joinedDate = member.joinedAt.split("T").firstOrNull() ?: "Bilinmiyor"
        Text(joinedDate, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = ArgepColors.Slate500)

        // Actions
        Row(modifier = Modifier.width(80.dp), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = ArgepColors.Slate400) }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp), tint = ArgepColors.Error.copy(alpha = 0.7f)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteMemberDialog(
    onDismiss: () -> Unit,
    onInvite: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(ProjectRole.ARGE_UZMANI) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ekibe Yeni Üye Ekle", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Üyenin e-posta adresini girin ve bir proje rolü atayın.", style = MaterialTheme.typography.bodySmall, color = ArgepColors.Slate500)
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-posta Adresi") },
                    placeholder = { Text("ornek@sirket.com") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
                    )
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedRole.name.replace("_", " "),
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Proje Rolü") },
                        modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                        shape = RoundedCornerShape(8.dp),
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        ProjectRole.entries.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.name.replace("_", " ")) },
                                onClick = {
                                    selectedRole = role
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (email.isNotEmpty()) onInvite(email, selectedRole.name) },
                colors = ButtonDefaults.buttonColors(containerColor = ArgepColors.Navy700),
                shape = RoundedCornerShape(10.dp),
                enabled = email.isNotEmpty()
            ) {
                Text("Ekle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = ArgepColors.Slate500)
            }
        },
        containerColor = Color.White
    )
}

@Composable
fun DeleteMemberDialog(
    memberName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Üyeyi Çıkar", fontWeight = FontWeight.Bold) },
        text = { Text("$memberName isimli üyeyi projeden çıkarmak istediğinize emin misiniz?", style = MaterialTheme.typography.bodyMedium, color = ArgepColors.Slate600) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ArgepColors.Error),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Üyeyi Çıkar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = ArgepColors.Slate500)
            }
        },
        containerColor = Color.White
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoleDialog(
    member: TeamMemberWithProfile,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedRole by remember { mutableStateOf(try { ProjectRole.valueOf(member.role ?: "ARGE_UZMANI") } catch (e: Exception) { ProjectRole.ARGE_UZMANI }) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rolü Düzenle", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("${member.profile?.fullName} için yeni bir proje rolü seçin.", style = MaterialTheme.typography.bodySmall, color = ArgepColors.Slate500)
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedRole.name.replace("_", " "),
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Yeni Rol") },
                        modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                        shape = RoundedCornerShape(10.dp),
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArgepColors.Navy200,
                            unfocusedBorderColor = ArgepColors.Slate200
                        )
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.7f).background(Color.White)
                    ) {
                        ProjectRole.entries.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.name.replace("_", " "), style = MaterialTheme.typography.bodyMedium) },
                                onClick = {
                                    selectedRole = role
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedRole.name) },
                colors = ButtonDefaults.buttonColors(containerColor = ArgepColors.Navy700),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Rolü Güncelle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = ArgepColors.Slate500)
            }
        },
        containerColor = Color.White
    )
}
