package com.argesurec.shared.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.argesurec.shared.ui.auth.LoginScreen
import com.argesurec.shared.ui.theme.ArgepTheme
import com.argesurec.shared.viewmodel.AuthViewModel

@Composable
fun AppNavigation() {
    val authViewModel = koinViewModel<AuthViewModel>()
    val settingsViewModel = koinViewModel<com.argesurec.shared.viewmodel.SettingsViewModel>()
    
    val authState by authViewModel.state.collectAsState()
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()

    ArgepTheme(darkTheme = isDarkMode) {
        if (authState.isLoggedIn) {
            TabNavigator(HomeTab) {
                Scaffold(
                    bottomBar = {
                        BottomNavBar()
                    }
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        CurrentTab()
                    }
                }
            }
        } else {
            Navigator(LoginScreen())
        }
    }
}
