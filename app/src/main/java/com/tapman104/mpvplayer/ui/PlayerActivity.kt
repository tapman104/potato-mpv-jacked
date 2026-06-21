package com.tapman104.mpvplayer.ui

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.tapman104.mpvplayer.feature.player.PlayerScreen
import com.tapman104.mpvplayer.feature.player.PlayerViewModel

class PlayerActivity : ComponentActivity() {
    private val viewModel: PlayerViewModel by viewModels()
    private var isPlayingFile = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        viewModel.initialize()
        
        val intentData = intent.data
        if (intentData != null) {
            isPlayingFile = true
            viewModel.loadAndPlay(intentData)
            applyPlaybackOrientation()
            hideSystemUI()
        } else {
            isPlayingFile = false
            applyIdleOrientation()
            showSystemUI()
        }
        
        setContent {
            PlayerScreen(
                viewModel = viewModel,
                onOrientationShouldUnlock = {
                    isPlayingFile = true
                    applyPlaybackOrientation()
                    hideSystemUI()
                },
                onReturnToIdle = {
                    isPlayingFile = false
                    applyIdleOrientation()
                    showSystemUI()
                }
            )
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && isPlayingFile) {
            hideSystemUI()
        }
    }

    private fun applyPlaybackOrientation() {
        val autoRotate = Settings.System.getInt(
            contentResolver,
            Settings.System.ACCELEROMETER_ROTATION, 0
        ) == 1
        requestedOrientation = if (autoRotate) {
            ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        } else {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }

    private fun applyIdleOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun hideSystemUI() {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun showSystemUI() {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.show(WindowInsetsCompat.Type.systemBars())
    }
}
