package com.aavarvd.halyra

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.MutableInteractionSource

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.WindowPlacement

import androidx.compose.material.Text
import androidx.compose.material.Icon

import androidx.compose.runtime.*
import androidx.compose.ui.window.WindowScope

@Composable
fun WindowScope.AppTitleBar(windowState: WindowState) {
    WindowDraggableArea {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(Color(0xFF2B2B2B))
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Halyra",
                    color = Color(0xFF56b5c3),
                    fontFamily = AppFonts.Inter,
                    modifier = Modifier.padding(start = 12.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                TitleBarButton("─") {
                    windowState.isMinimized = true
                }

                TitleBarButton(
                    if (windowState.placement == WindowPlacement.Maximized) "❐" else "□"
                ) {
                    windowState.placement =
                        if (windowState.placement == WindowPlacement.Maximized) WindowPlacement.Floating else WindowPlacement.Maximized
                }

                TitleBarButton("✕") {
                    kotlin.system.exitProcess(0)
                }
            }
        }
    }
}

@Composable
fun TitleBarButton(
    text: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    Box(
        modifier = Modifier
            .width(46.dp)
            .fillMaxHeight()
            .clickable { onClick() }
            .background(if (hovered) Color(0xFF515151) else Color(0xFF2B2B2B)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontFamily = AppFonts.Inter
        )
    }
}