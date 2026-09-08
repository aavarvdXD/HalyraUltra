package com.aavarvd.halyra.io

import androidx.compose.ui.text.input.TextFieldValue
import com.aavarvd.halyra.AppState
import com.aavarvd.halyra.editor.EditorTab

class FileManager(private val appState: AppState) {
    private var untitledCounter = 1

    private fun getNextUntitledName(): String {
        val name = if (untitledCounter == 1) "Untitled.py" else "Untitled$untitledCounter.py"
        untitledCounter++
        return name
    }

    fun newFile() {
        appState.tabs.add(EditorTab(untitledName = getNextUntitledName()))
        appState.activeTabIndex = appState.tabs.size - 1
        appState.output = ""
        appState.terminalInput = ""
    }

    fun openFileFromDialog() {
        openFile()?.let { (file, content) ->
            // Check if file is already open
            val existingIndex = appState.tabs.indexOfFirst { it.file == file }
            if (existingIndex != -1) {
                appState.activeTabIndex = existingIndex
                return@let
            }

            val newTab = EditorTab(
                file = file,
                text = TextFieldValue(content),
                lastSavedText = content
            )

            val activeTab = appState.activeTab
            if (appState.tabs.size == 1 && activeTab != null && activeTab.file == null && activeTab.text.text.isEmpty() && !activeTab.isModified) {
                appState.tabs[0] = newTab
            } else {
                appState.tabs.add(newTab)
                appState.activeTabIndex = appState.tabs.size - 1
            }

            appState.output = ""
            appState.terminalInput = ""
        }
    }

    fun saveCurrentFile() {
        val current = appState.activeTab ?: return
        val index = appState.activeTabIndex
        if (current.file != null) {
            saveFile(
                current.file,
                current.text.text
            )
            appState.tabs[index] = current.copy(lastSavedText = current.text.text)
        } else {
            saveFileAs(current.text.text, current.title)?.let {
                appState.tabs[index] = current.copy(
                    file = it,
                    lastSavedText = current.text.text
                )
            }
        }
    }

    fun saveCurrentFileAs() {
        val current = appState.activeTab ?: return
        val index = appState.activeTabIndex
        saveFileAs(current.text.text, current.title)?.let {
            appState.tabs[index] = current.copy(
                file = it,
                lastSavedText = current.text.text
            )
        }
    }
    
    fun closeTab(index: Int) {
        if (appState.tabs[index].isModified) {
            val tab = appState.tabs[index]
            if (tab.file != null) {
                saveFile(tab.file, tab.text.text)
            } else {
                appState.activeTabIndex = index
                saveCurrentFile()
            }
        }

        if (appState.tabs.isNotEmpty()) {
            appState.tabs.removeAt(index)
            if (appState.activeTabIndex >= appState.tabs.size) {
                appState.activeTabIndex = (appState.tabs.size - 1).coerceAtLeast(0)
            }
        }
    }
}
