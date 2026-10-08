package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.HeadsetOff
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.ChatMessage
import com.example.model.Group
import com.example.model.GroupMember
import com.example.model.User
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StealthBg
import com.example.ui.theme.StealthCardBorder
import com.example.ui.theme.StealthSurface
import com.example.ui.theme.StealthSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GroupRoomScreen(
    group: Group?,
    currentUser: User?,
    messages: List<ChatMessage>,
    isInVoiceChannel: Boolean,
    isMicMuted: Boolean,
    isDeafened: Boolean,
    micLevel: Float,
    speakingUsers: Set<String>,
    onSendMessage: (String) -> Unit,
    onToggleVoice: (hasPermission: Boolean) -> Unit,
    onToggleMicMute: () -> Unit,
    onToggleDeafen: () -> Unit,
    onToggleMemberMute: (userId: String) -> Unit,
    onRemoveMember: (userId: String) -> Unit,
    onLeaveGroup: () -> Unit,
    onBack: () -> Unit
) {
    if (group == null) {
        onBack()
        return
    }

    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Chat & Voice, 1: Members
    var messageInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var showMenu by remember { mutableStateOf(false) }

    val isOwner = group.ownerId == currentUser?.id

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onToggleVoice(granted)
    }

    // Auto-scroll chat to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
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
                    modifier = Modifier.testTag("group_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextSecondary
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Code: ${group.code}",
                            fontSize = 11.sp,
                            color = CyanNeon
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Code",
                            tint = TextMuted,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable {
                                    val clipboard =
                                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(
                                        ClipData.newPlainText("Group Code", group.code)
                                    )
                                }
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = TextSecondary)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(StealthSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Leave Squad", color = StatusRed) },
                            onClick = {
                                showMenu = false
                                onLeaveGroup()
                            }
                        )
                    }
                }
            }

            // TABS: CHAT & VOICE / SQUAD MEMBERS
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = StealthSurfaceVariant,
                contentColor = CyanNeon,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CyanNeon
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Comms & Voice",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) CyanNeon else TextSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Members (${group.members.size})",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) CyanNeon else TextSecondary
                        )
                    }
                )
            }

            if (selectedTab == 0) {
                // VOICE BAR DOCK
                VoiceChannelBar(
                    isInVoice = isInVoiceChannel,
                    isMicMuted = isMicMuted,
                    isDeafened = isDeafened,
                    micLevel = micLevel,
                    speakingUsers = speakingUsers,
                    onJoinOrLeave = {
                        if (!isInVoiceChannel) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            onToggleVoice(true)
                        }
                    },
                    onToggleMic = onToggleMicMute,
                    onToggleDeafen = onToggleDeafen
                )

                // CHAT MESSAGE LIST
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (messages.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Encrypted channel open. No messages yet.\nStart tactical briefing below.",
                                    color = TextMuted,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    } else {
                        items(messages) { msg ->
                            val isMe = msg.senderId == currentUser?.id
                            ChatMessageBubble(msg = msg, isMe = isMe)
                        }
                    }
                }

                // CHAT INPUT BAR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StealthSurface)
                        .border(1.dp, StealthCardBorder)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        placeholder = { Text("Encrypted message...", color = TextMuted, fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = StealthSurfaceVariant,
                            unfocusedContainerColor = StealthSurfaceVariant,
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = StealthCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = CyanNeon
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (messageInput.isNotBlank()) {
                                onSendMessage(messageInput)
                                messageInput = ""
                            }
                        },
                        enabled = messageInput.isNotBlank(),
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (messageInput.isNotBlank()) CyanNeon else StealthSurfaceVariant)
                            .testTag("send_chat_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (messageInput.isNotBlank()) Color(0xFF041924) else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                // MEMBERS LIST TAB
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(group.members) { member ->
                        val isCurrentUserOwner = group.ownerId == currentUser?.id
                        val canModerate = isCurrentUserOwner && member.userId != currentUser?.id

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(StealthSurface)
                                .border(1.dp, StealthCardBorder, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(
                                name = member.username,
                                size = 36,
                                isOnline = member.isOnline,
                                isSpeaking = speakingUsers.contains(member.username)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = member.username,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    if (member.role == "owner") {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(CyanNeon.copy(alpha = 0.2f))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("OWNER", fontSize = 9.sp, color = CyanNeon, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text(
                                    text = if (member.isMutedByAdmin) "Muted by Owner" else if (member.isOnline) "Active in squad" else "Offline",
                                    fontSize = 11.sp,
                                    color = if (member.isMutedByAdmin) StatusRed else if (member.isOnline) StatusGreen else TextMuted
                                )
                            }

                            if (canModerate) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onToggleMemberMute(member.userId) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (member.isMutedByAdmin) Icons.Default.MicOff else Icons.Default.Mic,
                                            contentDescription = "Mute User",
                                            tint = if (member.isMutedByAdmin) StatusRed else TextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onRemoveMember(member.userId) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove User",
                                            tint = TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceChannelBar(
    isInVoice: Boolean,
    isMicMuted: Boolean,
    isDeafened: Boolean,
    micLevel: Float,
    speakingUsers: Set<String>,
    onJoinOrLeave: () -> Unit,
    onToggleMic: () -> Unit,
    onToggleDeafen: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(StealthSurface)
            .border(1.dp, if (isInVoice) CyanNeon.copy(alpha = 0.4f) else StealthCardBorder)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isInVoice) StatusGreen else TextMuted)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isInVoice) "Voice Connected (Low Latency)" else "Voice Channel Disconnected",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        if (isInVoice && speakingUsers.isNotEmpty()) {
                            Text(
                                text = "Speaking: ${speakingUsers.joinToString(", ")}",
                                fontSize = 11.sp,
                                color = StatusGreen
                            )
                        } else if (isInVoice) {
                            Text(
                                text = "Channel ready • Speak freely",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isInVoice) {
                        IconButton(
                            onClick = onToggleMic,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isMicMuted) StatusRed.copy(alpha = 0.2f) else StealthSurfaceVariant)
                        ) {
                            Icon(
                                imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Toggle Mute",
                                tint = if (isMicMuted) StatusRed else CyanNeon,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onToggleDeafen,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isDeafened) StatusRed.copy(alpha = 0.2f) else StealthSurfaceVariant)
                        ) {
                            Icon(
                                imageVector = if (isDeafened) Icons.Default.HeadsetOff else Icons.Default.Headset,
                                contentDescription = "Toggle Deafen",
                                tint = if (isDeafened) StatusRed else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Button(
                        onClick = onJoinOrLeave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isInVoice) StatusRed else CyanNeon,
                            contentColor = if (isInVoice) Color.White else Color(0xFF041924)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("voice_channel_action_button")
                    ) {
                        Icon(
                            imageVector = if (isInVoice) Icons.Default.CallEnd else Icons.Default.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isInVoice) "Leave" else "Join Voice",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Real Live mic audio meter if in voice
            AnimatedVisibility(visible = isInVoice && !isMicMuted) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Live Mic Input", fontSize = 10.sp, color = TextMuted)
                        Text("${(micLevel * 100).toInt()}%", fontSize = 10.sp, color = CyanNeon)
                    }
                    LinearProgressIndicator(
                        progress = { micLevel },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (micLevel > 0.08f) StatusGreen else CyanNeon,
                        trackColor = StealthSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    msg: ChatMessage,
    isMe: Boolean
) {
    val timeFormatted = remember(msg.timestamp) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(msg.timestamp))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        if (!isMe) {
            Text(
                text = msg.senderUsername,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyanNeon,
                modifier = Modifier.padding(start = 6.dp, bottom = 2.dp)
            )
        }
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isMe) 12.dp else 2.dp,
                        bottomEnd = if (isMe) 2.dp else 12.dp
                    )
                )
                .background(if (isMe) CyanNeon.copy(alpha = 0.18f) else StealthSurfaceVariant)
                .border(
                    1.dp,
                    if (isMe) CyanNeon.copy(alpha = 0.4f) else StealthCardBorder,
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                Text(
                    text = msg.content,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = timeFormatted,
                    fontSize = 10.sp,
                    color = TextMuted,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
