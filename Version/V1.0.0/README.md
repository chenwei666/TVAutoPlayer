# 电视自动播放器

面向 Android TV / 电视盒子的本地视频循环播放应用。首次启动选择一个视频后，应用会保存该文件的长期只读授权；以后打开应用会直接全屏循环播放，也可在电视开机后自动尝试启动。

## 已实现功能

- 首次启动自动进入系统文件选择器，只需选择一次。
- 再次打开直接播放，单视频无限循环。
- 支持遥控器方向键、确认键、返回键、菜单键和设置键。
- 播放中按“返回 / 菜单 / 设置”进入功能设置。
- 可更换视频、清除视频、开关声音、切换完整显示/裁切铺满。
- 保存跨重启文件授权，不复制视频，不申请整盘存储权限。
- 电视开机广播触发自动播放（默认开启，可在设置中关闭）。
- 视频被移动、删除或授权失效时给出明确提示，不崩溃。
- 播放失败时显示 Media3 错误码，并提供重试或更换视频。
- 播放期间保持屏幕常亮、沉浸式全屏。
- 不联网、不读取数据库、不收集用户数据。

## 直接安装

可安装 APK 位于：

`dist/TVAutoPlayer-v1.0.0-debug.apk`

方法一：把 APK 复制到 U 盘，在电视文件管理器中打开并允许“安装未知应用”。

方法二：电视打开 USB/网络调试后，在电脑执行：

```powershell
adb install -r .\dist\TVAutoPlayer-v1.0.0-debug.apk
```

首次运行：

1. 打开“电视自动播放器”。
2. 在系统文件选择器中选择 U 盘或电视存储里的视频。
3. 选择完成后立即开始循环播放。
4. 播放中按遥控器“返回”键可进入设置并更换视频。
5. 冷启动电视，验证厂商系统是否允许本应用自启动。

> 交付 APK 使用 Android Debug 证书签名，适合内网、展厅、门店等直接侧载测试。正式批量部署前请使用企业自己的发布证书构建 Release APK，并妥善保管签名密钥。

## 开机自动播放说明

应用已声明 `RECEIVE_BOOT_COMPLETED` 并监听标准开机广播及部分电视盒子使用的 Quick Boot 广播。Android 10 以后限制后台直接拉起界面，部分品牌电视还会增加自己的自启动限制。因此第一次安装后请在电视系统设置中把本应用加入“允许自启动 / 后台运行 / 不受电池优化限制”名单。

如果厂商系统仍然拦截，普通第三方 APK 无法无权限绕过。需要百分之百无人值守启动时，应把设备配置为企业 Device Owner / Kiosk，或由设备厂商把应用加入系统白名单。相关限制见 [Android 后台界面启动说明](https://developer.android.com/guide/components/activities/background-starts)。

## 格式兼容性

播放器使用 Media3 ExoPlayer 1.10.1，文件选择器接受常见视频 MIME 类型。常见容器包括 MP4、MKV、WebM、MPEG-TS 等。容器能被识别不代表电视一定具备对应硬件解码器：

- 推荐最高兼容组合：MP4 + H.264/AVC + AAC。
- H.265/HEVC 需要电视芯片支持；老电视可能只有画面或无法播放。
- AV1 主要适用于较新的电视芯片。
- 4K、高码率、10-bit、特殊字幕或无损音频受电视硬件能力影响。
- AVI 等老旧容器兼容性不稳定，建议先转码为 MP4。

## 开发环境与构建

- JDK 17
- Android SDK Platform 36
- Android SDK Build Tools 36.0.0
- Android Gradle Plugin 9.2.1
- Gradle 9.4.1（已包含 Wrapper）
- 最低系统 Android 6.0（API 23）
- 目标系统 Android 16（API 36）

Android Studio 可直接打开项目。命令行构建：

```powershell
.\scripts\build.ps1
```

脚本会把源码同步到 `C:\tmp\TVAutoPlayer-build` 的英文临时路径，再依次运行单元测试、Lint 和 Debug APK 构建，最后把 APK 复制到 `dist`。这样可规避 Windows 下 Gradle 测试进程对中文项目路径的兼容问题。脚本只会清理自己固定的临时构建目录，不会删除项目源码。若需要改用其他英文目录，可先设置环境变量 `TV_AUTOPLAYER_BUILD_ROOT`。

## 项目结构

```text
app/src/main/java/...        播放策略、配置、播放器、设置页、开机接收器
app/src/main/res/            电视布局、样式、图标和中文文案
app/src/test/                关键启动与播放策略单元测试
docs/                        架构、测试和部署说明
scripts/build.ps1            Windows 可复现构建脚本
dist/                        可安装 APK 与 SHA-256
Version/V1.0.0/              V1.0.0 完整源码快照
```

## 隐私与权限

应用只申请 `RECEIVE_BOOT_COMPLETED`。视频通过 Android Storage Access Framework 由用户明确选择，应用只保存该单个文件的 URI 与显示名称，不扫描存储、不复制视频、不访问网络、不连接数据库。卸载应用会清除设置和文件授权，但不会删除原视频。

更多信息：

- [架构说明](docs/architecture.md)
- [测试说明](docs/testing.md)
- [部署说明](docs/deployment.md)
- [变更记录](CHANGELOG.md)
