package com.aavarvd.halyra

import kotlin.test.Test
import kotlin.test.assertEquals

class ComposeAppDesktopTest {

    @Test
    fun example() {
        assertEquals(3, 1 + 2)
    }

    @Test
    fun `calculateScrollTargetForCaret returns null when caret is visible`() {
        val target = calculateScrollTargetForCaret(
            currentScroll = 100,
            viewportHeight = 200,
            caretTop = 120f,
            caretBottom = 140f,
        )

        assertEquals(null, target)
    }

    @Test
    fun `calculateScrollTargetForCaret scrolls up when caret is above viewport`() {
        val target = calculateScrollTargetForCaret(
            currentScroll = 100,
            viewportHeight = 200,
            caretTop = 70f,
            caretBottom = 90f,
        )

        assertEquals(70, target)
    }

    @Test
    fun `calculateScrollTargetForCaret scrolls down when caret is below viewport`() {
        val target = calculateScrollTargetForCaret(
            currentScroll = 100,
            viewportHeight = 200,
            caretTop = 310f,
            caretBottom = 330f,
        )

        assertEquals(130, target)
    }
}