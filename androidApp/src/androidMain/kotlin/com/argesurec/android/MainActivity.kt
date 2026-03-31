package com.argesurec.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import com.argesurec.shared.navigation.AppNavigation
import com.argesurec.shared.initKoin
import com.argesurec.shared.viewmodel.AuthViewModel
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext

class MainActivity : ComponentActivity() {
    
    private val authViewModel: AuthViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        initKoin(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            androidContext(this@MainActivity)
        }

        setContent {
            LaunchedEffect(Unit) {
                authViewModel.checkSession()
            }
            AppNavigation()
        }
    }
}
