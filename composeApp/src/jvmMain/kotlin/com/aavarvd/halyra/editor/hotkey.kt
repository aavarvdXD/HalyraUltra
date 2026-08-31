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
    onOpen: () -> Unit,
    onFind: () -> Unit,
    onFindReplace: () -> Unit,
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
        else -> false
    }
}
