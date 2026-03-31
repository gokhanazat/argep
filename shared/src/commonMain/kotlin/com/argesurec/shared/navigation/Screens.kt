package com.argesurec.shared.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.argesurec.shared.ui.home.HomeScreen
import com.argesurec.shared.ui.home.ProfileScreen
import com.argesurec.shared.ui.home.ReportsScreen
import com.argesurec.shared.ui.project.ProjectsScreen
import com.argesurec.shared.ui.team.TeamScreen

object HomeTab : Tab {
    @Composable
    override fun Content() {
        HomeScreen().Content()
    }

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Home)
            return remember {
                TabOptions(index = 0u, title = "Ana Sayfa", icon = icon)
            }
        }
}

object ProjectsTab : Tab {
    @Composable
    override fun Content() {
        ProjectsScreen().Content()
    }

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.List)
            return remember {
                TabOptions(index = 1u, title = "Projeler", icon = icon)
            }
        }
}

object ReportsTab : Tab {
    @Composable
    override fun Content() {
        ReportsScreen().Content()
    }

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.DateRange)
            return remember {
                TabOptions(index = 2u, title = "Raporlar", icon = icon)
            }
        }
}

object TeamTab : Tab {
    @Composable
    override fun Content() {
        // Not: Global ekip ekranı her projeyi kapsayabilir 
        // veya varsayılan bir proje ID üzerinden başlatılabilir.
        TeamScreen("global").Content() 
    }

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.AccountBox)
            return remember {
                TabOptions(index = 3u, title = "Ekip", icon = icon)
            }
        }
}

object ProfileTab : Tab {
    @Composable
    override fun Content() {
        ProfileScreen().Content()
    }

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Person)
            return remember {
                TabOptions(index = 4u, title = "Profil", icon = icon)
            }
        }
}
