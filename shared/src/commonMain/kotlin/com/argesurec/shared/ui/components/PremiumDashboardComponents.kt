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
import androidx.compose.ui.draw.shadow

@Composable
fun ExecutiveStatCard(
    label: String,
    value: String,
    trend: String,
    icon: String,
    iconBg: Color = ArgepColors.ExecutiveSurfaceLow,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.shadow(
            elevation = 0.dp, // No standard shadow
            spotColor = ArgepColors.ExecutivePrimary.copy(alpha = 0.08f)
        ),
        colors = CardDefaults.cardColors(containerColor = ArgepColors.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = iconBg
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(icon, fontSize = 20.sp)
                    }
                }
                
                Surface(
                    color = if (trend.contains("+") || trend.contains("↑")) ArgepColors.Phase3Light else ArgepColors.Phase2Light,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        trend,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (trend.contains("+") || trend.contains("↑")) ArgepColors.Phase3 else ArgepColors.Phase2
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.05.sp, fontWeight = FontWeight.Bold),
                color = ArgepColors.Slate500
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp), 
                color = ArgepColors.ExecutivePrimary
            )
        }
    }
}

@Composable
fun ExecutiveProjectRow(
    name: String,
    phase: String,
    progress: Float,
    status: String = "Normal"
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ArgepColors.ExecutivePrimary)
            Text(
                "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = ArgepColors.ExecutivePrimary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth().height(4.dp), // Architectural: Slim, no round ends logic below
            color = ArgepColors.ExecutiveSecondary,
            trackColor = ArgepColors.ExecutiveSurfaceLow,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt 
        )
    }
}

@Composable
fun ExecutiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ArgepColors.ExecutivePrimary),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.02.sp),
            color = ArgepColors.White
        )
    }
}

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
