package com.tapman104.mpvplayer.engine

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import `is`.xyz.mpv.MPVLib
import `is`.xyz.mpv.MPVNode

import com.tapman104.mpvplayer.engine.TrackListParser
import com.tapman104.mpvplayer.state.AudioTrack
import com.tapman104.mpvplayer.state.PlayerState
import com.tapman104.mpvplayer.state.PlayerStatus
import com.tapman104.mpvplayer.state.SubtitleTrack

class MpvController(private val context: Context) : MPVLib.EventObserver {

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var fileLoaded = false

    private val handlerThread = HandlerThread("mpv-thread").apply { start() }
    private val handler = Handler(handlerThread.looper)

    fun initialize() {
        post {
            MPVLib.create(context)
            MPVLib.setOptionString("config", "yes")
            MPVLib.setOptionString("config-dir", context.filesDir.path)
            MPVLib.setOptionString("vo", "gpu")
            MPVLib.setOptionString("hwdec", "mediacodec")
            MPVLib.setOptionString("force-window", "no")
            MPVLib.setOptionString("idle", "once")
            MPVLib.init()
            MPVLib.addObserver(this)
            MPVLib.observeProperty("pause", MPVLib.MpvFormat.MPV_FORMAT_FLAG)
            MPVLib.observeProperty("time-pos", MPVLib.MpvFormat.MPV_FORMAT_DOUBLE)
            MPVLib.observeProperty("duration", MPVLib.MpvFormat.MPV_FORMAT_DOUBLE)
            MPVLib.observeProperty("demuxer-cache-time", MPVLib.MpvFormat.MPV_FORMAT_DOUBLE)
            MPVLib.observeProperty("track-list", MPVLib.MpvFormat.MPV_FORMAT_NODE)
            MPVLib.observeProperty("aid", MPVLib.MpvFormat.MPV_FORMAT_INT64)
            MPVLib.observeProperty("sid", MPVLib.MpvFormat.MPV_FORMAT_INT64)
        }
    }

    fun attachSurface(surface: Surface) {
        post { MPVLib.attachSurface(surface) }
    }

    fun detachSurface() {
        post { MPVLib.detachSurface() }
    }

    fun loadFile(path: String) {
        fileLoaded = true
        post { MPVLib.command("loadfile", path) }
    }

    fun play() {
        post {
            MPVLib.setPropertyBoolean("pause", false)
            _state.value = _state.value.copy(status = PlayerStatus.Playing)
        }
    }

    fun pause() {
        post {
            MPVLib.setPropertyBoolean("pause", true)
            _state.value = _state.value.copy(status = PlayerStatus.Paused)
        }
    }

    fun seekTo(ms: Long) {
        post { MPVLib.command("seek", (ms / 1000.0).toString(), "absolute") }
    }

    fun setAudioTrack(trackId: Int) {
        post { MPVLib.setPropertyInt("aid", trackId) }
    }

    fun setSubtitleTrack(trackId: Int) {
        post {
            if (trackId == -1) {
                MPVLib.setPropertyString("sid", "no")
            } else {
                MPVLib.setPropertyInt("sid", trackId)
            }
        }
    }

    fun setSubtitleScale(scale: Float) {
        post {
            MPVLib.setPropertyDouble("sub-scale", scale.toDouble())
            _state.value = _state.value.copy(
                subtitleAppearance = _state.value.subtitleAppearance.copy(scale = scale)
            )
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        post { MPVLib.setPropertyDouble("speed", speed.toDouble()) }
    }

    fun setAspectRatio(ratio: String) {
        val value = when (ratio) {
            "16:9"  -> "16/9"
            "4:3"   -> "4/3"
            "21:9"  -> "21/9"
            "Fill"  -> "-1"        // mpv stretches to fill
            else    -> "no"        // "Fit" — mpv default, preserves source ratio
        }
        post { MPVLib.setPropertyString("video-aspect-override", value) }
    }

    /**
     * Sets the primary subtitle text color using mpv's ARGB hex format (e.g. "#FFFFFFFF").
     * [color] is a 32-bit ARGB integer as produced by [androidx.compose.ui.graphics.Color.value]
     * converted to Android ARGB via [androidx.compose.ui.graphics.toArgb].
     */
    fun setSubtitleColor(argb: Int) {
        // mpv sub-color format: #AARRGGBB
        val hex = String.format("#%08X", argb)
        post {
            MPVLib.setPropertyString("sub-color", hex)
            _state.value = _state.value.copy(
                subtitleAppearance = _state.value.subtitleAppearance.copy(colorArgb = argb)
            )
        }
    }

    fun release() {
        fileLoaded = false
        post {
            MPVLib.removeObserver(this)
            MPVLib.destroy()
            handlerThread.quitSafely()
        }
    }

    override fun eventProperty(property: String, value: Boolean) {
        if (!fileLoaded) return
        if (property == "pause") {
            _state.value = _state.value.copy(
                status = if (value) PlayerStatus.Paused else PlayerStatus.Playing
            )
        }
    }

    override fun eventProperty(property: String) {}
    override fun eventProperty(property: String, value: Long) {
        if (!fileLoaded) return
        when (property) {
            "aid" -> _state.value = _state.value.copy(selectedAudioTrackId = value.toInt())
            "sid" -> _state.value = _state.value.copy(selectedSubtitleTrackId = value.toInt())
        }
    }
    override fun eventProperty(property: String, value: Double) {
        if (!fileLoaded) return
        when (property) {
            "time-pos" -> _state.value = _state.value.copy(
                              positionMs = (value * 1000).toLong()
                          )
            "duration"  -> _state.value = _state.value.copy(
                              durationMs = (value * 1000).toLong()
                          )
            "demuxer-cache-time" -> _state.value = _state.value.copy(
                              bufferedMs = (value * 1000).toLong()
                          )
        }
    }
    override fun eventProperty(property: String, value: String) {}
    override fun eventProperty(property: String, node: MPVNode) {
        if (!fileLoaded) return
        if (property == "track-list") {
            val (audioTracks, subtitleTracks) = TrackListParser.parse(node)
            _state.value = _state.value.copy(
                audioTracks = audioTracks,
                subtitleTracks = subtitleTracks
            )
        }
    }
    override fun event(eventId: Int, node: MPVNode) {
        if (!fileLoaded) return
        when (eventId) {
            MPVLib.MpvEvent.MPV_EVENT_START_FILE -> {
                _state.value = _state.value.copy(status = PlayerStatus.Loading)
            }
            MPVLib.MpvEvent.MPV_EVENT_PLAYBACK_RESTART -> {
                // mpv fires this when playback actually starts after buffering
                // status will be corrected by the next "pause" property event
                // but force Playing here as a fallback:
                if (_state.value.status == PlayerStatus.Loading) {
                    _state.value = _state.value.copy(status = PlayerStatus.Playing)
                }
            }
            MPVLib.MpvEvent.MPV_EVENT_END_FILE -> {
                _state.value = _state.value.copy(
                    status = PlayerStatus.Idle,
                    positionMs = 0L,
                    durationMs = 0L,
                    bufferedMs = 0L,
                    subtitleAppearance = com.tapman104.mpvplayer.state.SubtitleAppearance()
                )
                fileLoaded = false
            }
        }
    }

    private fun post(block: () -> Unit) = handler.post(block)
}
