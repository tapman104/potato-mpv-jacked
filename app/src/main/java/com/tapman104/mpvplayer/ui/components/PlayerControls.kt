package com.tapman104.mpvplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tapman104.mpvplayer.feature.player.PlayerViewModel
import com.tapman104.mpvplayer.state.PlayerStatus
import com.tapman104.mpvplayer.util.formatMs
import kotlinx.coroutines.delay

@Composable
fun PlayerControls(viewModel: PlayerViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val fileName by viewModel.fileName.collectAsStateWithLifecycle()

    var controlsVisible by remember { mutableStateOf(true) }
    var showAudioDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showMoreDialog by remember { mutableStateOf(false) }

    val isPlaying = state.status == PlayerStatus.Playing

    LaunchedEffect(isPlaying, controlsVisible) {
        if (isPlaying && controlsVisible) {
            delay(3000)
            controlsVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                controlsVisible = !controlsVisible
            }
    ) {
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            TopBar(
                title = fileName,
                onBack = { /* No-op, handled by Activity */ },
                onAudioTrack = { showAudioDialog = true },
                onSubtitle = { showSubtitleDialog = true },
                onMore = { showMoreDialog = true }
            )
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val progress = if (state.durationMs > 0) {
                state.positionMs.toFloat() / state.durationMs.toFloat()
            } else 0f
            
            val buffered = if (state.durationMs > 0) {
                state.bufferedMs.toFloat() / state.durationMs.toFloat()
            } else 0f

            BottomBar(
                isPlaying = isPlaying,
                currentTime = formatMs(state.positionMs),
                totalTime = formatMs(state.durationMs),
                progress = progress,
                buffered = buffered,
                onSeek = { fraction ->
                    viewModel.seekTo((fraction * state.durationMs).toLong())
                },
                onPlayPause = { viewModel.togglePlayPause() }
            )
        }

        if (showAudioDialog) {
            showAudioDialog = false
        }
        if (showSubtitleDialog) {
            showSubtitleDialog = false
        }
        if (showMoreDialog) {
            showMoreDialog = false
        }
    }
}
