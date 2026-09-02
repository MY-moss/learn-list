package com.mymoss.learnlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
    fun todayCanOpenHistoryCalendar() {
        skipOnboardingIfPresent()
        composeRule.onNodeWithContentDescription("打开学习日历").performClick()
        composeRule.onNodeWithText("学习日历").assertIsDisplayed()
        composeRule.onNodeWithText("点选日期查看当天进度；未来日期不可补记。").assertIsDisplayed()
        composeRule.onNodeWithText("关闭").performClick()
    }

    @Test
    fun settingsShowsUpdateCenterAndManualCheckButton() {
        skipOnboardingIfPresent()
        composeRule.onNodeWithText("设置").performClick()
        composeRule.onNodeWithText("更新中心").assertIsDisplayed()
        composeRule.onNodeWithText("检查更新").assertIsDisplayed()
        val settingsScroll = composeRule.onNode(hasScrollToNodeAction())
        settingsScroll.performScrollToNode(hasText("通知权限"))
        composeRule.onNodeWithText("通知权限").assertIsDisplayed()
        settingsScroll.performScrollToNode(hasText("精确提醒权限"))
        composeRule.onNodeWithText("精确提醒权限").assertIsDisplayed()
        settingsScroll.performScrollToNode(hasText("声音提示"))
        composeRule.onNodeWithText("声音提示").assertIsDisplayed()
        settingsScroll.performScrollToNode(hasText("振动提示"))
        composeRule.onNodeWithText("振动提示").assertIsDisplayed()
        settingsScroll.performScrollToNode(hasText("导出脱敏诊断"))
        composeRule.onNodeWithText("导出脱敏诊断").assertIsDisplayed()
    }

    @Test
    fun settingsSectionsCanCollapseAndExpand() {
        skipOnboardingIfPresent()
        composeRule.onNodeWithText("设置").performClick()
        composeRule.onNodeWithText("更新中心").performClick()
        composeRule.onAllNodesWithText("检查更新").assertCountEquals(0)
        composeRule.onNodeWithText("更新中心").performClick()
        composeRule.onNodeWithText("检查更新").assertIsDisplayed()
    }

    @Test
    fun todoSectionsCanCollapseAndExpand() {
        skipOnboardingIfPresent()
        val todoNodes = composeRule.onAllNodesWithText("待办")
        todoNodes[todoNodes.fetchSemanticsNodes().lastIndex].performClick()
        composeRule.onNodeWithText("待完成").assertIsDisplayed()
        composeRule.onNodeWithText("没有到期待办，点击右下角添加。").assertIsDisplayed()
        composeRule.onNodeWithText("待完成").performClick()
        composeRule.onAllNodesWithText("没有到期待办，点击右下角添加。").assertCountEquals(0)
        composeRule.onNodeWithText("待完成").performClick()
        composeRule.onNodeWithText("没有到期待办，点击右下角添加。").assertIsDisplayed()
        composeRule.onNodeWithText("已完成").performClick()
        composeRule.onNodeWithText("完成的待办会显示在这里。").assertIsDisplayed()
    }

    @Test
    fun learningProjectsCanCollapseAndExpand() {
        skipOnboardingIfPresent()
        val learnNodes = composeRule.onAllNodesWithText("学习")
        learnNodes[learnNodes.fetchSemanticsNodes().lastIndex].performClick()
        composeRule.onNodeWithText("学习项目").assertIsDisplayed()
        composeRule.onNodeWithText("创建第一个学习项目：书籍、课程或技能").assertIsDisplayed()
        composeRule.onNodeWithText("学习项目").performClick()
        composeRule.onAllNodesWithText("创建第一个学习项目：书籍、课程或技能").assertCountEquals(0)
        composeRule.onNodeWithText("学习项目").performClick()
        composeRule.onNodeWithText("创建第一个学习项目：书籍、课程或技能").assertIsDisplayed()
    }
}

