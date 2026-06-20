package com.tapman104.mpvplayer.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tapman104.mpvplayer.state.DecoderMode

private val AccentBlue = Color(0xFF80BFFF)

@Composable
fun MoreOptionsDialog(
    currentDecoderMode: DecoderMode,
    onDismiss: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onAspectRatio: (String) -> Unit,
    onDecoderModeChange: (DecoderMode) -> Unit,
    onSettings: () -> Unit
) {
    var selectedSpeed by remember { mutableFloatStateOf(1.0f) }
    var selectedAspect by remember { mutableStateOf("Fit") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E), RoundedCornerShape(16.dp))
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "More Options",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

            // ── Playback speed ─────────────────────────────────────────────
            SectionLabel("Playback Speed  ${formatSpeed(selectedSpeed)}")
            ChipRow(
                options = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f),
                selected = selectedSpeed,
                label = { "${it}×".trimEnd('0').trimEnd('.') + "×" },
                onSelect = { speed ->
                    selectedSpeed = speed
                    onSpeedChange(speed)
                }
            )

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

            // ── Aspect ratio ───────────────────────────────────────────────
            SectionLabel("Aspect Ratio")
            ChipRowStrings(
                options = listOf("Fit", "Fill", "16:9", "4:3", "21:9"),
                selected = selectedAspect,
                onSelect = { ratio ->
                    selectedAspect = ratio
                    onAspectRatio(ratio)
                }
            )

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

            // ── Decoder mode ───────────────────────────────────────────────
            SectionLabel("Decoder")
            ChipRowDecoder(
                current = currentDecoderMode,
                onSelect = onDecoderModeChange
            )

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

            // ── Settings button ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismiss()
                        onSettings()
                    }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Settings", color = Color.White, fontSize = 15.sp)
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 12.dp, bottom = 4.dp)
            ) {
                Text("Close", color = AccentBlue)
            }
        }
    }
}

// ── Private helpers ───────────────────────────────────────────────────────────

private fun formatSpeed(speed: Float): String {
    return if (speed == speed.toInt().toFloat()) "${speed.toInt()}×" else "${speed}×"
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.55f),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 6.dp, end = 20.dp)
    )
}

@Composable
private fun ChipRow(
    options: List<Float>,
    selected: Float,
    label: (Float) -> String,
    onSelect: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { opt ->
            val isSelected = opt == selected
            Chip(text = label(opt), isSelected = isSelected) { onSelect(opt) }
        }
    }
}

@Composable
private fun ChipRowStrings(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { opt ->
            Chip(text = opt, isSelected = opt == selected) { onSelect(opt) }
        }
    }
}

@Composable
private fun ChipRowDecoder(
    current: DecoderMode,
    onSelect: (DecoderMode) -> Unit
) {
    val entries = listOf(
        DecoderMode.Hardware     to "HW",
        DecoderMode.HardwareCopy to "HW+",
        DecoderMode.Software     to "SW"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        entries.forEach { (mode, label) ->
            Chip(text = label, isSelected = mode == current) { onSelect(mode) }
        }
    }
}

@Composable
private fun RowScope.Chip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(36.dp)
            .background(
                if (isSelected) AccentBlue.copy(alpha = 0.25f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                if (isSelected) AccentBlue else Color.White.copy(alpha = 0.2f),
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) AccentBlue else Color.White,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}
