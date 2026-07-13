package com.aavarvd.halyra.io

import java.io.File

fun runPython(file: File): String {
    return try {
        val process = ProcessBuilder("python", file.absolutePath)
            .redirectErrorStream(true)
            .start()
        process.inputStream.bufferedReader().readText()
    } catch (e: Exception) {
        e.message ?: "Error running Python"
    }
}