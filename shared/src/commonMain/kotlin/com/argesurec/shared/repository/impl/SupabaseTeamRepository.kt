package com.argesurec.shared.repository.impl

import com.argesurec.shared.model.TeamMember
import com.argesurec.shared.model.TeamMemberWithProfile
import com.argesurec.shared.repository.TeamRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SupabaseTeamRepository(
    private val supabase: SupabaseClient
) : TeamRepository {

    override fun getAll(): Flow<List<TeamMember>> = flow {
        val members = supabase.from("team_members").select().decodeList<TeamMember>()
        emit(members)
    }

    override fun getAllWithProfiles(): Flow<List<TeamMemberWithProfile>> = flow {
        try {
            val members = supabase.from("team_members")
                .select(columns = Columns.raw("*, profiles(*)"))
                .decodeList<TeamMemberWithProfile>()
            emit(members)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override fun getByProjectWithProfiles(projectId: String): Flow<List<TeamMemberWithProfile>> = flow {
        try {
            val members = supabase.from("team_members")
                .select(columns = Columns.raw("*, profiles(*)")) {
                    filter { eq("project_id", projectId) }
                }
                .decodeList<TeamMemberWithProfile>()
            emit(members)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getById(id: String): TeamMember? {
        return try {
            supabase.from("team_members").select {
                filter { eq("id", id) }
            }.decodeSingleOrNull<TeamMember>()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun insert(item: TeamMember): Result<TeamMember> = try {
        val inserted = supabase.from("team_members").insert(item) {
            select()
        }.decodeSingle<TeamMember>()
        Result.success(inserted)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun update(item: TeamMember): Result<TeamMember> = try {
        val updated = supabase.from("team_members").update(item) {
            select()
            filter { eq("id", item.id) }
        }.decodeSingle<TeamMember>()
        Result.success(updated)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun delete(id: String): Result<Unit> = try {
        supabase.from("team_members").delete {
            filter { eq("id", id) }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun inviteMember(email: String, role: String): Result<Unit> = try {
        // Invite logic handled by Edge Function or Admin API
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getRoleInProject(userId: String, projectId: String): Flow<String?> = flow {
        try {
            val response = supabase.from("team_members")
                .select(columns = Columns.raw("role")) {
                    filter {
                        eq("user_id", userId)
                        eq("project_id", projectId)
                    }
                }
            val role = response.decodeAs<List<Map<String, String>>>()
                .firstOrNull()?.get("role")
            emit(role)
        } catch (e: Exception) {
            emit(null)
        }
    }
}
