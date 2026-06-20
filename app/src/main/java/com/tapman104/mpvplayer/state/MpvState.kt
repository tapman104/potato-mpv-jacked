package com.tapman104.mpvplayer.state

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

sealed class PlayerStatus {
    object Idle : PlayerStatus()
    object Playing : PlayerStatus()
    object Paused : PlayerStatus()
    object Loading : PlayerStatus()
    data class Error(val msg: String) : PlayerStatus()
}

data class SubtitleAppearance(
    val scale: Float = 1.0f,
    val colorArgb: Int = android.graphics.Color.WHITE
)

data class PlayerState(
    val status: PlayerStatus = PlayerStatus.Idle,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedMs: Long = 0L,
    val audioTracks: List<AudioTrack> = emptyList(),
    val selectedAudioTrackId: Int = -1,
    val subtitleTracks: List<SubtitleTrack> = emptyList(),
    val selectedSubtitleTrackId: Int = -1,
    val subtitleAppearance: SubtitleAppearance = SubtitleAppearance()
)
