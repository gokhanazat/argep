package com.argesurec.shared.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ProjectPhase {
    @SerialName("Kuluçka") INCUBATION,
    @SerialName("Geliştirme") DEVELOPMENT,
    @SerialName("Ticarileşme") COMMERCIALIZATION
}

@Serializable
enum class TaskPriority {
    @SerialName("LOW") LOW,
    @SerialName("MEDIUM") MEDIUM,
    @SerialName("HIGH") HIGH
}

@Serializable
enum class TaskStatus {
    @SerialName("TODO") TODO,
    @SerialName("IN_PROGRESS") IN_PROGRESS,
    @SerialName("DONE") DONE
}

@Serializable
data class Project(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String? = null,
    @SerialName("phase") val phase: ProjectPhase,
    @SerialName("status") val status: String? = null,
    @SerialName("owner_id") val ownerId: String,
    @SerialName("budget_total") val budgetTotal: Double? = 0.0,
    @SerialName("budget_spent") val budgetSpent: Double? = 0.0,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Milestone(
    @SerialName("id") val id: String? = null,
    @SerialName("project_id") val projectId: String,
    @SerialName("title") val title: String,
    @SerialName("due_date") val dueDate: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Task(
    @SerialName("id") val id: String,
    @SerialName("milestone_id") val milestoneId: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String? = null,
    @SerialName("assigned_to") val assignedTo: String? = null,
    @SerialName("priority") val priority: TaskPriority,
    @SerialName("status") val status: TaskStatus,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class TeamMember(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("project_id") val projectId: String,
    @SerialName("role") val role: String? = null,
    @SerialName("joined_at") val joinedAt: String
)

@Serializable
data class UserProfile(
    @SerialName("id") val id: String,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("department") val department: String? = null
)

@Serializable
data class TeamMemberWithProfile(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("project_id") val projectId: String,
    @SerialName("role") val role: String? = null,
    @SerialName("joined_at") val joinedAt: String,
    @SerialName("profiles") val profile: UserProfile? = null
)
