package com.example.videoplayer.presentation.gallery

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun Modifier.gridSwipeSelection(
    enabled: Boolean,
    gridState: LazyGridState,
    scope: CoroutineScope,
    onMediaAtPosition: (Long, isStart: Boolean) -> Unit,
    onDragFinished: () -> Unit,
): Modifier {
    if (!enabled) return this
    return pointerInput(gridState, enabled) {
        detectDragGestures(
            onDragStart = { offset ->
                mediaIdAt(gridState, offset)?.let { onMediaAtPosition(it, true) }
            },
            onDrag = { change, _ ->
                change.consume()
                mediaIdAt(gridState, change.position)?.let { onMediaAtPosition(it, false) }
                val layoutInfo = gridState.layoutInfo
                val viewportEnd = layoutInfo.viewportEndOffset
                val viewportStart = layoutInfo.viewportStartOffset
                val y = change.position.y
                when {
                    y > viewportEnd - 120f -> scope.launch { gridState.animateScrollBy(40f) }
                    y < viewportStart + 120f -> scope.launch { gridState.animateScrollBy(-40f) }
                }
            },
            onDragEnd = onDragFinished,
            onDragCancel = onDragFinished,
        )
    }
}

private fun mediaIdAt(gridState: LazyGridState, position: Offset): Long? {
    val item = gridState.layoutInfo.visibleItemsInfo.firstOrNull { info ->
        position.x >= info.offset.x &&
            position.x <= info.offset.x + info.size.width &&
            position.y >= info.offset.y &&
            position.y <= info.offset.y + info.size.height
    } ?: return null
    val key = item.key as? String ?: return null
    if (!key.startsWith("cell-")) return null
    return key.removePrefix("cell-").toLongOrNull()
}
