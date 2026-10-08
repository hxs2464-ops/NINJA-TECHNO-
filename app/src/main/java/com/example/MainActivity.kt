package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Screen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GroupRoomScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.NinjaTechnoTheme
import com.example.ui.theme.StealthBg
import com.example.viewmodel.NinjaViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: NinjaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NinjaTechnoTheme {
                val snackbarHostState = remember { SnackbarHostState() }

                // Collect toasts
                LaunchedEffect(Unit) {
                    viewModel.toastMessage.collectLatest { msg ->
                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = StealthBg,
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(StealthBg)
                            .padding(innerPadding)
                    ) {
                        NinjaTechnoApp(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun NinjaTechnoApp(viewModel: NinjaViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val currentGroup by viewModel.currentGroup.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val audioSettings by viewModel.audioSettings.collectAsStateWithLifecycle()
    val isInVoiceChannel by viewModel.isInVoiceChannel.collectAsStateWithLifecycle()
    val isMicMuted by viewModel.isMicMuted.collectAsStateWithLifecycle()
    val isDeafened by viewModel.isDeafened.collectAsStateWithLifecycle()
    val micLevel by viewModel.micLevel.collectAsStateWithLifecycle()
    val isRecordingActive by viewModel.isRecordingActive.collectAsStateWithLifecycle()
    val isMicrophoneDetected by viewModel.isMicrophoneDetected.collectAsStateWithLifecycle()
    val speakingUsers by viewModel.speakingUsers.collectAsStateWithLifecycle()

    // Handle system back navigation
    BackHandler(enabled = currentScreen != Screen.LOGIN && currentScreen != Screen.DASHBOARD) {
        viewModel.navigateBack()
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            Screen.LOGIN -> {
                LoginScreen(
                    connectionStatus = connectionStatus,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onLogin = { u, p -> viewModel.login(u, p) },
                    onNavigateToRegister = { viewModel.navigateTo(Screen.REGISTER) },
                    onNavigateToSettings = { viewModel.navigateTo(Screen.SETTINGS) }
                )
            }
            Screen.REGISTER -> {
                RegisterScreen(
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onRegister = { u, p, c -> viewModel.register(u, p, c) },
                    onNavigateToLogin = { viewModel.navigateTo(Screen.LOGIN) }
                )
            }
            Screen.DASHBOARD -> {
                DashboardScreen(
                    user = currentUser,
                    groups = groups,
                    connectionStatus = connectionStatus,
                    onEnterGroup = { g -> viewModel.enterGroup(g) },
                    onCreateGroup = { name, desc, priv -> viewModel.createGroup(name, desc, priv) },
                    onJoinGroup = { code -> viewModel.joinGroup(code) },
                    onNavigateToSettings = { viewModel.navigateTo(Screen.SETTINGS) },
                    onLogout = { viewModel.logout() }
                )
            }
            Screen.GROUP_ROOM -> {
                GroupRoomScreen(
                    group = currentGroup,
                    currentUser = currentUser,
                    messages = messages,
                    isInVoiceChannel = isInVoiceChannel,
                    isMicMuted = isMicMuted,
                    isDeafened = isDeafened,
                    micLevel = micLevel,
                    speakingUsers = speakingUsers,
                    onSendMessage = { content -> viewModel.sendMessage(content) },
                    onToggleVoice = { hasPerm -> viewModel.toggleVoiceChannel(hasPerm) },
                    onToggleMicMute = { viewModel.toggleMicMute() },
                    onToggleDeafen = { viewModel.toggleDeafen() },
                    onToggleMemberMute = { uid -> viewModel.backend.toggleMemberMute(uid) },
                    onRemoveMember = { uid -> viewModel.backend.removeMember(uid) },
                    onLeaveGroup = { viewModel.leaveCurrentGroup() },
                    onBack = { viewModel.navigateBack() }
                )
            }
            Screen.SETTINGS -> {
                SettingsScreen(
                    currentSettings = audioSettings,
                    currentUser = currentUser,
                    micLevel = micLevel,
                    isTestingMic = isRecordingActive,
                    isMicDetected = isMicrophoneDetected,
                    onStartMicTest = { hasPerm -> viewModel.startMicTest(hasPerm) },
                    onStopMicTest = { viewModel.stopMicTest() },
                    onSaveSettings = { s -> viewModel.saveAudioSettings(s) },
                    onUpdateProfile = { d, st -> viewModel.updateProfile(d, st) },
                    onDeleteAccount = { viewModel.deleteAccount() },
                    onLogout = { viewModel.logout() },
                    onBack = { viewModel.navigateBack() }
                )
            }
        }
    }
}
