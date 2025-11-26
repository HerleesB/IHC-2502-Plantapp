package com.jardin.inteligente.model

data class Plant(
    val id: Int,
    val name: String,
    val species: String,
    val health: Int,
    val status: String,
    val nextAction: String,
    val streak: Int,
    val imageUrl: String
)

data class Badge(
    val id: Int,
    val name: String,
    val description: String,
    val earned: Boolean,
    val date: String?,
    val color: String,
    val progress: Int? = null,
    val total: Int? = null
)

data class Mission(
    val id: Int,
    val title: String,
    val description: String,
    val progress: Int,
    val total: Int,
    val points: Int,
    val completed: Boolean
)

data class Achievement(
    val date: String,
    val action: String,
    val points: Int
)

data class CommunityPost(
    val id: Int,
    val author: String,
    val authorAvatar: String,
    val isExpert: Boolean,
    val date: String,
    val plant: String,
    val issue: String,
    val description: String,
    val imageUrl: String,
    val diagnosis: String,
    val likes: Int,
    val comments: Int,
    val views: Int,
    val helpful: Int,
    val status: PostStatus
)

enum class PostStatus {
    OPEN, RESOLVED, SUCCESS
}

data class Contributor(
    val name: String,
    val avatar: String,
    val points: Int,
    val badge: String
)

enum class CaptureStep {
    IDLE, LIGHTING, FOCUS, DISTANCE, READY, CAPTURING, SUCCESS
}
