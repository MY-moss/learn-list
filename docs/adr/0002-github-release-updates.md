# ADR-0002：使用 GitHub Stable Release 作为首版更新源

- 状态：已接受
- 日期：2026-08-31

## 决策

应用只检查 `MY-moss/learn-list` 的 GitHub Stable Release。每次 Release 同时提供签名 APK 和 SHA-256 文件；应用校验 HTTPS GitHub 域名、版本号和摘要后，交给 Android 系统安装器，不做静默安装。

## 原因

当前仓库没有服务器或账号体系，GitHub Release 可以用较小的工程成本提供版本历史和可重复下载。系统安装器保留用户确认，避免应用绕过 Android 的安装安全边界。

## 取舍

侧载需要开启“允许安装未知来源”，并可能受到 Android 开发者身份验证政策影响；用户必须自己持有 Release keystore。Release 缺少摘要或摘要不匹配时更新会失败，即使这意味着旧的手工发布包无法直接更新。

## 发布不变量

- keystore、密码和别名只能来自本地安全位置或 GitHub Actions Secrets。
- `vX.Y.Z` 标签必须产生签名 APK 和同名 `.sha256` 资产。
- 自动检查最多每 24 小时一次；用户可手动检查。
