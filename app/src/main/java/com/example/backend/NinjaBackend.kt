package com.example.backend

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AudioSettings
import com.example.model.ChatMessage
import com.example.model.Group
import com.example.model.GroupMember
import com.example.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

sealed class ConnectionStatus {
    object Connected : ConnectionStatus()
    object Connecting : ConnectionStatus()
    data class Reconnecting(val attempt: Int) : ConnectionStatus()
    data class Disconnected(val reason: String) : ConnectionStatus()
}

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

sealed class GroupResult {
    data class Success(val group: Group) : GroupResult()
    data class Error(val message: String) : GroupResult()
}

class NinjaBackend(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ninja_techno_db", Context.MODE_PRIVATE)

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Connected)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups.asStateFlow()

    private val _currentGroup = MutableStateFlow<Group?>(null)
    val currentGroup: StateFlow<Group?> = _currentGroup.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _audioSettings = MutableStateFlow(AudioSettings())
    val audioSettings: StateFlow<AudioSettings> = _audioSettings.asStateFlow()

    // Voice channel state
    private val _isInVoiceChannel = MutableStateFlow(false)
    val isInVoiceChannel: StateFlow<Boolean> = _isInVoiceChannel.asStateFlow()

    private val _isMicMuted = MutableStateFlow(false)
    val isMicMuted: StateFlow<Boolean> = _isMicMuted.asStateFlow()

    private val _isDeafened = MutableStateFlow(false)
    val isDeafened: StateFlow<Boolean> = _isDeafened.asStateFlow()

    private val _speakingUsers = MutableStateFlow<Set<String>>(emptySet())
    val speakingUsers: StateFlow<Set<String>> = _speakingUsers.asStateFlow()

    private val backendScope = CoroutineScope(Dispatchers.IO)

    init {
        loadSettingsFromStorage()
        loadGroupsFromStorage()
        restoreSession()
    }

    // SHA-256 password hashing with salt
    private fun hashPassword(password: String, salt: String = "ninja_salt_techno_dev_shan"): String {
        val bytes = (password + salt).toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    suspend fun register(username: String, pass: String, passConfirm: String): AuthResult {
        delay(350) // network latency
        val trimmedUser = username.trim()
        val trimmedPass = pass.trim()
        val trimmedPassConfirm = passConfirm.trim()

        if (trimmedUser.length < 3) {
            return AuthResult.Error("Username must be at least 3 characters")
        }
        if (trimmedUser.length > 20) {
            return AuthResult.Error("Username must not exceed 20 characters")
        }
        if (!trimmedUser.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            return AuthResult.Error("Username can only contain letters, numbers, and underscores")
        }
        if (trimmedPass.length < 6) {
            return AuthResult.Error("Password must be at least 6 characters")
        }
        if (trimmedPass != trimmedPassConfirm) {
            return AuthResult.Error("Passwords do not match")
        }

        // Check if user already exists
        val userAccountsJson = prefs.getString("user_accounts", "{}") ?: "{}"
        val accountsObj = JSONObject(userAccountsJson)

        val lowerUser = trimmedUser.lowercase()
        if (accountsObj.has(lowerUser)) {
            return AuthResult.Error("Username already exists")
        }

        val userId = UUID.randomUUID().toString()
        val hashed = hashPassword(trimmedPass)

        val newAccountData = JSONObject().apply {
            put("id", userId)
            put("username", trimmedUser)
            put("passwordHash", hashed)
            put("displayName", trimmedUser)
            put("createdAt", System.currentTimeMillis())
        }

        accountsObj.put(lowerUser, newAccountData)
        prefs.edit().putString("user_accounts", accountsObj.toString()).apply()

        val user = User(
            id = userId,
            username = trimmedUser,
            displayName = trimmedUser,
            avatarTag = "ninja",
            status = "online",
            createdAt = System.currentTimeMillis()
        )

        saveSession(user)
        _currentUser.value = user
        return AuthResult.Success(user)
    }

    suspend fun login(username: String, pass: String): AuthResult {
        delay(300)
        val trimmedUser = username.trim()
        val trimmedPass = pass.trim()

        if (trimmedUser.isEmpty() || trimmedPass.isEmpty()) {
            return AuthResult.Error("Please enter both username and password")
        }

        val userAccountsJson = prefs.getString("user_accounts", "{}") ?: "{}"
        val accountsObj = JSONObject(userAccountsJson)
        val lowerUser = trimmedUser.lowercase()

        if (!accountsObj.has(lowerUser)) {
            return AuthResult.Error("Account not found")
        }

        val account = accountsObj.getJSONObject(lowerUser)
        val storedHash = account.getString("passwordHash")
        val inputHash = hashPassword(trimmedPass)

        if (storedHash != inputHash) {
            return AuthResult.Error("Incorrect username or password")
        }

        val user = User(
            id = account.getString("id"),
            username = account.getString("username"),
            displayName = account.optString("displayName", account.getString("username")),
            avatarTag = "ninja",
            status = "online",
            createdAt = account.optLong("createdAt", System.currentTimeMillis())
        )

        saveSession(user)
        _currentUser.value = user
        return AuthResult.Success(user)
    }

    fun logout() {
        prefs.edit().remove("active_session_user").apply()
        _currentUser.value = null
        _currentGroup.value = null
        _isInVoiceChannel.value = false
        _messages.value = emptyList()
    }

    private fun saveSession(user: User) {
        val userObj = JSONObject().apply {
            put("id", user.id)
            put("username", user.username)
            put("displayName", user.displayName)
            put("avatarTag", user.avatarTag)
            put("status", user.status)
            put("customStatus", user.customStatus)
            put("createdAt", user.createdAt)
        }
        prefs.edit().putString("active_session_user", userObj.toString()).apply()
    }

    private fun restoreSession() {
        val activeUserJson = prefs.getString("active_session_user", null) ?: return
        try {
            val obj = JSONObject(activeUserJson)
            val user = User(
                id = obj.getString("id"),
                username = obj.getString("username"),
                displayName = obj.optString("displayName", obj.getString("username")),
                avatarTag = obj.optString("avatarTag", "ninja"),
                status = obj.optString("status", "online"),
                customStatus = obj.optString("customStatus", "Ready for squad comms"),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
            _currentUser.value = user
        } catch (_: Exception) {
            prefs.edit().remove("active_session_user").apply()
        }
    }

    fun updateProfile(displayName: String, customStatus: String) {
        val user = _currentUser.value ?: return
        val updated = user.copy(
            displayName = displayName.ifBlank { user.username },
            customStatus = customStatus
        )
        _currentUser.value = updated
        saveSession(updated)

        // Update in user_accounts
        val userAccountsJson = prefs.getString("user_accounts", "{}") ?: "{}"
        try {
            val accountsObj = JSONObject(userAccountsJson)
            val lower = user.username.lowercase()
            if (accountsObj.has(lower)) {
                val acc = accountsObj.getJSONObject(lower)
                acc.put("displayName", updated.displayName)
                acc.put("customStatus", updated.customStatus)
                prefs.edit().putString("user_accounts", accountsObj.toString()).apply()
            }
        } catch (_: Exception) {}
    }

    fun deleteAccount(): Boolean {
        val user = _currentUser.value ?: return false
        val userAccountsJson = prefs.getString("user_accounts", "{}") ?: "{}"
        try {
            val accountsObj = JSONObject(userAccountsJson)
            val lower = user.username.lowercase()
            accountsObj.remove(lower)
            prefs.edit().putString("user_accounts", accountsObj.toString()).apply()
            logout()
            return true
        } catch (_: Exception) {
            return false
        }
    }

    suspend fun createGroup(name: String, description: String, isPrivate: Boolean): GroupResult {
        delay(250)
        val user = _currentUser.value ?: return GroupResult.Error("Authentication required")
        val trimmedName = name.trim()
        if (trimmedName.length < 3) {
            return GroupResult.Error("Group name must be at least 3 characters")
        }

        val code = generateGroupCode()
        val newGroup = Group(
            id = UUID.randomUUID().toString(),
            name = trimmedName,
            description = description.trim(),
            code = code,
            ownerId = user.id,
            isPrivate = isPrivate,
            createdAt = System.currentTimeMillis(),
            members = listOf(
                GroupMember(
                    userId = user.id,
                    username = user.username,
                    role = "owner",
                    isOnline = true
                )
            )
        )

        val updatedList = _groups.value + newGroup
        _groups.value = updatedList
        saveGroupsToStorage(updatedList)
        return GroupResult.Success(newGroup)
    }

    suspend fun joinGroupByCode(code: String): GroupResult {
        delay(250)
        val user = _currentUser.value ?: return GroupResult.Error("Authentication required")
        val cleanCode = code.trim().uppercase()

        val group = _groups.value.find { it.code.uppercase() == cleanCode }
            ?: return GroupResult.Error("Group not found with code $cleanCode")

        if (group.members.any { it.userId == user.id }) {
            return GroupResult.Success(group) // already in
        }

        val updatedMembers = group.members + GroupMember(
            userId = user.id,
            username = user.username,
            role = "member",
            isOnline = true
        )
        val updatedGroup = group.copy(members = updatedMembers)
        val updatedList = _groups.value.map { if (it.id == group.id) updatedGroup else it }
        _groups.value = updatedList
        saveGroupsToStorage(updatedList)

        if (_currentGroup.value?.id == group.id) {
            _currentGroup.value = updatedGroup
        }
        return GroupResult.Success(updatedGroup)
    }

    fun selectGroup(group: Group) {
        _currentGroup.value = group
        loadMessagesForGroup(group.id)
    }

    fun leaveCurrentGroup() {
        val user = _currentUser.value ?: return
        val group = _currentGroup.value ?: return
        val updatedMembers = group.members.filter { it.userId != user.id }
        val updatedList = if (updatedMembers.isEmpty()) {
            _groups.value.filter { it.id != group.id }
        } else {
            val updatedGroup = group.copy(members = updatedMembers)
            _groups.value.map { if (it.id == group.id) updatedGroup else it }
        }
        _groups.value = updatedList
        saveGroupsToStorage(updatedList)
        _currentGroup.value = null
        _isInVoiceChannel.value = false
        _messages.value = emptyList()
    }

    fun toggleMemberMute(userId: String) {
        val current = _currentGroup.value ?: return
        val user = _currentUser.value ?: return
        if (current.ownerId != user.id) return // owner control only

        val updatedMembers = current.members.map { member ->
            if (member.userId == userId) member.copy(isMutedByAdmin = !member.isMutedByAdmin)
            else member
        }
        val updatedGroup = current.copy(members = updatedMembers)
        _currentGroup.value = updatedGroup
        val updatedList = _groups.value.map { if (it.id == current.id) updatedGroup else it }
        _groups.value = updatedList
        saveGroupsToStorage(updatedList)
    }

    fun removeMember(userId: String) {
        val current = _currentGroup.value ?: return
        val user = _currentUser.value ?: return
        if (current.ownerId != user.id || userId == user.id) return

        val updatedMembers = current.members.filter { it.userId != userId }
        val updatedGroup = current.copy(members = updatedMembers)
        _currentGroup.value = updatedGroup
        val updatedList = _groups.value.map { if (it.id == current.id) updatedGroup else it }
        _groups.value = updatedList
        saveGroupsToStorage(updatedList)
    }

    fun sendMessage(content: String): Boolean {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return false
        val user = _currentUser.value ?: return false
        val group = _currentGroup.value ?: return false

        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            groupId = group.id,
            senderId = user.id,
            senderUsername = user.username,
            content = trimmed,
            timestamp = System.currentTimeMillis()
        )

        val updated = _messages.value + msg
        _messages.value = updated
        saveMessagesForGroup(group.id, updated)
        return true
    }

    // Voice controls
    fun joinVoiceChannel() {
        _isInVoiceChannel.value = true
        val user = _currentUser.value ?: return
        _speakingUsers.value = setOf(user.username)
    }

    fun leaveVoiceChannel() {
        _isInVoiceChannel.value = false
        _speakingUsers.value = emptySet()
    }

    fun toggleMicMute() {
        val newMuted = !_isMicMuted.value
        _isMicMuted.value = newMuted
        val user = _currentUser.value
        if (user != null) {
            if (newMuted) {
                _speakingUsers.value = _speakingUsers.value - user.username
            } else if (_isInVoiceChannel.value) {
                _speakingUsers.value = _speakingUsers.value + user.username
            }
        }
    }

    fun toggleDeafen() {
        _isDeafened.value = !_isDeafened.value
    }

    fun setSpeakingLevel(level: Float) {
        val user = _currentUser.value ?: return
        if (_isInVoiceChannel.value && !_isMicMuted.value) {
            if (level > 0.08f) {
                _speakingUsers.value = _speakingUsers.value + user.username
            } else {
                _speakingUsers.value = _speakingUsers.value - user.username
            }
        }
    }

    // Settings
    fun saveAudioSettings(settings: AudioSettings) {
        _audioSettings.value = settings
        val obj = JSONObject().apply {
            put("voiceVolume", settings.voiceVolume.toDouble())
            put("dashboardMusicVolume", settings.dashboardMusicVolume.toDouble())
            put("noiseSuppression", settings.noiseSuppression)
            put("echoCancellation", settings.echoCancellation)
            put("autoGainControl", settings.autoGainControl)
            put("micSensitivity", settings.micSensitivity.toDouble())
            put("interfaceSounds", settings.interfaceSounds)
        }
        prefs.edit().putString("audio_settings", obj.toString()).apply()
    }

    private fun loadSettingsFromStorage() {
        val str = prefs.getString("audio_settings", null) ?: return
        try {
            val obj = JSONObject(str)
            _audioSettings.value = AudioSettings(
                voiceVolume = obj.optDouble("voiceVolume", 1.0).toFloat(),
                dashboardMusicVolume = obj.optDouble("dashboardMusicVolume", 0.5).toFloat(),
                noiseSuppression = obj.optBoolean("noiseSuppression", true),
                echoCancellation = obj.optBoolean("echoCancellation", true),
                autoGainControl = obj.optBoolean("autoGainControl", true),
                micSensitivity = obj.optDouble("micSensitivity", 0.75).toFloat(),
                interfaceSounds = obj.optBoolean("interfaceSounds", true)
            )
        } catch (_: Exception) {}
    }

    private fun generateGroupCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val part1 = (1..3).map { chars.random() }.joinToString("")
        val part2 = (1..3).map { chars.random() }.joinToString("")
        return "NINJA-$part1-$part2"
    }

    private fun loadGroupsFromStorage() {
        val groupsJson = prefs.getString("ninja_groups", null)
        if (groupsJson == null) {
            // Seed default community squad channel so users immediately have an active multiplayer comms network
            val defaultGroup = Group(
                id = "squad_alpha_global",
                name = "Alpha Comm Squad",
                description = "Primary tactical communications and squad coordination hub.",
                code = "NINJA-DEV-77",
                ownerId = "dev_shan_system",
                isPrivate = false,
                members = listOf(
                    GroupMember("dev_shan_system", "DEV_SHAN", role = "owner", isOnline = true),
                    GroupMember("ghost_recon", "GhostNinja", role = "member", isOnline = true),
                    GroupMember("cipher_99", "CipherX", role = "member", isOnline = false)
                )
            )
            val initial = listOf(defaultGroup)
            _groups.value = initial
            saveGroupsToStorage(initial)
            return
        }

        try {
            val array = JSONArray(groupsJson)
            val list = mutableListOf<Group>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val membersArray = obj.optJSONArray("members") ?: JSONArray()
                val membersList = mutableListOf<GroupMember>()
                for (j in 0 until membersArray.length()) {
                    val mObj = membersArray.getJSONObject(j)
                    membersList.add(
                        GroupMember(
                            userId = mObj.getString("userId"),
                            username = mObj.getString("username"),
                            role = mObj.optString("role", "member"),
                            isOnline = mObj.optBoolean("isOnline", true),
                            isMutedByAdmin = mObj.optBoolean("isMutedByAdmin", false),
                            joinedAt = mObj.optLong("joinedAt", System.currentTimeMillis())
                        )
                    )
                }
                list.add(
                    Group(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        description = obj.optString("description", ""),
                        code = obj.getString("code"),
                        ownerId = obj.getString("ownerId"),
                        isPrivate = obj.optBoolean("isPrivate", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        members = membersList
                    )
                )
            }
            _groups.value = list
        } catch (_: Exception) {
            _groups.value = emptyList()
        }
    }

    private fun saveGroupsToStorage(groups: List<Group>) {
        val array = JSONArray()
        for (g in groups) {
            val obj = JSONObject().apply {
                put("id", g.id)
                put("name", g.name)
                put("description", g.description)
                put("code", g.code)
                put("ownerId", g.ownerId)
                put("isPrivate", g.isPrivate)
                put("createdAt", g.createdAt)
                val memArray = JSONArray()
                for (m in g.members) {
                    val mObj = JSONObject().apply {
                        put("userId", m.userId)
                        put("username", m.username)
                        put("role", m.role)
                        put("isOnline", m.isOnline)
                        put("isMutedByAdmin", m.isMutedByAdmin)
                        put("joinedAt", m.joinedAt)
                    }
                    memArray.put(mObj)
                }
                put("members", memArray)
            }
            array.put(obj)
        }
        prefs.edit().putString("ninja_groups", array.toString()).apply()
    }

    private fun loadMessagesForGroup(groupId: String) {
        val msgsJson = prefs.getString("ninja_msgs_$groupId", null)
        if (msgsJson == null) {
            if (groupId == "squad_alpha_global") {
                val seed = listOf(
                    ChatMessage(
                        id = "welcome_msg",
                        groupId = groupId,
                        senderId = "dev_shan_system",
                        senderUsername = "DEV_SHAN",
                        content = "Welcome to NINJA TECHNO private comms. Tactical voice channels and encrypted chat operational.",
                        timestamp = System.currentTimeMillis() - 3600000
                    )
                )
                _messages.value = seed
                saveMessagesForGroup(groupId, seed)
                return
            }
            _messages.value = emptyList()
            return
        }

        try {
            val array = JSONArray(msgsJson)
            val list = mutableListOf<ChatMessage>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ChatMessage(
                        id = obj.getString("id"),
                        groupId = obj.getString("groupId"),
                        senderId = obj.getString("senderId"),
                        senderUsername = obj.getString("senderUsername"),
                        content = obj.getString("content"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            _messages.value = list
        } catch (_: Exception) {
            _messages.value = emptyList()
        }
    }

    private fun saveMessagesForGroup(groupId: String, messages: List<ChatMessage>) {
        val array = JSONArray()
        for (m in messages) {
            val obj = JSONObject().apply {
                put("id", m.id)
                put("groupId", m.groupId)
                put("senderId", m.senderId)
                put("senderUsername", m.senderUsername)
                put("content", m.content)
                put("timestamp", m.timestamp)
            }
            array.put(obj)
        }
        prefs.edit().putString("ninja_msgs_$groupId", array.toString()).apply()
    }
}
