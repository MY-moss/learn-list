package com.mymoss.learnlist.system

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.provider.OpenableColumns
import com.mymoss.learnlist.R
import com.mymoss.learnlist.data.AppSettings
import java.io.File
import java.util.UUID

/** Plays and stores the user's selected feedback source without relying on notification channels. */
object FeedbackAudioManager {
    data class ImportedAudio(val path: String, val displayName: String)

    private const val DIRECTORY_NAME = "feedback-audio"
    private const val MAX_AUDIO_BYTES = 5L * 1024L * 1024L
    private const val DEFAULT_DISPLAY_NAME = "应用内置提示音（默认）"
    private const val SYSTEM_DISPLAY_NAME = "手机系统铃声"

    private val playbackLock = Any()
    private var activePlayer: MediaPlayer? = null
    private var activeRingtone: Ringtone? = null

    fun importToPrivateDirectory(context: Context, uri: Uri): ImportedAudio {
        val resolver = context.contentResolver
        val sourceName = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }?.takeIf(String::isNotBlank) ?: "自定义音效"
        val displayName = sourceName.substringAfterLast('/').take(80)
        val extension = displayName.substringAfterLast('.', "audio")
            .lowercase()
            .filter(Char::isLetterOrDigit)
            .take(8)
            .ifBlank { "audio" }
        val destination = File(audioDirectory(context).apply { mkdirs() }, "custom-${UUID.randomUUID()}.$extension")
        try {
            val input = resolver.openInputStream(uri) ?: error("无法读取音效文件")
            input.use { source ->
                destination.outputStream().use { target ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var total = 0L
                    while (true) {
                        val read = source.read(buffer)
                        if (read <= 0) break
                        total += read
                        require(total <= MAX_AUDIO_BYTES) { "音效文件不能超过 5 MB" }
                        target.write(buffer, 0, read)
                    }
                }
            }
        } catch (error: Exception) {
            destination.delete()
            throw error
        }
        return ImportedAudio(destination.absolutePath, displayName)
    }

    fun deleteIfOwned(context: Context, path: String?) {
        ownedFile(context, path)?.delete()
    }

    /** Plays custom audio, a selected system ringtone, or the packaged fallback in that order. */
    fun play(context: Context, settings: AppSettings) {
        if (playCustom(context, settings.feedbackAudioPath)) return
        val selectedUri = settings.feedbackAudioUri?.let { value -> runCatching { Uri.parse(value) }.getOrNull() }
        if (selectedUri != null && playRingtone(context, selectedUri)) return
        if (playBuiltIn(context)) return
        playSystemDefault(context)
    }

    fun preview(context: Context, settings: AppSettings) = play(context, settings)

    fun displayName(context: Context, uri: Uri): String = runCatching {
        RingtoneManager.getRingtone(context.applicationContext, uri)?.getTitle(context)
            ?.takeIf(String::isNotBlank)
    }.getOrNull() ?: SYSTEM_DISPLAY_NAME

    fun defaultDisplayName(): String = DEFAULT_DISPLAY_NAME

    private fun playCustom(context: Context, path: String?): Boolean {
        val file = ownedFile(context, path)?.takeIf { it.isFile && it.length() > 0L } ?: return false
        return playMedia { createPreparedMediaPlayer(context, Uri.fromFile(file)) }
    }

    private fun playBuiltIn(context: Context): Boolean = playMedia {
        createPreparedMediaPlayer(
            context,
            Uri.parse("android.resource://${context.packageName}/${R.raw.feedback_complete}"),
        )
    }

    /** Creates a prepared player with audio attributes applied before prepare(). */
    internal fun createPreparedMediaPlayer(context: Context, uri: Uri): MediaPlayer? {
        var player: MediaPlayer? = null
        return runCatching {
            player = MediaPlayer().apply {
                setAudioAttributes(notificationAudioAttributes())
                setDataSource(context.applicationContext, uri)
                prepare()
            }
            player
        }.getOrElse {
            player?.let { runCatching { it.release() } }
            null
        }
    }

    private fun playSystemDefault(context: Context): Boolean {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) ?: return false
        return playRingtone(context, uri)
    }

    private fun playRingtone(context: Context, uri: Uri): Boolean {
        val ringtone = runCatching { RingtoneManager.getRingtone(context.applicationContext, uri) }.getOrNull() ?: return false
        return runCatching {
            ringtone.audioAttributes = notificationAudioAttributes()
            synchronized(playbackLock) {
                stopActiveLocked()
                activeRingtone = ringtone
            }
            ringtone.play()
            true
        }.getOrElse {
            runCatching { ringtone.stop() }
            false
        }
    }

    private fun playMedia(create: () -> MediaPlayer?): Boolean {
        val player = runCatching { create() }.getOrNull() ?: return false
        return runCatching {
            synchronized(playbackLock) {
                stopActiveLocked()
                activePlayer = player
            }
            player.setOnCompletionListener(::releasePlayer)
            player.setOnErrorListener { mediaPlayer, _, _ -> releasePlayer(mediaPlayer); true }
            player.start()
            true
        }.getOrElse {
            releasePlayer(player)
            false
        }
    }

    private fun releasePlayer(player: MediaPlayer) {
        synchronized(playbackLock) {
            if (activePlayer === player) activePlayer = null
        }
        runCatching { player.release() }
    }

    private fun stopActiveLocked() {
        activeRingtone?.let { runCatching { it.stop() } }
        activeRingtone = null
        activePlayer?.let {
            runCatching { it.stop() }
            runCatching { it.release() }
        }
        activePlayer = null
    }

    private fun notificationAudioAttributes(): AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private fun ownedFile(context: Context, path: String?): File? {
        if (path.isNullOrBlank()) return null
        val directory = runCatching { audioDirectory(context).canonicalFile }.getOrNull() ?: return null
        val file = runCatching { File(path).canonicalFile }.getOrNull() ?: return null
        return file.takeIf { it.path.startsWith(directory.path + File.separator) }
    }

    private fun audioDirectory(context: Context): File = File(context.filesDir, DIRECTORY_NAME)
}

