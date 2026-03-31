import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
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
        supabaseUrl = "https://poelkfxcehixweytutrl.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InBvZWxrZnhjZWhpeHdleXR1dHJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzQ4NTQwMzAsImV4cCI6MjA5MDQzMDAzMH0.Wo_Axkp4-0meFv099XrNZSXgGq9xiHyaU9H1DoMtVaA"
    )
    val webApp = WebApp()
    
    CanvasBasedWindow(title = "ArGeSurec") {
        LaunchedEffect(Unit) {
            webApp.authViewModel.checkSession()
        }
        AppNavigation()
    }
}
