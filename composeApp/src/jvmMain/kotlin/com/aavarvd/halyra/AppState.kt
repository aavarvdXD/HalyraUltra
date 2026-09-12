package com.aavarvd.halyra

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.aavarvd.halyra.editor.EditorTab
import com.aavarvd.halyra.editor.search.SearchState
import com.aavarvd.halyra.io.PythonProcess
import kotlinx.coroutines.Job
import java.io.File

class AppState {
    val tabs = mutableStateListOf<EditorTab>()
    var activeTabIndex by mutableStateOf(0)
    
    val activeTab: EditorTab?
        get() = tabs.getOrNull(activeTabIndex)

    var searchState by mutableStateOf(SearchState())
    var output by mutableStateOf("")
    var terminalInput by mutableStateOf("")
    var pythonRunJob by mutableStateOf<Job?>(null)
    var pythonProcess by mutableStateOf<PythonProcess?>(null)
    var shellVisible by mutableStateOf(true)
    var projectTreeVisible by mutableStateOf(false)
    var projectRoot by mutableStateOf<java.io.File?>(null)
    var showExitConfirmation by mutableStateOf(false)
    var showFolderSelectionWarning by mutableStateOf(false)
    var terminalHeightPx by mutableStateOf(0f)

    val selectedFiles = mutableStateListOf<java.io.File>()
    var clipboardFiles by mutableStateOf<List<java.io.File>>(emptyList())
    var isCutOperation by mutableStateOf(false)
    var showNewFileDialog by mutableStateOf(false)
    var showFolderPickerDialog by mutableStateOf(false)
    var newFileParent by mutableStateOf<java.io.File?>(null)
    var isLoadingFile by mutableStateOf(false)
    var pendingLargeFile by mutableStateOf<File?>(null)

    val anyModified: Boolean
        get() = tabs.any { it.isModified }
}

@Composable
fun rememberAppState(): AppState {
    return remember { AppState() }
}
