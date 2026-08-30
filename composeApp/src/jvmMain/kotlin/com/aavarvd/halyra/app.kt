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
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
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
import com.aavarvd.halyra.io.runPython
import com.aavarvd.halyra.io.saveFile
import com.aavarvd.halyra.io.saveFileAs

import com.aavarvd.halyra.ui.AppButton
import com.aavarvd.halyra.ui.AppFonts
import com.aavarvd.halyra.ui.AppTitleBar

import com.aavarvd.halyra.editor.search.SearchState
import com.aavarvd.halyra.editor.search.FindBar

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

import java.io.File

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
fun WindowScope.App(windowState: WindowState, useCustomTitlebar: Boolean) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val minTerminalHeightPx = with(density) { 60.dp.toPx() }
    val maxTerminalHeightPx = with(density) { 420.dp.toPx() }

    var searchState by remember { mutableStateOf(SearchState()) }
    var terminalHeightPx by remember {
        mutableStateOf(with(density) { 160.dp.toPx() })
    }
    var currentFile by remember { mutableStateOf<File?>(null) }
    var text by remember { mutableStateOf(TextFieldValue("")) }
    var output by remember { mutableStateOf("") }
    var pythonRunJob by remember { mutableStateOf<Job?>(null) }
    var editorViewportHeightPx by remember { mutableStateOf(0) }
    var editorTextLayout by remember { mutableStateOf<TextLayoutResult?>(null) }

    val editorScrollState = rememberScrollState()
    val editorVerticalPadding = 8.dp
    val editorVerticalPaddingPx = with(density) {
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

    fun runCurrentFile() {
        currentFile?.let { file ->
            pythonRunJob?.cancel()
            pythonRunJob = coroutineScope.launch {
                output = runPython(file)
            }
        }
    }

    LaunchedEffect(text.selection, editorTextLayout, editorViewportHeightPx) {
        val layout = editorTextLayout ?: return@LaunchedEffect
        if (editorViewportHeightPx <= 0) return@LaunchedEffect

        val layoutTextLength = layout.layoutInput.text.length
        if (text.text.length != layoutTextLength) return@LaunchedEffect

        val caretOffset = text.selection.end.coerceIn(0, layoutTextLength)
        val cursorRect = layout.getCursorRect(caretOffset)

        val scrollTarget = calculateScrollTargetForCaret(
            currentScroll = editorScrollState.value,
            viewportHeight = editorViewportHeightPx,
            caretTop = editorVerticalPaddingPx + cursorRect.top,
            caretBottom = editorVerticalPaddingPx + cursorRect.bottom,
        )

        if (scrollTarget != null) {
            editorScrollState.animateScrollTo(
                scrollTarget.coerceIn(0, editorScrollState.maxValue)
            )
        }
    }

    val lineCount = remember(text.text) {
        text.text.lineSequence().count().coerceAtLeast(1)
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
                AppTitleBar(windowState)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onPreviewKeyEvent { event ->
                        AppHotkeys(
                            event = event,
                            text = text,
                            onTextChange = { text = it },

                            onSave = {
                                if (currentFile != null) {
                                    saveFile(currentFile!!, text.text)
                                } else {
                                    currentFile = saveFileAs(text.text)
                                }
                            },

                            onRun = ::runCurrentFile,

                            onNew = {
                                pythonRunJob?.cancel()
                                currentFile = null
                                text = TextFieldValue("")
                                output = ""
                            },

                            onOpen = {
                                openFile()?.let { (file, content) ->
                                    pythonRunJob?.cancel()
                                    currentFile = file
                                    output = ""
                                    text = TextFieldValue(content)
                                }
                            },

                            onFind = {
                                searchState = searchState.copy(
                                    visible = true,
                                    replaceMode = false
                                )
                            },

                            onFindReplace = {
                                searchState = searchState.copy(
                                    visible = true,
                                    replaceMode = true
                                )
                            }
                        )
                    }
                    .focusable()
            ) {

                // =========================
                // Toolbar
                // =========================

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF3C3F41))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppButton("Run", ::runCurrentFile)

                    Spacer(Modifier.width(8.dp))

                    AppButton("Open") {
                        openFile()?.let { (file, content) ->
                            pythonRunJob?.cancel()
                            currentFile = file
                            output = ""
                            text = TextFieldValue(content)
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    AppButton("Save") {
                        if (currentFile != null) {
                            saveFile(currentFile!!, text.text)
                        } else {
                            currentFile = saveFileAs(text.text)
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    AppButton("Save As") {
                        saveFileAs(text.text)?.let {
                            currentFile = it
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = currentFile?.name ?: "No file Selected",
                        color = Color(0xFFBBBBBB),
                        fontFamily = AppFonts.Inter
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF2B2B2B))
                ) {

                    // Find / Replace bar
                    if (searchState.visible) {
                        FindBar(
                            query = searchState.query,
                            replaceMode = searchState.replaceMode,
                            replaceText = searchState.replaceText,

                            onQueryChange = { query ->
                                searchState = searchState.copy(
                                    query = query
                                )
                            },

                            onReplaceTextChange = { replaceText ->
                                searchState = searchState.copy(
                                    replaceText = replaceText
                                )
                            },

                            onClose = {
                                searchState = SearchState()
                            },

                            onNext = {
                                // TODO: Navigate to next match
                            },

                            onPrevious = {
                                // TODO: Navigate to previous match
                            },

                            onReplace = {
                                // TODO: Replace current match
                            },

                            onReplaceAll = {
                                // TODO: Replace all matches
                            }
                        )
                    }

                    // Actual editor
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {

                        // Gutter
                        Column(
                            modifier = Modifier
                                .width(48.dp)
                                .fillMaxHeight()
                                .background(Color(0xFF252525))
                                .padding(top = 8.dp, end = 8.dp)
                                .verticalScroll(editorScrollState),
                            horizontalAlignment = Alignment.End
                        ) {
                            for (i in 1..lineCount) {
                                Text(
                                    text = i.toString(),
                                    color = Color(0xFF666666),
                                    style = editorTextStyle.copy(
                                        color = Color(0xFF666666),
                                        textAlign = TextAlign.End
                                    )
                                )
                            }
                        }

                        // Text editor
                        CompositionLocalProvider(
                            LocalTextSelectionColors provides editorSelectionColors
                        ) {
                            BasicTextField(
                                value = text,
                                onValueChange = { newValue ->
                                    val oldText = text.text
                                    val newText = newValue.text
                                    val newSelection = newValue.selection

                                    // Handle auto-closing brackets
                                    if (newText.length == oldText.length + 1 && newSelection.collapsed) {
                                        val cursor = newSelection.start
                                        val insertedChar = if (cursor > 0) newText[cursor - 1] else null
                                        
                                        val closingChar = when (insertedChar) {
                                            '(' -> ')'
                                            '[' -> ']'
                                            '{' -> '}'
                                            '"' -> '"'
                                            '\'' -> '\''
                                            else -> null
                                        }
                                        
                                        if (closingChar != null) {
                                            val finalText = newText.substring(0, cursor) + 
                                                          closingChar + 
                                                          newText.substring(cursor)
                                            
                                            text = TextFieldValue(
                                                finalText,
                                                selection = TextRange(cursor)
                                            )
                                            return@BasicTextField
                                        }
                                    }

                                    val insertedNewLine =
                                        newText.length == oldText.length + 1 &&
                                                newSelection.start > 0 &&
                                                newText[newSelection.start - 1] == '\n'

                                    if (insertedNewLine) {
                                        val cursor = newSelection.start
                                        val beforeCursor =
                                            newText.substring(0, cursor)

                                        val lines = beforeCursor.split('\n')

                                        if (lines.size >= 2) {
                                            val previousLine =
                                                lines[lines.size - 2]

                                            val baseIndent =
                                                previousLine.takeWhile {
                                                    it == ' ' || it == '\t'
                                                }

                                            val extraIndent =
                                                if (previousLine.trimEnd().endsWith(":")) {
                                                    "    "
                                                } else {
                                                    ""
                                                }

                                            val indent =
                                                baseIndent + extraIndent

                                            val finalText =
                                                newText.substring(0, cursor) +
                                                        indent +
                                                        newText.substring(cursor)

                                            text = TextFieldValue(
                                                finalText,
                                                selection = TextRange(
                                                    cursor + indent.length
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
                                    .background(Color(0xFF2B2B2B))
                                    .onSizeChanged {
                                        editorViewportHeightPx = it.height
                                    }
                                    .verticalScroll(editorScrollState)
                                    .padding(
                                        start = 4.dp,
                                        top = editorVerticalPadding,
                                        end = 8.dp,
                                        bottom = editorVerticalPadding
                                    ),

                                textStyle = editorTextStyle,

                                cursorBrush = SolidColor(Color.White),

                                visualTransformation =
                                    PythonHighLightTransformation(),

                                onTextLayout = {
                                    editorTextLayout = it
                                },

                                singleLine = false,
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(Color(0xFF3C3F41))
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()

                                terminalHeightPx =
                                    (terminalHeightPx - dragAmount.y)
                                        .coerceIn(
                                            minTerminalHeightPx,
                                            maxTerminalHeightPx
                                        )
                            }
                        }
                )

                Text(
                    text = output,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            with(density) {
                                terminalHeightPx.toDp()
                            }
                        )
                        .background(Color.Black)
                        .padding(8.dp),
                    color = Color(0xFFCCCCCC),
                    fontFamily = AppFonts.JBMono
                )
            }
        }
    }
}
