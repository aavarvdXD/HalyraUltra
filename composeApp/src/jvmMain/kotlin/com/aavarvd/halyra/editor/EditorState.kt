package com.aavarvd.halyra.editor

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.text.TextLayoutResult
import kotlin.math.roundToInt

class EditorState(
    val scrollState: ScrollState
) {
    var viewportHeightPx by mutableStateOf(0)
    var textLayout by mutableStateOf<TextLayoutResult?>(null)

    fun calculateScrollTargetForCaret(
        currentScroll: Int,
        viewportHeight: Int,
        caretTop: Float,
        caretBottom: Float,
    ): Int? {
        if (viewportHeight <= 0) return null

        val caretTopPx = caretTop.roundToInt().coerceAtLeast(0)
        val caretBottomPx = caretBottom.roundToInt().coerceAtLeast(caretTopPx)

        return when {
            caretTopPx < currentScroll -> caretTopPx
            caretBottomPx > currentScroll + viewportHeight ->
                caretBottomPx - viewportHeight
            else -> null
        }
    }
}

@Composable
fun rememberEditorState(scrollState: ScrollState): EditorState {
    return remember(scrollState) { EditorState(scrollState) }
}
