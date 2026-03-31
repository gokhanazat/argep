package com.argesurec.shared.util

import androidx.compose.runtime.*
import com.argesurec.shared.repository.TeamRepository
import io.github.jan.supabase.auth.Auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import org.koin.compose.koinInject

enum class ProjectRole {
    PROJE_MUDURU,
    TEKNIK_LIDER,
    ARGE_UZMANI,
    TEST_MUHENDISI,
    MALI_UZMAN,
    HUKUK_PATENT,
    GOZLEMCI
}

enum class RoleAction {
    CREATE_TASK,
    ASSIGN_TASK,
    UPDATE_OWN_TASK,
    DELETE_TASK,
    INVITE_MEMBER,
    CREATE_PROJECT,
    VIEW_ONLY
}

object RoleManager {
    fun hasPermission(role: ProjectRole?, action: RoleAction): Boolean {
        if (role == null) return false
        
        return when (role) {
            ProjectRole.PROJE_MUDURU -> true
            ProjectRole.TEKNIK_LIDER -> when (action) {
                RoleAction.CREATE_TASK -> false
                RoleAction.UPDATE_OWN_TASK -> true
                RoleAction.VIEW_ONLY -> false
                else -> true // Diğer yetkiler PM'e yakın veya kısıtlı olabilir, isteğe göre daraltılır
            }
            ProjectRole.ARGE_UZMANI, 
            ProjectRole.TEST_MUHENDISI, 
            ProjectRole.MALI_UZMAN, 
            ProjectRole.HUKUK_PATENT -> action == RoleAction.UPDATE_OWN_TASK
            
            ProjectRole.GOZLEMCI -> action == RoleAction.VIEW_ONLY
        }
    }

    private fun String?.toProjectRole(): ProjectRole? {
        return try {
            if (this == null) null else ProjectRole.valueOf(this)
        } catch (e: Exception) {
            null
        }
    }

    fun currentUserRole(projectId: String, teamRepository: TeamRepository, auth: Auth): Flow<ProjectRole?> {
        return auth.sessionStatus.flatMapLatest { status ->
            val user: UserInfo? = when (status) {
                is SessionStatus.Authenticated -> status.session.user
                else -> null
            }
            if (user == null) flowOf(null)
            else teamRepository.getRoleInProject(user.id, projectId).map { it.toProjectRole() }
        }
    }
}

@Composable
fun RoleGuard(
    requiredAction: RoleAction,
    projectId: String,
    content: @Composable () -> Unit
) {
    val teamRepository = koinInject<TeamRepository>()
    val auth = koinInject<Auth>()
    
    val currentRole by remember(projectId) {
        RoleManager.currentUserRole(projectId, teamRepository, auth)
    }.collectAsState(initial = null)

    if (RoleManager.hasPermission(currentRole, requiredAction)) {
        content()
    }
}
