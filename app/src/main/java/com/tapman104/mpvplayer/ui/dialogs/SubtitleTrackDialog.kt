package com.tapman104.mpvplayer.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tapman104.mpvplayer.state.SubtitleAppearance
import com.tapman104.mpvplayer.state.SubtitleBorderStyle
import com.tapman104.mpvplayer.state.SubtitleTrack

private val AccentBlue = Color(0xFF80BFFF)

@Composable
fun SubtitleTrackDialog(
    tracks: List<SubtitleTrack>,
    selectedTrackId: Int,
    currentScale: Float,
    currentColorArgb: Int,
    appearance: SubtitleAppearance,
    onTrackSelected: (Int) -> Unit,
    onScaleChanged: (Float) -> Unit,
    onColorChanged: (Int) -> Unit,
    onBoldChanged: (Boolean) -> Unit,
    onBorderStyleChanged: (SubtitleBorderStyle) -> Unit,
    onBorderSizeChanged: (Float) -> Unit,
    onShadowOffsetChanged: (Float) -> Unit,
    onBackgroundAlphaChanged: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E), RoundedCornerShape(16.dp))
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Subtitles",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

            LazyColumn(modifier = Modifier.heightIn(max = 560.dp)) {

                // ── Track list ─────────────────────────────────────────────
                if (tracks.isNotEmpty()) {
                    item {
                        DialogSectionHeader("Track")
                    }
                    // "Off" option
                    item {
                        TrackRow(
                            label = "Off",
                            subLabel = null,
                            isSelected = selectedTrackId == -1,
                            onClick = { onTrackSelected(-1) }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                    }
                    items(tracks) { track ->
                        TrackRow(
                            label = track.title,
                            subLabel = track.lang.takeIf { it.isNotBlank() }?.uppercase(),
                            isSelected = track.id == selectedTrackId,
                            onClick = { onTrackSelected(track.id) }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                    }
                }

                // ── Scale ──────────────────────────────────────────────────
                item {
                    DialogSectionHeader("Scale  ${String.format("%.1f", currentScale)}×")
                    Slider(
                        value = currentScale,
                        onValueChange = onScaleChanged,
                        valueRange = 0.5f..3.0f,
                        colors = sliderColors(),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // ── Bold ───────────────────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Bold", color = Color.White, fontSize = 14.sp)
                        Switch(
                            checked = appearance.bold,
                            onCheckedChange = onBoldChanged,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AccentBlue,
                                checkedTrackColor = AccentBlue.copy(alpha = 0.4f)
                            )
                        )
                    }
                }

                // ── Border style ───────────────────────────────────────────
                item {
                    DialogSectionHeader("Border Style")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SubtitleBorderStyle.entries.forEach { style ->
                            val isSelected = appearance.borderStyle == style
                            val label = when (style) {
                                SubtitleBorderStyle.None      -> "None"
                                SubtitleBorderStyle.Outline   -> "Outline"
                                SubtitleBorderStyle.OpaqueBox -> "Box"
                                SubtitleBorderStyle.ShadowBox -> "Shadow"
                            }
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
                                    .clickable { onBorderStyleChanged(style) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) AccentBlue else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // ── Border size ────────────────────────────────────────────
                item {
                    DialogSectionHeader("Border Size  ${String.format("%.1f", appearance.borderSize)}")
                    Slider(
                        value = appearance.borderSize,
                        onValueChange = onBorderSizeChanged,
                        valueRange = 0f..8f,
                        colors = sliderColors(),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // ── Shadow offset ──────────────────────────────────────────
                item {
                    DialogSectionHeader("Shadow Offset  ${String.format("%.1f", appearance.shadowOffset)}")
                    Slider(
                        value = appearance.shadowOffset,
                        onValueChange = onShadowOffsetChanged,
                        valueRange = 0f..10f,
                        colors = sliderColors(),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // ── Background alpha ───────────────────────────────────────
                item {
                    DialogSectionHeader("Background Alpha  ${(appearance.backgroundAlpha * 100).toInt()}%")
                    Slider(
                        value = appearance.backgroundAlpha,
                        onValueChange = onBackgroundAlphaChanged,
                        valueRange = 0f..1f,
                        colors = sliderColors(),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // ── Color picker (preset swatches) ─────────────────────────
                item {
                    DialogSectionHeader("Color")
                    ColorSwatchRow(
                        currentArgb = currentColorArgb,
                        onColorSelected = onColorChanged
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
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

@Composable
private fun DialogSectionHeader(text: String) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.55f),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 4.dp, end = 20.dp)
    )
}

@Composable
private fun TrackRow(
    label: String,
    subLabel: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = if (isSelected) AccentBlue else Color.White,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
            if (subLabel != null) {
                Text(text = subLabel, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            }
        }
        if (isSelected) {
            Icon(Icons.Default.Check, contentDescription = "Selected", tint = AccentBlue, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ColorSwatchRow(currentArgb: Int, onColorSelected: (Int) -> Unit) {
    val presets = listOf(
        Color.White, Color.Yellow, Color(0xFFFF8C00),
        Color(0xFFFF4444), Color(0xFF80FF80), Color(0xFF80BFFF),
        Color(0xFFBF80FF), Color(0xFFFF80BF)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        presets.forEach { color ->
            val argb = color.toArgb()
            val isSelected = argb == currentArgb
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color, RoundedCornerShape(50))
                    .then(
                        if (isSelected) Modifier.border(2.dp, Color.White, RoundedCornerShape(50))
                        else Modifier
                    )
                    .clickable { onColorSelected(argb) }
            )
        }
    }
}

@Composable
private fun sliderColors() = SliderDefaults.colors(
    thumbColor = AccentBlue,
    activeTrackColor = AccentBlue,
    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
)
