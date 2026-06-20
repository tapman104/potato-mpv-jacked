package com.tapman104.mpvplayer.vm

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.view.Surface
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.StateFlow

import com.tapman104.mpvplayer.engine.MpvController
import com.tapman104.mpvplayer.engine.PipManager
import com.tapman104.mpvplayer.engine.PlaylistItem
import com.tapman104.mpvplayer.engine.PlaylistManager
import com.tapman104.mpvplayer.engine.RepeatMode
import com.tapman104.mpvplayer.state.DecoderMode
import com.tapman104.mpvplayer.state.PlayerState
import com.tapman104.mpvplayer.state.PlayerStatus
import com.tapman104.mpvplayer.state.SubtitleBorderStyle
import com.tapman104.mpvplayer.subtitle.SubtitlePrefsRepository
import com.tapman104.mpvplayer.util.UriResolver
import android.content.Intent

class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    private val controller = MpvController(app)
    private val subtitlePrefs = SubtitlePrefsRepository(app)

    val state: StateFlow<PlayerState> = controller.state

    // ── Playlist ──────────────────────────────────────────────────────────────

    val playlist = PlaylistManager()
    val playlistItems: StateFlow<List<PlaylistItem>>  = playlist.items
    val playlistCurrentIndex: StateFlow<Int>           = playlist.currentIndex
    val isShuffled: StateFlow<Boolean>                 = playlist.isShuffled
    val repeatMode: StateFlow<RepeatMode>              = playlist.repeatMode

    init {
        // When a file ends naturally, try to advance the playlist.
        controller.onEndOfFile = {
            val next = playlist.next()
            if (next != null) {
                loadUri(next.uri)
            }
        }
    }

    // ── Initialisation ────────────────────────────────────────────────────────

    fun initialize() = controller.initialize()

    // ── Playback ──────────────────────────────────────────────────────────────

    fun loadAndPlay(uri: Uri) {
        try {
            getApplication<Application>().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val path = UriResolver.resolve(getApplication(), uri)
        val title = uri.lastPathSegment ?: uri.toString()

        // Add to playlist if not already the current queued item.
        val existingIdx = playlist.items.value.indexOfFirst { it.uri == uri.toString() }
        if (existingIdx >= 0) {
            playlist.jumpTo(existingIdx)
        } else {
            playlist.add(PlaylistItem(uri = uri.toString(), title = title))
            playlist.jumpTo(playlist.items.value.lastIndex)
        }

        loadUri(path)
    }

    /** Internal load by resolved path (no playlist interaction). */
    private fun loadUri(path: String) {
        controller.loadFile(path)
        controller.play()
        controller.setSubtitleScale(subtitlePrefs.getDefaultScale())
    }

    fun togglePlayPause() {
        if (state.value.status is PlayerStatus.Playing) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    fun seekTo(ms: Long) = controller.seekTo(ms)

    // ── Tracks ────────────────────────────────────────────────────────────────

    fun setAudioTrack(trackId: Int)    = controller.setAudioTrack(trackId)
    fun setSubtitleTrack(trackId: Int) = controller.setSubtitleTrack(trackId)

    // ── Subtitle appearance ───────────────────────────────────────────────────

    fun setSubtitleScale(scale: Float)                   = controller.setSubtitleScale(scale)
    fun setSubtitleColor(argb: Int)                      = controller.setSubtitleColor(argb)
    fun setSubtitleBold(bold: Boolean)                   = controller.setSubtitleBold(bold)
    fun setSubtitleBorderStyle(style: SubtitleBorderStyle) = controller.setSubtitleBorderStyle(style)
    fun setSubtitleBorderSize(size: Float)               = controller.setSubtitleBorderSize(size)
    fun setSubtitleShadow(offset: Float)                 = controller.setSubtitleShadow(offset)
    fun setSubtitleBackgroundAlpha(alpha: Float)         = controller.setSubtitleBackgroundAlpha(alpha)

    // ── Playback options ──────────────────────────────────────────────────────

    fun setPlaybackSpeed(speed: Float)  = controller.setPlaybackSpeed(speed)
    fun setAspectRatio(ratio: String)   = controller.setAspectRatio(ratio)

    // ── Feature 1: Decoder mode ───────────────────────────────────────────────

    fun setDecoderMode(mode: DecoderMode) = controller.setDecoderMode(mode)

    // ── Feature 3: PiP ───────────────────────────────────────────────────────

    /**
     * Called by the Activity when it wants to enter PiP.
     * [activity] must be the currently running [PlayerActivity].
     */
    fun enterPip(activity: Activity) {
        val s = state.value
        PipManager(activity).enterPip(s.videoWidth, s.videoHeight)
    }

    // ── Feature 4: Playlist operations ───────────────────────────────────────

    fun playlistAdd(item: PlaylistItem)     = playlist.add(item)
    fun playlistRemove(index: Int)          = playlist.remove(index)
    fun playlistClear()                     = playlist.clear()
    fun playlistJumpTo(index: Int) {
        val item = playlist.jumpTo(index) ?: return
        loadUri(item.uri)
    }
    fun playlistMoveUp(index: Int) {
        if (index <= 0) return
        val list = playlist.items.value.toMutableList()
        val tmp = list[index]; list[index] = list[index - 1]; list[index - 1] = tmp
        playlist.clear()
        list.forEach { playlist.add(it) }
    }
    fun playlistMoveDown(index: Int) {
        val list = playlist.items.value
        if (index >= list.size - 1) return
        val mut = list.toMutableList()
        val tmp = mut[index]; mut[index] = mut[index + 1]; mut[index + 1] = tmp
        playlist.clear()
        mut.forEach { playlist.add(it) }
    }
    fun toggleShuffle()    = playlist.toggleShuffle()
    fun cycleRepeatMode()  = playlist.cycleRepeatMode()

    // ── Surface ───────────────────────────────────────────────────────────────

    fun attachSurface(surface: Surface) = controller.attachSurface(surface)
    fun detachSurface()                 = controller.detachSurface()

    override fun onCleared() {
        super.onCleared()
        controller.release()
    }
}
