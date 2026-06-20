package com.tapman104.mpvplayer.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tapman104.mpvplayer.state.PlayerStatus
import com.tapman104.mpvplayer.ui.dialogs.AudioTrackDialog
import com.tapman104.mpvplayer.ui.dialogs.MoreOptionsDialog
import com.tapman104.mpvplayer.ui.dialogs.SubtitleTrackDialog
import com.tapman104.mpvplayer.util.formatMs
import com.tapman104.mpvplayer.vm.PlayerViewModel
import kotlinx.coroutines.delay

/**
 * Full-screen player controls overlay.
 *
 * The public signature keeps `viewModel` as the single argument so that
 * [com.tapman104.mpvplayer.ui.PlayerScreen] does not need modification.
 * All state is derived from the ViewModel inside this composable.
 */
@Composable
fun PlayerControls(viewModel: PlayerViewModel) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    val isPlaying = state.status is PlayerStatus.Playing

    // Derive normalised 0f–1f fractions from the raw millisecond values.
    val progress = if (state.durationMs > 0)
        (state.positionMs.toFloat() / state.durationMs.toFloat()).coerceIn(0f, 1f)
    else 0f

    val buffered = if (state.durationMs > 0)
        (state.bufferedMs.toFloat() / state.durationMs.toFloat()).coerceIn(0f, 1f)
    else 0f

    val currentTime = formatMs(state.positionMs)
    val totalTime   = formatMs(state.durationMs)

    // ── Controls visibility ──────────────────────────────────────────────────
    var controlsVisible    by remember { mutableStateOf(true) }
    var showMoreDialog     by remember { mutableStateOf(false) }
    var showAudioDialog    by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showPlaylistSheet  by remember { mutableStateOf(false) }

    // Auto-hide after 3 s when playing (timer resets whenever the key changes).
    LaunchedEffect(isPlaying, controlsVisible) {
        if (isPlaying && controlsVisible) {
            delay(3_000)
            controlsVisible = false
        }
    }

    // ── Layout ───────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            // Tap anywhere on the overlay to toggle controls.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { controlsVisible = !controlsVisible }
    ) {
        // Top bar
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            TopBar(
                title = "Now Playing",
                onBack = {
                    (context as? ComponentActivity)?.onBackPressedDispatcher?.onBackPressed()
                },
                onAudioTrack = { showAudioDialog = true },
                onSubtitle   = { showSubtitleDialog = true },
                onQueue      = { showPlaylistSheet = true },
                onPip        = { viewModel.enterPip(context as ComponentActivity) },
                onMore       = { showMoreDialog = true }
            )
        }

        // Bottom bar
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomBar(
                isPlaying   = isPlaying,
                currentTime = currentTime,
                totalTime   = totalTime,
                progress    = progress,
                buffered    = buffered,
                onSeek      = { fraction ->
                    viewModel.seekTo((fraction * state.durationMs).toLong())
                },
                onPlayPause = { viewModel.togglePlayPause() }
            )
        }
    }

    // ── More-options dialog ──────────────────────────────────────────────────
    if (showMoreDialog) {
        MoreOptionsDialog(
            currentDecoderMode  = state.decoderMode,
            onDismiss           = { showMoreDialog = false },
            onSpeedChange       = { viewModel.setPlaybackSpeed(it) },
            onAspectRatio       = { viewModel.setAspectRatio(it) },
            onDecoderModeChange = { viewModel.setDecoderMode(it) },
            onSettings          = {
                showMoreDialog = false
                val intent = android.content.Intent(
                    context,
                    com.tapman104.mpvplayer.settings.SettingsActivity::class.java
                )
                context.startActivity(intent)
            }
        )
    }

    if (showAudioDialog) {
        AudioTrackDialog(
            tracks          = state.audioTracks,
            selectedTrackId = state.selectedAudioTrackId,
            onTrackSelected = { viewModel.setAudioTrack(it) },
            onDismiss       = { showAudioDialog = false }
        )
    }

    if (showSubtitleDialog) {
        SubtitleTrackDialog(
            tracks                   = state.subtitleTracks,
            selectedTrackId          = state.selectedSubtitleTrackId,
            currentScale             = state.subtitleAppearance.scale,
            currentColorArgb         = state.subtitleAppearance.colorArgb,
            appearance               = state.subtitleAppearance,
            onTrackSelected          = { viewModel.setSubtitleTrack(it) },
            onScaleChanged           = { viewModel.setSubtitleScale(it) },
            onColorChanged           = { viewModel.setSubtitleColor(it) },
            onBoldChanged            = { viewModel.setSubtitleBold(it) },
            onBorderStyleChanged     = { viewModel.setSubtitleBorderStyle(it) },
            onBorderSizeChanged      = { viewModel.setSubtitleBorderSize(it) },
            onShadowOffsetChanged    = { viewModel.setSubtitleShadow(it) },
            onBackgroundAlphaChanged = { viewModel.setSubtitleBackgroundAlpha(it) },
            onDismiss                = { showSubtitleDialog = false }
        )
    }

    if (showPlaylistSheet) {
        PlaylistSheet(
            viewModel = viewModel,
            onDismiss = { showPlaylistSheet = false }
        )
    }
}
