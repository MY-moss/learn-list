package com.mymoss.learnlist.system

import org.junit.Assert.assertEquals
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

    @Test(expected = IllegalArgumentException::class)
    fun `update URLs reject non standard HTTPS ports`() {
        ReleaseChecker.validateUrl("https://github.com:8443/MY-moss/learn-list/releases/latest")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `update URLs reject embedded credentials`() {
        ReleaseChecker.validateUrl("https://release-user:release-password@github.com/MY-moss/learn-list/releases/latest")
    }

    @Test
    fun `checksum parser accepts a digest followed by a filename`() {
        val digest = "A".repeat(64)

        assertEquals(digest.lowercase(), UpdateSecurityPolicy.parseSha256("$digest  learn-list.apk\n"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `checksum parser rejects malformed digests`() {
        UpdateSecurityPolicy.parseSha256("not-a-sha256 learn-list.apk")
    }

    @Test
    fun `package validation requires a strictly newer version and matching certificates`() {
        assertTrue(UpdateSecurityPolicy.isStrictUpgrade(11, 10))
        assertFalse(UpdateSecurityPolicy.isStrictUpgrade(10, 10))
        assertFalse(UpdateSecurityPolicy.isStrictUpgrade(9, 10))
        assertTrue(UpdateSecurityPolicy.certificatesMatch(setOf("abc"), setOf("abc")))
        assertFalse(UpdateSecurityPolicy.certificatesMatch(emptySet(), setOf("abc")))
        assertFalse(UpdateSecurityPolicy.certificatesMatch(setOf("abc"), setOf("def")))
    }
}
