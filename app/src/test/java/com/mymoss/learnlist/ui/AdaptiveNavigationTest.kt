package com.mymoss.learnlist.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveNavigationTest {
    @Test
    fun compactWidthsKeepBottomNavigation() {
        assertFalse(usesRailNavigation(599))
    }

    @Test
    fun expandedWidthsUseRailNavigation() {
        assertTrue(usesRailNavigation(600))
        assertTrue(usesRailNavigation(840))
    }
}
