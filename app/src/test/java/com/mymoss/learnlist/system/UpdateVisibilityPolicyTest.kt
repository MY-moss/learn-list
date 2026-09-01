package com.mymoss.learnlist.system

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateVisibilityPolicyTest {
    private val update = UpdateInfo(
        tagName = "v0.3.2",
        versionName = "0.3.2",
        downloadUrl = "https://github.com/MY-moss/learn-list/releases/download/v0.3.2/learn-list.apk",
        sha256Url = "https://github.com/MY-moss/learn-list/releases/download/v0.3.2/learn-list.apk.sha256",
        releaseNotes = "",
    )

    @Test
    fun `automatic check hides the dismissed release`() {
        assertFalse(UpdateVisibilityPolicy.shouldShow(update, "0.3.2", manual = false))
        assertEquals(
            "已暂不提醒 v0.3.2；有更新版本时会再次提示",
            UpdateVisibilityPolicy.statusMessage(update, "0.3.2", manual = false),
        )
    }

    @Test
    fun `manual check can show a dismissed release again`() {
        assertTrue(UpdateVisibilityPolicy.shouldShow(update, "0.3.2", manual = true))
        assertEquals(
            "发现 v0.3.2，可以下载更新",
            UpdateVisibilityPolicy.statusMessage(update, "0.3.2", manual = true),
        )
    }

    @Test
    fun `a newer release is visible automatically`() {
        assertTrue(UpdateVisibilityPolicy.shouldShow(update, "0.3.1", manual = false))
    }

    @Test
    fun `no update uses the latest status`() {
        assertFalse(UpdateVisibilityPolicy.shouldShow(null, null, manual = false))
        assertEquals("当前已是最新版本", UpdateVisibilityPolicy.statusMessage(null, null, manual = false))
    }
}
