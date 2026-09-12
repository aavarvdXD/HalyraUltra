package com.aavarvd.halyra.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aavarvd.halyra.ui.AppColors

@Composable
fun EditorLineNumbers(
    lineCount: Int,
    editorState: EditorState,
    textStyle: TextStyle
) {
    val lineNumbersText = remember(lineCount) {
        (1..lineCount).joinToString("\n")
    }

    Box(
        modifier = Modifier
            .width(48.dp)
            .fillMaxHeight()
            .background(AppColors.LineNumberBackground)
            .padding(top = 8.dp, end = 8.dp)
            .verticalScroll(editorState.scrollState)
    ) {
        Text(
            text = lineNumbersText,
            color = AppColors.LineNumber,
            style = textStyle.copy(
                color = AppColors.LineNumber,
                textAlign = TextAlign.End
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}