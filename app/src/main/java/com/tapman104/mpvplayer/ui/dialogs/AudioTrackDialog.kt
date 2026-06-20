package com.tapman104.mpvplayer.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tapman104.mpvplayer.state.AudioTrack

@Composable
fun AudioTrackDialog(
    tracks: List<AudioTrack>,
    selectedTrackId: Int,
    onTrackSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E), RoundedCornerShape(16.dp))
                .padding(vertical = 8.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Text(
                text = "Audio Track",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

            if (tracks.isEmpty()) {
                Text(
                    text = "No audio tracks found",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(20.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(tracks) { track ->
                        val isSelected = track.id == selectedTrackId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onTrackSelected(track.id)
                                    onDismiss()
                                }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    color = if (isSelected) Color(0xFF80BFFF) else Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                                )
                                if (track.lang.isNotBlank()) {
                                    Text(
                                        text = track.lang.uppercase(),
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color(0xFF80BFFF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                    }
                }
            }

            // ── Dismiss button ────────────────────────────────────────────────
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 12.dp, bottom = 4.dp)
            ) {
                Text("Close", color = Color(0xFF80BFFF))
            }
        }
    }
}
