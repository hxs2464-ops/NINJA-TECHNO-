package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.RealAudioEngine
import com.example.backend.AuthResult
import com.example.backend.ConnectionStatus
import com.example.backend.GroupResult
import com.example.backend.NinjaBackend
import com.example.model.AudioSettings
import com.example.model.ChatMessage
import com.example.model.Group
import com.example.model.Screen
import com.example.model.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NinjaViewModel(application: Application) : AndroidViewModel(application) {

    val backend = NinjaBackend(application)
    val audioEngine = RealAudioEngine(application)

    private val _currentScreen = MutableStateFlow(Screen.LOGIN)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _previousScreen = MutableStateFlow(Screen.LOGIN)

    // UI Loading & feedback states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    // Expose backend flows
    val currentUser: StateFlow<User?> = backend.currentUser
    val connectionStatus: StateFlow<ConnectionStatus> = backend.connectionStatus
    val groups: StateFlow<List<Group>> = backend.groups
    val currentGroup: StateFlow<Group?> = backend.currentGroup
    val messages: StateFlow<List<ChatMessage>> = backend.messages
    val audioSettings: StateFlow<AudioSettings> = backend.audioSettings
    val isInVoiceChannel: StateFlow<Boolean> = backend.isInVoiceChannel
    val isMicMuted: StateFlow<Boolean> = backend.isMicMuted
    val isDeafened: StateFlow<Boolean> = backend.isDeafened
    val speakingUsers: StateFlow<Set<String>> = backend.speakingUsers

    // Real audio mic level
    val micLevel: StateFlow<Float> = audioEngine.micLevel
    val isRecordingActive: StateFlow<Boolean> = audioEngine.isRecordingActive
    val isMicrophoneDetected: StateFlow<Boolean?> = audioEngine.isMicrophoneDetected

    init {
        // If user already had a saved session, route to Dashboard directly
        if (backend.currentUser.value != null) {
            _currentScreen.value = Screen.DASHBOARD
        }

        // Pipe real mic level to backend voice speaking calculation
        viewModelScope.launch {
            micLevel.collect { level ->
                backend.setSpeakingLevel(level)
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _previousScreen.value = _currentScreen.value
        _currentScreen.value = screen
        _errorMessage.value = null
    }

    fun navigateBack() {
        val curr = _currentScreen.value
        when (curr) {
            Screen.REGISTER -> _currentScreen.value = Screen.LOGIN
            Screen.SETTINGS -> _currentScreen.value = _previousScreen.value
            Screen.GROUP_ROOM -> {
                backend.leaveVoiceChannel()
                audioEngine.stopMicrophoneTest()
                _currentScreen.value = Screen.DASHBOARD
            }
            Screen.DASHBOARD -> {
                // Keep on dashboard or exit
            }
            Screen.LOGIN -> {}
        }
        _errorMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun login(username: String, pass: String) {
        if (_isLoading.value) return
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            when (val res = backend.login(username, pass)) {
                is AuthResult.Success -> {
                    _isLoading.value = false
                    _currentScreen.value = Screen.DASHBOARD
                    _toastMessage.emit("Welcome back, ${res.user.displayName}")
                }
                is AuthResult.Error -> {
                    _isLoading.value = false
                    _errorMessage.value = res.message
                }
            }
        }
    }

    fun register(username: String, pass: String, passConfirm: String) {
        if (_isLoading.value) return
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            when (val res = backend.register(username, pass, passConfirm)) {
                is AuthResult.Success -> {
                    _isLoading.value = false
                    _currentScreen.value = Screen.DASHBOARD
                    _toastMessage.emit("Account created successfully!")
                }
                is AuthResult.Error -> {
                    _isLoading.value = false
                    _errorMessage.value = res.message
                }
            }
        }
    }

    fun logout() {
        audioEngine.stopMicrophoneTest()
        backend.logout()
        _currentScreen.value = Screen.LOGIN
        viewModelScope.launch {
            _toastMessage.emit("Logged out securely")
        }
    }

    fun createGroup(name: String, description: String, isPrivate: Boolean) {
        if (_isLoading.value) return
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            when (val res = backend.createGroup(name, description, isPrivate)) {
                is GroupResult.Success -> {
                    _isLoading.value = false
                    backend.selectGroup(res.group)
                    _currentScreen.value = Screen.GROUP_ROOM
                    _toastMessage.emit("Group ${res.group.name} created!")
                }
                is GroupResult.Error -> {
                    _isLoading.value = false
                    _errorMessage.value = res.message
                }
            }
        }
    }

    fun joinGroup(code: String) {
        if (_isLoading.value) return
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            when (val res = backend.joinGroupByCode(code)) {
                is GroupResult.Success -> {
                    _isLoading.value = false
                    backend.selectGroup(res.group)
                    _currentScreen.value = Screen.GROUP_ROOM
                    _toastMessage.emit("Joined ${res.group.name}")
                }
                is GroupResult.Error -> {
                    _isLoading.value = false
                    _errorMessage.value = res.message
                }
            }
        }
    }

    fun enterGroup(group: Group) {
        backend.selectGroup(group)
        _currentScreen.value = Screen.GROUP_ROOM
    }

    fun leaveCurrentGroup() {
        backend.leaveCurrentGroup()
        audioEngine.stopMicrophoneTest()
        _currentScreen.value = Screen.DASHBOARD
        viewModelScope.launch {
            _toastMessage.emit("Left group")
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        backend.sendMessage(content)
    }

    fun toggleVoiceChannel(hasPermission: Boolean) {
        if (!backend.isInVoiceChannel.value) {
            if (!hasPermission) {
                viewModelScope.launch {
                    _errorMessage.value = "Microphone permission required for voice channel."
                }
                return
            }
            backend.joinVoiceChannel()
            audioEngine.startMicrophoneTest()
            viewModelScope.launch {
                _toastMessage.emit("Connected to voice channel")
            }
        } else {
            backend.leaveVoiceChannel()
            audioEngine.stopMicrophoneTest()
            viewModelScope.launch {
                _toastMessage.emit("Disconnected from voice channel")
            }
        }
    }

    fun toggleMicMute() {
        backend.toggleMicMute()
    }

    fun toggleDeafen() {
        backend.toggleDeafen()
    }

    fun startMicTest(hasPermission: Boolean) {
        if (!hasPermission) {
            _errorMessage.value = "Microphone permission is required to test your microphone."
            return
        }
        audioEngine.startMicrophoneTest()
    }

    fun stopMicTest() {
        audioEngine.stopMicrophoneTest()
    }

    fun saveAudioSettings(settings: AudioSettings) {
        backend.saveAudioSettings(settings)
        viewModelScope.launch {
            _toastMessage.emit("Settings saved")
        }
        navigateBack()
    }

    fun updateProfile(displayName: String, customStatus: String) {
        backend.updateProfile(displayName, customStatus)
        viewModelScope.launch {
            _toastMessage.emit("Profile updated")
        }
    }

    fun deleteAccount() {
        if (backend.deleteAccount()) {
            _currentScreen.value = Screen.LOGIN
            viewModelScope.launch {
                _toastMessage.emit("Account deleted")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
