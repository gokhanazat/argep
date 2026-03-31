package com.argesurec.shared.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.jsonPrimitive
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.viewmodel.koinViewModel
import com.argesurec.shared.ui.theme.ArgepColors
import com.argesurec.shared.viewmodel.AuthViewModel
import com.argesurec.shared.viewmodel.SettingsViewModel

class ProfileScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val authViewModel = koinViewModel<AuthViewModel>()
        val settingsViewModel = koinViewModel<SettingsViewModel>()
        
        val state by authViewModel.state.collectAsState()
        val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
        val user = state.currentUser
        val fullName = user?.userMetadata?.get("full_name")?.jsonPrimitive?.content ?: "Kullanıcı"

        var showEditDialog by remember { mutableStateOf(false) }
        var showSettingsDialog by remember { mutableStateOf(false) }
        var showAboutDialog by remember { mutableStateOf(false) }
        var editName by remember { mutableStateOf("") }
        var editDept by remember { mutableStateOf("") }

        val departments = listOf(
            "Yazılım Geliştirme",
            "Hardware & PCB",
            "Mekanik Tasarım",
            "Gömülü Sistemler",
            "Proje Yönetimi",
            "Veri Analitiği",
            "Kalite Kontrol"
        )

        // Dialogs
        if (showEditDialog) {
            ProfileEditDialog(
                currentName = editName,
                currentDept = editDept,
                departments = departments,
                onDismiss = { showEditDialog = false },
                onSave = { name, dept ->
                    authViewModel.updateProfile(name, dept)
                    showEditDialog = false
                }
            )
        }

        if (showSettingsDialog) {
            SettingsDialog(
                isDarkMode = isDarkMode,
                onDarkModeChange = { settingsViewModel.toggleDarkMode() },
                onDismiss = { showSettingsDialog = false }
            )
        }

        if (showAboutDialog) {
            AboutDialog(onDismiss = { showAboutDialog = false })
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Hesap Ayarları", style = MaterialTheme.typography.titleLarge) },
                    actions = {
                        TextButton(onClick = { authViewModel.signOut() }, colors = ButtonDefaults.textButtonColors(contentColor = ArgepColors.Error)) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Çıkış Yap")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ArgepColors.White)
                )
            },
            containerColor = ArgepColors.Slate100
        ) { padding ->
            Column(
                modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // User Profile Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = ArgepColors.Navy100) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(fullName.take(1), color = ArgepColors.Navy900, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        Column {
                            Text(fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(user?.email ?: "email@uygulama.com", style = MaterialTheme.typography.bodyLarge, color = ArgepColors.Slate500)
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(color = ArgepColors.Phase3Light, shape = RoundedCornerShape(20.dp)) {
                                Text("AKTİF ÜYE", modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = ArgepColors.Phase3, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Settings Sections
                SettingsSection("Kişisel Bilgiler") {
                    SettingsRow(Icons.Default.Person, "Profil Bilgileri", "İsim, soyisim ve departman güncelleyin.") {
                        editName = fullName
                        editDept = "" 
                        showEditDialog = true
                    }
                    SettingsRow(Icons.Default.Settings, "Uygulama Tercihleri", "Tema ve bildirim ayarlarını yönetin.") {
                        showSettingsDialog = true
                    }
                    SettingsRow(Icons.Default.Info, "Hakkında", "Versiyon v1.0.4 - Argep Dashboard") {
                        showAboutDialog = true
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title.uppercase(), style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate500, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(modifier = Modifier.size(40.dp), color = ArgepColors.Navy50, shape = RoundedCornerShape(8.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = ArgepColors.Navy700, modifier = Modifier.size(20.dp)) }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = ArgepColors.Slate500)
        }
        Icon(Icons.Default.KeyboardArrowRight, null, tint = ArgepColors.Slate300, modifier = Modifier.size(20.dp))
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = ArgepColors.Slate100)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditDialog(
    currentName: String,
    currentDept: String,
    departments: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var dept by remember { mutableStateOf(currentDept) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Profili Düzenle", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tam İsim") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dept,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Departman") },
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
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        departments.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d) },
                                onClick = {
                                    dept = d
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
                onClick = { onSave(name, dept) },
                colors = ButtonDefaults.buttonColors(containerColor = ArgepColors.Navy700),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Kaydet")
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
fun SettingsDialog(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Uygulama Tercihleri", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Karanlık Mod", style = MaterialTheme.typography.titleMedium)
                        Text("Göz yorgunluğunu azaltın", style = MaterialTheme.typography.bodySmall, color = ArgepColors.Slate500)
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = onDarkModeChange,
                        colors = SwitchDefaults.colors(checkedThumbColor = ArgepColors.Navy700)
                    )
                }
                
                HorizontalDivider(color = ArgepColors.Slate100)
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Bildirimler", style = MaterialTheme.typography.titleMedium)
                        Text("Yeni görevlerden haberdar olun", style = MaterialTheme.typography.bodySmall, color = ArgepColors.Slate500)
                    }
                    Switch(checked = true, onCheckedChange = {}, enabled = false)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Kapat") }
        },
        containerColor = Color.White
    )
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hakkında", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(modifier = Modifier.size(64.dp), color = ArgepColors.Navy100, shape = RoundedCornerShape(12.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("AG", color = ArgepColors.Navy900, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                    }
                }
                Text("Argep Dashboard", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Sürüm 1.0.4 (Beta)", style = MaterialTheme.typography.bodyMedium, color = ArgepColors.Slate500)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Argep, Ar-Ge süreçlerini ve proje yönetimini kolaylaştırmak için tasarlanmış bir platformdur.",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("© 2024 Argesurec Teknoloji", style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate400)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Anladım") }
        },
        containerColor = Color.White
    )
}
