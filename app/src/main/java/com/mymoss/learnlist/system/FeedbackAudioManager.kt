package com.mymoss.learnlist.system

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.provider.OpenableColumns
import com.mymoss.learnlist.data.AppSettings
import java.io.File
import java.util.UUID

/** Keeps an optional imported sound in app-private storage after the picker closes. */
object FeedbackAudioManager {
    data class ImportedAudio(val path: String, val displayName: String)

    private const val DIRECTORY_NAME = "feedback-audio"
    private const val MAX_AUDIO_BYTES = 5L * 1024L * 1024L

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

    fun play(context: Context, settings: AppSettings) {
        if (!playCustom(context, settings.feedbackAudioPath)) playSystem(context)
    }

    fun preview(context: Context, settings: AppSettings) = play(context, settings)

    private fun playCustom(context: Context, path: String?): Boolean {
        val file = ownedFile(context, path)?.takeIf { it.isFile && it.length() > 0L } ?: return false
        var player: MediaPlayer? = null
        return runCatching {
            val created = MediaPlayer.create(context.applicationContext, Uri.fromFile(file))
                ?: return@runCatching false
            player = created
            created.setOnCompletionListener { it.release() }
            created.setOnErrorListener { mediaPlayer, _, _ -> mediaPlayer.release(); true }
            created.start()
            true
        }.getOrElse {
            player?.runCatching(MediaPlayer::release)
            false
        }
    }

    private fun playSystem(context: Context) {
        runCatching {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, uri) ?: return@runCatching
            ringtone.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            ringtone.play()
        }
    }

    private fun ownedFile(context: Context, path: String?): File? {
        if (path.isNullOrBlank()) return null
        val directory = runCatching { audioDirectory(context).canonicalFile }.getOrNull() ?: return null
        val file = runCatching { File(path).canonicalFile }.getOrNull() ?: return null
        return file.takeIf { it.path.startsWith(directory.path + File.separator) }
    }

    private fun audioDirectory(context: Context): File = File(context.filesDir, DIRECTORY_NAME)
}
