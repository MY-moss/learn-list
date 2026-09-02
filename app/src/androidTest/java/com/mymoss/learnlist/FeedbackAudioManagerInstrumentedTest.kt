package com.mymoss.learnlist

import android.content.Context
import android.media.MediaPlayer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mymoss.learnlist.data.AppSettings
import com.mymoss.learnlist.system.FeedbackAudioManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeedbackAudioManagerInstrumentedTest {
    @Test
    fun packagedFeedbackSoundIsPresentAndDecodable() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val player = MediaPlayer.create(context, R.raw.feedback_complete)
        assertNotNull("APK 应包含可播放的内置提示音", player)
        assertTrue("内置提示音时长应该大于 0", player.duration > 0)
        player.release()
    }

    @Test
    fun defaultFeedbackUsesPackagedSoundWhenNoCustomSourceIsSelected() {
        assertEquals("应用内置提示音（默认）", FeedbackAudioManager.defaultDisplayName())
        assertTrue(AppSettings().feedbackAudioPath == null)
        assertTrue(AppSettings().feedbackAudioUri == null)
    }
}

