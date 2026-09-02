# Learn List Release 流程

## 首次配置

由应用持有人在本地生成并安全保存 Release keystore，不要把 keystore、密码或明文备份提交到仓库。GitHub Actions 需要配置：

- `RELEASE_KEYSTORE_BASE64`
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

Secret 值不会回显，也不应写入 Issue、提交或聊天记录。需要由仓库持有人在 GitHub Actions 的仓库设置中配置这四项后，Release 工作流才会发布正式包；缺少签名 Secret 时应失败，不发布未签名包。当前仓库已配置这四个 Secret 名称，值仍只由 GitHub Actions 使用；当前稳定版为 `v0.3.8`。

## 发布步骤

1. 修改 `app/build.gradle.kts` 中的 `versionCode` 和 `versionName`。
2. 在 `CHANGELOG` 或 GitHub 提交中记录用户可见变更，并先合并到 `main`。
3. 不要重复使用已经发布的标签；创建并推送形如 `vX.Y.Z` 的新标签（当前稳定版是 `v0.3.8`，以下用下一版 `v0.3.9` 举例）：

   ```powershell
   git tag v0.3.9
   git push origin v0.3.9
   ```

4. `release.yml` 会用 Actions Secret 签名 Release APK，验证签名，生成同名 `.sha256` 文件并创建 GitHub Release。
5. 在一台已安装旧版本的 Android 设备上验证 SHA-256、系统安装器和覆盖升级，再向可信用户分发。

当前 v0.3.8 Release：

- APK：[learn-list-v0.3.8.apk](https://github.com/MY-moss/learn-list/releases/download/v0.3.8/learn-list-v0.3.8.apk)
- SHA-256：`963b5d6e975843d4f2843fd0fb9cd8c0e42faa73184e57f0e27287056abdb8d6`
- CI 已完成 API 26/33/36 构建、Lint、单元测试和 AndroidTest；实体手机上的通知、省电、安装器回查和同签名覆盖升级仍需实测。

如果标签推送没有触发工作流，可以在 Actions 中手动运行 `Android Release`，选择 `main` 分支并填写一个已经存在的 `vX.Y.Z` 标签；工作流会先检出该标签，再执行同样的签名、校验和发布步骤。这样不依赖旧标签里是否已经包含最新的工作流文件。

## 本地签名验证

```powershell
$env:RELEASE_KEYSTORE_PATH = 'D:\\secure\\learn-list-release.jks'
$env:RELEASE_STORE_PASSWORD = '...'
$env:RELEASE_KEY_ALIAS = 'learn-list'
$env:RELEASE_KEY_PASSWORD = '...'
.\\gradlew.bat :app:assembleRelease
& "$env:ANDROID_HOME\\build-tools\\36.0.0\\apksigner.bat" verify --verbose app\\build\\outputs\\apk\\release\\app-release.apk
Get-FileHash app\\build\\outputs\\apk\\release\\app-release.apk -Algorithm SHA256
```

未设置签名环境变量时，构建会保留 `app-release-unsigned.apk`，这是预期的安全结果；该文件不能覆盖已安装的正式版本。正式发布前必须用同一 Release keystore 生成并核验签名 APK。


