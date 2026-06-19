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