package com.tapman104.mpvplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Bottom controls bar.
 *
 * [progress] and [buffered] are both normalised 0f–1f fractions.
 * Internally they are mapped to a fixed 100 000 ms timeline so the
 * existing [PlayerSeekBar] (which works in milliseconds) can be reused
 * without modification.
 */
@Composable
fun BottomBar(
    isPlaying: Boolean,
    currentTime: String,
    totalTime: String,
    progress: Float,
    buffered: Float,
    onSeek: (Float) -> Unit,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Map normalised fractions → millisecond domain for PlayerSeekBar.
    val fakeDurationMs = 100_000L
    val fakePositionMs = (progress.coerceIn(0f, 1f) * fakeDurationMs).toLong()
    val fakeBufferedMs = (buffered.coerceIn(0f, 1f) * fakeDurationMs).toLong()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                )
            )
            .padding(bottom = 16.dp)
    ) {
        // ── Row 1 — Seekbar row ─────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentTime,
                color = Color.White,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )

            PlayerSeekBar(
                positionMs = fakePositionMs,
                durationMs = fakeDurationMs,
                bufferedMs = fakeBufferedMs,
                onSeek = { seekMs ->
                    // Convert back to 0f–1f fraction
                    onSeek(seekMs.toFloat() / fakeDurationMs.toFloat())
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            Text(
                text = totalTime,
                color = Color.White,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // ── Row 2 — Play/Pause row ──────────────────────────────────────────
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}
