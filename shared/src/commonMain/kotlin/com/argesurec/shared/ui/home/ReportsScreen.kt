package com.argesurec.shared.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.argesurec.shared.ui.components.*
import com.argesurec.shared.ui.theme.ArgepColors
import com.argesurec.shared.util.UiState
import com.argesurec.shared.viewmodel.ReportsViewModel
import com.argesurec.shared.viewmodel.PortfolioReport

class ReportsScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = koinViewModel<ReportsViewModel>()
        val state by viewModel.state.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Verim & Raporlar", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            containerColor = ArgepColors.Slate50
        ) { padding ->
            when (val uiState = state) {
                is UiState.Loading -> LoadingScreen("Raporlar hazırlanıyor...")
                is UiState.Error -> ErrorScreen(uiState.message, onRetry = { viewModel.loadReport() })
                is UiState.Success -> {
                    val report = uiState.data
                    ReportContent(padding, report)
                }
            }
        }
    }

    @Composable
    private fun ReportContent(padding: PaddingValues, report: PortfolioReport) {
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Key Performance Indicators Grid
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CorporateStatCard(
                        title = "Portföy Verimi",
                        value = "%${report.efficiency}",
                        trend = report.efficiencyTrend,
                        isPositive = report.efficiencyTrend.contains("↑"),
                        modifier = Modifier.weight(1f)
                    )
                    CorporateStatCard(
                        title = "Ekip Skoru",
                        value = ((report.teamScore * 10).toInt() / 10.0).toString(),
                        trend = report.scoreTrend,
                        isPositive = report.scoreTrend.contains("↑"),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CorporateStatCard(
                        title = "Bütçe Kullanımı",
                        value = "%${report.budgetUsage}",
                        trend = report.budgetTrend,
                        isPositive = report.budgetTrend.contains("↓"), // Bütçe harcamasının az olması pozitiftir? Veya tersi. Genelde verim bazında ↓ iyidir.
                        modifier = Modifier.weight(1f)
                    )
                    CorporateStatCard(
                        title = "Risk Seviyesi",
                        value = report.riskLevel,
                        trend = report.riskTrend,
                        isPositive = report.riskTrend == "Stabil",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Project Portfolio Health
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ArgepColors.Slate200)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Portföy Sağlık Durumu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ArgepColors.Slate900)
                    Text("Projelerin genel ilerleme ve faz bilgileri", style = MaterialTheme.typography.bodySmall, color = ArgepColors.Slate500)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (report.projectHealths.isEmpty()) {
                        Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            Text("Henüz proje verisi bulunamadı.", color = ArgepColors.Slate400)
                        }
                    } else {
                        report.projectHealths.forEachIndexed { index, project ->
                            CorporateProgressRow(
                                title = project.name,
                                subtitle = project.statusText,
                                progress = project.progress,
                                color = getPhaseColor(project.phase)
                            )
                            if (index < report.projectHealths.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = ArgepColors.Slate100)
                            }
                        }
                    }
                }
            }

            // Advanced Insight Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ArgepColors.Navy900),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Yapay Zeka Analizi", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                        Text("Portföyünüz %12 daha verimli yönetilebilir.", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Detay", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }

    @Composable
    private fun CorporateStatCard(
        title: String,
        value: String,
        trend: String,
        isPositive: Boolean,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ArgepColors.Slate200)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(title, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), color = ArgepColors.Slate500)
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = ArgepColors.Slate900)
                    Surface(
                        color = if (isPositive) ArgepColors.Phase3Light else ArgepColors.Slate100,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            trend,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = if (isPositive) ArgepColors.Phase3 else ArgepColors.Slate600
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun CorporateProgressRow(
        title: String,
        subtitle: String,
        progress: Float,
        color: Color
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ArgepColors.Slate900)
                    Text(subtitle, style = MaterialTheme.typography.labelSmall, color = ArgepColors.Slate500)
                }
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
            }
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.1f)
            )
        }
    }

    private fun getPhaseColor(phase: com.argesurec.shared.model.ProjectPhase): Color {
        return when(phase) {
            com.argesurec.shared.model.ProjectPhase.INCUBATION -> ArgepColors.Phase1
            com.argesurec.shared.model.ProjectPhase.DEVELOPMENT -> ArgepColors.Phase2
            com.argesurec.shared.model.ProjectPhase.COMMERCIALIZATION -> ArgepColors.Phase3
        }
    }
}
