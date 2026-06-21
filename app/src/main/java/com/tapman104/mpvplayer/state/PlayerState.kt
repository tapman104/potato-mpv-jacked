package com.tapman104.mpvplayer.state

data class PlayerState(
    val status: PlayerStatus = PlayerStatus.Idle,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedMs: Long = 0L,
    val audioTracks: List<AudioTrack> = emptyList(),
    val selectedAudioTrackId: Int = -1,
    val subtitleTracks: List<SubtitleTrack> = emptyList(),
    val selectedSubtitleTrackId: Int = -1,
    val videoWidth: Int = 0,
    val videoHeight: Int = 0,
    val subtitleAppearance: SubtitleAppearance = SubtitleAppearance(),
    val decoderMode: DecoderMode = DecoderMode.Hardware
)
