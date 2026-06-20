package com.tapman104.mpvplayer.subtitle

import android.content.Context
import androidx.preference.PreferenceManager

/**
 * Simple SharedPreferences-backed repository for subtitle defaults.
 *
 * Currently persists the default subtitle scale so it can be restored
 * when a new file is loaded.
 */
class SubtitlePrefsRepository(context: Context) {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    fun getDefaultScale(): Float {
        return prefs.getFloat(KEY_SCALE, DEFAULT_SCALE)
    }

    fun saveDefaultScale(scale: Float) {
        prefs.edit().putFloat(KEY_SCALE, scale).apply()
    }

    companion object {
        private const val KEY_SCALE    = "subtitle_scale"
        private const val DEFAULT_SCALE = 1.0f
    }
}
