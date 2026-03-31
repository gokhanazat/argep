package com.argesurec.shared.repository.impl

import com.argesurec.shared.model.Project
import com.argesurec.shared.model.ProjectPhase
import com.argesurec.shared.repository.ProjectRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SupabaseProjectRepository(
    private val supabase: SupabaseClient
) : ProjectRepository {

    override fun getAll(): Flow<List<Project>> = flow {
        val projects = supabase.from("projects").select().decodeList<Project>()
        emit(projects)
    }

    override suspend fun getById(id: String): Project? {
        return try {
            supabase.from("projects").select {
                filter { eq("id", id) }
            }.decodeSingleOrNull<Project>()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun insert(item: Project): Result<Project> {
        return try {
            val result = supabase.from("projects").insert(item) {
                select()
            }.decodeSingle<Project>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun update(item: Project): Result<Project> {
        return try {
            val result = supabase.from("projects").update(item) {
                filter { eq("id", item.id!!) }
                select()
            }.decodeSingle<Project>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun delete(id: String): Result<Unit> {
        return try {
            supabase.from("projects").delete {
                filter { eq("id", id) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getByPhase(phase: ProjectPhase): Flow<List<Project>> = flow {
        val projects = supabase.from("projects").select {
            filter { eq("phase", phase.name) }
        }.decodeList<Project>()
        emit(projects)
    }
}
