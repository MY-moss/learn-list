# Learn List（学习清单）

Learn List 是一个离线优先的 Android 学习打卡应用：把艾宾浩斯复习、阅读计划、重复待办、番茄钟、目标和倒计时放进同一个每日驾驶舱。数据默认只保存在手机本机，不需要账号或服务器。

## 首版能力

- 固定间隔复习：1 / 2 / 4 / 7 / 15 / 30 / 60 / 90 天；记得、模糊、忘记、稍后四种反馈。
- 学习项目、学习任务、回忆提示、隐藏资料、来源、标签、暂停、归档与恢复。
- 阅读计划：总页数、每日目标、截止日、阅读日志、欠页均摊和当天目标微调。
- 今日必做百分比、连续打卡、复习积压、阅读页数和专注时长。
- 一次性/每天/每周/工作日/自定义星期待办，可设置一次性到期日或重复开始日。
- 后台可恢复且到点提醒的番茄钟、量化目标、最近 28 天热力图和事件倒计时。
- 固定提醒、每日进度摘要、安静时段、Android 通知/精确提醒权限适配。
- 声音与振动反馈可独立开关，支持声音 + 振动、仅声音、仅振动或静音；系统通知保持静音，避免重复提示。
- 连续打卡休息日设置；休息日不会打断连续记录。
- 加密 AES-GCM 备份、明文备份、导入预览、合并/替换、数据库迁移。
- GitHub Stable Release 更新：下载后校验 SHA-256，再交给 Android 安装器，不静默安装；更新中心会展示连接、下载百分比、文件大小、校验和安装阶段。

界面采用暖纸张、番茄红与叶绿的学习工作台风格：今日驾驶舱优先展示必做进度，专注页提供一键番茄钟，设置页集中管理提醒、备份和更新。首次打开应用会有简短使用引导，也可在设置中重新查看。应用启动后每 24 小时自动检查一次 GitHub 稳定版，也支持在“设置 → 更新中心”手动检查。

首版不包含云同步、账号、PDF/OCR、AI 生成卡片、社交排行、应用屏蔽、穿戴设备、日历同步和桌面小组件。

## 构建

工程使用 Kotlin、Jetpack Compose、Room、DataStore 和单一 `app` 模块。最低 Android 版本为 8.0（API 26），`compileSdk/targetSdk` 均为 36；Compose BOM 使用 2026.06.00，Navigation Compose 使用 2.9.8，保持 API 36 编译基线，不改变 Android 8.0+ 的安装范围。

本机 SDK 安装在 `D:\android-sdk\sdk`（如果 Android Studio 使用嵌套 SDK 目录，请以实际 SDK 根目录为准），构建基线为 JDK 17。Windows PowerShell 示例：

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
$env:ANDROID_HOME = 'D:\android-sdk\sdk'
$env:ANDROID_SDK_ROOT = 'D:\android-sdk\sdk'
.\gradlew.bat :app:assembleDebug
```

Debug APK 输出在：`app/build/outputs/apk/debug/app-debug.apk`。

质量检查：

```powershell
.\gradlew.bat lintDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

需要真机/模拟器的 Compose 测试可使用：

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

## GitHub Release 更新

稳定版使用 `vX.Y.Z` 标签触发 `.github/workflows/release.yml`。Release 必须包含：

1. 已用用户自己的 Release keystore 签名的 `learn-list-vX.Y.Z.apk`；
2. 同名 `.sha256` 文件，内容为 APK 的 SHA-256 摘要。

本地签名构建通过环境变量提供密钥，不把 keystore 或密码放入仓库：

```powershell
$env:RELEASE_KEYSTORE_PATH = 'D:\secure\learn-list-release.jks'
$env:RELEASE_STORE_PASSWORD = '...'
$env:RELEASE_KEY_ALIAS = 'learn-list'
$env:RELEASE_KEY_PASSWORD = '...'
.\gradlew.bat :app:assembleRelease
```

GitHub Actions 使用以下 Secrets：`RELEASE_KEYSTORE_BASE64`、`RELEASE_STORE_PASSWORD`、`RELEASE_KEY_ALIAS`、`RELEASE_KEY_PASSWORD`。Release workflow 会拒绝缺少签名密钥或 SHA-256 摘要的发布。

## 数据安全和分发说明

加密备份使用 AES-GCM，密码只在当前操作中使用；忘记密码无法恢复。明文导出包含全部学习记录，应用会明确提示。更新包安装前校验 HTTPS GitHub 地址和 SHA-256，并通过系统安装器请求用户确认。

直接从 GitHub 侧载面向自用或少量可信用户；随着 Android 开发者身份验证要求逐步实施，后续可增加商店或已验证分发渠道。

安装、备份和更新说明见 [`docs/installation.md`](docs/installation.md) 与 [`docs/release.md`](docs/release.md)；更多领域词汇、架构取舍和验收清单见 [`CONTEXT.md`](CONTEXT.md)、[`docs/adr`](docs/adr) 和 [`tasks/plan.md`](tasks/plan.md)。

