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
        val newText =
            text.substring(0, selection.start) +
            indent +
            text.substring(selection.end)

        return TextFieldValue(
            text = newText,
            selection = TextRange(selection.start + indent.length)
        )
    }

    val rangeStart = minOf(selection.start, selection.end)
    val rangeEnd = maxOf(selection.start, selection.end)

    val linesToIndent = getLinesTouchedByRange(text, rangeStart, rangeEnd)
    
    if (linesToIndent.isEmpty()) {
        return value
    }

    val lines = text.split("\n").toMutableList()
    var totalIndentAdded = 0
    
    for (lineIndex in linesToIndent) {
        lines[lineIndex] = indent + lines[lineIndex]
        totalIndentAdded += indent.length
    }

    val newText = lines.joinToString("\n")

    val newSelection = if (selection.collapsed) {
        val firstLineStart = getLineStartOffset(text, linesToIndent.first())
        TextRange(firstLineStart + indent.length)
    } else {
        TextRange(
            start = selection.start + totalIndentAdded,
            end = selection.end + totalIndentAdded
        )
    }

    return TextFieldValue(
        text = newText,
        selection = newSelection
    )
}

fun dedentSelection(
    value: TextFieldValue,
    indentSize: Int = 4
): TextFieldValue {
    val text = value.text
    val selection = value.selection

    val (rangeStart, rangeEnd) = if (selection.collapsed) {
        val cursorPos = selection.start
        val isAtLineStart = cursorPos == 0 || (cursorPos > 0 && text.getOrNull(cursorPos - 1) == '\n')

        if (isAtLineStart && cursorPos > 0) {
            val prevNewlinePos = text.lastIndexOf('\n', cursorPos - 1)
            val lineStart = prevNewlinePos + 1

            val nextNewlinePos = text.indexOf('\n', cursorPos)
            val lineEnd = if (nextNewlinePos == -1) text.length else nextNewlinePos

            val currentLine = text.substring(lineStart, lineEnd)
            val isCurrentLineBlank = currentLine.isBlank()

            if (isCurrentLineBlank) {
                lineStart to lineStart
            } else {
                val prevLineEnd = prevNewlinePos
                val prevLineStart = if (prevLineEnd >= 0) text.lastIndexOf('\n', prevLineEnd - 1) + 1 else 0
                prevLineStart to prevLineStart
            }
        } else {
            val lineRange = getLineRangeForOffset(text, selection.start)
            lineRange.first to lineRange.first
        }
    } else {
        minOf(selection.start, selection.end) to maxOf(selection.start, selection.end)
    }

    val linesToDedent = getLinesTouchedByRange(text, rangeStart, rangeEnd)
    
    if (linesToDedent.isEmpty()) {
        return value
    }

    val lines = text.split("\n").toMutableList()
    var totalCharsRemoved = 0
    
    for (lineIndex in linesToDedent) {
        val line = lines[lineIndex]
        val (newLine, charsRemoved) = removeIndentFromLine(line, indentSize)
        lines[lineIndex] = newLine
        totalCharsRemoved += charsRemoved
    }

    val newText = lines.joinToString("\n")

    val newSelection = if (selection.collapsed) {
        val firstLineStart = getLineStartOffset(text, linesToDedent.first())
        val newCursor = (selection.start - totalCharsRemoved).coerceAtLeast(firstLineStart)
        TextRange(newCursor)
    } else {
        TextRange(
            start = (selection.start - totalCharsRemoved).coerceAtLeast(0),
            end = (selection.end - totalCharsRemoved).coerceAtLeast(0)
        )
    }

    return TextFieldValue(
        text = newText,
        selection = newSelection
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
        val lineStart = currentOffset
        val effectiveLineEnd = if (lineEnd < text.length) lineEnd + 1 else lineEnd

        if (lineStart <= rangeEnd && effectiveLineEnd >= rangeStart) {
            lines.add(lineIndex)
        }

        if (lineEnd >= text.length) break

        currentOffset = lineEnd + 1
        lineIndex++
    }

    return lines
}

private fun getLineRangeForOffset(text: String, offset: Int): IntRange {
    if (text.isEmpty()) {
        return 0..0
    }

    val clampedOffset = offset.coerceIn(0, text.length)
    
    val lineStart = text.lastIndexOf('\n', clampedOffset - 1) + 1
    val lineEnd = text.indexOf('\n', clampedOffset).let {
        if (it == -1) text.length else it
    }

    return lineStart..lineEnd
}

private fun getLineStartOffset(text: String, lineIndex: Int): Int {
    if (lineIndex < 0) return 0
    if (text.isEmpty()) return 0

    var currentOffset = 0
    var currentLine = 0

    while (currentOffset < text.length) {
        if (currentLine == lineIndex) {
            return currentOffset
        }
        
        val nextNewline = text.indexOf('\n', currentOffset)
        if (nextNewline == -1) {
            return if (currentLine + 1 == lineIndex) text.length else 0
        }
        
        currentOffset = nextNewline + 1
        currentLine++
    }

    return if (currentLine == lineIndex) text.length else 0
}