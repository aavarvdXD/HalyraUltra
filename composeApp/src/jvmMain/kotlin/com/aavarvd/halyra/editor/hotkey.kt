package com.aavarvd.halyra.editor

import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

fun AppHotkeys(
    event: KeyEvent,
    text: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    onSave: ()                     -> Unit,
    onRun: ()                      -> Unit,
    onNew: ()                      -> Unit,
    onOpen: ()                     -> Unit,
    onFind: ()                     -> Unit,
    onFindReplace: ()              -> Unit,
): Boolean {
    if (event.type != KeyEventType.KeyDown) return false
    val ctrlOnly = event.isCtrlPressed && !event.isAltPressed && !event.isShiftPressed

    return when {
        ctrlOnly -> when (event.key) {
            Key.S -> { onSave();        true }
            Key.O -> { onOpen();        true }
            Key.F -> { onFind();        true }
            Key.H -> { onFindReplace(); true }
            Key.N -> { onNew();         true }
            else -> false
        }
        event.key == Key.Tab -> {
            onTextChange(if (event.isShiftPressed) dedentSelection(text) else indentSelection(text))
            true
        }
        event.key == Key.F5 -> { onRun(); true }
        event.key == Key.Backspace -> {
            val selectionStart = minOf(text.selection.start, text.selection.end)
            val selectionEnd = maxOf(text.selection.start, text.selection.end)

            if (selectionStart != selectionEnd) onTextChange(TextFieldValue(text.text.removeRange(selectionStart, selectionEnd), TextRange(selectionStart)))
            else
                if (selectionStart > 0) {
                    val lastChunk = text.text.substring(0, selectionStart).takeLast(4)
                    val deleteCount = if (lastChunk.length == 4 && lastChunk.all { it == ' ' }) 4 else 1
                    onTextChange(TextFieldValue(text.text.removeRange(selectionStart - deleteCount, selectionStart), TextRange(selectionStart - deleteCount)))
                }
                true
        }
        else -> false
    }
}
