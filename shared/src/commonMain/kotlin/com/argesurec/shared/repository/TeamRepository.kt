package com.argesurec.shared.repository

import com.argesurec.shared.model.TeamMember
import com.argesurec.shared.model.TeamMemberWithProfile
import kotlinx.coroutines.flow.Flow

interface TeamRepository {
    fun getAll(): Flow<List<TeamMember>>
    fun getAllWithProfiles(): Flow<List<TeamMemberWithProfile>>
    fun getByProjectWithProfiles(projectId: String): Flow<List<TeamMemberWithProfile>>
    suspend fun getById(id: String): TeamMember?
    suspend fun insert(item: TeamMember): Result<TeamMember>
    suspend fun update(item: TeamMember): Result<TeamMember>
    suspend fun delete(id: String): Result<Unit>
    suspend fun inviteMember(email: String, role: String): Result<Unit>
    fun getRoleInProject(userId: String, projectId: String): Flow<String?>
}
