# Learn List Release 流程

## 首次配置

由应用持有人在本地生成并安全保存 Release keystore，不要把 keystore、密码或明文备份提交到仓库。GitHub Actions 需要配置：

- `RELEASE_KEYSTORE_BASE64`
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

当前仓库已配置这四个 Secret；Secret 值不会回显，也不应写入 Issue、提交或聊天记录。Release 工作流只在推送 `vX.Y.Z` 标签时运行，缺少签名 Secret 时会失败，不会发布未签名包。

## 发布步骤

1. 修改 `app/build.gradle.kts` 中的 `versionCode` 和 `versionName`。
2. 在 `CHANGELOG` 或 GitHub 提交中记录用户可见变更，并先合并到 `main`。
3. 创建并推送形如 `v0.1.0` 的标签：

   ```powershell
   git tag v0.1.0
   git push origin v0.1.0
   ```

4. `release.yml` 会用 Actions Secret 签名 Release APK，验证签名，生成同名 `.sha256` 文件并创建 GitHub Release。
5. 在一台已安装旧版本的 Android 设备上验证 SHA-256、系统安装器和覆盖升级，再向可信用户分发。

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

未设置签名环境变量时，构建会保留 `app-release-unsigned.apk`，这是预期的安全结果。

