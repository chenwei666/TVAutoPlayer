# 部署说明

## 测试部署

1. 从 [GitHub Release v1.2.0](https://github.com/chenwei666/TVAutoPlayer/releases/tag/v1.2.0) 下载 `TVAutoPlayer-v1.2.0-debug.apk` 并复制到 U 盘。
2. 电视开启“允许安装未知来源应用”。
3. 安装 APK，首次打开后授予视频只读权限，在内置媒体库中选择一个或多个视频。
4. 在电视的应用/电池/后台管理中允许本应用自启动和后台运行。
5. 完全断电后重新上电，执行冷启动验收。

ADB 安装：

```powershell
adb install -r .\TVAutoPlayer-v1.2.0-debug.apk
```

模拟开机广播（仅测试，不等同于真实冷启动）：

```powershell
adb shell am broadcast -a android.intent.action.BOOT_COMPLETED com.chenwei.tvautoplay
```

## 正式发布签名

不要把密钥或密码提交到仓库。由企业安全保管发布 keystore，通过 Android Studio 的 “Generate Signed Bundle / APK” 或 CI 密钥库注入签名配置，生成 Release APK。以后升级必须保持 applicationId `com.chenwei.tvautoplay` 和签名证书不变。

## 版本升级

- 相同签名：`adb install -r 新版本.apk`，保留 SharedPreferences 和选片设置。
- 从 V1.0.0 升级到 V1.1.0 时，原单视频设置会自动转换为一项播放列表。
- 从 V1.0.0～V1.1.2 升级到 V1.2.0 时保留原播放列表和播放偏好；原系统文件选择器 URI 仍可继续播放。
- 首次使用 V1.2.0 内置媒体库时需要授予视频只读权限；拒绝不会删除已有配置或视频。
- 不同签名：Android 会拒绝覆盖，必须卸载旧版，设置与 URI 授权将被清除。
- 视频文件本身始终位于用户选择的原位置，卸载应用不会删除视频。

## 无系统文件选择器电视的操作

1. 在 U 盘创建 `Movies/TVAutoPlay`，把视频复制进去后安全弹出。
2. 把 U 盘插到电视并等待系统完成媒体索引。
3. 打开应用，允许读取视频；进入内置媒体库后使用遥控器确认键逐个勾选。
4. 如果列表为空，重新插拔 U 盘、等待约 30 秒并选择“重新扫描”。不同厂商的媒体索引时机可能不同。
5. “使用系统文件选择器（可选）”按钮只会在电视确实存在可处理该请求的应用时显示。

## 大规模无人值守部署

普通侧载 APK 的开机拉起受 Android 和厂商限制。大量设备或必须百分之百开机播放时，建议：

- 使用 Android Enterprise Device Owner / Dedicated Device（Kiosk）。
- 由电视/盒子厂商预装为系统应用或加入自启动白名单。
- 固定发布签名、设备型号、固件版本和媒体编码配置。
- 在上线前执行断电恢复、网络断开、U 盘松动、连续播放和存储损坏演练。
