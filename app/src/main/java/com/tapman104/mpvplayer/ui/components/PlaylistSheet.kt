package com.tapman104.mpvplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tapman104.mpvplayer.engine.RepeatMode
import com.tapman104.mpvplayer.vm.PlayerViewModel

private val AccentBlue = Color(0xFF80BFFF)
private val SurfaceColor = Color(0xFF1A1A2E)
private val SurfaceVariant = Color(0xFF232340)

/**
 * Modal bottom sheet showing the current playlist / queue.
 *
 * Current item is highlighted in accent blue.  Each row has Up/Down arrow
 * buttons for reordering and a remove (×) button.  The header row shows
 * shuffle and repeat toggles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistSheet(
    viewModel: PlayerViewModel,
    onDismiss: () -> Unit
) {
    val items         by viewModel.playlistItems.collectAsStateWithLifecycle()
    val currentIndex  by viewModel.playlistCurrentIndex.collectAsStateWithLifecycle()
    val isShuffled    by viewModel.isShuffled.collectAsStateWithLifecycle()
    val repeatMode    by viewModel.repeatMode.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        tonalElevation = 0.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Queue  (${items.size})",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                // Shuffle
                IconButton(onClick = { viewModel.toggleShuffle() }) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffled) AccentBlue else Color.White.copy(alpha = 0.5f)
                    )
                }

                // Repeat
                IconButton(onClick = { viewModel.cycleRepeatMode() }) {
                    Icon(
                        imageVector = when (repeatMode) {
                            RepeatMode.One -> Icons.Default.RepeatOne
                            else           -> Icons.Default.Repeat
                        },
                        contentDescription = "Repeat",
                        tint = when (repeatMode) {
                            RepeatMode.None -> Color.White.copy(alpha = 0.5f)
                            else            -> AccentBlue
                        }
                    )
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Queue is empty",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                ) {
                    itemsIndexed(items) { index, item ->
                        val isCurrent = index == currentIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isCurrent) AccentBlue.copy(alpha = 0.12f)
                                    else Color.Transparent
                                )
                                .clickable { viewModel.playlistJumpTo(index) }
                                .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Playing indicator
                            if (isCurrent) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Now playing",
                                    tint = AccentBlue,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(end = 2.dp)
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    color = Color.White.copy(alpha = 0.35f),
                                    fontSize = 12.sp,
                                    modifier = Modifier.width(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Title
                            Text(
                                text = item.title,
                                color = if (isCurrent) AccentBlue else Color.White,
                                fontSize = 14.sp,
                                fontWeight = if (isCurrent) FontWeight.Medium else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            // Reorder up
                            IconButton(
                                onClick = { viewModel.playlistMoveUp(index) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.ArrowUpward,
                                    contentDescription = "Move up",
                                    tint = Color.White.copy(alpha = if (index > 0) 0.6f else 0.2f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Reorder down
                            IconButton(
                                onClick = { viewModel.playlistMoveDown(index) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.ArrowDownward,
                                    contentDescription = "Move down",
                                    tint = Color.White.copy(alpha = if (index < items.size - 1) 0.6f else 0.2f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Remove
                            IconButton(
                                onClick = { viewModel.playlistRemove(index) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
