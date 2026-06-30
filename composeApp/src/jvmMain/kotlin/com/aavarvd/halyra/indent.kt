package com.aavarvd.halyra

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

fun dedentSelection(
    value: TextFieldValue,
    indentSize: Int = 4
): TextFieldValue {
    val text = value.text
    val selection = value.selection

    val lines = text.split('\n').toMutableList()

    var currentOffset = 0
    var startLine = 0
    var endLine = 0

    for (i in lines.indices) {
        val lineStart = currentOffset
        val lineEnd = currentOffset + lines[i].length

        if (selection.start in lineStart..lineEnd)
            startLine = i

        if (selection.end in lineStart..lineEnd)
            endLine = i

        currentOffset = lineEnd + 1
    }

    var removedBeforeStart = 0
    var removedBeforeEnd = 0

    currentOffset = 0

    for (i in lines.indices) {

        val removable =
            lines[i]
                .takeWhile { it == ' ' }
                .length
                .coerceAtMost(indentSize)

        if (i in startLine..endLine && removable > 0) {
            lines[i] = lines[i].drop(removable)

            val lineStart = currentOffset

            if (lineStart < selection.start)
                removedBeforeStart += removable

            if (lineStart < selection.end)
                removedBeforeEnd+= removable
        }

        currentOffset += lines[i].length + 1
    }

    val newText = lines.joinToString(separator = "\n")

    return TextFieldValue(
        text = newText,
        selection = TextRange(
            start = (selection.start - removedBeforeStart).coerceAtLeast(0),
            end = (selection.end - removedBeforeEnd).coerceAtLeast(0)
        )
    )
}

fun indentSelection(
    value: TextFieldValue,
    indent: String = "    "
): TextFieldValue {
    val text = value.text
    val selection = value.selection

    if (selection.collapsed) {
        val newText =
            text.substring(0, selection.start) +
            indent +
            text.substring(selection.end)

        val newCursor = selection.start + indent.length

        return TextFieldValue(
            text = newText,
            selection = TextRange(newCursor)
        )
    }

    val lines = text.split('\n').toMutableList()

    var currentOffset = 0
    var endLine = 0
    var startLine = 0

    for (i in lines.indices) {
        val lineStart = currentOffset
        val lineEnd = currentOffset + lines[i].length

        if (selection.start in lineStart..lineEnd)
            startLine = i

        if (selection.end in lineStart..lineEnd)
            endLine = i

        currentOffset = lineEnd + 1
    }

    for (i in startLine..endLine)
        lines[i] = indent + lines[i]

    val newText = lines.joinToString(separator = "\n")

    val added = indent.length * (endLine - startLine + 1)

    return TextFieldValue(
        text = newText,
        selection = TextRange(
            selection.start + indent.length,
            selection.end + added
        )
    )
}