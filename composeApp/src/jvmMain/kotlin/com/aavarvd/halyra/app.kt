package com.aavarvd.halyra

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.*

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowState

import com.aavarvd.halyra.editor.AppHotkeys
import com.aavarvd.halyra.editor.PythonHighLightTransformation

import com.aavarvd.halyra.io.openFile
import com.aavarvd.halyra.io.PythonProcess
import com.aavarvd.halyra.io.saveFile
import com.aavarvd.halyra.io.saveFileAs

import com.aavarvd.halyra.ui.AppFonts
import com.aavarvd.halyra.ui.AppTitleBar
import com.aavarvd.halyra.ui.Sidebar

import com.aavarvd.halyra.editor.search.SearchState
import com.aavarvd.halyra.editor.search.FindBar

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

import java.io.File
import java.awt.Cursor

import kotlin.math.roundToInt

internal fun calculateScrollTargetForCaret(
    currentScroll: Int,
    viewportHeight: Int,
    caretTop: Float,
    caretBottom: Float,
): Int? {
    if (viewportHeight <= 0) return null

    val caretTopPx = caretTop.roundToInt().coerceAtLeast(0)
    val caretBottomPx = caretBottom.roundToInt().coerceAtLeast(caretTopPx)

    return when {
        caretTopPx < currentScroll -> caretTopPx

        caretBottomPx > currentScroll + viewportHeight ->
            caretBottomPx - viewportHeight

        else -> null
    }
}

@Composable
fun WindowScope.App(
    windowState: WindowState,
    useCustomTitlebar: Boolean,
    closeRequested: Boolean = false,
    onCloseRequest: () -> Unit,
    onCloseCancelled: () -> Unit = {}
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val minTerminalHeightPx =
        with(density) { 60.dp.toPx() }

    val maxTerminalHeightPx =
        with(density) { 420.dp.toPx() }

    var searchState by remember {
        mutableStateOf(SearchState())
    }

    var terminalHeightPx by remember {
        mutableStateOf(
            with(density) { 160.dp.toPx() }
        )
    }
    var currentFile by remember { mutableStateOf<File?>(null) }
    var text by remember { mutableStateOf(TextFieldValue("")) }
    var lastSavedText by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var terminalInput by remember { mutableStateOf("") }
    var pythonRunJob by remember { mutableStateOf<Job?>(null) }
    var pythonProcess by remember { mutableStateOf<PythonProcess?>(null) }

    var showExitConfirmation by remember { mutableStateOf(false) }

    val isModified = remember(text.text, lastSavedText, currentFile) {
        text.text != lastSavedText
    }

    LaunchedEffect(closeRequested) {
        if (closeRequested) {
            if (isModified) {
                showExitConfirmation = true
            } else {
                onCloseRequest()
            }
        }
    }

    var editorViewportHeightPx by remember {
        mutableStateOf(0)
    }

    var editorTextLayout by remember {
        mutableStateOf<TextLayoutResult?>(null)
    }

    val editorScrollState = rememberScrollState()

    val editorVerticalPadding = 8.dp

    val editorVerticalPaddingPx =
        with(density) {
            editorVerticalPadding.toPx()
        }

    val editorSelectionColors = remember {
        TextSelectionColors(
            handleColor = Color(0xFF4EA1FF),
            backgroundColor = Color(0x664EA1FF)
        )
    }

    val editorTextStyle = LocalTextStyle.current.copy(
        color = Color(0xFFBBBBBB),
        fontFamily = AppFonts.JBMono,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )

    fun stopPython() {
        pythonRunJob?.cancel()
        pythonRunJob = null

        pythonProcess?.stop()
        pythonProcess = null
    }

    fun newFile() {
        stopPython()

        currentFile = null
        text = TextFieldValue("")
        lastSavedText = ""
        output = ""
        terminalInput = ""
    }

    fun openFileFromDialog() {
        openFile()?.let { (file, content) ->
            stopPython()

            currentFile = file
            text = TextFieldValue(content)
            lastSavedText = content
            output = ""
            terminalInput = ""
        }
    }

    fun saveCurrentFile() {
        if (currentFile != null) {
            saveFile(
                currentFile!!,
                text.text
            )
            lastSavedText = text.text
        } else {
            saveFileAs(text.text)?.let {
                currentFile = it
                lastSavedText = text.text
            }
        }
    }

    fun saveCurrentFileAs() {
        saveFileAs(text.text)?.let {
            currentFile = it
            lastSavedText = text.text
        }
    }

    fun runCurrentFile() {
        stopPython()

        output = ""
        terminalInput = ""

        val process = PythonProcess(
            code = if (currentFile == null) text.text else null,
            file = currentFile,

            onOutput = { chunk ->
                coroutineScope.launch {
                    output += chunk
                }
            },

            onFinished = {
                coroutineScope.launch {
                    pythonProcess = null
                }
            }
        )

        pythonProcess = process

        pythonRunJob = coroutineScope.launch {
            process.start()
        }
    }

    LaunchedEffect(
        text.selection,
        editorTextLayout,
        editorViewportHeightPx
    ) {
        val layout = editorTextLayout
            ?: return@LaunchedEffect

        if (editorViewportHeightPx <= 0) {
            return@LaunchedEffect
        }

        val layoutTextLength =
            layout.layoutInput.text.length

        if (text.text.length != layoutTextLength) {
            return@LaunchedEffect
        }

        val caretOffset =
            text.selection.end.coerceIn(
                0,
                layoutTextLength
            )

        val cursorRect =
            layout.getCursorRect(caretOffset)

        val scrollTarget =
            calculateScrollTargetForCaret(
                currentScroll = editorScrollState.value,
                viewportHeight = editorViewportHeightPx,
                caretTop =
                    editorVerticalPaddingPx +
                            cursorRect.top,
                caretBottom =
                    editorVerticalPaddingPx +
                            cursorRect.bottom,
            )

        if (scrollTarget != null) {
            editorScrollState.animateScrollTo(
                scrollTarget.coerceIn(
                    0,
                    editorScrollState.maxValue
                )
            )
        }
    }

    val lineCount = remember(text.text) {
        text.text
            .lineSequence()
            .count()
            .coerceAtLeast(1)
    }

    MaterialTheme(
        colors = darkColors(
            primary = Color(0xFF3C3F41),
            background = Color(0xFF2B2B2B),
            surface = Color(0xFF2B2B2B),
            onPrimary = Color.White,
            onBackground = Color(0xFFBBBBBB)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            if (useCustomTitlebar) {
                AppTitleBar(windowState) {
                    if (isModified) {
                        showExitConfirmation = true
                    } else {
                        onCloseRequest()
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .onPreviewKeyEvent { event ->

                        AppHotkeys(
                            event = event,
                            text = text,

                            onTextChange = { text = it },
                            onSave = { saveCurrentFile() },
                            onRun = { runCurrentFile() },
                            onNew = { newFile() },
                            onOpen = { openFileFromDialog() },

                            onFind = {
                                if (
                                    searchState.visible &&
                                    !searchState.replaceMode
                                ) {
                                    searchState = SearchState()
                                } else {
                                    searchState =
                                        searchState.copy(
                                            visible = true,
                                            replaceMode = false
                                        )
                                }
                            },

                            onFindReplace = {
                                if (
                                    searchState.visible &&
                                    searchState.replaceMode
                                ) {
                                    searchState = SearchState()
                                } else {
                                    searchState =
                                        searchState.copy(
                                            visible = true,
                                            replaceMode = true
                                        )
                                }
                            }
                        )
                    }
                    .focusable()
            ) {

                Sidebar(
                    onNew = { newFile() },
                    onOpen = { openFileFromDialog() },
                    onRun = { runCurrentFile() },
                    onSave = { saveCurrentFile() }
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color(0xFF2B2B2B))
                ) {

                    if (searchState.visible) {
                        FindBar(
                            query = searchState.query,

                            replaceMode =
                                searchState.replaceMode,

                            replaceText =
                                searchState.replaceText,

                            onQueryChange = { query ->
                                searchState =
                                    searchState.copy(
                                        query = query
                                    )
                            },

                            onReplaceTextChange = {
                                    replaceText ->
                                searchState =
                                    searchState.copy(
                                        replaceText = replaceText
                                    )
                            },

                            onClose = { searchState = SearchState() },
                            onNext = {},
                            onPrevious = {},
                            onReplace = {},
                            onReplaceAll = {}
                        )
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier
                                .width(48.dp)
                                .fillMaxHeight()
                                .background(
                                    Color(0xFF252525)
                                )
                                .padding(
                                    top = 8.dp,
                                    end = 8.dp
                                )
                                .verticalScroll(
                                    editorScrollState
                                ),

                            horizontalAlignment =
                                Alignment.End
                        ) {
                            for (i in 1..lineCount) {
                                Text(
                                    text = i.toString(),

                                    color =
                                        Color(0xFF666666),

                                    style =
                                        editorTextStyle.copy(
                                            color =
                                                Color(0xFF666666),

                                            textAlign =
                                                TextAlign.End
                                        )
                                )
                            }
                        }

                        CompositionLocalProvider(
                            LocalTextSelectionColors provides
                                    editorSelectionColors
                        ) {
                            BasicTextField(
                                value = text,

                                onValueChange = { newValue ->

                                    val oldText =
                                        text.text

                                    val newText =
                                        newValue.text

                                    val newSelection =
                                        newValue.selection

                                    if (
                                        newText.length ==
                                        oldText.length + 1 &&
                                        newSelection.collapsed
                                    ) {
                                        val cursor =
                                            newSelection.start

                                        val insertedChar =
                                            if (cursor > 0) {
                                                newText[cursor - 1]
                                            } else {
                                                null
                                            }

                                        val closingChar =
                                            when (insertedChar) {
                                                '(' -> ')'
                                                '[' -> ']'
                                                '{' -> '}'
                                                '"' -> '"'
                                                '\'' -> '\''
                                                else -> null
                                            }

                                        if (
                                            closingChar != null
                                        ) {
                                            val finalText =
                                                newText.substring(
                                                    0,
                                                    cursor
                                                ) +
                                                        closingChar +
                                                        newText.substring(
                                                            cursor
                                                        )

                                            text =
                                                TextFieldValue(
                                                    finalText,
                                                    selection =
                                                        TextRange(
                                                            cursor
                                                        )
                                                )

                                            return@BasicTextField
                                        }
                                    }

                                    val insertedNewLine =
                                        newText.length ==
                                                oldText.length + 1 &&
                                                newSelection.start > 0 &&
                                                newText[
                                                    newSelection.start - 1
                                                ] == '\n'

                                    if (insertedNewLine) {

                                        val cursor =
                                            newSelection.start

                                        val beforeCursor =
                                            newText.substring(
                                                0,
                                                cursor
                                            )

                                        val lines =
                                            beforeCursor.split('\n')

                                        if (lines.size >= 2) {

                                            val previousLine =
                                                lines[
                                                    lines.size - 2
                                                ]

                                            val baseIndent =
                                                previousLine
                                                    .takeWhile {
                                                        it == ' ' ||
                                                                it == '\t'
                                                    }

                                            val extraIndent =
                                                if (
                                                    previousLine
                                                        .trimEnd()
                                                        .endsWith(":")
                                                ) {
                                                    "    "
                                                } else {
                                                    ""
                                                }

                                            val indent =
                                                baseIndent +
                                                        extraIndent

                                            val finalText =
                                                newText.substring(
                                                    0,
                                                    cursor
                                                ) +
                                                        indent +
                                                        newText.substring(
                                                            cursor
                                                        )

                                            text =
                                                TextFieldValue(
                                                    finalText,

                                                    selection =
                                                        TextRange(
                                                            cursor +
                                                                    indent.length
                                                        )
                                                )

                                            return@BasicTextField
                                        }
                                    }

                                    text = newValue
                                },

                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(
                                        Color(0xFF2B2B2B)
                                    )
                                    .onSizeChanged {
                                        editorViewportHeightPx =
                                            it.height
                                    }
                                    .verticalScroll(
                                        editorScrollState
                                    )
                                    .padding(
                                        start = 4.dp,
                                        top =
                                            editorVerticalPadding,
                                        end = 8.dp,
                                        bottom =
                                            editorVerticalPadding
                                    )
                                    .onPreviewKeyEvent { event ->

                                        if (
                                            event.type ==
                                            KeyEventType.KeyDown &&
                                            event.key ==
                                            Key.Backspace
                                        ) {

                                            val selectionStart =
                                                minOf(
                                                    text.selection.start,
                                                    text.selection.end
                                                )

                                            val selectionEnd =
                                                maxOf(
                                                    text.selection.start,
                                                    text.selection.end
                                                )

                                            if (
                                                selectionStart !=
                                                selectionEnd
                                            ) {
                                                text =
                                                    TextFieldValue(
                                                        text.text.removeRange(
                                                            selectionStart,
                                                            selectionEnd
                                                        ),

                                                        TextRange(
                                                            selectionStart
                                                        )
                                                    )
                                            } else if (
                                                selectionStart > 0
                                            ) {

                                                val lastChunk =
                                                    text.text
                                                        .substring(
                                                            0,
                                                            selectionStart
                                                        )
                                                        .takeLast(4)

                                                val deleteCount =
                                                    if (
                                                        lastChunk.length ==
                                                        4 &&
                                                        lastChunk.all {
                                                            it == ' '
                                                        }
                                                    ) {
                                                        4
                                                    } else {
                                                        1
                                                    }

                                                text =
                                                    TextFieldValue(
                                                        text.text.removeRange(
                                                            selectionStart -
                                                                    deleteCount,
                                                            selectionStart
                                                        ),

                                                        TextRange(
                                                            selectionStart -
                                                                    deleteCount
                                                        )
                                                    )
                                            }

                                            true
                                        } else {
                                            false
                                        }
                                    },

                                textStyle =
                                    editorTextStyle,

                                cursorBrush =
                                    SolidColor(Color.White),

                                visualTransformation =
                                    PythonHighLightTransformation(),

                                onTextLayout = {
                                    editorTextLayout = it
                                },

                                singleLine = false,
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .background(
                                Color(0xFF3C3F41)
                            )
                            .pointerHoverIcon(
                                PointerIcon(
                                    Cursor.getPredefinedCursor(
                                        Cursor.N_RESIZE_CURSOR
                                    )
                                )
                            )
                            .pointerInput(Unit) {
                                detectDragGestures {
                                        change,
                                        dragAmount ->

                                    change.consume()

                                    terminalHeightPx =
                                        (
                                                terminalHeightPx -
                                                        dragAmount.y
                                                ).coerceIn(
                                                minTerminalHeightPx,
                                                maxTerminalHeightPx
                                            )
                                }
                            }
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                with(density) {
                                    terminalHeightPx.toDp()
                                }
                            )
                            .background(Color.Black)
                            .padding(8.dp)
                    ) {

                        Text(
                            text = output,

                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(
                                    rememberScrollState()
                                ),

                            color =
                                Color(0xFFCCCCCC),

                            fontFamily =
                                AppFonts.JBMono
                        )

                        BasicTextField(
                            value = terminalInput,

                            onValueChange = {
                                terminalInput = it
                            },

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),

                            textStyle =
                                LocalTextStyle.current.copy(
                                    color =
                                        Color(0xFFCCCCCC),

                                    fontFamily =
                                        AppFonts.JBMono
                                ),

                            cursorBrush =
                                SolidColor(Color.White),

                            singleLine = true,

                            onTextLayout = {}
                        )
                    }
                }
            }
        }

        if (showExitConfirmation) {
            AlertDialog(
                onDismissRequest = { 
                    showExitConfirmation = false 
                    onCloseCancelled()
                },
                title = { Text("Unsaved Changes") },
                text = { Text("You have unsaved changes. Do you want to save before exiting?") },
                confirmButton = {
                    Button(
                        onClick = {
                            saveCurrentFile()
                            showExitConfirmation = false
                            onCloseRequest()
                        }
                    ) {
                        Text("Save and Exit")
                    }
                },
                dismissButton = {
                    Row {
                        Button(
                            onClick = {
                                showExitConfirmation = false
                                onCloseRequest()
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.Gray
                            )
                        ) {
                            Text("Exit Without Saving")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { 
                                showExitConfirmation = false 
                                onCloseCancelled()
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                },
                backgroundColor = Color(0xFF2B2B2B),
                contentColor = Color.White
            )
        }
    }
}