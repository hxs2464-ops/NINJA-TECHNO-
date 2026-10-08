package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioSettings
import com.example.model.User
import com.example.ui.components.NinjaCard
import com.example.ui.components.NinjaPrimaryButton
import com.example.ui.components.NinjaSecondaryButton
import com.example.ui.components.NinjaSettingRow
import com.example.ui.components.NinjaSliderRow
import com.example.ui.components.NinjaTextField
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StealthCardBorder
import com.example.ui.theme.StealthSurface
import com.example.ui.theme.StealthSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    currentSettings: AudioSettings,
    currentUser: User?,
    micLevel: Float,
    isTestingMic: Boolean,
    isMicDetected: Boolean?,
    onStartMicTest: (hasPermission: Boolean) -> Unit,
    onStopMicTest: () -> Unit,
    onSaveSettings: (AudioSettings) -> Unit,
    onUpdateProfile: (displayName: String, customStatus: String) -> Unit,
    onDeleteAccount: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Local mutable state for editing settings
    var voiceVolume by remember { mutableFloatStateOf(currentSettings.voiceVolume) }
    var musicVolume by remember { mutableFloatStateOf(currentSettings.dashboardMusicVolume) }
    var noiseSuppression by remember { mutableStateOf(currentSettings.noiseSuppression) }
    var echoCancellation by remember { mutableStateOf(currentSettings.echoCancellation) }
    var autoGainControl by remember { mutableStateOf(currentSettings.autoGainControl) }
    var interfaceSounds by remember { mutableStateOf(currentSettings.interfaceSounds) }

    // Account state
    var displayName by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var customStatus by remember { mutableStateOf(currentUser?.customStatus ?: "") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onStartMicTest(true)
        }
    }

    // Stop mic test when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            onStopMicTest()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StealthSurface)
                    .border(1.dp, StealthCardBorder)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextSecondary
                    )
                }
                Text(
                    text = "System Settings",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        val newSettings = AudioSettings(
                            voiceVolume = voiceVolume,
                            dashboardMusicVolume = musicVolume,
                            noiseSuppression = noiseSuppression,
                            echoCancellation = echoCancellation,
                            autoGainControl = autoGainControl,
                            interfaceSounds = interfaceSounds
                        )
                        onSaveSettings(newSettings)
                    },
                    modifier = Modifier.testTag("save_settings_top_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save Settings",
                        tint = CyanNeon
                    )
                }
            }

            // CONTENT LIST
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // CATEGORY 1: Audio Engine & Microphone Test
                Text(
                    text = "AUDIO & VOICE ENGINE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon,
                    letterSpacing = 1.sp
                )

                NinjaCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Hardware Microphone Test",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Verify microphone capture and live input volume.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Meter bar
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(StealthSurfaceVariant)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isTestingMic) "Live Input Level" else "Test inactive",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = "${(micLevel * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanNeon
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { micLevel },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (micLevel > 0.1f) StatusGreen else CyanNeon,
                                trackColor = StealthSurface
                            )
                            if (isTestingMic) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isMicDetected == true) "● Microphone signal detected" else "● Listening for voice input...",
                                    fontSize = 11.sp,
                                    color = if (isMicDetected == true) StatusGreen else Color(0xFFF59E0B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!isTestingMic) {
                                NinjaPrimaryButton(
                                    text = "TEST MICROPHONE",
                                    onClick = {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    },
                                    testTag = "start_mic_test_button",
                                    icon = Icons.Default.Mic,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Button(
                                    onClick = onStopMicTest,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = StatusRed,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("stop_mic_test_button")
                                ) {
                                    Icon(Icons.Default.MicOff, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("STOP TEST", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        NinjaSliderRow(
                            title = "Voice Chat Volume",
                            value = voiceVolume,
                            onValueChange = { voiceVolume = it }
                        )

                        NinjaSliderRow(
                            title = "Dashboard Music",
                            value = musicVolume,
                            onValueChange = { musicVolume = it }
                        )

                        NinjaSettingRow(
                            title = "Noise Suppression",
                            description = "Reduce environmental background noise in voice channels.",
                            checked = noiseSuppression,
                            onCheckedChange = { noiseSuppression = it }
                        )

                        NinjaSettingRow(
                            title = "Echo Cancellation",
                            description = "Prevent feedback loops between speaker and microphone.",
                            checked = echoCancellation,
                            onCheckedChange = { echoCancellation = it }
                        )

                        NinjaSettingRow(
                            title = "Automatic Gain Control",
                            description = "Dynamically balance soft and loud speaking voices.",
                            checked = autoGainControl,
                            onCheckedChange = { autoGainControl = it }
                        )

                        NinjaSettingRow(
                            title = "Interface Sounds",
                            description = "Play subtle tactical audio cues for actions.",
                            checked = interfaceSounds,
                            onCheckedChange = { interfaceSounds = it }
                        )
                    }
                }

                // CATEGORY 2: Account & Identity
                Text(
                    text = "ACCOUNT & OPERATOR PROFILE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon,
                    letterSpacing = 1.sp
                )

                NinjaCard {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        NinjaTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = "Display Name",
                            placeholder = "Operator callsign",
                            testTag = "settings_display_name_input"
                        )

                        NinjaTextField(
                            value = customStatus,
                            onValueChange = { customStatus = it },
                            label = "Squad Status Message",
                            placeholder = "e.g. On mission in Squad Alpha",
                            testTag = "settings_status_input"
                        )

                        NinjaSecondaryButton(
                            text = "Update Profile Info",
                            onClick = { onUpdateProfile(displayName, customStatus) },
                            testTag = "update_profile_button"
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onLogout,
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Log Out", color = TextSecondary, fontSize = 12.sp)
                            }
                            Button(
                                onClick = { showDeleteConfirm = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StatusRed.copy(alpha = 0.2f),
                                    contentColor = StatusRed
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) {
                                Text("Delete Account", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // CATEGORY 3: Permissions & Privacy
                Text(
                    text = "PERMISSIONS & SYSTEM",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon,
                    letterSpacing = 1.sp
                )

                NinjaCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Microphone Permission",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Required for joining squad voice channels and microphone testing.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )
                        NinjaSecondaryButton(
                            text = "Open Device App Settings",
                            onClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            },
                            testTag = "open_app_settings_button"
                        )
                    }
                }

                // CATEGORY 4: About
                Text(
                    text = "ABOUT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon,
                    letterSpacing = 1.sp
                )

                NinjaCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("NINJA TECHNO", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("MADE BY DEV SHAN", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = CyanNeon)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "High-fidelity private multiplayer communication network engineered for real-time squad voice & encrypted text coordination.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Architecture Version: 2.4.0-stable", fontSize = 11.sp, color = TextMuted)
                    }
                }

                // Save & Return Button
                NinjaPrimaryButton(
                    text = "SAVE & RETURN",
                    onClick = {
                        val newSettings = AudioSettings(
                            voiceVolume = voiceVolume,
                            dashboardMusicVolume = musicVolume,
                            noiseSuppression = noiseSuppression,
                            echoCancellation = echoCancellation,
                            autoGainControl = autoGainControl,
                            interfaceSounds = interfaceSounds
                        )
                        onSaveSettings(newSettings)
                    },
                    testTag = "save_and_return_button"
                )

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    // Delete Account Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = StealthSurface,
            title = { Text("Delete Account", color = StatusRed, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to permanently delete your operator profile and access keys? This cannot be undone.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Text("Delete Permanently", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
