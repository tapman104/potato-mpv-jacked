package com.tapman104.mpvplayer.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.tapman104.mpvplayer.engine.MpvSurface
import com.tapman104.mpvplayer.state.PlayerState
import com.tapman104.mpvplayer.state.PlayerStatus
import com.tapman104.mpvplayer.vm.PlayerViewModel

@Composable
fun PlayerVideo(viewModel: PlayerViewModel) {
    AndroidView(
        factory = { ctx ->
            MpvSurface(ctx).apply {
                onSurfaceReady = { surface -> viewModel.attachSurface(surface) }
                onSurfaceDestroyed = { viewModel.detachSurface() }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
