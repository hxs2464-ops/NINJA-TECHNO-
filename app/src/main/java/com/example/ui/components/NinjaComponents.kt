package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backend.ConnectionStatus
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

@Composable
fun NinjaHeader(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 8.dp else 10.dp)
                    .clip(CircleShape)
                    .background(CyanNeon)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "NINJA TECHNO",
                style = MaterialTheme.typography.titleLarge.copy(
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimary
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "MADE BY DEV SHAN",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Medium
            ),
            color = CyanNeon.copy(alpha = 0.85f)
        )
    }
}

@Composable
fun ConnectionBadge(
    status: ConnectionStatus,
    modifier: Modifier = Modifier
) {
    val (dotColor, text) = when (status) {
        is ConnectionStatus.Connected -> StatusGreen to "Connected"
        is ConnectionStatus.Connecting -> Color(0xFFF59E0B) to "Connecting..."
        is ConnectionStatus.Reconnecting -> Color(0xFFF59E0B) to "Reconnecting (${status.attempt})..."
        is ConnectionStatus.Disconnected -> StatusRed to "Offline"
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(StealthSurfaceVariant.copy(alpha = 0.8f))
            .border(1.dp, StealthCardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun NinjaCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(StealthSurface)
            .border(1.dp, StealthCardBorder, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        content()
    }
}

@Composable
fun NinjaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    testTag: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false,
    errorMessage: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextMuted, fontSize = 14.sp) },
            singleLine = true,
            visualTransformation = visualTransformation,
            isError = isError,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = StealthSurfaceVariant,
                unfocusedContainerColor = StealthSurfaceVariant,
                errorContainerColor = StealthSurfaceVariant,
                focusedBorderColor = CyanNeon,
                unfocusedBorderColor = StealthCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = CyanNeon
            ),
            leadingIcon = if (leadingIcon != null) {
                { Icon(leadingIcon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) }
            } else null,
            trailingIcon = trailingIcon
        )
        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = StatusRed,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp, start = 2.dp)
            )
        }
    }
}

@Composable
fun NinjaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CyanNeon,
            contentColor = Color(0xFF041924),
            disabledContainerColor = CyanNeon.copy(alpha = 0.35f),
            disabledContentColor = Color(0xFF041924).copy(alpha = 0.5f)
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color(0xFF041924),
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun NinjaSecondaryButton(
    text: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = TextPrimary
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.horizontalGradient(listOf(StealthCardBorder, StealthCardBorder))
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun NinjaSettingRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF041924),
                checkedTrackColor = CyanNeon,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = StealthSurfaceVariant,
                uncheckedBorderColor = StealthCardBorder
            )
        )
    }
}

@Composable
fun NinjaSliderRow(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val percent = (value * 100).toInt()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = "$percent%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyanNeon
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = CyanNeon,
                activeTrackColor = CyanNeon,
                inactiveTrackColor = StealthSurfaceVariant
            )
        )
    }
}

@Composable
fun UserAvatar(
    name: String,
    size: Int = 36,
    isOnline: Boolean = true,
    isSpeaking: Boolean = false
) {
    val initial = if (name.isNotBlank()) name.first().uppercase() else "N"
    val borderColor by animateColorAsState(
        targetValue = if (isSpeaking) StatusGreen else StealthCardBorder
    )

    Box(
        modifier = Modifier.size((size + 4).dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(StealthSurfaceVariant)
                .border(if (isSpeaking) 2.dp else 1.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = CyanNeon,
                fontSize = (size * 0.42).sp,
                fontWeight = FontWeight.Bold
            )
        }
        // Small status pip
        Box(
            modifier = Modifier
                .size((size * 0.28).dp.coerceAtLeast(8.dp))
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(if (isOnline) StatusGreen else TextMuted)
                .border(1.5.dp, StealthBg, CircleShape)
        )
    }
}
