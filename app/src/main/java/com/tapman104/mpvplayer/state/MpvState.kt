package com.tapman104.mpvplayer.state

// ── Decoder mode ─────────────────────────────────────────────────────────────

enum class DecoderMode(val value: String) {
    Hardware("mediacodec"),
    Software("no"),
    HardwareCopy("mediacodec-copy")
}

// ── Track types ───────────────────────────────────────────────────────────────

data class AudioTrack(
    val id: Int,
    val title: String,
    val lang: String
)

data class SubtitleTrack(
    val id: Int,
    val title: String,
    val lang: String
)

// ── Player status ─────────────────────────────────────────────────────────────

sealed class PlayerStatus {
    object Idle : PlayerStatus()
    object Playing : PlayerStatus()
    object Paused : PlayerStatus()
    object Loading : PlayerStatus()
    data class Error(val msg: String) : PlayerStatus()
}

// ── Subtitle border style ─────────────────────────────────────────────────────

/**
 * Maps to mpv's sub-border-style property values:
 *   0 = no style, 1 = outline+shadow, 3 = opaque box, 4 = background box
 */
enum class SubtitleBorderStyle(val mpvValue: Int) {
    None(0),
    Outline(1),
    OpaqueBox(3),
    ShadowBox(4)
}

// ── Subtitle appearance ───────────────────────────────────────────────────────

data class SubtitleAppearance(
    val scale: Float = 1.0f,
    val colorArgb: Int = android.graphics.Color.WHITE,
    val bold: Boolean = false,
    val borderStyle: SubtitleBorderStyle = SubtitleBorderStyle.Outline,
    val borderSize: Float = 2.0f,
    val shadowOffset: Float = 1.0f,
    val backgroundAlpha: Float = 0.0f
)

// ── Player state ──────────────────────────────────────────────────────────────

data class PlayerState(
    val status: PlayerStatus = PlayerStatus.Idle,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedMs: Long = 0L,
    val audioTracks: List<AudioTrack> = emptyList(),
    val selectedAudioTrackId: Int = -1,
    val subtitleTracks: List<SubtitleTrack> = emptyList(),
    val selectedSubtitleTrackId: Int = -1,
    val subtitleAppearance: SubtitleAppearance = SubtitleAppearance(),
    val decoderMode: DecoderMode = DecoderMode.Hardware,
    val videoWidth: Int = 0,
    val videoHeight: Int = 0
)
