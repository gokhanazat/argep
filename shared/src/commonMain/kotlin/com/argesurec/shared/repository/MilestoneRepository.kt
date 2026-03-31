package com.argesurec.shared.repository

import com.argesurec.shared.model.Milestone
import kotlinx.coroutines.flow.Flow

interface MilestoneRepository {
    fun getByProject(projectId: String): Flow<List<Milestone>>
    fun getAll(): Flow<List<Milestone>>
    suspend fun getById(id: String): Milestone?
    suspend fun insert(item: Milestone): Result<Milestone>
    suspend fun update(item: Milestone): Result<Milestone>
    suspend fun delete(id: String): Result<Unit>
}
