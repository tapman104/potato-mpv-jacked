package com.tapman104.mpvplayer.state

sealed class PlayerStatus {
    object Idle : PlayerStatus()
    object Playing : PlayerStatus()
    object Paused : PlayerStatus()
    object Loading : PlayerStatus()
    data class Error(val msg: String) : PlayerStatus()
}
