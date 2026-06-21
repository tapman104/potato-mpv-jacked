package com.tapman104.mpvplayer.state

data class SubtitleAppearance(
    val scale: Float = 1f,
    val colorArgb: Int = 0xFFFFFFFF.toInt(),
    val bold: Boolean = false,
    val borderStyle: SubtitleBorderStyle = SubtitleBorderStyle.Outline,
    val borderSize: Float = 3f,
    val shadowOffset: Float = 0f,
    val backgroundAlpha: Float = 0f
)
