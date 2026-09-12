package com.aavarvd.halyra.io

import androidx.compose.ui.text.input.TextFieldValue
import com.aavarvd.halyra.AppState
import com.aavarvd.halyra.editor.EditorTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            openFile(file, content)
        }
    }

    fun openProjectFolder() {
        val folder = openFolder()
        if (folder != null && folder.isDirectory) {
            appState.projectRoot = folder
            appState.projectTreeVisible = true
        } else {
            appState.showFolderSelectionWarning = true
        }
    }

    companion object {
        const val LARGE_FILE_WARNING_BYTES = 300_000L
    }

    fun openFile(file: java.io.File, content: String? = null, forceOpen: Boolean = false) {
        val existingIndex = appState.tabs.indexOfFirst { it.file == file }
        if (existingIndex != -1) {
            appState.activeTabIndex = existingIndex
            return
        }

        if (content == null && !forceOpen && file.length() > LARGE_FILE_WARNING_BYTES) {
            appState.pendingLargeFile = file
            return
        }

        appState.isLoadingFile = true

        CoroutineScope(Dispatchers.IO).launch {
            val fileContent = content ?: file.readText()

            withContext(Dispatchers.Main) {
                val newTab = EditorTab(
                    file = file,
                    text = TextFieldValue(fileContent),
                    lastSavedText = fileContent
                )

                val activeTab = appState.activeTab
                if (
                    appState.tabs.size == 1 &&
                    activeTab != null &&
                    activeTab.file == null &&
                    activeTab.text.text.isEmpty() &&
                    !activeTab.isModified
                ) {
                    appState.tabs[0] = newTab
                } else {
                    appState.tabs.add(newTab)
                    appState.activeTabIndex = appState.tabs.size - 1
                }
                appState.output = ""
                appState.terminalInput = ""
                appState.isLoadingFile = false
            }
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

    fun deleteFiles(files: List<java.io.File>) {
        files.forEach { file ->
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
            // Close tab if deleted file was open
            val tabIndex = appState.tabs.indexOfFirst { it.file == file }
            if (tabIndex != -1) {
                appState.tabs.removeAt(tabIndex)
                if (appState.activeTabIndex >= appState.tabs.size) {
                    appState.activeTabIndex = (appState.tabs.size - 1).coerceAtLeast(0)
                }
            }
        }
    }

    fun createNewFile(parent: java.io.File, name: String) {
        val newFile = java.io.File(parent, name)
        if (newFile.exists()) return // Or show error

        if (name.contains("/") || name.contains("\\")) {
            // It might be a path, let's just create it as a file for now or handle directories
            newFile.parentFile.mkdirs()
        }

        newFile.createNewFile()
        openFile(newFile)
    }

    fun copyFiles(files: List<java.io.File>, cut: Boolean) {
        appState.clipboardFiles = files
        appState.isCutOperation = cut
    }

    fun pasteFiles(targetFolder: java.io.File) {
        val destination = if (targetFolder.isDirectory) targetFolder else targetFolder.parentFile
        appState.clipboardFiles.forEach { file ->
            val destFile = java.io.File(destination, file.name)
            if (appState.isCutOperation) {
                file.renameTo(destFile)
            } else {
                if (file.isDirectory) {
                    file.copyRecursively(destFile, overwrite = true)
                } else {
                    file.copyTo(destFile, overwrite = true)
                }
            }
        }
        if (appState.isCutOperation) {
            appState.clipboardFiles = emptyList()
            appState.isCutOperation = false
        }
    }
}
