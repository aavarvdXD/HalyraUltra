package com.aavarvd.halyra

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.aavarvd.halyra.editor.EditorTab
import com.aavarvd.halyra.editor.search.SearchState
import com.aavarvd.halyra.io.PythonProcess
import kotlinx.coroutines.Job

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
    var showExitConfirmation by mutableStateOf(false)
    var terminalHeightPx by mutableStateOf(0f)

    val anyModified: Boolean
        get() = tabs.any { it.isModified }
}

@Composable
fun rememberAppState(): AppState {
    return remember { AppState() }
}
