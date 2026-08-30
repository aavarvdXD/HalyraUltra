package com.aavarvd.halyra.editor.search

import kotlin.test.Test
import kotlin.test.assertEquals

class SearchEngineTest {

    @Test
    fun findsAllMatches() {
        val text = """
            print(value)

            value = 10

            my_value = value
        """.trimIndent()

        val matches = SearchEngine.findMatches(text, "value")

        assertEquals(4, matches.size)
    }
}