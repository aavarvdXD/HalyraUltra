package com.aavarvd.halyra.io

import com.aavarvd.halyra.AppState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PythonRunner(
    private val appState: AppState,
    private val scope: CoroutineScope
) {
    fun stopPython() {
        appState.pythonRunJob?.cancel()
        appState.pythonRunJob = null

        appState.pythonProcess?.stop()
        appState.pythonProcess = null
    }

    fun runCurrentFile() {
        val current = appState.activeTab ?: return
        stopPython()

        appState.output = ""
        appState.terminalInput = ""
        
        val process = PythonProcess(
            code = if (current.file == null) current.text.text else null,
            file = current.file,
            onOutput = { chunk ->
                scope.launch { appState.output += chunk }
            },
            onFinished = {
                scope.launch { appState.pythonProcess = null }
            }
        )

        appState.pythonProcess = process
        appState.pythonRunJob = scope.launch { process.start() }
    }
}
