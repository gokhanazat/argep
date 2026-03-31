package com.argesurec.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.argesurec.shared.model.Task
import com.argesurec.shared.model.TaskPriority
import com.argesurec.shared.ui.theme.ArgepColors

@Composable
fun PremiumStatCard(
    label: String,
    value: String,
    delta: String,
    icon: String,
    iconBg: Color = ArgepColors.Navy50,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = ArgepColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(9.dp),
                color = iconBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(icon, fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = ArgepColors.Slate500
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 28.sp
                ),
                color = ArgepColors.Navy900
            )
            Text(
                text = delta,
                style = MaterialTheme.typography.labelSmall,
                color = if (delta.contains("↑")) ArgepColors.Phase3 else ArgepColors.Slate500
            )
        }
    }
}

@Composable
fun PremiumTaskRow(
    task: Task,
    onClick: () -> Unit
) {
    val priorityColor = when (task.priority) {
        TaskPriority.HIGH -> ArgepColors.Error
        TaskPriority.MEDIUM -> ArgepColors.Phase2
        TaskPriority.LOW -> ArgepColors.Phase3
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(32.dp)
                .background(priorityColor, RoundedCornerShape(2.dp))
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                color = ArgepColors.Navy800
            )
            Text(
                text = "Proje Detayı · Bitiş: 2 Mayıs", // Placeholder metadata
                style = MaterialTheme.typography.bodySmall,
                color = ArgepColors.Slate500
            )
        }

        Surface(
            color = when(task.priority) {
                TaskPriority.HIGH -> ArgepColors.Error.copy(alpha = 0.1f)
                TaskPriority.MEDIUM -> ArgepColors.Phase2Light
                else -> ArgepColors.Phase3Light
            },
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = task.priority.name,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = when(task.priority) {
                    TaskPriority.HIGH -> ArgepColors.Error
                    TaskPriority.MEDIUM -> ArgepColors.Phase2
                    else -> ArgepColors.Phase3
                }
            )
        }
    }
}

@Composable
fun ProjectProgressRow(
    name: String,
    phase: String,
    progress: Float,
    phaseColor: Color,
    phaseBg: Color
) {
    Column(modifier = Modifier.padding(vertical = 9.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Surface(color = phaseBg, shape = RoundedCornerShape(20.dp)) {
                Text(
                    phase,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = phaseColor
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = phaseColor,
            trackColor = ArgepColors.Slate200,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        Text(
            text = "${(progress * 100).toInt()}% · Milestone Bilgisi",
            style = MaterialTheme.typography.labelSmall,
            color = ArgepColors.Slate500,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
