package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backend.ConnectionStatus
import com.example.model.Group
import com.example.model.User
import com.example.ui.components.ConnectionBadge
import com.example.ui.components.NinjaHeader
import com.example.ui.components.NinjaPrimaryButton
import com.example.ui.components.NinjaSecondaryButton
import com.example.ui.components.NinjaTextField
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StealthCardBorder
import com.example.ui.theme.StealthSurface
import com.example.ui.theme.StealthSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    user: User?,
    groups: List<Group>,
    connectionStatus: ConnectionStatus,
    onEnterGroup: (Group) -> Unit,
    onCreateGroup: (name: String, desc: String, isPrivate: Boolean) -> Unit,
    onJoinGroup: (code: String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showJoinGroupDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NinjaHeader(compact = true, modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("dashboard_settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondary)
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("dashboard_logout_button")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Sign Out", tint = TextMuted)
                    }
                }
            }

            // User Profile Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(StealthSurface)
                    .border(1.dp, StealthCardBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        name = user?.displayName ?: "Ninja",
                        size = 44,
                        isOnline = true
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user?.displayName ?: "Operator",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "@${user?.username ?: "user"}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = user?.customStatus ?: "Ready for squad comms",
                            fontSize = 12.sp,
                            color = CyanNeon.copy(alpha = 0.9f)
                        )
                    }
                    ConnectionBadge(status = connectionStatus)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Quick Buttons: Create Group / Join Group
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NinjaSecondaryButton(
                    text = "Join Group",
                    onClick = { showJoinGroupDialog = true },
                    testTag = "join_group_open_dialog_button",
                    icon = Icons.Default.GroupAdd,
                    modifier = Modifier.weight(1f)
                )
                NinjaPrimaryButton(
                    text = "Create Group",
                    onClick = { showCreateGroupDialog = true },
                    testTag = "create_group_open_dialog_button",
                    icon = Icons.Default.Add,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Group List Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Squads (${groups.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Tap to enter voice & chat",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (groups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No active groups joined", color = TextSecondary, fontSize = 14.sp)
                        Text("Create a squad or enter a squad invite code above", color = TextMuted, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(groups) { group ->
                        GroupListItem(
                            group = group,
                            currentUserId = user?.id ?: "",
                            onClick = { onEnterGroup(group) }
                        )
                    }
                }
            }
        }
    }

    // Dialog: Create Group
    if (showCreateGroupDialog) {
        var groupName by remember { mutableStateOf("") }
        var groupDesc by remember { mutableStateOf("") }
        var isPrivate by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showCreateGroupDialog = false },
            containerColor = StealthSurface,
            title = {
                Text("Create New Squad", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    NinjaTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = "Squad Name",
                        placeholder = "e.g. Shadow Vanguard",
                        testTag = "create_group_name_input"
                    )
                    NinjaTextField(
                        value = groupDesc,
                        onValueChange = { groupDesc = it },
                        label = "Description / Mission",
                        placeholder = "Tactical comms & voice channel",
                        testTag = "create_group_desc_input"
                    )
                }
            },
            confirmButton = {
                NinjaPrimaryButton(
                    text = "Create Squad",
                    onClick = {
                        if (groupName.isNotBlank()) {
                            onCreateGroup(groupName, groupDesc, isPrivate)
                            showCreateGroupDialog = false
                        }
                    },
                    testTag = "confirm_create_group_button",
                    enabled = groupName.isNotBlank()
                )
            },
            dismissButton = {
                TextButton(onClick = { showCreateGroupDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Dialog: Join Group by Code
    if (showJoinGroupDialog) {
        var groupCode by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showJoinGroupDialog = false },
            containerColor = StealthSurface,
            title = {
                Text("Join Squad by Code", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Enter 8-digit squad code (e.g. NINJA-DEV-77)",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    NinjaTextField(
                        value = groupCode,
                        onValueChange = { groupCode = it.uppercase() },
                        label = "Squad Code",
                        placeholder = "NINJA-DEV-77",
                        testTag = "join_group_code_input"
                    )
                }
            },
            confirmButton = {
                NinjaPrimaryButton(
                    text = "Join Squad",
                    onClick = {
                        if (groupCode.isNotBlank()) {
                            onJoinGroup(groupCode)
                            showJoinGroupDialog = false
                        }
                    },
                    testTag = "confirm_join_group_button",
                    enabled = groupCode.isNotBlank()
                )
            },
            dismissButton = {
                TextButton(onClick = { showJoinGroupDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun GroupListItem(
    group: Group,
    currentUserId: String,
    onClick: () -> Unit
) {
    val isOwner = group.ownerId == currentUserId
    val onlineCount = group.members.count { it.isOnline }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StealthSurface)
            .border(1.dp, StealthCardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag("group_item_${group.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(StealthSurfaceVariant)
                    .border(1.dp, CyanNeon.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (group.isPrivate) Icons.Default.Lock else Icons.Default.Public,
                    contentDescription = null,
                    tint = CyanNeon,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = group.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (isOwner) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyanNeon.copy(alpha = 0.15f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("OWNER", fontSize = 9.sp, color = CyanNeon, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (group.description.isNotBlank()) {
                    Text(
                        text = group.description,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
                Text(
                    text = "Code: ${group.code} • ${group.members.size} members ($onlineCount online)",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(StatusGreen)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$onlineCount",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
