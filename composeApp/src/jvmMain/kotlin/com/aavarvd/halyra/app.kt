package com.aavarvd.halyra

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures

import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity


import java.io.File

import androidx.compose.material.*
import androidx.compose.runtime.*

import kotlin.math.max
import kotlin.math.min

@Composable
fun App() {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val minTerminalHeightPx = with(density) {60.dp.toPx()}
    val maxTerminalHeightPx = with(density) {420.dp.toPx()}

    var terminalHeightPx by remember { mutableStateOf(with(density) { 160.dp.toPx() }) }
    var currentFile by remember { mutableStateOf<File?>(null) }
    var text by remember { mutableStateOf(TextFieldValue(""))}
    var output by remember { mutableStateOf("") }
    MaterialTheme(
        colors = darkColors(
            primary = Color(0xFF3C3F41),
            background = Color(0xFF2B2B2B),
            surface = Color(0xFF2B2B2B),
            onPrimary = Color.White,
            onBackground = Color(0xFFBBBBBB)
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF3C3F41))
                    .padding(8.dp)
            ) {
                Button(
                    onClick = {
                        currentFile?.let { file ->
                            output = runPython(file)
                        }
                    },
                    elevation = ButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp,
                        hoveredElevation = 0.dp,
                        focusedElevation = 0.dp
                    )
                ) {
                    Text("Run")
                }
                Button(
                    onClick = {
                        openFile()?.let { (file, content) ->
                            currentFile = file
                            text = TextFieldValue(content)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF3C3F41),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp,
                        hoveredElevation = 0.dp,
                        focusedElevation = 0.dp
                    )
                ) {
                    Text("Open")
                }

                Spacer(Modifier.width(8.dp))

                Button(onClick = {
                    if (currentFile != null) {
                        saveFile(currentFile!!, text.text)
                    } else {
                        currentFile = saveFileAs(text.text)
                    }
                },
                    elevation = ButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp,
                        hoveredElevation = 0.dp,
                        focusedElevation = 0.dp
                    )
                ) {
                    Text("Save")
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = {
                        currentFile = saveFileAs(text.text)
                    },
                    elevation = ButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp,
                        hoveredElevation = 0.dp,
                        focusedElevation = 0.dp
                    )
                ) {
                    Text("Save As")
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = currentFile?.name ?: "No file",
                    color = androidx.compose.ui.graphics.Color(0xFFBBBBBB),
                    fontFamily = FontFamily.Monospace
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
                    fontFamily = FontFamily.Monospace
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
                fontFamily = FontFamily.Monospace
            )
        }
    }
}