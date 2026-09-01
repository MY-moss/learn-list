package com.mymoss.learnlist.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticsServiceTest {
    @Test
    fun reportContainsOnlyAggregateMetadata() {
        val report = buildDiagnosticsJson(
            generatedAtEpochMillis = 123L,
            versionName = "0.2.8",
            apiLevel = 36,
            manufacturer = "Test\nMaker",
            model = "Test\u0000Phone",
            records = mapOf("projects" to DiagnosticsRecordCount(total = 4, archived = 1, deleted = 2)),
            activityFlags = mapOf("soundEnabled" to true),
        )
        assertTrue(report.contains("\"format\":\"learn-list-diagnostics\""))
        assertTrue(report.contains("\"projects\":{\"total\":4,\"archived\":1,\"deleted\":2}"))
        assertTrue(report.contains("\"manufacturer\":\"TestMaker\""))
        assertFalse(report.contains("Test\nMaker"))
        assertFalse(report.contains("Test\u0000Phone"))
        assertFalse(report.contains("secret title"))
        assertTrue(report.contains("\"soundEnabled\":true"))
    }

    @Test
    fun negativeCountsAreClamped() {
        val report = buildDiagnosticsJson(
            generatedAtEpochMillis = 1L,
            versionName = "test",
            apiLevel = 26,
            manufacturer = "maker",
            model = "model",
            records = mapOf("projects" to DiagnosticsRecordCount(-1, -2, -3)),
            activityFlags = emptyMap(),
        )

        assertTrue(report.contains("\"projects\":{\"total\":0,\"archived\":0,\"deleted\":0}"))
    }
}
