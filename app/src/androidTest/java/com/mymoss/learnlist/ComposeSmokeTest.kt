package com.mymoss.learnlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComposeSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun skipOnboardingIfPresent() {
        composeRule.waitUntil(3_000) {
            composeRule.onAllNodesWithText("跳过", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("今日必做", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        val skip = composeRule.onAllNodesWithText("跳过", useUnmergedTree = true)
        if (skip.fetchSemanticsNodes().isNotEmpty()) skip[0].performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun todayTabIsVisible() {
        skipOnboardingIfPresent()
        composeRule.onNodeWithText("今日必做").assertIsDisplayed()
        composeRule.onNodeWithText("设置").assertIsDisplayed()
    }

    @Test
    fun settingsShowsUpdateCenterAndManualCheckButton() {
        skipOnboardingIfPresent()
        composeRule.onNodeWithText("设置").performClick()
        composeRule.onNodeWithText("更新中心").assertIsDisplayed()
        composeRule.onNodeWithText("检查更新").assertIsDisplayed()
        composeRule.onNodeWithText("通知权限").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("精确提醒权限").performScrollTo().assertIsDisplayed()
        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("声音提示"))
        composeRule.onNodeWithText("声音提示").assertIsDisplayed()
        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("振动提示"))
        composeRule.onNodeWithText("振动提示").assertIsDisplayed()
    }
}
