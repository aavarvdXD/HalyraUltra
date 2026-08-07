package com.aavarvd.halyra.editor.search

object SearchEngine {

    fun findMatches(
        text: String,
        query: String,
        options: SearchOptions = SearchOptions()
    ): List<SearchMatch> {

        if (query.isEmpty()) return emptyList()

        val source =
            if (options.caseSensitive)
                text
            else
                text.lowercase()

        val target =
            if (options.caseSensitive)
                query
            else
                query.lowercase()

        val results = mutableListOf<SearchMatch>()

        var index = source.indexOf(target)

        while (index >= 0) {

            val end = index + target.length

            if (!options.wholeWord ||
                isWholeWord(text, index, end)
            ) {
                results.add(
                    SearchMatch(index, end)
                )
            }

            index = source.indexOf(
                target,
                index + 1
            )
        }

        return results
    }


    private fun isWholeWord(
        text: String,
        start: Int,
        end: Int
    ): Boolean {

        val before =
            start == 0 ||
                    !isWordCharacter(text[start - 1])

        val after =
            end == text.length ||
                    !isWordCharacter(text[end])

        return before && after
    }


    private fun isWordCharacter(
        char: Char
    ): Boolean {
        return char.isLetterOrDigit()
                || char == '_'
    }
}