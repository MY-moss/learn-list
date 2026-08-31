package com.mymoss.learnlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComposeSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun todayTabIsVisible() {
        composeRule.onNodeWithText("今日必做").assertIsDisplayed()
        composeRule.onNodeWithText("设置").assertIsDisplayed()
    }

    @Test
    fun settingsShowsUpdateCenterAndManualCheckButton() {
        composeRule.onNodeWithText("设置").performClick()
        composeRule.onNodeWithText("更新中心").assertIsDisplayed()
        composeRule.onNodeWithText("检查更新").assertIsDisplayed()
        composeRule.onNodeWithText("通知权限").assertIsDisplayed()
        composeRule.onNodeWithText("精确提醒权限").assertIsDisplayed()
    }
}

