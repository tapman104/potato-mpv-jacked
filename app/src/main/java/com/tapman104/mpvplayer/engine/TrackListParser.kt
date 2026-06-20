package com.tapman104.mpvplayer.engine

import android.util.Log
import com.tapman104.mpvplayer.state.AudioTrack
import com.tapman104.mpvplayer.state.SubtitleTrack
import `is`.xyz.mpv.MPVNode

object TrackListParser {

    fun parse(node: MPVNode): Pair<List<AudioTrack>, List<SubtitleTrack>> {
        val audioTracks = mutableListOf<AudioTrack>()
        val subtitleTracks = mutableListOf<SubtitleTrack>()
        try {
            val array = node.asArray() ?: return Pair(audioTracks, subtitleTracks)
            for (item in array) {
                val map = item.asMap() ?: continue
                val id = map["id"]?.asInt()?.toInt() ?: continue
                val type = map["type"]?.asString() ?: continue
                val title = map["title"]?.asString() ?: ""
                val lang = map["lang"]?.asString() ?: ""

                when (type) {
                    "audio" -> audioTracks.add(AudioTrack(id, title.ifEmpty { "Audio $id" }, lang))
                    "sub" -> subtitleTracks.add(SubtitleTrack(id, title.ifEmpty { "Subtitle $id" }, lang))
                }
            }
        } catch (e: Exception) {
            Log.e("MpvController", "track-list parse error", e)
        }
        return Pair(audioTracks, subtitleTracks)
    }
}
