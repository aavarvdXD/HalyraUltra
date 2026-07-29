package com.aavarvd.halyra.io

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

suspend fun runPython(file: File): String = withContext(Dispatchers.IO) {
    var process: Process? = null

    try {
        process = ProcessBuilder("python", file.absolutePath)
            .redirectErrorStream(true)
            .start()

        process.inputStream.bufferedReader().use { reader ->
            reader.readText()
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        e.message ?: "Error running Python"
    } finally {
        process?.let {
            if (it.isAlive) {
                it.destroyForcibly()
            }
        }
    }
}
