package com.aavarvd.halyra.editor.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.*
import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.draw.clip

import com.aavarvd.halyra.ui.AppFonts

@Composable
fun FindBar(
    query: String,
    onQueryChange: (String) -> Unit,
    replaceMode: Boolean = false,
    replaceText: String = "",
    onReplaceTextChange: (String) -> Unit = {},
    onClose: () -> Unit,
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onReplace: () -> Unit = {},
    onReplaceAll: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val queryFocusRequester = remember { FocusRequester() }
    val replaceFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        queryFocusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF3C3F41))
            .padding(8.dp)
    ) {
        // Find row
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Find:",
                color = Color(0xFFBBBBBB),
                fontFamily = AppFonts.Inter,
                fontSize = 12.sp,
                modifier = Modifier.width(50.dp)
            )

            Spacer(Modifier.width(4.dp))

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = AppFonts.JBMono
                ),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                modifier = Modifier
                    .width(200.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF2B2B2B))
                    .focusRequester(queryFocusRequester)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .onPreviewKeyEvent { event ->
                        when {
                            event.type == KeyEventType.KeyDown && event.key == Key.Enter && event.isShiftPressed -> {
                                onPrevious()
                                true
                            }
                            event.type == KeyEventType.KeyDown && event.key == Key.Enter -> {
                                onNext()
                                true
                            }
                            event.type == KeyEventType.KeyDown && event.key == Key.Escape -> {
                                onClose()
                                true
                            }
                            event.type == KeyEventType.KeyDown && event.key == Key.Tab && replaceMode -> {
                                replaceFocusRequester.requestFocus()
                                true
                            }
                            event.type == KeyEventType.KeyDown && event.key == Key.Backspace -> {
                                // Handle backspace within the field, don't propagate
                                false
                            }
                            else -> false
                        }
                    }
            )

            Spacer(Modifier.width(4.dp))

            TextButton(
                onClick = onPrevious,
                modifier = Modifier.padding(0.dp)
            ) {
                Text("↑", color = Color(0xFFBBBBBB), fontSize = 12.sp)
            }

            TextButton(
                onClick = onNext,
                modifier = Modifier.padding(0.dp)
            ) {
                Text("↓", color = Color(0xFFBBBBBB), fontSize = 12.sp)
            }

            if (replaceMode) {
                TextButton(
                    onClick = onReplace,
                    modifier = Modifier.padding(0.dp)
                ) {
                    Text("Replace", color = Color(0xFFBBBBBB), fontSize = 10.sp)
                }

                TextButton(
                    onClick = onReplaceAll,
                    modifier = Modifier.padding(0.dp)
                ) {
                    Text("All", color = Color(0xFFBBBBBB), fontSize = 10.sp)
                }
            }

            Spacer(Modifier.weight(1f))

            TextButton(
                onClick = onClose,
                modifier = Modifier.padding(0.dp)
            ) {
                Text("✕", color = Color(0xFFBBBBBB), fontSize = 12.sp)
            }
        }

        // Replace row (only shown in replace mode)
        if (replaceMode) {
            Spacer(Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Replace:",
                    color = Color(0xFFBBBBBB),
                    fontFamily = AppFonts.Inter,
                    fontSize = 12.sp,
                    modifier = Modifier.width(50.dp)
                )

                Spacer(Modifier.width(4.dp))

                BasicTextField(
                    value = replaceText,
                    onValueChange = onReplaceTextChange,
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = AppFonts.JBMono
                    ),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                    modifier = Modifier
                        .width(200.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF2B2B2B))
                        .focusRequester(replaceFocusRequester)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .onPreviewKeyEvent { event ->
                            when {
                                event.type == KeyEventType.KeyDown && event.key == Key.Enter && event.isCtrlPressed -> {
                                    onReplaceAll()
                                    true
                                }
                                event.type == KeyEventType.KeyDown && event.key == Key.Enter -> {
                                    onReplace()
                                    true
                                }
                                event.type == KeyEventType.KeyDown && event.key == Key.Escape -> {
                                    onClose()
                                    true
                                }
                                event.type == KeyEventType.KeyDown && event.key == Key.Backspace -> {
                                    // Handle backspace within the field, don't propagate
                                    false
                                }
                                else -> false
                            }
                        }
                )
            }
        }
    }
}
