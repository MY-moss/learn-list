package com.mymoss.learnlist.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.mymoss.learnlist.system.UpdateInfo
import com.mymoss.learnlist.ui.theme.LearnListTheme
import org.junit.Rule
import org.junit.Test

class UpdateCenterInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun downloadingUpdateShowsPhasePercentAndBytes() {
        composeRule.setContent {
            LearnListTheme {
                UpdateCenterCard(
                    updateState = UpdateUiState(
                        isDownloading = true,
                        available = UpdateInfo(
                            tagName = "v0.2.5",
                            versionName = "0.2.5",
                            downloadUrl = "https://github.com/MY-moss/learn-list/releases/download/v0.2.5/learn-list-v0.2.5.apk",
                            sha256Url = "https://github.com/MY-moss/learn-list/releases/download/v0.2.5/learn-list-v0.2.5.apk.sha256",
                            releaseNotes = "",
                        ),
                        phase = UpdatePhase.DOWNLOADING,
                        downloadProgress = 0.42f,
                        downloadedBytes = 43L * 1024L,
                        totalDownloadBytes = 100L * 1024L,
                    ),
                    onCheck = {},
                    onDownload = {},
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithText("正在下载更新包…").assertIsDisplayed()
        composeRule.onNodeWithText("下载进度 42% · 已下载 43 KB / 100 KB").assertIsDisplayed()
    }
}

