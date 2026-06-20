package com.tapman104.mpvplayer.ui

import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

import com.tapman104.mpvplayer.vm.PlayerViewModel

class PlayerActivity : ComponentActivity() {

    private val viewModel: PlayerViewModel by viewModels()

    private val insetsController by lazy {
        WindowInsetsControllerCompat(window, window.decorView)
    }

    /** Hide status bar + navigation bar — true immersive for video playback. */
    private fun hideSystemUI() {
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    /** Show system bars again (used when back in the idle/picker state). */
    private fun showSystemUI() {
        insetsController.show(WindowInsetsCompat.Type.systemBars())
    }

    private fun applyPlaybackOrientation() {
        val autoRotate = android.provider.Settings.System.getInt(
            contentResolver,
            android.provider.Settings.System.ACCELEROMETER_ROTATION,
            0
        ) == 1
        requestedOrientation = if (autoRotate) {
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        } else {
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }

    private fun applyIdleOrientation() {
        requestedOrientation =
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Draw edge-to-edge so the video fills the whole screen.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewModel.initialize()

        val intentUri = intent.data
        if (intentUri == null) {
            applyIdleOrientation()
            showSystemUI()
        } else {
            applyPlaybackOrientation()
            hideSystemUI()
            viewModel.loadAndPlay(intentUri)
        }

        setContent {
            PlayerScreen(
                viewModel = viewModel,
                onOrientationShouldUnlock = {
                    applyPlaybackOrientation()
                    hideSystemUI()
                },
                onReturnToIdle = {
                    applyIdleOrientation()
                    showSystemUI()
                }
            )
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Re-hide system UI if another app/dialog temporarily took focus.
        if (hasFocus) hideSystemUI()
    }

    /**
     * Called when the user presses the Home button.
     * Auto-enter PiP if a file is currently loaded (status is Playing or Paused).
     */
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val status = viewModel.state.value.status
        val isFileLoaded = status !is com.tapman104.mpvplayer.state.PlayerStatus.Idle
        if (isFileLoaded) {
            viewModel.enterPip(this)
        }
    }

    /**
     * Called when the Activity enters or exits PiP mode.
     * Hide/show controls when entering PiP to keep the video clean.
     */
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: android.content.res.Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        // System handles the layout change automatically because we declared
        // screenSize|smallestScreenSize|screenLayout in configChanges.
    }
}
