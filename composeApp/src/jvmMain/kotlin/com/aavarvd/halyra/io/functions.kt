package com.aavarvd.halyra.io

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

fun openFile(): Pair<File, String>? {
    val dialog = FileDialog(null as Frame?, "Open File", FileDialog.LOAD)
    dialog.isVisible = true

    val fileName = dialog.file ?: return null
    val path = dialog.directory + fileName

    val file = File(path)
    return file to file.readText()
}

fun saveFileAs(content: String, suggestedName: String = "Untitled.py"): File? {
    val dialog = FileDialog(null as Frame?, "Save File", FileDialog.SAVE)
    dialog.file = suggestedName
    dialog.isVisible = true

    val file = dialog.file ?: return null
    val path = dialog.directory + file

    val f = File(path)
    f.writeText(content)
    return f
}

fun saveFile(file: File, content: String) {
    file.writeText(content)
}