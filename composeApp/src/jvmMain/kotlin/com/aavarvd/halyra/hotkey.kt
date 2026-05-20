package com.aavarvd.halyra

import androidx.compose.ui.input.key.*

fun AppHotkeys(
    event: KeyEvent,
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

    return false
}