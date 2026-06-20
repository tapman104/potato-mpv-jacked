package com.tapman104.mpvplayer.vm

import android.app.Application
import android.content.Context
import android.net.Uri
import android.view.Surface
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.StateFlow

import com.tapman104.mpvplayer.engine.MpvController
import com.tapman104.mpvplayer.state.PlayerState
import com.tapman104.mpvplayer.state.PlayerStatus
import com.tapman104.mpvplayer.subtitle.SubtitlePrefsRepository
import com.tapman104.mpvplayer.util.UriResolver
import android.content.Intent

class PlayerViewModel(app: Application) : AndroidViewModel(app) {
    private val controller = MpvController(app)
    private val subtitlePrefs = SubtitlePrefsRepository(app)
    val state: StateFlow<PlayerState> = controller.state

    fun initialize() = controller.initialize()

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
        controller.loadFile(path)
        controller.play()
        val savedScale = subtitlePrefs.getDefaultScale()
        controller.setSubtitleScale(savedScale)
    }

    fun togglePlayPause() {
        if (state.value.status is PlayerStatus.Playing) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    fun seekTo(ms: Long) {
        controller.seekTo(ms)
    }

    fun setAudioTrack(trackId: Int) {
        controller.setAudioTrack(trackId)
    }

    fun setSubtitleTrack(trackId: Int) {
        controller.setSubtitleTrack(trackId)
    }

    fun setSubtitleScale(scale: Float) {
        controller.setSubtitleScale(scale)
    }

    fun setPlaybackSpeed(speed: Float) = controller.setPlaybackSpeed(speed)
    fun setAspectRatio(ratio: String) = controller.setAspectRatio(ratio)

    fun setSubtitleColor(argb: Int) {
        controller.setSubtitleColor(argb)
    }

    fun attachSurface(surface: Surface) {
        controller.attachSurface(surface)
    }

    fun detachSurface() {
        controller.detachSurface()
    }

    override fun onCleared() {
        super.onCleared()
        controller.release()
    }
}
