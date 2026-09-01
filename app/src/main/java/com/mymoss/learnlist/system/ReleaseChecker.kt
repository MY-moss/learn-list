package com.mymoss.learnlist.system

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.mymoss.learnlist.BuildConfig
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class UpdateInfo(
    val tagName: String,
    val versionName: String,
    val downloadUrl: String,
    val sha256Url: String?,
    val releaseNotes: String,
)

enum class UpdateDownloadStage {
    CONNECTING,
    DOWNLOADING,
    VERIFYING,
}

data class UpdateDownloadProgress(
    val stage: UpdateDownloadStage,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long? = null,
)

class ReleaseChecker {
    suspend fun checkLatest(): Result<UpdateInfo?> = withContext(Dispatchers.IO) {
        runCatching {
            val root = getJson("https://api.github.com/repos/MY-moss/learn-list/releases/latest")
            val tag = root.optString("tag_name").takeIf(String::isNotBlank) ?: return@runCatching null
            val version = tag.removePrefix("v")
            if (!RELEASE_VERSION_PATTERN.matches(version)) return@runCatching null
            if (!isNewer(version, BuildConfig.VERSION_NAME)) return@runCatching null
            val assets = root.optJSONArray("assets") ?: return@runCatching null
            var apk: String? = null
            var checksum: String? = null
            for (index in 0 until assets.length()) {
                val asset = assets.getJSONObject(index)
                val name = asset.optString("name")
                val url = asset.optString("browser_download_url")
                if (name.endsWith(".apk", ignoreCase = true)) apk = url
                if (name.endsWith(".sha256", ignoreCase = true) || name.endsWith(".sha256.txt", ignoreCase = true)) checksum = url
            }
            val apkUrl = apk ?: return@runCatching null
            val checksumUrl = checksum ?: return@runCatching null
            validateUrl(apkUrl)
            validateUrl(checksumUrl)
            UpdateInfo(tag, version, apkUrl, checksumUrl, root.optString("body", ""))
        }
    }

    private fun getJson(url: String): JSONObject {
        validateUrl(url)
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 15_000
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        connection.setRequestProperty("User-Agent", "LearnList/${BuildConfig.VERSION_NAME}")
        connection.instanceFollowRedirects = false
        if (connection.responseCode !in 200..299) error("GitHub 返回 ${connection.responseCode}")
        return JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
    }

    companion object {
        fun isNewer(candidate: String, current: String): Boolean {
            val left = candidate.split('.', '-', '+').map { it.toIntOrNull() ?: 0 }
            val right = current.split('.', '-', '+').map { it.toIntOrNull() ?: 0 }
            for (index in 0 until maxOf(left.size, right.size)) {
                val a = left.getOrElse(index) { 0 }
                val b = right.getOrElse(index) { 0 }
                if (a != b) return a > b
            }
            return false
        }

        fun validateUrl(value: String) {
            val url = URL(value)
            require(url.protocol == "https") { "只允许 HTTPS 更新地址" }
            require(url.host == "api.github.com" || url.host == "github.com" || url.host == "objects.githubusercontent.com" || url.host == "release-assets.githubusercontent.com") { "更新地址不在 GitHub 域名内" }
        }
    }
}

class UpdateInstaller(private val context: Context) {
    suspend fun downloadAndVerify(
        info: UpdateInfo,
        onProgress: (UpdateDownloadProgress) -> Unit = {},
    ): File = withContext(Dispatchers.IO) {
        ReleaseChecker.validateUrl(info.downloadUrl)
        require(RELEASE_VERSION_PATTERN.matches(info.versionName)) { "更新版本号无效" }
        val folder = File(context.cacheDir, "updates").apply { mkdirs() }
        val apk = File(folder, "learn-list-${info.versionName}.apk")
        val totalBytes = download(info.downloadUrl, apk, onProgress)
        onProgress(UpdateDownloadProgress(UpdateDownloadStage.VERIFYING, apk.length(), totalBytes))
        val checksumUrl = requireNotNull(info.sha256Url) { "Release 缺少 SHA-256 摘要，已停止安装" }
        val expected = downloadText(checksumUrl).trim().split(Regex("\\s+")).firstOrNull()
        require(expected?.matches(Regex("[0-9a-fA-F]{64}")) == true) { "Release 摘要格式无效" }
        val actual = sha256(apk)
        require(actual.equals(expected, ignoreCase = true)) { "更新包 SHA-256 校验失败" }
        apk
    }

    fun install(apk: File): Boolean {
        if (!context.packageManager.canRequestPackageInstalls()) {
            context.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, "package:${context.packageName}".toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return false
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apk)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return true
    }

    private fun download(
        url: String,
        target: File,
        onProgress: (UpdateDownloadProgress) -> Unit,
    ): Long? {
        onProgress(UpdateDownloadProgress(UpdateDownloadStage.CONNECTING))
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000; connection.readTimeout = 30_000; connection.instanceFollowRedirects = true
        if (connection.responseCode !in 200..299) error("下载失败：${connection.responseCode}")
        require(isAllowedHost(connection.url.host)) { "重定向地址不安全" }
        val totalBytes = connection.contentLengthLong.takeIf { it > 0L }
        require(totalBytes == null || totalBytes <= MAX_APK_BYTES) { "更新包过大" }
        onProgress(UpdateDownloadProgress(UpdateDownloadStage.DOWNLOADING, 0L, totalBytes))
        connection.inputStream.use { input ->
            target.outputStream().use { output ->
                val buffer = ByteArray(16 * 1024)
                var total = 0L
                while (true) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    total += read
                    require(total <= MAX_APK_BYTES) { "更新包过大" }
                    output.write(buffer, 0, read)
                    onProgress(UpdateDownloadProgress(UpdateDownloadStage.DOWNLOADING, total, totalBytes))
                }
            }
        }
        return totalBytes
    }

    private fun downloadText(url: String): String {
        ReleaseChecker.validateUrl(url)
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000; connection.readTimeout = 15_000
        if (connection.responseCode !in 200..299) error("摘要下载失败：${connection.responseCode}")
        require(isAllowedHost(connection.url.host)) { "摘要重定向地址不安全" }
        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    private fun isAllowedHost(host: String): Boolean = host == "api.github.com" || host == "github.com" || host == "objects.githubusercontent.com" || host == "release-assets.githubusercontent.com"

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(16 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private companion object {
        const val MAX_APK_BYTES = 100L * 1024L * 1024L
    }
}

private val RELEASE_VERSION_PATTERN = Regex("\\d+(?:\\.\\d+){2}")

