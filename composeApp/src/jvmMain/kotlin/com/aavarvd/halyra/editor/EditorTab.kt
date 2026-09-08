package com.aavarvd.halyra.editor

import androidx.compose.ui.text.input.TextFieldValue
import java.io.File

data class EditorTab(
    val file: File? = null,
    val text: TextFieldValue = TextFieldValue(""),
    val lastSavedText: String = "",
    val untitledName: String = "Untitled.py",
    val id: Long = System.nanoTime()
) {
    val isModified: Boolean
        get() = text.text != lastSavedText

    val title: String
        get() = file?.name ?: untitledName
}
