package com.aavarvd.halyra

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.focusable

import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalDensity

import java.io.File

import androidx.compose.material.*
import androidx.compose.runtime.*

@Composable
fun App() {
    val density = LocalDensity.current
    val minTerminalHeightPx = with(density) {60.dp.toPx()}
    val maxTerminalHeightPx = with(density) {420.dp.toPx()}

    var terminalHeightPx by remember { mutableStateOf(with(density) { 160.dp.toPx() }) }
    var currentFile by remember { mutableStateOf<File?>(null) }
    var text by remember { mutableStateOf(TextFieldValue(""))}
    var output by remember { mutableStateOf("") }

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
                    .padding(8.dp)
                    .weight(1f),
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
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF2B2B2B)),
                textStyle = LocalTextStyle.current.copy(
                    color = Color(0xFFBBBBBB),
                    fontFamily = AppFonts.JBMono
                ),
                colors = TextFieldDefaults.textFieldColors(
                    cursorColor = Color.White,
                    textColor = Color(0xFFBBBBBB),
                    backgroundColor = Color(0xFF2B2B2B)
                )
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color(0xFF3C3F41))
                    .pointerInput(Unit) {
                        detectDragGestures{ change, dragAmount ->
                            change.consume()

                            terminalHeightPx = (terminalHeightPx - dragAmount.y)
                                .coerceIn(minTerminalHeightPx, maxTerminalHeightPx)
                        }
                    }
            )
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