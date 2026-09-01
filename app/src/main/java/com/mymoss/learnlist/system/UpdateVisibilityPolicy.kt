package com.mymoss.learnlist.system

/**
 * Keeps an update that the user dismissed from reappearing during automatic checks.
 * A manual check is an explicit request to see the latest release again.
 */
internal object UpdateVisibilityPolicy {
    fun shouldShow(info: UpdateInfo?, dismissedVersionName: String?, manual: Boolean): Boolean =
        info != null && (manual || info.versionName != dismissedVersionName)

    fun statusMessage(info: UpdateInfo?, dismissedVersionName: String?, manual: Boolean): String? = when {
        info == null -> "当前已是最新版本"
        shouldShow(info, dismissedVersionName, manual) -> "发现 v${info.versionName}，可以下载更新"
        else -> "已暂不提醒 v${info.versionName}；有更新版本时会再次提示"
    }
}
