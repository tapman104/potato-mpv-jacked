package com.tapman104.mpvplayer.util

import android.content.Context
import android.net.Uri

object UriResolver {
    fun resolve(context: Context, uri: Uri): String {
        return when (uri.scheme) {
            "content" -> {
                // Open as file descriptor — mpv handles fd:// natively
                val fd = context.contentResolver
                    .openFileDescriptor(uri, "r") ?: return uri.toString()
                "fd://${fd.detachFd()}"
            }
            "file" -> uri.path ?: uri.toString()
            else -> uri.toString()
        }
    }
}
