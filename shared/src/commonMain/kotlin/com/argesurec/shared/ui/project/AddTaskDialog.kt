package com.argesurec.shared.ui.project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.argesurec.shared.model.TeamMemberWithProfile
import com.argesurec.shared.model.TaskPriority
import com.argesurec.shared.ui.theme.ArgepColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    teamMembers: List<TeamMemberWithProfile>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, priority: TaskPriority, assignedTo: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var selectedAssignee by remember { mutableStateOf<TeamMemberWithProfile?>(null) }
    
    var priorityExpanded by remember { mutableStateOf(false) }
    var assigneeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Görev Oluştur", fontWeight = FontWeight.Bold, color = ArgepColors.Navy900) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Görev Başlığı") },
                    placeholder = { Text("Örn: Veritabanı optimizasyonu") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArgepColors.Navy700,
                        unfocusedBorderColor = ArgepColors.Slate200
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Açıklama (Opsiyonel)") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArgepColors.Navy700,
                        unfocusedBorderColor = ArgepColors.Slate200
                    )
                )

                // Priority Selection
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = when(selectedPriority) {
                            TaskPriority.LOW -> "Düşük"
                            TaskPriority.MEDIUM -> "Orta"
                            TaskPriority.HIGH -> "Yüksek"
                        },
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Öncelik") },
                        modifier = Modifier.fillMaxWidth().clickable { priorityExpanded = true },
                        shape = RoundedCornerShape(10.dp),
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArgepColors.Navy700,
                            unfocusedBorderColor = ArgepColors.Slate200
                        )
                    )
                    DropdownMenu(
                        expanded = priorityExpanded,
                        onDismissRequest = { priorityExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        TaskPriority.entries.forEach { priority ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        when(priority) {
                                            TaskPriority.LOW -> "Düşük"
                                            TaskPriority.MEDIUM -> "Orta"
                                            TaskPriority.HIGH -> "Yüksek"
                                        }
                                    ) 
                                },
                                onClick = {
                                    selectedPriority = priority
                                    priorityExpanded = false
                                }
                            )
                        }
                    }
                }

                // Assignee Selection
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedAssignee?.profile?.fullName ?: "Atanmamış",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Sorumlu Kişi") },
                        modifier = Modifier.fillMaxWidth().clickable { assigneeExpanded = true },
                        shape = RoundedCornerShape(10.dp),
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArgepColors.Navy700,
                            unfocusedBorderColor = ArgepColors.Slate200
                        )
                    )
                    DropdownMenu(
                        expanded = assigneeExpanded,
                        onDismissRequest = { assigneeExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Atanmamış") },
                            onClick = {
                                selectedAssignee = null
                                assigneeExpanded = false
                            }
                        )
                        teamMembers.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.profile?.fullName ?: "İsimsiz") },
                                onClick = {
                                    selectedAssignee = member
                                    assigneeExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (title.isNotBlank()) {
                        onConfirm(title, description, selectedPriority, selectedAssignee?.userId)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ArgepColors.Navy700),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Görevi Oluştur")
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
