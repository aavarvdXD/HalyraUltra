package com.aavarvd.halyra

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

    // Indenting
    if (
        event.type == KeyEventType.KeyDown &&
        event.key == Key.Backspace
    ) {
        val cursor = text.selection.start
        val fullText = text.text

        if (cursor > 0) {
            val beforeCursor = fullText.substring(0, cursor)
            val afterCursor = fullText.substring(text.selection.end)

            val lastChunk = beforeCursor.takeLast(4)

            val isOnlySpaces = lastChunk.all { it == ' ' }

            val deleteCount =
                if (isOnlySpaces) lastChunk.length
                else 1

            val newCursor = cursor - deleteCount

            val newText =
                beforeCursor.dropLast(deleteCount) + afterCursor

            onTextChange(
                TextFieldValue(
                    text = newText,
                    selection = TextRange(newCursor)
                )
            )
        }
        return true
    }

    // Save
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

    // Open
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

    // Run
    if (
        event.type == KeyEventType.KeyDown &&
        event.key == Key.F5
    ) {
        onRun()
        return true
    }

    // New
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
        event.key == Key.Tab
    ) {
        val tab = "    "

        val newValue = TextFieldValue(
            text = text.text.substring(0, text.selection.min) +
                   tab +
                   text.text.substring(text.selection.end),
            selection = TextRange(text.selection.start + tab.length)
        )

        onTextChange(newValue)
        return true
    }

    return false
}