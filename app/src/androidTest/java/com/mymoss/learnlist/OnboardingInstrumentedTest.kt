package com.mymoss.learnlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mymoss.learnlist.data.SettingsRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val settingsRepository by lazy {
        SettingsRepository(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Before
    fun resetOnboarding() {
        runBlocking {
            settingsRepository.update { it.copy(hasCompletedOnboarding = false) }
        }
    }

    @After
    fun completeOnboarding() {
        runBlocking {
            settingsRepository.update { it.copy(hasCompletedOnboarding = true) }
        }
    }

    @Test
    fun firstRunGuidesUserThroughCoreTabs() {
        waitForWelcome()
        composeRule.onNodeWithText("把每天的学习变成一张清单").assertIsDisplayed()
        composeRule.onNodeWithText("下一步").performClick()
        composeRule.onNodeWithText("先看今日，再开始行动").assertIsDisplayed()
        composeRule.onNodeWithText("下一步").performClick()
        composeRule.onNodeWithText("让复习按遗忘曲线回来").assertIsDisplayed()
        composeRule.onNodeWithText("下一步").performClick()
        composeRule.onNodeWithText("用专注和提醒守住节奏").assertIsDisplayed()
        composeRule.onNodeWithText("开始使用").performClick()
        composeRule.onNodeWithText("今日必做").assertIsDisplayed()
    }

    @Test
    fun settingsCanReplayOnboarding() {
        waitForWelcome()
        composeRule.onNodeWithText("跳过").performClick()
        composeRule.onNodeWithText("今日必做").assertIsDisplayed()
        composeRule.onNodeWithText("设置").performClick()
        composeRule.onNodeWithText("重新查看使用引导").performClick()
        composeRule.onNodeWithText("把每天的学习变成一张清单").assertIsDisplayed()
    }

    private fun waitForWelcome() {
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("把每天的学习变成一张清单", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
    }
}

