# Learn List 安装与使用

## 安装首版 APK

推荐下载已签名的稳定版：[v0.2.3 GitHub Release](https://github.com/MY-moss/learn-list/releases/tag/v0.2.3)，直接获取 [APK](https://github.com/MY-moss/learn-list/releases/download/v0.2.3/learn-list-v0.2.3.apk)；旁边的 `.sha256` 文件可用于校验完整性。

1. 在手机上允许文件管理器安装来自此来源的应用。
2. 将 Release APK 或 `app/build/outputs/apk/debug/app-debug.apk` 传到手机并打开。
3. 安装后在系统设置中按需授予通知权限；如果需要精确时间提醒，再授予精确闹钟权限。

Debug APK 只适合自用测试。正式覆盖升级必须使用同一把 Release keystore 签名的 APK；未签名 Release 不能直接安装。

## 从 GitHub 获取主线测试包

每次 `main` 分支的 Android CI 成功后，工作流会保留 14 天的 Debug APK 和 SHA-256 文件。打开仓库的 [Actions](https://github.com/MY-moss/learn-list/actions)，进入最新的 `Android CI` 成功运行，在页面底部下载 `learn-list-debug-apk-...`。它只用于测试，不等同于正式 Release，也不能覆盖安装正式签名版本。

## 首次设置建议

- 先创建一个学习项目，再加入学习任务或阅读计划。
- 为阅读计划填写总页数和每日页数；填写截止日后可以使用“剩余页数均摊”。
- 在“设置 → 固定提醒”添加每日进度或项目提醒，并按需要选择星期和安静时段。
- 新建待办时可填写一次性到期日，或为重复待办填写开始日期。
- Android 13 及以上若拒绝通知权限，复习仍可在应用内完成，但系统提醒不会显示。

## 数据备份

“设置 → 数据与升级”支持 AES-GCM 加密备份和明文备份。加密密码不会保存在应用内，忘记密码无法恢复。

导入时先输入密码并选择文件，确认预览后选择“合并”或“替换”：

- 合并：按记录 ID 写入，保留未冲突的本机数据。
- 替换：清空本机数据库后写入备份；操作前应确认备份可用。

## GitHub 更新

应用启动或回到前台后最多每 24 小时自动检查一次，也可以在“设置 → 更新中心”手动检查。它只检查仓库的稳定 Release，下载前要求 HTTPS GitHub 地址和 `.sha256` 文件，校验通过后交给 Android 系统安装器。安装仍需要用户确认，不会静默安装；检查失败不会占用 24 小时窗口，下一次启动或回到前台会继续重试。

