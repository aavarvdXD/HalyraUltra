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
                modifier = Modifier.width(60.dp)
            )

            Spacer(Modifier.width(8.dp))

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = AppFonts.JBMono
                ),
                modifier = Modifier
                    .width(300.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF2B2B2B))
                    .focusRequester(queryFocusRequester)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown) {
                            when {
                                event.key == Key.Enter && event.isShiftPressed -> {
                                    onPrevious()
                                    true
                                }
                                event.key == Key.Enter -> {
                                    onNext()
                                    true
                                }
                                event.key == Key.Escape -> {
                                    onClose()
                                    true
                                }
                                event.key == Key.Tab && replaceMode -> {
                                    replaceFocusRequester.requestFocus()
                                    true
                                }
                                else -> false
                            }
                        } else false
                    }
            )

            Spacer(Modifier.width(8.dp))

            TextButton(onClick = onPrevious) {
                Text("↑", color = Color(0xFFBBBBBB))
            }

            TextButton(onClick = onNext) {
                Text("↓", color = Color(0xFFBBBBBB))
            }

            Spacer(Modifier.width(8.dp))

            TextButton(onClick = onClose) {
                Text("Close", color = Color(0xFFBBBBBB))
            }
        }

        // Replace row (only shown in replace mode)
        if (replaceMode) {
            Spacer(Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Replace:",
                    color = Color(0xFFBBBBBB),
                    fontFamily = AppFonts.Inter,
                    modifier = Modifier.width(60.dp)
                )

                Spacer(Modifier.width(8.dp))

                BasicTextField(
                    value = replaceText,
                    onValueChange = onReplaceTextChange,
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = AppFonts.JBMono
                    ),
                    modifier = Modifier
                        .width(300.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF2B2B2B))
                        .focusRequester(replaceFocusRequester)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .onPreviewKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown) {
                                when {
                                    event.key == Key.Enter && event.isCtrlPressed -> {
                                        onReplaceAll()
                                        true
                                    }
                                    event.key == Key.Enter -> {
                                        onReplace()
                                        true
                                    }
                                    event.key == Key.Escape -> {
                                        onClose()
                                        true
                                    }
                                    else -> false
                                }
                            } else false
                        }
                )

                Spacer(Modifier.width(8.dp))

                TextButton(onClick = onReplace) {
                    Text("Replace", color = Color(0xFFBBBBBB))
                }

                TextButton(onClick = onReplaceAll) {
                    Text("Replace All", color = Color(0xFFBBBBBB))
                }
            }
        }
    }
}
