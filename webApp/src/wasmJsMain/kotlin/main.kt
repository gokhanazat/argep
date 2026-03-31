import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.argesurec.shared.SupabaseConfig
import com.argesurec.shared.initKoin
import com.argesurec.shared.navigation.AppNavigation
import com.argesurec.shared.viewmodel.AuthViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WebApp : KoinComponent {
    val authViewModel: AuthViewModel by inject()
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin(
        supabaseUrl = SupabaseConfig.URL,
        supabaseKey = SupabaseConfig.ANON_KEY
    )
    val webApp = WebApp()
    
    CanvasBasedWindow(title = "ArGeSürec") {
        LaunchedEffect(Unit) {
            webApp.authViewModel.checkSession()
        }
        AppNavigation()
    }
}
