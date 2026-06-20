package com.tapman104.mpvplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Top control bar shown during playback.
 *
 * Displays a back button, a title, and action icons for:
 * audio track, subtitles, playlist queue, PiP, and more options.
 */
@Composable
fun TopBar(
    title: String,
    onBack: () -> Unit,
    onAudioTrack: () -> Unit,
    onSubtitle: () -> Unit,
    onQueue: () -> Unit,
    onPip: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
        )

        IconButton(onClick = onAudioTrack) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Audio track",
                tint = Color.White
            )
        }

        IconButton(onClick = onSubtitle) {
            Icon(
                imageVector = Icons.Default.ClosedCaption,
                contentDescription = "Subtitles",
                tint = Color.White
            )
        }

        IconButton(onClick = onQueue) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                contentDescription = "Playlist",
                tint = Color.White
            )
        }

        IconButton(onClick = onPip) {
            Icon(
                imageVector = Icons.Default.PictureInPicture,
                contentDescription = "Picture in Picture",
                tint = Color.White
            )
        }

        IconButton(onClick = onMore) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = Color.White
            )
        }
    }
}
