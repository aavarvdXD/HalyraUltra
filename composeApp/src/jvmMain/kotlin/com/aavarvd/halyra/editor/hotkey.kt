package com.aavarvd.halyra.editor

import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

fun AppHotkeys(
    event: KeyEvent,
    text: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    onSave: () -> Unit,
    onRun: () -> Unit,
    onNew: () -> Unit,
    onOpen: () -> Unit
): Boolean {
    if (
        event.type == KeyEventType.KeyDown &&
        event.key == Key.Backspace
    ) {
        val selectionStart = minOf(text.selection.start, text.selection.end)
        val selectionEnd = maxOf(text.selection.start, text.selection.end)

        if (selectionStart != selectionEnd) {
            onTextChange(
                TextFieldValue(
                    text.text.removeRange(selectionStart, selectionEnd),
                    selection = TextRange(selectionStart)
                )
            )
            return true
        }

        if (selectionStart > 0) {
            val beforeCursor = text.text.substring(0, selectionStart)
            val afterCursor = text.text.substring(selectionEnd)
            val lastChunk = beforeCursor.takeLast(4)
            val deleteCount = if (lastChunk.all { it == ' ' }) lastChunk.length else 1
            val newCursor = selectionStart - deleteCount

            onTextChange(
                TextFieldValue(
                    text = beforeCursor.dropLast(deleteCount) + afterCursor,
                    selection = TextRange(newCursor)
                )
            )
        }

        return true
    }

    if (
        event.type == KeyEventType.KeyDown &&
        event.key == Key.Tab &&
        event.isShiftPressed
    ) {
        onTextChange(dedentSelection(text))
        return true
    }

    if (
        event.type == KeyEventType.KeyDown &&
        event.isCtrlPressed &&
        !event.isAltPressed &&
        !event.isShiftPressed &&
        event.key == Key.S
    ) {
        onSave()
        return true
    }

    if (
        event.type == KeyEventType.KeyDown &&
        event.isCtrlPressed &&
        !event.isAltPressed &&
        !event.isShiftPressed &&
        event.key == Key.O
    ) {
        onOpen()
        return true
    }

    if (
        event.type == KeyEventType.KeyDown &&
        event.key == Key.F5
    ) {
        onRun()
        return true
    }

    if (
        event.type == KeyEventType.KeyDown &&
        event.isCtrlPressed &&
        !event.isAltPressed &&
        !event.isShiftPressed &&
        event.key == Key.N
    ) {
        onNew()
        return true
    }

    if (
        event.type == KeyEventType.KeyDown &&
        event.key == Key.Tab &&
        !event.isShiftPressed
    ) {
        onTextChange(indentSelection(text))
        return true
    }

    return false
}
