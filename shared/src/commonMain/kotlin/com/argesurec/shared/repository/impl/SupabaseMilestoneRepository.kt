package com.argesurec.shared.repository.impl

import com.argesurec.shared.model.Milestone
import com.argesurec.shared.repository.MilestoneRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SupabaseMilestoneRepository(
    private val supabase: SupabaseClient
) : MilestoneRepository {

    override fun getByProject(projectId: String): Flow<List<Milestone>> = flow {
        val milestones = supabase.from("milestones").select {
            filter { eq("project_id", projectId) }
        }.decodeList<Milestone>()
        emit(milestones)
    }

    override fun getAll(): Flow<List<Milestone>> = flow {
        val milestones = supabase.from("milestones")
            .select()
            .decodeList<Milestone>()
        emit(milestones)
    }

    override suspend fun getById(id: String): Milestone? {
        return try {
            supabase.from("milestones").select {
                filter { eq("id", id) }
            }.decodeSingleOrNull<Milestone>()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun insert(item: Milestone): Result<Milestone> {
        return try {
            val result = supabase.from("milestones").insert(item) {
                select()
            }.decodeSingle<Milestone>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun update(item: Milestone): Result<Milestone> {
        return try {
            val result = supabase.from("milestones").update(item) {
                filter { eq("id", item.id!!) }
                select()
            }.decodeSingle<Milestone>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun delete(id: String): Result<Unit> {
        return try {
            supabase.from("milestones").delete {
                filter { eq("id", id) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
