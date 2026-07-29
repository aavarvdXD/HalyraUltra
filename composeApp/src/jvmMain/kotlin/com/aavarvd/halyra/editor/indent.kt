package com.aavarvd.halyra.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

fun indentSelection(
    value: TextFieldValue,
    indent: String = "    "
): TextFieldValue {
    val text = value.text
    val selection = value.selection

    if (selection.collapsed) {
        val cursor = selection.start
        val newText = text.substring(0, cursor) + indent + text.substring(cursor)

        return TextFieldValue(
            text = newText,
            selection = TextRange(cursor + indent.length)
        )
    }

    val linesToIndent = getLinesTouchedByRange(
        text = text,
        rangeStart = minOf(selection.start, selection.end),
        rangeEnd = maxOf(selection.start, selection.end)
    )

    if (linesToIndent.isEmpty()) {
        return value
    }

    val lineStarts = lineStartOffsets(text)
    val newText = buildString(text.length + linesToIndent.size * indent.length) {
        var previousOffset = 0

        for (lineIndex in linesToIndent) {
            val lineStart = lineStarts[lineIndex]
            append(text, previousOffset, lineStart)
            append(indent)
            previousOffset = lineStart
        }

        append(text, previousOffset, text.length)
    }

    fun adjustedOffset(offset: Int): Int {
        val insertedBeforeOrAtOffset = linesToIndent.count { lineStarts[it] <= offset }
        return offset + insertedBeforeOrAtOffset * indent.length
    }

    return TextFieldValue(
        text = newText,
        selection = TextRange(
            start = adjustedOffset(selection.start),
            end = adjustedOffset(selection.end)
        )
    )
}

fun dedentSelection(
    value: TextFieldValue,
    indentSize: Int = 4
): TextFieldValue {
    val text = value.text
    val selection = value.selection
    val cursor = selection.start

    val linesToDedent = if (selection.collapsed) {
        listOf(lineIndexForOffset(text, cursor))
    } else {
        getLinesTouchedByRange(
            text = text,
            rangeStart = minOf(selection.start, selection.end),
            rangeEnd = maxOf(selection.start, selection.end)
        )
    }

    if (linesToDedent.isEmpty()) {
        return value
    }

    val lines = text.split("\n").toMutableList()
    val lineStarts = lineStartOffsets(text)
    val removedByLine = IntArray(lines.size)

    for (lineIndex in linesToDedent) {
        val (newLine, charsRemoved) = removeIndentFromLine(lines[lineIndex], indentSize)
        lines[lineIndex] = newLine
        removedByLine[lineIndex] = charsRemoved
    }

    fun adjustedOffset(offset: Int): Int {
        var removed = 0

        for (lineIndex in linesToDedent) {
            val lineStart = lineStarts[lineIndex]
            if (offset <= lineStart) {
                continue
            }

            removed += minOf(removedByLine[lineIndex], offset - lineStart)
        }

        return (offset - removed).coerceAtLeast(0)
    }

    return TextFieldValue(
        text = lines.joinToString("\n"),
        selection = TextRange(
            start = adjustedOffset(selection.start),
            end = adjustedOffset(selection.end)
        )
    )
}

private fun removeIndentFromLine(line: String, indentSize: Int): Pair<String, Int> {
    if (line.isEmpty()) {
        return line to 0
    }

    val leadingSpaces = line.takeWhile { it == ' ' }
    val leadingTabs = line.takeWhile { it == '\t' }

    return when {
        leadingSpaces.isNotEmpty() && leadingTabs.isEmpty() -> {
            val removeCount = leadingSpaces.length.coerceAtMost(indentSize)
            line.drop(removeCount) to removeCount
        }

        leadingTabs.isNotEmpty() && leadingSpaces.isEmpty() -> {
            line.drop(1) to 1
        }

        leadingSpaces.isNotEmpty() -> {
            val removeCount = leadingSpaces.length.coerceAtMost(indentSize)
            line.drop(removeCount) to removeCount
        }

        else -> line to 0
    }
}

private fun getLinesTouchedByRange(text: String, rangeStart: Int, rangeEnd: Int): List<Int> {
    if (text.isEmpty()) {
        return if (rangeStart == 0) listOf(0) else emptyList()
    }

    val lines = mutableListOf<Int>()
    var currentOffset = 0
    var lineIndex = 0

    while (currentOffset <= text.length) {
        val lineEnd = text.indexOf('\n', currentOffset).let {
            if (it == -1) text.length else it
        }
        val effectiveLineEnd = if (lineEnd < text.length) lineEnd + 1 else lineEnd

        if (currentOffset <= rangeEnd && effectiveLineEnd >= rangeStart) {
            lines += lineIndex
        }

        if (lineEnd >= text.length) {
            break
        }

        currentOffset = lineEnd + 1
        lineIndex++
    }

    return lines
}

private fun lineStartOffsets(text: String): List<Int> {
    val offsets = mutableListOf(0)

    text.forEachIndexed { index, character ->
        if (character == '\n') {
            offsets += index + 1
        }
    }

    return offsets
}

private fun lineIndexForOffset(text: String, offset: Int): Int {
    val clampedOffset = offset.coerceIn(0, text.length)
    var lineIndex = 0

    for (index in 0 until clampedOffset) {
        if (text[index] == '\n') {
            lineIndex++
        }
    }

    return lineIndex
}
