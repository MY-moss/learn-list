# 发布前待办

- [x] 由应用持有人创建并离线保存 Release keystore，配置 GitHub Actions Secrets；`v0.2.2` 签名 APK 已上传并完成远端摘要/签名核验，仍待真机覆盖安装。
- [ ] 在实体 Android 8/13/当前版本设备执行通知、AlarmManager、精确提醒、省电和重启测试。
- [x] AndroidTest 已覆盖加密备份错误密码、损坏引用、替换导入和设置恢复（仍待真机执行）。
- [ ] 在真机上验证文件选择器、损坏文件、合并/替换和旧数据库迁移。
- [ ] 运行 `connectedDebugAndroidTest`，确认 Compose 烟雾测试。
- [x] 发布稳定版 `v0.2.2`：已生成签名 APK 与 `.sha256` 并上传 GitHub Release；覆盖安装仍待真机执行。
- [ ] 后续版本再评估真正的前台计时服务、更多提醒动作和云同步。

