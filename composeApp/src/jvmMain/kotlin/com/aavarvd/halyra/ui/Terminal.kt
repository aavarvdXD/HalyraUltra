package com.aavarvd.halyra.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.aavarvd.halyra.AppState
import java.awt.Cursor

@Composable
fun Terminal(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val minHeightPx = with(density) { 60.dp.toPx() }
    val maxHeightPx = with(density) { 420.dp.toPx() }

    Column(modifier = modifier.fillMaxWidth()) {
        // Drag handle
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .background(AppColors.Divider)
                .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR)))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        appState.terminalHeightPx = (appState.terminalHeightPx - dragAmount.y)
                            .coerceIn(minHeightPx, maxHeightPx)
                    }
                }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) { appState.terminalHeightPx.toInt().toDp() })
                .background(AppColors.TerminalBackground)
                .padding(8.dp)
        ) {
            // Output area
            Text(
                text = appState.output,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                color = AppColors.TerminalText,
                fontFamily = AppFonts.JBMono
            )

            // Input area
            BasicTextField(
                value = appState.terminalInput,
                onValueChange = { appState.terminalInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textStyle = LocalTextStyle.current.copy(
                    color = AppColors.TerminalText,
                    fontFamily = AppFonts.JBMono
                ),
                cursorBrush = SolidColor(AppColors.Cursor),
                singleLine = true
            )
        }
    }
}
