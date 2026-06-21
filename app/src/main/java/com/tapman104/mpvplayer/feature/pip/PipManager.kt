package com.tapman104.mpvplayer.feature.pip

import android.app.Activity
import android.app.PictureInPictureParams
import android.os.Build
import android.util.Rational

/**
 * Handles Picture-in-Picture entry and state queries.
 *
 * Instantiated by [com.tapman104.mpvplayer.ui.PlayerActivity] which passes itself
 * as the [activity] parameter.  The ViewModel supplies the video dimensions via
 * [PlayerState.videoWidth] / [PlayerState.videoHeight].
 */
class PipManager(private val activity: Activity) {

    /**
     * Attempts to enter Picture-in-Picture mode.
     *
     * Requires API 26+.  If [videoWidth] or [videoHeight] is 0 the aspect ratio
     * falls back to 16:9.  The rational is clamped to the valid range [1/2.39, 2.39/1]
     * that Android enforces.
     */
    fun enterPip(videoWidth: Int, videoHeight: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val w = videoWidth.takeIf { it > 0 } ?: 16
        val h = videoHeight.takeIf { it > 0 } ?: 9

        // Android requires the aspect ratio to be between 1/2.39 and 2.39/1.
        val maxDim = maxOf(w, h)
        val minDim = minOf(w, h)
        val ratio = if (minDim > 0) maxDim.toFloat() / minDim.toFloat() else 16f / 9f

        val (rW, rH) = if (ratio > 2.39f) {
            // Clamp to 2.39:1
            Pair(239, 100)
        } else {
            Pair(w, h)
        }

        val params = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(rW, rH))
            .build()

        activity.enterPictureInPictureMode(params)
    }

    /** Returns true when the hosting Activity is currently in PiP mode (API 24+). */
    fun isInPipMode(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            activity.isInPictureInPictureMode
        } else {
            false
        }
    }
}
