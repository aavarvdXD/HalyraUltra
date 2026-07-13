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
import com.aavarvd.halyra.ui.SplashScreen
import kotlinx.coroutines.delay
import java.awt.GraphicsConfiguration
import java.awt.GraphicsEnvironment
import java.awt.Point
import java.awt.Rectangle
import java.awt.Toolkit
import java.awt.Window
import java.beans.PropertyChangeListener
import kotlin.time.Duration.Companion.milliseconds

private fun screenBoundsForWindow(window: Window): Rectangle {
    val screenDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices
    val fallbackDevice = window.graphicsConfiguration?.device ?: GraphicsEnvironment
        .getLocalGraphicsEnvironment()
        .defaultScreenDevice
    val windowCenter = Point(window.x + window.width / 2, window.y + window.height / 2)

    var bestDevice = fallbackDevice
    var bestDistance = Long.MAX_VALUE

    for (candidate in screenDevices) {
        val bounds = candidate.defaultConfiguration.bounds
        if (bounds.contains(windowCenter)) {
            bestDevice = candidate
            bestDistance = 0L
            break
        }

        val distance = distanceSquaredToBounds(windowCenter, bounds)
        if (distance < bestDistance) {
            bestDistance = distance
            bestDevice = candidate
        }
    }

    return usableBounds(bestDevice.defaultConfiguration)
}

internal fun usableBounds(configuration: GraphicsConfiguration): Rectangle {
    val insets = Toolkit.getDefaultToolkit().getScreenInsets(configuration)
    val bounds = configuration.bounds
    val width = (bounds.width - insets.left - insets.right).coerceAtLeast(1)
    val height = (bounds.height - insets.top - insets.bottom).coerceAtLeast(1)
    return Rectangle(
        bounds.x + insets.left,
        bounds.y + insets.top,
        width,
        height
    )
}

private fun distanceSquaredToBounds(point: Point, bounds: Rectangle): Long {
    val dx = when {
        point.x < bounds.x -> (bounds.x - point.x).toLong()
        point.x > bounds.x + bounds.width -> (point.x - (bounds.x + bounds.width)).toLong()
        else -> 0L
    }
    val dy = when {
        point.y < bounds.y -> (bounds.y - point.y).toLong()
        point.y > bounds.y + bounds.height -> (point.y - (bounds.y + bounds.height)).toLong()
        else -> 0L
    }
    return dx * dx + dy * dy
}

private fun clampWindowToBounds(window: Window, bounds: Rectangle) {
    val width = window.width.coerceAtMost(bounds.width).coerceAtLeast(1)
    val height = window.height.coerceAtMost(bounds.height).coerceAtLeast(1)
    val maxX = bounds.x + bounds.width - width
    val maxY = bounds.y + bounds.height - height
    val x = window.x.coerceIn(bounds.x, maxX)
    val y = window.y.coerceIn(bounds.y, maxY)

    if (window.x != x || window.y != y || window.width != width || window.height != height) {
        window.setBounds(x, y, width, height)
    }
}

fun main() = application {
    // Set to false to use system default titlebar
    val useCustomTitlebar = false

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
        val mainWindowState = rememberWindowState(placement = WindowPlacement.Maximized)

        Window(
            onCloseRequest = ::exitApplication,
            title = "Halyra",
            icon = painterResource("images/icon.png"),
            state = mainWindowState,
            undecorated = useCustomTitlebar,
            resizable = mainWindowState.placement != WindowPlacement.Maximized || !useCustomTitlebar
        ) {
            if (useCustomTitlebar) {
                DisposableEffect(window) {
                    fun applyMaxBounds() {
                        window.graphicsConfiguration?.let { cfg ->
                            val b = usableBounds(cfg)
                            window.maximizedBounds = b
                            if (mainWindowState.placement == WindowPlacement.Maximized) {
                                java.awt.EventQueue.invokeLater {
                                    (window as? java.awt.Frame)?.extendedState = java.awt.Frame.MAXIMIZED_BOTH
                                }
                            } else {
                                clampWindowToBounds(window, b)
                            }
                        }
                    }

                    applyMaxBounds()

                    val propertyListener = PropertyChangeListener { event ->
                        if (event.propertyName == "graphicsConfiguration") {
                            applyMaxBounds()
                        }
                    }

                    window.addPropertyChangeListener(propertyListener)

                    onDispose {
                        window.removePropertyChangeListener(propertyListener)
                    }
                }
            }
            App(mainWindowState, useCustomTitlebar)
        }
    }
}
