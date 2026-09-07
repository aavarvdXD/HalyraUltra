package com.aavarvd.halyra

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowState
import com.aavarvd.halyra.editor.*
import com.aavarvd.halyra.editor.search.FindBar
import com.aavarvd.halyra.editor.search.SearchState
import com.aavarvd.halyra.io.FileManager
import com.aavarvd.halyra.io.PythonRunner
import com.aavarvd.halyra.ui.*

@Composable
fun WindowScope.App(
    windowState: WindowState,
    useCustomTitlebar: Boolean,
    closeRequested: Boolean = false,
    onCloseRequest: () -> Unit,
    onCloseCancelled: () -> Unit = {}
) {
    val appState = rememberAppState()
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    
    val fileManager = remember { FileManager(appState) }
    val pythonRunner = remember { PythonRunner(appState, coroutineScope) }
    val editorInputHandler = remember { EditorInputHandler(appState) }
    val editorScrollState = rememberScrollState()
    val editorState = rememberEditorState(editorScrollState)

    LaunchedEffect(Unit) {
        appState.terminalHeightPx = with(density) { 160.dp.toPx() }
    }

    LaunchedEffect(closeRequested) {
        if (closeRequested) {
            if (appState.anyModified) {
                appState.showExitConfirmation = true
            } else {
                onCloseRequest()
            }
        }
    }

    val editorTextStyle = LocalTextStyle.current.copy(
        color = AppColors.Text,
        fontFamily = AppFonts.JBMono,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )

    LaunchedEffect(
        appState.activeTabIndex,
        appState.activeTab?.text?.selection,
        editorState.textLayout,
        editorState.viewportHeightPx
    ) {
        val activeTab = appState.activeTab ?: return@LaunchedEffect
        val layout = editorState.textLayout ?: return@LaunchedEffect
        if (editorState.viewportHeightPx <= 0) return@LaunchedEffect

        val layoutTextLength = layout.layoutInput.text.length
        if (activeTab.text.text.length != layoutTextLength) return@LaunchedEffect

        val caretOffset = activeTab.text.selection.end.coerceIn(0, layoutTextLength)
        val cursorRect = layout.getCursorRect(caretOffset)

        val scrollTarget = editorState.calculateScrollTargetForCaret(
            currentScroll = editorScrollState.value,
            viewportHeight = editorState.viewportHeightPx,
            caretTop = with(density) { 8.dp.toPx() } + cursorRect.top,
            caretBottom = with(density) { 8.dp.toPx() } + cursorRect.bottom,
        )

        if (scrollTarget != null) {
            editorScrollState.animateScrollTo(
                scrollTarget.coerceIn(0, editorScrollState.maxValue)
            )
        }
    }

    MaterialTheme(
        colors = darkColors(
            primary = AppColors.Divider,
            background = AppColors.Background,
            surface = AppColors.Background,
            onPrimary = Color.White,
            onBackground = AppColors.Text
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (useCustomTitlebar) {
                AppTitleBar(windowState) {
                    if (appState.anyModified) {
                        appState.showExitConfirmation = true
                    } else {
                        onCloseRequest()
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .onPreviewKeyEvent { event ->
                        val activeTab = appState.activeTab ?: return@onPreviewKeyEvent false
                        AppHotkeys(
                            event = event,
                            text = activeTab.text,
                            onTextChange = { appState.tabs[appState.activeTabIndex] = activeTab.copy(text = it) },
                            onSave = { fileManager.saveCurrentFile() },
                            onRun = { pythonRunner.runCurrentFile() },
                            onNew = { fileManager.newFile() },
                            onOpen = { fileManager.openFileFromDialog() },
                            onFind = {
                                appState.searchState = if (appState.searchState.visible && !appState.searchState.replaceMode) {
                                    SearchState()
                                } else {
                                    appState.searchState.copy(visible = true, replaceMode = false)
                                }
                            },
                            onFindReplace = {
                                appState.searchState = if (appState.searchState.visible && appState.searchState.replaceMode) {
                                    SearchState()
                                } else {
                                    appState.searchState.copy(visible = true, replaceMode = true)
                                }
                            }
                        )
                    }
                    .focusable()
            ) {
                Sidebar(
                    onNew = { fileManager.newFile() },
                    onOpen = { fileManager.openFileFromDialog() },
                    onRun = {
                        pythonRunner.runCurrentFile()
                        appState.shellVisible = true
                    },
                    onSave = { fileManager.saveCurrentFile() },
                    onToggleShell = { appState.shellVisible = !appState.shellVisible }
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(AppColors.Background)
                ) {
                    TabBar(
                        tabs = appState.tabs,
                        activeTabIndex = appState.activeTabIndex,
                        onTabClick = { appState.activeTabIndex = it },
                        onTabClose = { fileManager.closeTab(it) }
                    )

                    if (appState.searchState.visible) {
                        FindBar(
                            query = appState.searchState.query,
                            replaceMode = appState.searchState.replaceMode,
                            replaceText = appState.searchState.replaceText,
                            onQueryChange = { appState.searchState = appState.searchState.copy(query = it) },
                            onReplaceTextChange = { appState.searchState = appState.searchState.copy(replaceText = it) },
                            onClose = { appState.searchState = SearchState() },
                            onNext = {},
                            onPrevious = {},
                            onReplace = {},
                            onReplaceAll = {}
                        )
                    }

                    if (appState.tabs.isEmpty()) {
                        NoFileOpenedView()
                    } else {
                        val activeTab = appState.activeTab!!
                        val lineCount = activeTab.text.text.lineSequence().count().coerceAtLeast(1)

                        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            EditorLineNumbers(lineCount, editorState, editorTextStyle)
                            EditorPane(activeTab, editorState, editorInputHandler, editorTextStyle, Modifier.weight(1f))
                        }
                    }

                    if (appState.shellVisible) {
                        Terminal(appState)
                    }
                }
            }
        }

        if (appState.showExitConfirmation) {
            ExitConfirmationDialog(appState, onCloseRequest, onCloseCancelled)
        }
    }
}

@Composable
private fun NoFileOpenedView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No file opened",
                color = Color(0xFF888888),
                fontSize = 20.sp,
                fontFamily = AppFonts.Inter
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Ctrl+N to create a new file",
                color = Color(0xFF666666),
                fontSize = 14.sp,
                fontFamily = AppFonts.Inter
            )
            Text(
                text = "Ctrl+O to open a file",
                color = Color(0xFF666666),
                fontSize = 14.sp,
                fontFamily = AppFonts.Inter
            )
        }
    }
}