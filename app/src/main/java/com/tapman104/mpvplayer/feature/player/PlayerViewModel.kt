package com.tapman104.mpvplayer.feature.player

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.view.Surface
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.tapman104.mpvplayer.engine.MpvController
import com.tapman104.mpvplayer.state.PlayerState
import com.tapman104.mpvplayer.state.PlayerStatus
import com.tapman104.mpvplayer.util.UriResolver

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val controller = MpvController(application)
    
    val state: StateFlow<PlayerState> = controller.state
    
    private val _fileName = MutableStateFlow("")
    val fileName: StateFlow<String> = _fileName.asStateFlow()

    fun initialize() {
        controller.initialize()
    }

    fun loadAndPlay(uri: Uri) {
        val application = getApplication<Application>()
        try {
            application.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: SecurityException) {
            // Ignore if not a document URI
        }
        
        val path = UriResolver.resolve(application, uri)
        controller.loadFile(path)
        controller.play()
        
        // Derive file name
        var name = uri.lastPathSegment ?: ""
        if (uri.scheme == "content") {
            application.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        name = cursor.getString(index)
                    }
                }
            }
        }
        _fileName.value = name
    }

    fun togglePlayPause() {
        if (state.value.status == PlayerStatus.Playing) {
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
