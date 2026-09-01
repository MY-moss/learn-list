package com.mymoss.learnlist.ui

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LearnListViewModelActionTest {
    @Test
    fun successfulActionReportsSuccessWithoutAnError() = runTest {
        val results = mutableListOf<Boolean>()
        val errors = mutableListOf<String>()

        runViewModelAction(
            onResult = results::add,
            onError = errors::add,
        ) { }

        assertEquals(listOf(true), results)
        assertTrue(errors.isEmpty())
    }

    @Test
    fun failedActionKeepsTheFormOpenContractByReportingFailure() = runTest {
        val results = mutableListOf<Boolean>()
        val errors = mutableListOf<String>()

        runViewModelAction(
            onResult = results::add,
            onError = errors::add,
        ) { error("日期格式不正确") }

        assertEquals(listOf(false), results)
        assertEquals(listOf("日期格式不正确"), errors)
    }
}
