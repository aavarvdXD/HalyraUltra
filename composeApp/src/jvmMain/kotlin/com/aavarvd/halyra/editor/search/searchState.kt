package com.aavarvd.halyra.editor.search

data class SearchState(
    val visible: Boolean = false,
    val replaceMode: Boolean = false,

    val query: String = "",
    val replaceText: String = "",

    val matches: List<SearchMatch> = emptyList(),
    val currentMatch: Int = -1,

    val options: SearchOptions = SearchOptions()
)