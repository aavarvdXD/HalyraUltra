package com.aavarvd.halyra

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.focusable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll

import androidx.compose.ui.unit.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.WindowState

import java.io.File

import androidx.compose.material.*
import androidx.compose.runtime.*
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
        caretBottomPx > currentScroll + viewportHeight -> caretBottomPx - viewportHeight
        else -> null
    }
}

@Composable
fun App(windowState: WindowState) {
    val density = LocalDensity.current
    val minTerminalHeightPx = with(density) {60.dp.toPx()}
    val maxTerminalHeightPx = with(density) {420.dp.toPx()}

    var terminalHeightPx by remember { mutableStateOf(with(density) { 160.dp.toPx() }) }
    var currentFile by remember { mutableStateOf<File?>(null) }
    var text by remember { mutableStateOf(TextFieldValue(""))}
    var output by remember { mutableStateOf("") }
    var editorViewportHeightPx by remember { mutableStateOf(0) }
    var editorTextLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val editorScrollState = rememberScrollState()
    val editorVerticalPadding = 8.dp
    val editorVerticalPaddingPx = with(density) { editorVerticalPadding.toPx() }
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
            editorScrollState.animateScrollTo(scrollTarget.coerceIn(0, editorScrollState.maxValue))
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

            AppTitleBar(windowState)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onPreviewKeyEvent { event ->
                        AppHotkeys(
                            event,
                            onSave = {
                                if (currentFile != null) {
                                    saveFile(currentFile!!, text.text)
                                } else {
                                    currentFile = saveFileAs(text.text)
                                }
                            },
                            onRun = {
                                currentFile?.let { file ->
                                    output = runPython(file)
                                }
                            },
                            onNew = {
                                currentFile = null
                                text = TextFieldValue("")
                                output = ""
                            },
                            onOpen = {
                                openFile()?.let { (file, content) ->
                                    currentFile = file
                                    text = TextFieldValue(content)
                                }
                            }
                        )
                    }
                    .focusable()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF3C3F41))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppButton("Run") {
                        currentFile?.let { file ->
                            output = runPython(file)
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    AppButton("Open") {
                        openFile()?.let { (file, content) ->
                            currentFile = file
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
                        currentFile = saveFileAs(text.text)
                    }

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = currentFile?.name ?: "No file Selected",
                        color = Color(0xFFBBBBBB),
                        fontFamily = AppFonts.Inter
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF2B2B2B))
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

                    // Editor
                    CompositionLocalProvider(LocalTextSelectionColors provides editorSelectionColors) {
                        BasicTextField(
                            value = text,
                            onValueChange = { text = it },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color(0xFF2B2B2B))
                                .onSizeChanged { editorViewportHeightPx = it.height }
                                .verticalScroll(editorScrollState)
                                .padding(
                                    start = 4.dp,
                                    top = editorVerticalPadding,
                                    end = 8.dp,
                                    bottom = editorVerticalPadding
                                ),
                            textStyle = editorTextStyle,
                            cursorBrush = SolidColor(Color.White),
                            onTextLayout = { editorTextLayout = it },
                            singleLine = false,
                        )
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

                                terminalHeightPx = (terminalHeightPx - dragAmount.y)
                                    .coerceIn(minTerminalHeightPx, maxTerminalHeightPx)
                            }
                        }
                )

                // Console
                Text(
                    text = output,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(with(density) { terminalHeightPx.toDp() })
                        .background(Color.Black)
                        .padding(8.dp),
                    color = Color(0xFFCCCCCC),
                    fontFamily = AppFonts.JBMono
                )
            }
        }
    }
}