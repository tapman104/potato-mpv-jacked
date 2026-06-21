package com.tapman104.mpvplayer.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tapman104.mpvplayer.state.PlayerStatus
import com.tapman104.mpvplayer.ui.components.Buffering
import com.tapman104.mpvplayer.ui.components.EmptyPlayer
import com.tapman104.mpvplayer.ui.components.PlayerControls
import com.tapman104.mpvplayer.ui.components.PlayerVideo

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onOrientationShouldUnlock: () -> Unit = {},
    onReturnToIdle: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.status) {
        if (state.status == PlayerStatus.Idle) {
            onReturnToIdle()
        } else {
            onOrientationShouldUnlock()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        PlayerVideo(viewModel = viewModel)
        
        when (state.status) {
            PlayerStatus.Idle -> {
                EmptyPlayer { uri ->
                    viewModel.loadAndPlay(uri)
                }
            }
            PlayerStatus.Loading -> {
                Buffering()
            }
            else -> {
                PlayerControls(viewModel = viewModel)
            }
        }
    }
}
