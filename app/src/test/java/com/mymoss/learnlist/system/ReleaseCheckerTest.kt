package com.mymoss.learnlist.system

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseCheckerTest {
    @Test
    fun `version comparison accepts a newer patch release`() {
        assertTrue(ReleaseChecker.isNewer("0.1.1", "0.1.0"))
        assertTrue(ReleaseChecker.isNewer("0.1.10", "0.1.9"))
        assertFalse(ReleaseChecker.isNewer("0.1.0", "0.1.0"))
        assertFalse(ReleaseChecker.isNewer("0.0.9", "0.1.0"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `update URLs must use HTTPS`() {
        ReleaseChecker.validateUrl("http://github.com/MY-moss/learn-list/releases/latest")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `update URLs must stay on an allowed GitHub host`() {
        ReleaseChecker.validateUrl("https://example.com/learn-list.apk")
    }
}
