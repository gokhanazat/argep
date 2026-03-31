package com.argesurec.shared.repository.impl

import com.argesurec.shared.model.Task
import com.argesurec.shared.repository.TaskRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SupabaseTaskRepository(
    private val supabase: SupabaseClient
) : TaskRepository {

    override fun getAll(): Flow<List<Task>> = flow {
        val tasks = supabase.from("tasks").select().decodeList<Task>()
        emit(tasks)
    }

    override suspend fun getById(id: String): Task? {
        return try {
            supabase.from("tasks").select {
                filter { eq("id", id) }
            }.decodeSingleOrNull<Task>()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun insert(item: Task): Result<Task> {
        return try {
            val result = supabase.from("tasks").insert(item) {
                select()
            }.decodeSingle<Task>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun update(item: Task): Result<Task> {
        return try {
            val result = supabase.from("tasks").update(item) {
                filter { eq("id", item.id) }
                select()
            }.decodeSingle<Task>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun delete(id: String): Result<Unit> {
        return try {
            supabase.from("tasks").delete {
                filter { eq("id", id) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getByMilestone(milestoneId: String): Flow<List<Task>> = flow {
        val tasks = supabase.from("tasks").select {
            filter { eq("milestone_id", milestoneId) }
        }.decodeList<Task>()
        emit(tasks)
    }

    override fun getAssignedToMe(): Flow<List<Task>> = flow {
        val userId = supabase.auth.currentUserOrNull()?.id
        if (userId != null) {
            val tasks = supabase.from("tasks").select {
                filter { eq("assigned_to", userId) }
            }.decodeList<Task>()
            emit(tasks)
        } else {
            emit(emptyList())
        }
    }
}
