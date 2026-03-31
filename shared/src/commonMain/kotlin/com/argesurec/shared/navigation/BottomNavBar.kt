package com.argesurec.shared.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import com.argesurec.shared.ui.theme.ArgepColors
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavBar() {
    NavigationBar(
        containerColor = ArgepColors.White,
        tonalElevation = 8.dp
    ) {
        TabNavigationItem(HomeTab)
        TabNavigationItem(ProjectsTab)
        TabNavigationItem(ReportsTab)
        TabNavigationItem(TeamTab)
        TabNavigationItem(ProfileTab)
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    val selected = tabNavigator.current == tab

    NavigationBarItem(
        selected = selected,
        onClick = { tabNavigator.current = tab },
        label = { 
            Text(
                text = tab.options.title, 
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                color = if (selected) ArgepColors.Navy900 else ArgepColors.Slate500
            ) 
        },
        icon = {
            tab.options.icon?.let { icon ->
                Icon(
                    painter = icon, 
                    contentDescription = tab.options.title,
                    tint = if (selected) ArgepColors.Navy900 else ArgepColors.Slate500
                )
            }
        },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = ArgepColors.Navy50
        )
    )
}
