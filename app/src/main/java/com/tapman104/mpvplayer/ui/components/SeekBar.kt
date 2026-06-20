package com.tapman104.mpvplayer.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapman104.mpvplayer.util.formatMs

data class ChapterMark(val positionMs: Long, val title: String)

@Composable
fun PlayerSeekBar(
    positionMs: Long,
    durationMs: Long,
    bufferedMs: Long = 0L,
    chapters: List<ChapterMark> = emptyList(),
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableStateOf(0f) }

    val currentFraction = if (isDragging) dragFraction else {
        if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    }
    val bufferedFraction = if (durationMs > 0) (bufferedMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val thumbRadius by animateDpAsState(targetValue = if (isDragging) 9.dp else 6.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(durationMs) {
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val downX = down.position.x
                            var didDrag = false

                            do {
                                val event = awaitPointerEvent()
                                val drag = event.changes.firstOrNull() ?: break
                                val deltaX = drag.position.x - downX

                                if (!didDrag && kotlin.math.abs(deltaX) > 8.dp.toPx()) {
                                    didDrag = true
                                    isDragging = true
                                }

                                if (didDrag) {
                                    dragFraction = (drag.position.x / size.width).coerceIn(0f, 1f)
                                    drag.consume()
                                }
                            } while (event.changes.any { it.pressed })

                            if (didDrag) {
                                isDragging = false
                                onSeek((dragFraction * durationMs).toLong())
                            } else {
                                // tap
                                val fraction = (downX / size.width).coerceIn(0f, 1f)
                                onSeek((fraction * durationMs).toLong())
                            }
                        }
                    }
                }
        ) {
            val totalWidth = size.width
            val trackHeight = 3.dp.toPx()
            val centerY = size.height / 2f
            val cornerRadius = CornerRadius(trackHeight / 2f)

            // 1. Background track
            drawRoundRect(
                color = Color.White.copy(alpha = 0.25f),
                topLeft = Offset(0f, centerY - trackHeight / 2f),
                size = Size(totalWidth, trackHeight),
                cornerRadius = cornerRadius
            )

            // 2. Buffered track
            if (bufferedFraction > 0f) {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.45f),
                    topLeft = Offset(0f, centerY - trackHeight / 2f),
                    size = Size(bufferedFraction * totalWidth, trackHeight),
                    cornerRadius = cornerRadius
                )
            }

            // 3. Active track
            if (currentFraction > 0f) {
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(0f, centerY - trackHeight / 2f),
                    size = Size(currentFraction * totalWidth, trackHeight),
                    cornerRadius = cornerRadius
                )
            }

            // 4. Chapter marks
            val chapterWidth = 2.dp.toPx()
            val chapterHeight = 8.dp.toPx()
            chapters.forEach { chapter ->
                val chapterFraction = if (durationMs > 0) (chapter.positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
                val chapterX = chapterFraction * totalWidth
                drawRect(
                    color = Color.White.copy(alpha = 0.7f),
                    topLeft = Offset(chapterX - chapterWidth / 2f, centerY - chapterHeight / 2f),
                    size = Size(chapterWidth, chapterHeight)
                )
            }

            // 5. Thumb
            val thumbX = currentFraction * totalWidth
            drawCircle(
                color = Color.White,
                radius = thumbRadius.toPx(),
                center = Offset(thumbX, centerY)
            )
        }

        // Time preview popup
        if (isDragging) {
            Layout(
                content = {
                    Box(
                        modifier = Modifier
                            .background(Color(0xCC000000), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = formatMs((dragFraction * durationMs).toLong()),
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { measurables, constraints ->
                val placeable = measurables.first().measure(constraints.copy(minWidth = 0, minHeight = 0))
                val thumbX = (dragFraction * constraints.maxWidth).toInt()
                var xPosition = thumbX - placeable.width / 2

                // Clamp so it never goes off the left or right edge
                if (xPosition < 0) xPosition = 0
                if (xPosition + placeable.width > constraints.maxWidth) {
                    xPosition = constraints.maxWidth - placeable.width
                }

                val trackCenterY = constraints.maxHeight / 2
                val yPosition = trackCenterY - 16.dp.roundToPx() - placeable.height

                layout(constraints.maxWidth, constraints.maxHeight) {
                    placeable.placeRelative(xPosition, yPosition)
                }
            }
        }
    }
}


