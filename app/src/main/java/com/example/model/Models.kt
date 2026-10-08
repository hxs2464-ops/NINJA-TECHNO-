package com.example.model

data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarTag: String = "ninja",
    val status: String = "online", // online, idle, dnd, offline
    val customStatus: String = "Ready for squad comms",
    val createdAt: Long = System.currentTimeMillis()
)

data class Group(
    val id: String,
    val name: String,
    val description: String,
    val code: String, // e.g. "SHAN-77"
    val ownerId: String,
    val isPrivate: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val members: List<GroupMember> = emptyList()
)

data class GroupMember(
    val userId: String,
    val username: String,
    val role: String = "member", // owner, admin, member
    val isOnline: Boolean = true,
    val isMutedByAdmin: Boolean = false,
    val joinedAt: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String,
    val groupId: String,
    val senderId: String,
    val senderUsername: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class Friend(
    val id: String,
    val username: String,
    val status: String = "online",
    val mutualGroups: Int = 1
)

data class AudioSettings(
    val voiceVolume: Float = 1.0f,
    val dashboardMusicVolume: Float = 0.5f,
    val noiseSuppression: Boolean = true,
    val echoCancellation: Boolean = true,
    val autoGainControl: Boolean = true,
    val micSensitivity: Float = 0.75f,
    val interfaceSounds: Boolean = true
)

enum class Screen {
    LOGIN,
    REGISTER,
    DASHBOARD,
    GROUP_ROOM,
    SETTINGS
}
