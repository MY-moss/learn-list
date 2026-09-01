package com.mymoss.learnlist.system

import android.content.Context
import android.content.Intent
import android.os.Build
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.mymoss.learnlist.BuildConfig
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.Locale
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
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
    RESUMING,
    DOWNLOADING,
    VERIFYING,
    CERTIFICATE,
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
            if (!UpdateSecurityPolicy.isReleaseVersion(version)) return@runCatching null
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
        return JSONObject(connection.inputStream.use { input -> readTextLimited(input, MAX_RELEASE_JSON_BYTES) })
    }

    companion object {
        private const val MAX_RELEASE_JSON_BYTES = 1 * 1024 * 1024

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
            require(url.protocol.equals("https", ignoreCase = true)) { "只允许 HTTPS 更新地址" }
            require(url.userInfo == null && (url.port == -1 || url.port == 443)) {
                "更新地址必须使用标准 HTTPS 端口且不能包含账号信息"
            }
            require(isAllowedHost(url.host)) { "更新地址不在 GitHub 域名内" }
        }

        internal fun isAllowedUrl(url: URL): Boolean =
            url.protocol.equals("https", ignoreCase = true) &&
                url.userInfo == null &&
                (url.port == -1 || url.port == 443) &&
                isAllowedHost(url.host)

        private fun isAllowedHost(host: String): Boolean =
            host.lowercase(Locale.ROOT).removeSuffix(".") in ALLOWED_GITHUB_HOSTS

        private val ALLOWED_GITHUB_HOSTS = setOf(
            "api.github.com",
            "github.com",
            "objects.githubusercontent.com",
            "release-assets.githubusercontent.com",
        )
    }
}

internal object UpdateSecurityPolicy {
    fun isReleaseVersion(value: String): Boolean = RELEASE_VERSION_PATTERN.matches(value)

    fun parseSha256(value: String): String {
        val digest = value.trim().split(Regex("\\s+"), limit = 2).firstOrNull()
        require(digest?.matches(SHA256_PATTERN) == true) { "Release 摘要格式无效" }
        return digest.lowercase(Locale.ROOT)
    }

    fun isStrictUpgrade(candidateVersionCode: Long, currentVersionCode: Long): Boolean =
        candidateVersionCode > currentVersionCode

    fun certificatesMatch(installed: Set<String>, candidate: Set<String>): Boolean =
        installed.isNotEmpty() && installed == candidate

    private val SHA256_PATTERN = Regex("[0-9a-fA-F]{64}")
}

class UpdateInstaller(private val context: Context) {
    suspend fun downloadAndVerify(
        info: UpdateInfo,
        onProgress: (UpdateDownloadProgress) -> Unit = {},
    ): File = withContext(Dispatchers.IO) {
        ReleaseChecker.validateUrl(info.downloadUrl)
        require(UpdateSecurityPolicy.isReleaseVersion(info.versionName)) { "更新版本号无效" }
        val folder = File(context.cacheDir, "updates").apply { mkdirs() }
        val apk = cachedApk(info)
        val partial = File(folder, "learn-list-${info.versionName}.apk.part")
        val totalBytes = download(info.downloadUrl, partial, onProgress)
        require(partial.isFile) { "更新包下载不完整" }
        if (apk.exists()) require(apk.delete()) { "无法替换旧的更新包" }
        require(partial.renameTo(apk)) { "无法保存更新包" }
        verifyApk(info, apk, totalBytes, onProgress)
        apk
    }

    /** Re-validates a previously downloaded package before handing it to Android. */
    suspend fun verifyCachedAndInstall(
        info: UpdateInfo,
        onProgress: (UpdateDownloadProgress) -> Unit = {},
    ): Boolean = withContext(Dispatchers.IO) {
        ReleaseChecker.validateUrl(info.downloadUrl)
        require(UpdateSecurityPolicy.isReleaseVersion(info.versionName)) { "更新版本号无效" }
        val apk = cachedApk(info)
        require(apk.isFile) { "本地没有可用的更新包，请重新下载" }
        verifyApk(info, apk, apk.length(), onProgress)
        withContext(Dispatchers.Main) { install(apk) }
    }

    /** Returns true after Android reports that the expected version is installed. */
    fun isInstalled(info: UpdateInfo): Boolean = runCatching {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName == info.versionName && versionCode(packageInfo) >= BuildConfig.VERSION_CODE
    }.getOrDefault(false)

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

    private suspend fun download(
        url: String,
        target: File,
        onProgress: (UpdateDownloadProgress) -> Unit,
    ): Long? {
        onProgress(UpdateDownloadProgress(UpdateDownloadStage.CONNECTING))
        val existingBytes = target.length().coerceAtMost(MAX_APK_BYTES)
        val connection = openCheckedConnection(url, existingBytes.takeIf { it > 0L })
        val responseCode = connection.responseCode
        if (responseCode == HTTP_RANGE_NOT_SATISFIABLE && existingBytes > 0L) {
            require(target.delete()) { "无法重置损坏的断点文件" }
            return download(url, target, onProgress)
        }
        if (responseCode !in 200..299) error("下载失败：$responseCode")
        val totalBytes = connection.contentLengthLong.takeIf { it > 0L }
        val append = existingBytes > 0L && responseCode == HttpURLConnection.HTTP_PARTIAL
        val initialBytes = if (append) existingBytes else 0L
        val expectedTotal = totalBytes?.let { if (append) it + initialBytes else it }
        require(expectedTotal == null || expectedTotal <= MAX_APK_BYTES) { "更新包过大" }
        if (append) onProgress(UpdateDownloadProgress(UpdateDownloadStage.RESUMING, initialBytes, expectedTotal))
        onProgress(UpdateDownloadProgress(UpdateDownloadStage.DOWNLOADING, initialBytes, expectedTotal))
        connection.inputStream.use { input ->
            FileOutputStream(target, append).use { output ->
                val buffer = ByteArray(16 * 1024)
                var total = initialBytes
                while (true) {
                    coroutineContext.ensureActive()
                    val read = input.read(buffer)
                    if (read <= 0) break
                    total += read
                    require(total <= MAX_APK_BYTES) { "更新包过大" }
                    output.write(buffer, 0, read)
                    onProgress(UpdateDownloadProgress(UpdateDownloadStage.DOWNLOADING, total, expectedTotal))
                }
            }
        }
        return expectedTotal
    }

    private fun downloadText(url: String): String {
        val connection = openCheckedConnection(url)
        return connection.inputStream.use { input -> readTextLimited(input, MAX_CHECKSUM_BYTES) }
    }

    private fun openCheckedConnection(url: String, rangeStart: Long? = null): HttpURLConnection {
        var nextUrl = URL(url)
        repeat(MAX_REDIRECTS + 1) { attempt ->
            ReleaseChecker.validateUrl(nextUrl.toString())
            val connection = (nextUrl.openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout = 30_000
                instanceFollowRedirects = false
                rangeStart?.let { setRequestProperty("Range", "bytes=$it-") }
            }
            val responseCode = connection.responseCode
            if (responseCode in 300..399) {
                require(attempt < MAX_REDIRECTS) { "更新地址重定向次数过多" }
                val location = connection.getHeaderField("Location")
                    ?.takeIf(String::isNotBlank)
                    ?: error("更新地址缺少重定向目标")
                val redirectedUrl = URL(nextUrl, location)
                require(ReleaseChecker.isAllowedUrl(redirectedUrl)) { "重定向地址不安全" }
                connection.disconnect()
                nextUrl = redirectedUrl
            } else {
                if (responseCode !in 200..299) error("下载失败：$responseCode")
                require(ReleaseChecker.isAllowedUrl(connection.url)) { "重定向地址不安全" }
                return connection
            }
        }
        error("更新地址重定向失败")
    }

    private fun readTextLimited(input: InputStream, maxBytes: Int): String {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var total = 0
        while (true) {
            val read = input.read(buffer)
            if (read <= 0) break
            total += read
            require(total <= maxBytes) { "摘要文件过大" }
            output.write(buffer, 0, read)
        }
        return output.toString(Charsets.UTF_8.name())
    }

    private suspend fun verifyApk(
        info: UpdateInfo,
        apk: File,
        totalBytes: Long?,
        onProgress: (UpdateDownloadProgress) -> Unit,
    ) {
        val checksumUrl = requireNotNull(info.sha256Url) { "Release 缺少 SHA-256 摘要，已停止安装" }
        onProgress(UpdateDownloadProgress(UpdateDownloadStage.VERIFYING, apk.length(), totalBytes))
        val expected = UpdateSecurityPolicy.parseSha256(downloadText(checksumUrl))
        val actual = sha256(apk)
        require(actual == expected) { "更新包 SHA-256 校验失败" }
        onProgress(UpdateDownloadProgress(UpdateDownloadStage.CERTIFICATE, apk.length(), totalBytes))
        verifyPackage(apk, info.versionName)
    }

    private fun cachedApk(info: UpdateInfo): File =
        File(context.cacheDir, "updates/learn-list-${info.versionName}.apk")

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

    private fun verifyPackage(apk: File, expectedVersionName: String) {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            @Suppress("DEPRECATION") PackageManager.GET_SIGNATURES
        }
        val archive = context.packageManager.getPackageArchiveInfo(apk.absolutePath, flags)
            ?: error("更新包不是有效的 Android 安装包")
        require(archive.packageName == context.packageName) { "更新包应用身份不匹配" }
        require(UpdateSecurityPolicy.isStrictUpgrade(versionCode(archive), BuildConfig.VERSION_CODE.toLong())) { "拒绝安装不高于当前版本的更新包" }
        require(archive.versionName == expectedVersionName) { "更新包版本号与 Release 不一致" }
        val installed = context.packageManager.getPackageInfo(context.packageName, flags)
        val installedCertificates = certificateDigests(installed)
        val archiveCertificates = certificateDigests(archive)
        require(UpdateSecurityPolicy.certificatesMatch(installedCertificates, archiveCertificates)) {
            "更新包签名证书与当前安装包不一致"
        }
    }

    private fun certificateDigests(info: PackageInfo): Set<String> {
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.signingInfo?.apkContentsSigners?.toList().orEmpty()
        } else {
            @Suppress("DEPRECATION") info.signatures?.toList().orEmpty()
        }
        val digest = MessageDigest.getInstance("SHA-256")
        return signatures.map { signature ->
            digest.digest(signature.toByteArray()).joinToString("") { "%02x".format(it) }
        }.toSet()
    }

    private fun versionCode(info: PackageInfo): Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        info.longVersionCode
    } else {
        @Suppress("DEPRECATION") info.versionCode.toLong()
    }

    private companion object {
        const val MAX_APK_BYTES = 100L * 1024L * 1024L
        const val MAX_CHECKSUM_BYTES = 4 * 1024
        const val MAX_REDIRECTS = 5
        const val HTTP_RANGE_NOT_SATISFIABLE = 416
    }
}

private val RELEASE_VERSION_PATTERN = Regex("\\d+(?:\\.\\d+){2}")

private fun readTextLimited(input: InputStream, maxBytes: Int): String {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(16 * 1024)
    var total = 0
    while (true) {
        val read = input.read(buffer)
        if (read <= 0) break
        total += read
        require(total <= maxBytes) { "Release 信息过大" }
        output.write(buffer, 0, read)
    }
    return output.toString(Charsets.UTF_8.name())
}
