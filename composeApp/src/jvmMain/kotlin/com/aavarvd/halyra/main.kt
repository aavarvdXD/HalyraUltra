@file:Suppress("DEPRECATION")

package com.aavarvd.halyra

// Halyra is a python IDE in development written in Kotlin Compose Desktop
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.WindowPosition

import kotlinx.coroutines.delay

import kotlin.time.Duration.Companion.milliseconds

fun main() = application {
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(4000.milliseconds)
        showSplash = false
    }

    if (showSplash) {
        Window(
            onCloseRequest = ::exitApplication,
            title = "",
            undecorated = true,
            resizable = false,
            alwaysOnTop = true,
            state = rememberWindowState(
                placement = WindowPlacement.Floating,
                position = WindowPosition.Aligned(Alignment.Center),
                size = DpSize(600.dp, 400.dp)
            )
        ) {
            SplashScreen()
        }
    } else {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Halyra",
            icon = painterResource("images/icon.png"),
            state = rememberWindowState(placement = WindowPlacement.Maximized)
        ) {
            App()
        }
    }
}